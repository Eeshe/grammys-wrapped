package me.eeshe.grammyswrapped.model;

import java.util.Date;

public record LoggableVoiceChatEvent(
    Date date,
    String userId,
    boolean muted,
    boolean deafened) {

  public LoggableVoiceChatEvent(String userId, boolean muted, boolean deafened) {
    this(new Date(), userId, muted, deafened);
  }
}
