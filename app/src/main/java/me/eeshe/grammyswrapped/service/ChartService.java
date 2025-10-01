package me.eeshe.grammyswrapped.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.model.userdata.UserVoiceChatData;
import me.eeshe.grammyswrapped.util.SessionTimeUtil;

public class ChartService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChartService.class);
  private static final List<Color> SERIES_COLORS = List.of(
      new Color(4, 150, 255),
      new Color(121, 180, 115),
      new Color(243, 146, 55),
      new Color(82, 150, 165),
      new Color(244, 228, 9),
      new Color(255, 105, 120),
      new Color(255, 22, 84),
      new Color(204, 146, 194),
      new Color(83, 255, 69),
      new Color(31, 1, 185));

  /**
   * Generates a time series chart for daily voice chat times and saves it as a
   * PNG image.
   *
   * @param userVoiceChatDatavcData UserVoiceChatData to chart.
   * @param startingDate            Starting date of the chart.
   * @param endingDate              Ending date of the chart.
   * @return true if the graph was successfully generated and saved, false
   *         otherwise.
   */
  public boolean generateUserVcTimeChart(
      UserVoiceChatData userVoiceChatData,
      Date startingDate,
      Date endingDate) {
    Map<LocalDate, Duration> voiceChatData = userVoiceChatData.getDailyVoiceChatTime();
    if (voiceChatData == null || voiceChatData.isEmpty()) {
      LOGGER.warn("No data provided for chart generation.");
      return false;
    }
    voiceChatData = fillMissingDates(
        (TreeMap<LocalDate, Duration>) voiceChatData,
        startingDate,
        endingDate);

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    TimeSeries series = new TimeSeries("VC Time");

    for (Map.Entry<LocalDate, Duration> entry : voiceChatData.entrySet()) {
      LocalDate date = entry.getKey();
      Duration duration = entry.getValue();

      Day day = new Day(
          date.getDayOfMonth(),
          date.getMonthValue(),
          date.getYear());

      // Convert Duration to hours (as a double) for the Y-axis
      // duration.toMillis() / (1000.0 * 60 * 60) gives hours with decimal precision
      double hours = duration.toMillis() / (3600000.0); // 1000ms * 60s * 60m = 3,600,000ms in an hour

      series.add(day, hours);
    }
    dataset.addSeries(series);

    // Create the JFreeChart object
    String username = userVoiceChatData.getUser().getName();
    String chartTitle = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_USER_TITLE.getFormatted(username);
    String xLabel = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_X_AXIS.get();
    String yLabel = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_Y_AXIS.get();
    boolean showLegend = false;
    boolean useTooltips = false;
    boolean generateUrls = false;
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        chartTitle,
        xLabel,
        yLabel,
        dataset,
        showLegend,
        useTooltips,
        generateUrls);

    applyChartStyling(chart);

    // Customize the renderer (line and shapes)
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) chart.getPlot()).getRenderer();
    renderer.setSeriesPaint(0, SERIES_COLORS.get(0));
    renderer.setSeriesShapesVisible(0, true); // Show shapes at each data point
    renderer.setSeriesStroke(0, new BasicStroke(5));

    addAnnotation(
        chart,
        String.format("Total: %s",
            SessionTimeUtil.formatMilliseconds(userVoiceChatData.getVoiceChatTimeMillis())),
        SERIES_COLORS.get(0),
        0.005,
        0.01);

    // Save the chart as a PNG image
    File outputFile = new File(username + ".png");
    File outputDir = outputFile.getParentFile();

    if (outputDir != null && !outputDir.exists()) {
      outputDir.mkdirs(); // Create parent directories if they don't exist
    }

    try {
      ChartUtils.saveChartAsPNG(outputFile, chart, 1920, 1080);
      LOGGER.info("Graph saved successfully to: " + outputFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      LOGGER.warn("Error saving chart: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Generates a time series chart for the overal daily voice chat times and saves
   * it as a
   * PNG image.
   *
   * @param userVoiceChatDatavcData UserVoiceChatData to chart.
   * @param startingDate            Starting date of the chart.
   * @param endingDate              Ending date of the chart.
   * @return true if the graph was successfully generated and saved, false
   *         otherwise.
   */
  public boolean generateGeneralVoiceChatTimeChart(
      List<UserVoiceChatData> voiceChatDataList,
      Date startingDate,
      Date endingDate) {
    if (voiceChatDataList == null || voiceChatDataList.isEmpty()) {
      LOGGER.warn("No data provided for chart generation.");
      return false;
    }
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    for (UserVoiceChatData userVoiceChatData : voiceChatDataList) {
      Map<LocalDate, Duration> dailyVoiceChatTime = fillMissingDates(
          (TreeMap<LocalDate, Duration>) userVoiceChatData.getDailyVoiceChatTime(),
          startingDate,
          endingDate);

      TimeSeries series = new TimeSeries(userVoiceChatData.getUser().getName());
      for (Entry<LocalDate, Duration> entry : dailyVoiceChatTime.entrySet()) {
        LocalDate date = entry.getKey();
        Duration duration = entry.getValue();

        Day day = new Day(
            date.getDayOfMonth(),
            date.getMonthValue(),
            date.getYear());

        // Convert Duration to hours (as a double) for the Y-axis
        // duration.toMillis() / (1000.0 * 60 * 60) gives hours with decimal precision
        double hours = duration.toMillis() / (3600000.0); // 1000ms * 60s * 60m = 3,600,000ms in an hour

        series.add(day, hours);
      }
      dataset.addSeries(series);
    }
    if (dataset.getSeriesCount() == 0) {
      LOGGER.warn("No valid data provided for overall voice chat time chart.");
      return false;
    }

    // 2. Create the JFreeChart object
    String chartTitle = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_OVERALL_TITLE.get();
    String xLabel = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_X_AXIS.get();
    String yLabel = LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHART_Y_AXIS.get();
    boolean showLegend = false;
    boolean useTooltips = false;
    boolean generateUrls = false;
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        chartTitle,
        xLabel,
        yLabel,
        dataset,
        showLegend,
        useTooltips,
        generateUrls);

    applyChartStyling(chart);

    // Customize the renderer (line and shapes)
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) chart.getPlot()).getRenderer();
    List<Color> seriesColors = new ArrayList<>(SERIES_COLORS);
    Collections.shuffle(seriesColors);

    final double xOffset = 0.01;
    double yOffset = 0.01;
    for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
      Color color = seriesColors.remove(0);
      renderer.setSeriesPaint(seriesIndex, color);
      renderer.setSeriesShapesVisible(seriesIndex, true); // Show shapes at each data point
      renderer.setSeriesStroke(seriesIndex, new BasicStroke(3));

      UserVoiceChatData userVoiceChatData = voiceChatDataList.get(seriesIndex);
      addAnnotation(chart,
          String.format("%s: %s",
              userVoiceChatData.getUser().getName(),
              SessionTimeUtil.formatMilliseconds(userVoiceChatData.getVoiceChatTimeMillis())),
          color,
          xOffset,
          yOffset);

      yOffset += 0.04;
    }

    // 4. Save the chart as a PNG image
    File outputFile = new File("overall.png");
    File outputDir = outputFile.getParentFile();

    if (outputDir != null && !outputDir.exists()) {
      outputDir.mkdirs(); // Create parent directories if they don't exist
    }

    try {
      ChartUtils.saveChartAsPNG(outputFile, chart, 1920, 1080);
      LOGGER.info("Graph saved successfully to: " + outputFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      LOGGER.warn("Error saving chart: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Fills the missing days of the month within the passed Map based on the
   * passed starting and ending date.
   *
   * @param originalData Data to fill.
   * @param startingDate Starting date of the fill.
   * @param endingDate   Ending date of the fill.
   * @return Map with the missing days filled.
   */
  private Map<LocalDate, Duration> fillMissingDates(
      TreeMap<LocalDate, Duration> originalData,
      Date startingDate,
      Date endingDate) {
    if (originalData == null || originalData.isEmpty()) {
      LOGGER.warn("Input data is null or empty, returning an empty map.");
      return new TreeMap<>(); // Or originalData if you prefer to return null/empty input directly
    }

    TreeMap<LocalDate, Duration> filledData = new TreeMap<>();

    ZoneId zoneId = SessionTimeUtil.getZoneId();
    LocalDate startingLocalDate = startingDate.toInstant().atZone(zoneId).toLocalDate();
    LocalDate endingLocalDate = endingDate.toInstant().atZone(zoneId).toLocalDate();
    LocalDate currentDate = startingLocalDate;

    // Iterate from the first date to the last date
    while (!currentDate.isAfter(endingLocalDate)) {
      // If the current date is missing, add it with 0 duration
      filledData.put(currentDate, originalData.getOrDefault(currentDate, Duration.ofSeconds(0)));
      currentDate = currentDate.plusDays(1); // Move to the next day
    }

    LOGGER.info("Missing dates filled. Original size: " + originalData.size() + ", Filled size: " + filledData.size());
    return filledData;
  }

  /**
   * Applies common styling to a JFreeChart object, including dynamic X-axis tick
   * units.
   *
   * @param chart        The JFreeChart object to style.
   * @param startingDate The logical starting date for the chart's data.
   * @param endingDate   The logical ending date for the chart's data.
   */
  private void applyChartStyling(JFreeChart chart) {
    chart.setBackgroundPaint(Color.BLACK);

    TextTitle title = chart.getTitle();
    title.setHorizontalAlignment(HorizontalAlignment.CENTER);
    title.setFont(title.getFont().deriveFont(Font.BOLD, 24f)); // Title font: bold, size 24
    title.setPaint(Color.WHITE);

    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.DARK_GRAY);
    plot.setDomainGridlinePaint(Color.WHITE);
    plot.setRangeGridlinePaint(Color.WHITE);
    plot.setOutlineVisible(false); // No border around the plot area

    // X-axis (DateAxis) styling
    DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
    dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
    dateAxis.setVerticalTickLabels(true); // Rotate labels for better readability
    dateAxis.setLabelFont(dateAxis.getLabelFont().deriveFont(Font.BOLD, 24f));
    dateAxis.setLabelPaint(Color.WHITE);
    dateAxis.setTickLabelFont(dateAxis.getTickLabelFont().deriveFont(24f));
    dateAxis.setTickLabelPaint(Color.WHITE);
    dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));

    // Y-axis (NumberAxis) styling
    NumberAxis hoursAxis = (NumberAxis) plot.getRangeAxis();
    hoursAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    hoursAxis.setNumberFormatOverride(new DecimalFormat("0.00"));
    hoursAxis.setRange(new Range(0.0, 12.0));
    hoursAxis.setLabelFont(hoursAxis.getLabelFont().deriveFont(Font.BOLD, 24f));
    hoursAxis.setTickLabelFont(hoursAxis.getTickLabelFont().deriveFont(24f));
    hoursAxis.setLabelPaint(Color.WHITE);
    hoursAxis.setTickLabelPaint(Color.WHITE);
  }

  private void addAnnotation(
      JFreeChart chart,
      String annotationText,
      Color color,
      double xOffset,
      double yOffset) {
    XYPlot plot = chart.getXYPlot();
    DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
    NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();

    // Determine the maximum data values for X and Y
    double maxX = dateAxis.getMaximumDate().getTime(); // Max time in milliseconds
    double maxY = numberAxis.getRange().getUpperBound(); // Max value on Y-axis

    // Calculate small offsets in data units to move the text slightly inwards.
    // For dates, a small percentage of the total range can work (e.g., 1% of the
    // chart's time span).
    // For values, a small percentage of the total range.
    double timeRangeMillis = dateAxis.getRange().getLength();
    double valueRange = numberAxis.getRange().getLength();

    double xOffsetDataUnits = timeRangeMillis * xOffset; // 1% of the total time range
    double yOffsetDataUnits = valueRange * yOffset; // 5% of the total value range

    // Adjust the coordinates:
    // For TextAnchor.TOP_RIGHT, subtract from X to move left, and subtract from Y
    // to move down.
    double xCoord = maxX - xOffsetDataUnits;
    double yCoord = maxY - yOffsetDataUnits;

    // Create the annotation object with the adjusted coordinates
    XYTextAnnotation annotation = new XYTextAnnotation(annotationText, xCoord, yCoord);
    annotation.setFont(new Font("SansSerif", Font.BOLD, 26));
    annotation.setPaint(color);
    annotation.setTextAnchor(TextAnchor.TOP_RIGHT);

    plot.addAnnotation(annotation);
  }
}
