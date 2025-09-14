package me.eeshe.grammyswrapped.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.UserGameData;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.service.UserGameDataService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class WrappedCommand {
  private final JDA bot;
  private final StatsService statsService;
  private final UserGameDataService userGameDataService;

  public WrappedCommand(JDA bot, StatsService statsService) {
    this.bot = bot;
    this.statsService = statsService;
    this.userGameDataService = new UserGameDataService(bot);
  }

  /**
   * Handles the interaction with the /wrapped <StartingDate> <EndingDate>
   * command.
   *
   * @param event SlashCommandInteractionEvent.
   */
  public void handle(SlashCommandInteractionEvent event) {
    event.deferReply(true).queue();

    String startingDateString = event.getOption("starting-date").getAsString();
    String endingDateString = event.getOption("ending-date").getAsString();
    Date startingDate = parseDate(startingDateString);
    Date endingDate = parseDate(endingDateString);
    if (startingDate == null || endingDate == null) {
      event.getHook().editOriginalFormat("Invalid date format. Please use: yyyy-MM-dd. Ex: 2001-9-11").queue();
      return;
    }
    if (endingDate.before(startingDate)) {
      event.getHook().editOriginalFormat("Final date must be AFTER initial date.").queue();
      return;
    }
    String statisticsText = compileStatistics(startingDate, endingDate);
    event.getHook().editOriginalFormat(statisticsText).queue();
  }

  private String compileStatistics(Date startingDate, Date endingDate) {
    StringBuilder stringBuilder = new StringBuilder();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    stringBuilder.append(String.format("# Grammys Wrapped %s - %s\n",
        simpleDateFormat.format(startingDate),
        simpleDateFormat.format(endingDate)));

    List<LoggablePresence> loggedPresences = statsService.fetchPresences(startingDate, endingDate);
    stringBuilder.append(compilePlayedGamesStatistics(loggedPresences));

    return stringBuilder.toString();
  }

  private String compilePlayedGamesStatistics(List<LoggablePresence> loggedPresences) {
    Map<String, UserGameData> userGameDataMap = userGameDataService.computeUserGameData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder();
    for (UserGameData userGameData : userGameDataMap.values()) {
      String username = userGameData.getUsername();
      List<String> playedGames = userGameData.getPlayedGameList();

      stringBuilder.append("## ").append(username).append("\n");
      for (String playedGame : playedGames) {
        stringBuilder.append("- ").append(playedGame).append("\n");
      }
    }
    return stringBuilder.toString();
  }

  private Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }
}
