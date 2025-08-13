package me.eeshe.grammyswrapped.model;

public record LoggableVoiceChatEvent(
    String userId,
    boolean muted,
    boolean deafened) {
}
