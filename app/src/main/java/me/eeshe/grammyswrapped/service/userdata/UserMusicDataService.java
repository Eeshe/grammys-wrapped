package me.eeshe.grammyswrapped.service.userdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.userdata.UserData;
import me.eeshe.grammyswrapped.model.userdata.UserMusicData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class UserMusicDataService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserMusicDataService.class);

  private final JDA bot;

  public UserMusicDataService(JDA bot) {
    this.bot = bot;
  }

  /**
   * Computes the UserMusicData present in the passed list of LoggablePresences.
   *
   * @param loggablePresences LoggablePresences to compute.
   * @return Map with the computed UserMusicData.
   */
  public Map<String, UserMusicData> computeUserMusicData(List<LoggablePresence> loggablePresences) {
    LOGGER.info("Computing UserMusicData from {} presences...", loggablePresences.size());
    Map<String, UserMusicData> userMusicDataMap = new HashMap<>();
    for (LoggablePresence loggablePresence : loggablePresences) {
      String userId = loggablePresence.userId();
      User user = bot.getUserById(userId);
      if (user == null) {
        LOGGER.error("User '{}' not found.", userId);
        continue;
      }
      if (!loggablePresence.type().equals("LISTENING")) {
        continue;
      }
      String appName = loggablePresence.name();
      if (!appName.equals("Spotify")) {
        continue;
      }
      String artistName = loggablePresence.state();
      if (artistName == null) {
        continue;
      }
      if (artistName.contains(";")) {
        artistName = artistName.substring(0, artistName.indexOf(";"));
      }
      String songName = loggablePresence.details();
      if (songName == null) {
        continue;
      }

      UserMusicData userMusicData = userMusicDataMap.getOrDefault(userId, new UserMusicData(user));
      userMusicData.addListenedSong(
          songName,
          artistName);

      userMusicDataMap.put(userId, userMusicData);
    }
    LOGGER.info("Finished computing {} UserMusicData objects.", userMusicDataMap.size());

    return (Map<String, UserMusicData>) UserData.sortByUsername(userMusicDataMap);
  }
}
