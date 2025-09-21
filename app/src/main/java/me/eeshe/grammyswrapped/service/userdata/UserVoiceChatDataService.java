package me.eeshe.grammyswrapped.service.userdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggableVoiceChatConnection;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatEvent;
import me.eeshe.grammyswrapped.model.userdata.UserData;
import me.eeshe.grammyswrapped.model.userdata.UserVoiceChatData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class UserVoiceChatDataService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserVoiceChatDataService.class);

  private final JDA bot;

  public UserVoiceChatDataService(JDA bot) {
    this.bot = bot;
  }

  /**
   * Computes the passed VoiceChatConnections and VoiceChatEvents.
   *
   * @param loggableVoiceChatConnections VoiceChatConnections to compute.
   * @param loggableVoiceChatEvents      VoiceChatEvents to compute.
   * @return Computed UserVoiceChatData.
   */
  public Map<String, UserVoiceChatData> computeUserVoiceChatData(
      List<LoggableVoiceChatConnection> loggableVoiceChatConnections,
      List<LoggableVoiceChatEvent> loggableVoiceChatEvents) {
    LOGGER.info("Computing UserVoiceChatData from {} entries...",
        loggableVoiceChatConnections.size() + loggableVoiceChatEvents.size());
    Map<String, UserVoiceChatData> userVoiceChatDataMap = new HashMap<>();

    computeVoiceChatConnections(userVoiceChatDataMap, loggableVoiceChatConnections);
    computeVoiceChatEvents(userVoiceChatDataMap, loggableVoiceChatEvents);

    return (Map<String, UserVoiceChatData>) UserData.sortByUsername(userVoiceChatDataMap);
  }

  /**
   * Computes the passed VoiceChatConnections and adds them to the passed
   * UserVoiceChatData Map.
   *
   * @param userVoiceChatDataMap         UserVoiceChatData Map to modify.
   * @param loggableVoiceChatConnections VoiceChatConnections to compute.
   */
  private void computeVoiceChatConnections(
      Map<String, UserVoiceChatData> userVoiceChatDataMap,
      List<LoggableVoiceChatConnection> loggableVoiceChatConnections) {
    Map<String, LoggableVoiceChatConnection> previousVoiceChatConnections = new HashMap<>();
    for (LoggableVoiceChatConnection voiceChatConnection : loggableVoiceChatConnections) {
      String userId = voiceChatConnection.userId();
      User user = bot.getUserById(userId);
      if (user == null) {
        LOGGER.error("User '{}' not found.", userId);
        continue;
      }
      boolean joined = voiceChatConnection.joined();

      UserVoiceChatData userVoiceChatData = userVoiceChatDataMap.getOrDefault(userId, new UserVoiceChatData(user));
      if (joined) {
        userVoiceChatData.increaseJoinedVoiceChats();
      } else {
        LoggableVoiceChatConnection previousVoiceChatConnection = previousVoiceChatConnections.remove(userId);
        if (previousVoiceChatConnection != null && previousVoiceChatConnection.joined()) {
          // Previous voice chat connection was a join and current one is a
          // leave, add voice chat time
          userVoiceChatData.addVoiceChatTime(
              previousVoiceChatConnection.date(),
              voiceChatConnection.date());
        }
      }
      userVoiceChatDataMap.put(userId, userVoiceChatData);
      previousVoiceChatConnections.put(userId, voiceChatConnection);
    }
  }

  /**
   * Computes the passed VoiceChatEvents and adds them to the passed
   * UserVoiceChatData Map.
   *
   * @param userVoiceChatDataMap    UserVoiceChatData Map to modify.
   * @param loggableVoiceChatEvents VoiceChatEvents to compute.
   */
  private void computeVoiceChatEvents(
      Map<String, UserVoiceChatData> userVoiceChatDataMap,
      List<LoggableVoiceChatEvent> loggableVoiceChatEvents) {
    Map<String, LoggableVoiceChatEvent> previousMuteEvents = new HashMap<>();
    Map<String, LoggableVoiceChatEvent> previousDeafenEvents = new HashMap<>();

    for (LoggableVoiceChatEvent voiceChatEvent : loggableVoiceChatEvents) {
      String userId = voiceChatEvent.userId();
      User user = bot.getUserById(userId);
      if (user == null) {
        LOGGER.error("User '{}' not found.", userId);
        continue;
      }
      UserVoiceChatData userVoiceChatData = userVoiceChatDataMap.getOrDefault(userId, new UserVoiceChatData(user));
      if (voiceChatEvent.muted()) {
        previousMuteEvents.put(userId, voiceChatEvent);
      } else {
        LoggableVoiceChatEvent previousMuteEvent = previousMuteEvents.get(userId);
        if (previousMuteEvent != null && previousMuteEvent.muted()) {
          userVoiceChatData.addMutedVoiceChatTime(
              previousMuteEvent.date(),
              voiceChatEvent.date());

          previousMuteEvents.remove(userId);
        }
      }
      if (voiceChatEvent.deafened() && voiceChatEvent.muted()) {
        previousDeafenEvents.put(userId, voiceChatEvent);
      } else {
        LoggableVoiceChatEvent previousDeafenEvent = previousDeafenEvents.get(userId);
        if (previousDeafenEvent != null && previousDeafenEvent.deafened()) {
          userVoiceChatData.addDeafenedVoiceChatTime(
              previousDeafenEvent.date(),
              voiceChatEvent.date());

          previousDeafenEvents.remove(userId);
        }
      }
    }
  }
}
