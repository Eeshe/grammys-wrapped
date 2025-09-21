package me.eeshe.grammyswrapped.model;

import java.util.Date;

public record LoggableVoiceChatConnection(
    Date date,
    String userId,
    boolean joined) {

  public LoggableVoiceChatConnection(String userId, boolean joined) {
    this(
        new Date(),
        userId,
        joined);
  }
}
