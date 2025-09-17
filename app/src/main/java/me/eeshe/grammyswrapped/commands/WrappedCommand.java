package me.eeshe.grammyswrapped.commands;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.ListenedArtist;
import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.UserGameData;
import me.eeshe.grammyswrapped.model.UserMusicData;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.service.UserGameDataService;
import me.eeshe.grammyswrapped.service.UserMusicDataService;
import me.eeshe.grammyswrapped.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class WrappedCommand {
  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedCommand.class);

  private final JDA bot;
  private final StatsService statsService;
  private final UserGameDataService userGameDataService;
  private final UserMusicDataService userMusicDataService;

  public WrappedCommand(JDA bot, StatsService statsService) {
    this.bot = bot;
    this.statsService = statsService;
    this.userGameDataService = new UserGameDataService(bot);
    this.userMusicDataService = new UserMusicDataService(bot);
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

    event.replyEmbeds(List.of(
        createPlayedGamesEmbed(title, loggedPresences),
        createPlayedMusicEmbed(title, loggedPresences))).queue();
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
    return createEmbed(Color.BLUE, title, stringBuilder.toString()).build();
  }

  private MessageEmbed createPlayedMusicEmbed(String title, List<LoggablePresence> loggedPresences) {
    Map<String, UserMusicData> userMusicDataMap = userMusicDataService.computeUserMusicData(loggedPresences);

    StringBuilder stringBuilder = new StringBuilder("## Listened Music").append("\n");
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

    return listenedArtists.subList(0, amount);
  }

  private Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  /**
   * Creates a basic EmbedBuilder with a title, description, and color.
   * Sets a default timestamp to the current time.
   *
   * @param color       The color of the embed.
   * @param title       The title of the embed.
   * @param description The main description text of the embed.
   * @return An EmbedBuilder instance, ready for further customization.
   */
  public static EmbedBuilder createEmbed(Color color, String title, String description) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setColor(color);
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(description);
    embedBuilder.setTimestamp(Instant.now()); // Good practice to include a timestamp by default
    return embedBuilder;
  }
}
