package me.eeshe.grammyswrapped.model.userdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.dv8tion.jda.api.entities.User;

public class UserMessageData extends UserData {
  // ChannelId, Messages/Attachments
  private final Map<String, List<String>> messages;
  private final Map<String, List<String>> attachmentUrls;

  public UserMessageData(User user) {
    super(user);

    this.messages = new HashMap<>();
    this.attachmentUrls = new HashMap<>();
  }

  /**
   * Counts the overall amount of messages sent to any channel.
   */
  public int countOverallMessages() {
    return (int) Stream.concat(
        messages.values().stream().flatMap(List::stream),
        attachmentUrls.values().stream().flatMap(List::stream)).count();
  }

  /**
   * Counts the overall amount of messages sent to the channel with the passed ID.
   *
   * @param channelId ID of the channel to count the messages of.
   */
  public int countOverallMessages(String channelId) {
    return countSentMessages(channelId) + countSentAttachments(channelId);
  }

  public Map<String, List<String>> getMessages() {
    return messages;
  }

  public void addMessage(String channelId, String message) {
    List<String> currentMessages = this.messages.getOrDefault(channelId, new ArrayList<>());
    currentMessages.add(message);

    this.messages.put(channelId, currentMessages);
  }

  /**
   * Counts the amount of messages sent in the channel with the passed ID.
   *
   * @param channelId ID of the channel to count the messages of.
   */
  public int countSentMessages(String channelId) {
    return messages.getOrDefault(channelId, new ArrayList<>()).size();
  }

  public Map<String, List<String>> getAttachmentUrls() {
    return attachmentUrls;
  }

  public void addAttachments(String channelId, List<String> attachmentUrls) {
    List<String> currentAttachmentUrls = this.attachmentUrls.getOrDefault(channelId, new ArrayList<>());
    currentAttachmentUrls.addAll(attachmentUrls);

    this.attachmentUrls.put(channelId, currentAttachmentUrls);
  }

  /**
   * Counts the amount of attachments sent in the channel with the passed ID.
   *
   * @param channelId ID of the channel to count the attachments of.
   */
  public int countSentAttachments(String channelId) {
    return attachmentUrls.getOrDefault(channelId, new ArrayList<>()).size();
  }

  /**
   * Computes a List with the IDs of all the channels the user has sent a message
   * in.
   *
   * @return Set with channel IDs.
   */
  public List<String> getMessagedChannelIds() {
    return Stream.concat(
        messages.keySet().stream(),
        attachmentUrls.keySet().stream())
        .distinct()
        .sorted(Comparator.comparingInt(channelId -> countOverallMessages((String) channelId)).reversed())
        .toList();
  }
}
