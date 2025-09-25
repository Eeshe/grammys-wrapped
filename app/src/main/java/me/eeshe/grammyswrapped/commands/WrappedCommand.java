package me.eeshe.grammyswrapped.commands;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.ListenedArtist;
import me.eeshe.grammyswrapped.model.LoggableMessage;
import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatConnection;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatEvent;
import me.eeshe.grammyswrapped.model.userdata.UserGameData;
import me.eeshe.grammyswrapped.model.userdata.UserMessageData;
import me.eeshe.grammyswrapped.model.userdata.UserMusicData;
import me.eeshe.grammyswrapped.model.userdata.UserVoiceChatData;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.service.userdata.UserGameDataService;
import me.eeshe.grammyswrapped.service.userdata.UserMessageDataService;
import me.eeshe.grammyswrapped.service.userdata.UserMusicDataService;
import me.eeshe.grammyswrapped.service.userdata.UserVoiceChatDataService;
import me.eeshe.grammyswrapped.util.EmbedUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class WrappedCommand {
  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedCommand.class);

  private final JDA bot;
  private final StatsService statsService;
  private final UserGameDataService userGameDataService;
  private final UserMusicDataService userMusicDataService;
  private final UserMessageDataService userMessageDataService;
  private final UserVoiceChatDataService userVoiceChatDataService;

  public WrappedCommand(JDA bot, StatsService statsService) {
    this.bot = bot;
    this.statsService = statsService;
    this.userGameDataService = new UserGameDataService(bot);
    this.userMusicDataService = new UserMusicDataService(bot);
    this.userMessageDataService = new UserMessageDataService(bot);
    this.userVoiceChatDataService = new UserVoiceChatDataService(bot);
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
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String title = String.format("Grammys Wrapped %s → %s\n",
        simpleDateFormat.format(startingDate),
        simpleDateFormat.format(endingDate));
    List<LoggablePresence> loggedPresences = statsService.fetchPresences(startingDate, endingDate);
    List<LoggableMessage> loggedMessages = statsService.fetchMessages(startingDate, endingDate);
    List<LoggableVoiceChatConnection> loggedVoiceChatConnections = statsService.fetchVoiceChatConnections(startingDate,
        endingDate);
    List<LoggableVoiceChatEvent> loggedVoiceChatEvents = statsService.fetchVoiceChatEvents(startingDate, endingDate);

    event.replyEmbeds(List.of(
        createPlayedGamesEmbed(
            title,
            loggedPresences),
        createPlayedMusicEmbed(
            title,
            loggedPresences),
        createMessagesSentEmbed(
            title,
            loggedMessages),
        createVoiceChatEmbed(
            title,
            loggedVoiceChatConnections,
            loggedVoiceChatEvents)))
        .queue();
  }

  private Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  private MessageEmbed createPlayedGamesEmbed(String title, List<LoggablePresence> loggedPresences) {
    Map<String, UserGameData> userGameDataMap = userGameDataService.computeUserGameData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder("# Played Games").append("\n");
    for (UserGameData userGameData : userGameDataMap.values()) {
      String username = userGameData.getUser().getName();
      List<String> playedGames = userGameData.getPlayedGameList();

      stringBuilder.append("### ").append(username).append("\n");
      for (String playedGame : playedGames) {
        stringBuilder.append("- ").append(playedGame).append("\n");
      }
    }
    return EmbedUtil.createEmbed(Color.BLUE, title, stringBuilder.toString()).build();
  }

  private MessageEmbed createPlayedMusicEmbed(String title, List<LoggablePresence> loggedPresences) {
    Map<String, UserMusicData> userMusicDataMap = userMusicDataService.computeUserMusicData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder("# Listened Music").append("\n");
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
    return EmbedUtil.createEmbed(Color.GREEN, title, stringBuilder.toString()).build();
  }

  private List<ListenedArtist> selectTopArtists(List<ListenedArtist> listenedArtists, int amount) {
    listenedArtists.sort(Comparator.comparing(listenedArtist -> listenedArtist.getListenedSongs().size()));
    listenedArtists = listenedArtists.reversed();

    return listenedArtists.subList(0, Math.min(listenedArtists.size(), amount));
  }

  private MessageEmbed createMessagesSentEmbed(String title, List<LoggableMessage> loggedMessages) {
    Map<String, UserMessageData> userMessageDataMap = userMessageDataService.computeUserMusicData(loggedMessages);
    StringBuilder stringBuilder = new StringBuilder("# Sent Messages").append("\n");

    for (UserMessageData userMessageData : userMessageDataMap.values()) {
      String username = userMessageData.getUser().getName();

      stringBuilder.append("## ").append(username).append(" (").append(userMessageData.countOverallMessages())
          .append(")\n");

      for (String channelId : userMessageData.getMessagedChannelIds()) {
        TextChannel textChannel = bot.getChannelById(TextChannel.class, channelId);
        if (textChannel == null) {
          LOGGER.error("Couldn't find text channel with ID '{}'", channelId);
          continue;
        }
        String channelName = textChannel.getName();

        stringBuilder.append("### ").append(channelName).append(" (")
            .append(userMessageData.countOverallMessages(channelId)).append(")\n");
        stringBuilder.append("- Messages: ").append(userMessageData.countSentMessages(channelId)).append("\n");
        stringBuilder.append("- Attachments: ").append(userMessageData.countSentAttachments(channelId)).append("\n");
      }
    }
    return EmbedUtil.createEmbed(Color.YELLOW, title, stringBuilder.toString()).build();
  }

  private MessageEmbed createVoiceChatEmbed(
      String title,
      List<LoggableVoiceChatConnection> loggedVoiceChatConnections,
      List<LoggableVoiceChatEvent> loggedVoiceChatEvents) {
    Map<String, UserVoiceChatData> userVoiceChatDataMap = userVoiceChatDataService
        .computeUserVoiceChatData(
            loggedVoiceChatConnections,
            loggedVoiceChatEvents);
    StringBuilder stringBuilder = new StringBuilder("# Voice Chat Stats").append("\n");

    for (UserVoiceChatData userVoiceChatData : userVoiceChatDataMap.values()) {
      String username = userVoiceChatData.getUser().getName();

      stringBuilder.append("## ").append(username).append("\n");
      stringBuilder.append("- Joined Voice Chats: ").append(userVoiceChatData.getJoinedVoiceChats()).append("\n");
      stringBuilder.append("- Total Voice Chat Time: ")
          .append(formatMilliseconds(userVoiceChatData.getVoiceChatTimeMillis())).append("\n");
      stringBuilder.append("- Total Muted Time: ")
          .append(formatMilliseconds(userVoiceChatData.getMutedVoiceChatTimeMillis())).append("\n");
      stringBuilder.append("- Total Deafened Time: ")
          .append(formatMilliseconds(userVoiceChatData.getDeafenedVoiceChatTimeMillis())).append("\n");
    }
    return EmbedUtil.createEmbed(Color.CYAN, title, stringBuilder.toString()).build();
  }

  /**
   * Converts a time in milliseconds into the format `XXhYYmZZs`.
   *
   * Generated by Gemini.
   *
   * @param milliseconds The time duration in milliseconds.
   * @return A formatted string representing the duration.
   */
  public static String formatMilliseconds(long milliseconds) {
    if (milliseconds < 0) {
      return "";
    }
    long totalSeconds = milliseconds / 1000;

    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;

    StringBuilder stringBuilder = new StringBuilder();

    if (hours > 0) {
      stringBuilder.append(hours).append("h");
    }
    if (minutes > 0) {
      stringBuilder.append(minutes).append("m");
    } else if (hours > 0 && seconds > 0) {
      stringBuilder.append("0m");
    }
    if (seconds > 0) {
      stringBuilder.append(seconds).append("s");
    }
    if (stringBuilder.length() == 0) {
      return "0s";
    }
    return stringBuilder.toString();
  }
}
