package me.eeshe.grammyswrapped.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.UserData;
import me.eeshe.grammyswrapped.model.UserGameData;
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

      UserGameData userGameData = userGameDataMap.getOrDefault(userId, new UserGameData(user));
      userGameData.addPlayedGame(gameName, 0L);

      userGameDataMap.put(userId, userGameData);
    }
    LOGGER.info("Finished computing {} UserGameData objects.", userGameDataMap.size());

    return (Map<String, UserGameData>) UserData.sortByUsername(userGameDataMap);
  }
}
