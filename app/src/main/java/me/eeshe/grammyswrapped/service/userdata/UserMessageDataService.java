package me.eeshe.grammyswrapped.service.userdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggableMessage;
import me.eeshe.grammyswrapped.model.userdata.UserData;
import me.eeshe.grammyswrapped.model.userdata.UserMessageData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class UserMessageDataService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserMessageDataService.class);

  private final JDA bot;

  public UserMessageDataService(JDA bot) {
    this.bot = bot;
  }

  /**
   * Computes the UserMessageData present in the passed list of LoggableMessages.
   *
   * @param loggableMessages LoggableMessages to compute.
   * @return Map with the computed UserMessageData.
   */
  public Map<String, UserMessageData> computeUserMusicData(List<LoggableMessage> loggableMessages) {
    LOGGER.info("Computing UserMessageData from {} messages...", loggableMessages.size());
    Map<String, UserMessageData> userMessageDataMap = new HashMap<>();
    for (LoggableMessage loggableMessage : loggableMessages) {
      String userId = loggableMessage.userId();
      User user = bot.getUserById(userId);
      if (user == null) {
        LOGGER.error("User '{}' not found.", userId);
        continue;
      }
      String channelId = loggableMessage.channelId();
      String message = loggableMessage.content();
      List<String> attachmentUrls = loggableMessage.attachmentUrls();

      UserMessageData userMessageData = userMessageDataMap.getOrDefault(userId, new UserMessageData(user));
      userMessageData.addMessage(channelId, message);
      userMessageData.addAttachments(channelId, attachmentUrls);

      userMessageDataMap.put(userId, userMessageData);
    }
    LOGGER.info("Finished computing {} UserMessageData objects.", userMessageDataMap.size());

    return (Map<String, UserMessageData>) UserData.sortByUsername(userMessageDataMap);
  }
}
