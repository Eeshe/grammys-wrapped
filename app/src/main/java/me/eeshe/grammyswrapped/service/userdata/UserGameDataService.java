package me.eeshe.grammyswrapped.service.userdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.userdata.UserData;
import me.eeshe.grammyswrapped.model.userdata.UserGameData;
import me.eeshe.grammyswrapped.util.MapUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class UserGameDataService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserGameDataService.class);

  private final JDA bot;

  public UserGameDataService(JDA bot) {
    this.bot = bot;
  }

  /**
   * Computes the UserGameData present in the passed list of LoggablePresences.
   *
   * @param loggablePresences LoggablePresences to compute.
   * @return Map with the computed UserGameData.
   */
  public Map<String, UserGameData> computeUserGameData(List<LoggablePresence> loggablePresences) {
    LOGGER.info("Computing UserGameData from {} presences...", loggablePresences.size());
    Map<String, UserGameData> userGameDataMap = new HashMap<>();
    // Map used to store the start time of multiple games per user
    Map<String, Map<String, LoggablePresence>> startGamePresences = new HashMap<>();
    for (LoggablePresence loggablePresence : loggablePresences) {
      String userId = loggablePresence.userId();
      User user = bot.getUserById(userId);
      if (user == null) {
        continue;
      }
      if (!loggablePresence.type().equals("PLAYING")) {
        continue;
      }
      String gameName = loggablePresence.name();
      if (loggablePresence.starting()) {
        Map<String, LoggablePresence> startPresences = startGamePresences.getOrDefault(userId, new HashMap<>());
        startPresences.put(gameName, loggablePresence);
        startGamePresences.put(userId, startPresences);
        continue;
      }
      Map<String, LoggablePresence> startPresences = startGamePresences.getOrDefault(userId, new HashMap<>());
      LoggablePresence startPresence = startPresences.get(gameName);
      long playedTimeMillis = 0L;
      if (startPresence != null) {
        long startTimeMillis = startPresence.date().toInstant().toEpochMilli();
        long endTimeMillis = loggablePresence.date().toInstant().toEpochMilli();
        playedTimeMillis = endTimeMillis - startTimeMillis;
        if (playedTimeMillis < 1000) {
          // Played time was less than one second, don't use it
          continue;
        }
        startPresences.remove(gameName);
        startGamePresences.put(userId, startPresences);
      }
      UserGameData userGameData = userGameDataMap.getOrDefault(userId, new UserGameData(user));
      userGameData.addPlayedGame(gameName, playedTimeMillis);

      userGameDataMap.put(userId, userGameData);
    }
    final boolean reverseSort = true;
    for (UserGameData userGameData : userGameDataMap.values()) {
      MapUtil.sortByValue(userGameData.getPlayedGames(), reverseSort);
    }
    LOGGER.info("Finished computing {} UserGameData objects.", userGameDataMap.size());

    return (Map<String, UserGameData>) UserData.sortByUsername(userGameDataMap);
  }
}

record GamePresence(String gameName, LoggablePresence loggablePresence) {

}
