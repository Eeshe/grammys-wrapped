package me.eeshe.grammyswrapped.commands;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.ListenedArtist;
import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.model.LoggableMessage;
import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatConnection;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatEvent;
import me.eeshe.grammyswrapped.model.userdata.UserGameData;
import me.eeshe.grammyswrapped.model.userdata.UserMessageData;
import me.eeshe.grammyswrapped.model.userdata.UserMusicData;
import me.eeshe.grammyswrapped.model.userdata.UserVoiceChatData;
import me.eeshe.grammyswrapped.service.ChartService;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.service.VoiceChatData;
import me.eeshe.grammyswrapped.service.userdata.UserGameDataService;
import me.eeshe.grammyswrapped.service.userdata.UserMessageDataService;
import me.eeshe.grammyswrapped.service.userdata.UserMusicDataService;
import me.eeshe.grammyswrapped.service.userdata.UserVoiceChatDataService;
import me.eeshe.grammyswrapped.util.EmbedUtil;
import me.eeshe.grammyswrapped.util.SessionTimeUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class WrappedCommand {
  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedCommand.class);

  private final JDA bot;
  private final StatsService statsService;
  private final UserGameDataService userGameDataService;
  private final UserMusicDataService userMusicDataService;
  private final UserMessageDataService userMessageDataService;
  private final UserVoiceChatDataService userVoiceChatDataService;
  private final ChartService chartService;

  public WrappedCommand(JDA bot, StatsService statsService) {
    this.bot = bot;
    this.statsService = statsService;
    this.userGameDataService = new UserGameDataService(bot);
    this.userMusicDataService = new UserMusicDataService(bot);
    this.userMessageDataService = new UserMessageDataService(bot);
    this.userVoiceChatDataService = new UserVoiceChatDataService(bot);
    this.chartService = new ChartService();
  }

  /**
   * Handles the interaction with the /wrapped <StartingDate> <EndingDate>
   * command.
   *
   * @param event SlashCommandInteractionEvent.
   */
  public void handle(SlashCommandInteractionEvent event) {
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
    event.reply("Processing stats...").queue();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String title = LocalizedMessage.GRAMMYS_WRAPPED_TITLE.getFormatted(
        simpleDateFormat.format(startingDate),
        simpleDateFormat.format(endingDate));
    List<LoggablePresence> loggedPresences = statsService.fetchPresences(startingDate, endingDate);
    List<LoggableMessage> loggedMessages = statsService.fetchMessages(startingDate, endingDate);
    List<LoggableVoiceChatConnection> loggedVoiceChatConnections = statsService.fetchVoiceChatConnections(startingDate,
        endingDate);
    List<LoggableVoiceChatEvent> loggedVoiceChatEvents = statsService.fetchVoiceChatEvents(startingDate, endingDate);

    List<FileUpload> fileUploads = new ArrayList<>();
    try {
      Files.list(Paths.get("."))
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().endsWith(".png"))
          .forEach(path -> {
            fileUploads.add(FileUpload.fromData(path));
          });
    } catch (IOException e) {
      LOGGER.error("Error uploading graph files. {}", e.getMessage());
    }

    TextChannel responseChannel = (TextChannel) event.getChannel();

    sendPlayedGamesEmbed(
        responseChannel,
        title,
        loggedPresences);
    sendPlayedMusicEmbed(
        responseChannel,
        title,
        loggedPresences);
    sendMessagesSentEmbed(
        responseChannel,
        title,
        loggedMessages);
    sendVoiceChatEmbeds(
        responseChannel,
        title,
        startingDate,
        endingDate,
        loggedVoiceChatConnections,
        loggedVoiceChatEvents);
  }

  private Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  private void sendPlayedGamesEmbed(
      TextChannel textChannel,
      String title,
      List<LoggablePresence> loggedPresences) {
    Map<String, UserGameData> userGameDataMap = userGameDataService.computeUserGameData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder(LocalizedMessage.GRAMMYS_WRAPPED_PLAYED_GAMES_TITLE.get())
        .append("\n");
    for (UserGameData userGameData : userGameDataMap.values()) {
      String username = userGameData.getUser().getName();
      List<String> playedGames = userGameData.getPlayedGameList();

      stringBuilder.append("### ").append(username).append("\n");
      for (String playedGame : playedGames) {
        stringBuilder.append("- ").append(playedGame).append("\n");
      }
    }
    textChannel.sendMessageEmbeds(EmbedUtil.createEmbed(Color.BLUE, title, stringBuilder.toString()).build()).queue();
  }

  private void sendPlayedMusicEmbed(
      TextChannel textChannel,
      String title,
      List<LoggablePresence> loggedPresences) {
    Map<String, UserMusicData> userMusicDataMap = userMusicDataService.computeUserMusicData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder(LocalizedMessage.GRAMMYS_WRAPPED_LISTENED_MUSIC_TITLE.get())
        .append("\n");
    for (UserMusicData userMusicData : userMusicDataMap.values()) {
      String username = userMusicData.getUser().getName();
      List<ListenedArtist> listenedArtists = userMusicData.getListenedArtistsList();
      int totalListenedSongs = listenedArtists.stream()
          .mapToInt(listenedArtist -> listenedArtist.getListenedSongs().size()).sum();
      listenedArtists = selectTopArtists(listenedArtists, 20);

      stringBuilder.append("### ").append(username).append(" (")
          .append(String.valueOf(totalListenedSongs)).append(")\n");
      for (ListenedArtist listenedArtist : listenedArtists) {
        String artistName = listenedArtist.getName();
        stringBuilder.append("- ").append(artistName).append(" (")
            .append(String.valueOf(listenedArtist.getListenedSongs().size())).append(")\n");
      }
    }
    textChannel.sendMessageEmbeds(EmbedUtil.createEmbed(Color.GREEN, title, stringBuilder.toString()).build()).queue();
    ;
  }

  private List<ListenedArtist> selectTopArtists(List<ListenedArtist> listenedArtists, int amount) {
    listenedArtists.sort(Comparator.comparing(listenedArtist -> listenedArtist.getListenedSongs().size()));
    listenedArtists = listenedArtists.reversed();

    return listenedArtists.subList(0, Math.min(listenedArtists.size(), amount));
  }

  private void sendMessagesSentEmbed(
      TextChannel textChannel,
      String title,
      List<LoggableMessage> loggedMessages) {
    Map<String, UserMessageData> userMessageDataMap = userMessageDataService.computeUserMusicData(loggedMessages);
    StringBuilder stringBuilder = new StringBuilder(LocalizedMessage.GRAMMYS_WRAPPED_SENT_MESSAGES_TITLE.get())
        .append("\n");

    for (UserMessageData userMessageData : userMessageDataMap.values()) {
      String username = userMessageData.getUser().getName();

      stringBuilder.append("## ").append(username).append(" (").append(userMessageData.countOverallMessages())
          .append(")\n");

      for (String channelId : userMessageData.getMessagedChannelIds()) {
        TextChannel messageChannel = bot.getChannelById(TextChannel.class, channelId);
        if (messageChannel == null) {
          LOGGER.error("Couldn't find text channel with ID '{}'", channelId);
          continue;
        }
        String messageChannelName = messageChannel.getName();

        stringBuilder.append("### ").append(messageChannelName).append(" (")
            .append(userMessageData.countOverallMessages(channelId)).append(")\n");
        stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_SENT_MESSAGES_MESSAGES_LABEL.getFormatted(
            userMessageData.countSentMessages(channelId))).append("\n");
        stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_SENT_MESSAGES_ATTACHMENTS_LABEL.getFormatted(
            userMessageData.countSentAttachments(channelId))).append("\n");
      }
    }
    textChannel.sendMessageEmbeds(EmbedUtil.createEmbed(Color.YELLOW, title, stringBuilder.toString()).build()).queue();
  }

  private void sendVoiceChatEmbeds(
      TextChannel textChannel,
      String title,
      Date startingDate,
      Date endingDate,
      List<LoggableVoiceChatConnection> loggedVoiceChatConnections,
      List<LoggableVoiceChatEvent> loggedVoiceChatEvents) {
    VoiceChatData voiceChatData = userVoiceChatDataService
        .computeUserVoiceChatData(
            loggedVoiceChatConnections,
            loggedVoiceChatEvents);
    Map<String, UserVoiceChatData> userVoiceChatDataMap = voiceChatData.getUserVoiceChatData();
    StringBuilder stringBuilder = new StringBuilder(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_TITLE.get())
        .append("\n");

    List<FileUpload> fileUploads = new ArrayList<>();

    chartService.generateGeneralVoiceChatTimeChart(
        new ArrayList<>(userVoiceChatDataMap.values()),
        startingDate,
        endingDate);
    fileUploads.add(FileUpload.fromData(Paths.get("overall.png")));

    for (UserVoiceChatData userVoiceChatData : userVoiceChatDataMap.values()) {
      String username = userVoiceChatData.getUser().getName();

      stringBuilder.append("## ").append(username).append("\n");
      stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_JOINED_VCS_LABEL.getFormatted(
          userVoiceChatData.getJoinedVoiceChats())).append("\n");
      stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_VC_TIME_LABEL.getFormatted(
          SessionTimeUtil.formatMilliseconds(userVoiceChatData.getVoiceChatTimeMillis()))).append("\n");
      stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_MUTED_TIME_LABEL.getFormatted(
          SessionTimeUtil.formatMilliseconds(userVoiceChatData.getMutedVoiceChatTimeMillis()))).append("\n");
      stringBuilder.append(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_DEAFENED_TIME_LABEL.getFormatted(
          SessionTimeUtil.formatMilliseconds(userVoiceChatData.getDeafenedVoiceChatTimeMillis()))).append("\n");

      chartService.generateUserVcTimeChart(
          userVoiceChatData,
          startingDate,
          endingDate);
      fileUploads.add(FileUpload.fromData(Paths.get(username + ".png")));
    }
    MessageEmbed mainEmbed = EmbedUtil.createEmbed(Color.CYAN, title, stringBuilder.toString())
        .setFooter(LocalizedMessage.GRAMMYS_WRAPPED_VOICE_CHAT_CHARTS_SENDING.get()).build();

    textChannel.sendMessageEmbeds(mainEmbed).queue();
    textChannel.sendFiles(fileUploads).queue();
  }

}
