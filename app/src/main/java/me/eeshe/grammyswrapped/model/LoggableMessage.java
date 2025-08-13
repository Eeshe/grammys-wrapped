package me.eeshe.grammyswrapped.model;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public record LoggableMessage(
    String userId,
    String channelId,
    String content,
    List<String> attachmentUrls) {

  public static LoggableMessage fromMessage(Message message) {
    return new LoggableMessage(
        message.getMember().getUser().getId(),
        message.getChannelId(),
        message.getContentRaw(),
        message.getAttachments().stream().map(Attachment::getUrl).toList());
  }
}
