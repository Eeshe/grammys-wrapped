package me.eeshe.grammyswrapped.model;

import me.eeshe.grammyswrapped.service.LocalizationService;

public enum LocalizedMessage {
  BOT_TOKEN_NOT_CONFIGURED("bot.token.not_configured"),
  GRAMMYS_WRAPPED_TITLE("wrapped.title"),
  GRAMMYS_WRAPPED_PLAYED_GAMES_TITLE("wrapped.played_games.title"),
  GRAMMYS_WRAPPED_LISTENED_MUSIC_TITLE("wrapped.listened_music.title"),
  GRAMMYS_WRAPPED_SENT_MESSAGES_TITLE("wrapped.sent_messages.title"),
  GRAMMYS_WRAPPED_SENT_MESSAGES_MESSAGES_LABEL("wrapped.sent_messages.messages_label"),
  GRAMMYS_WRAPPED_SENT_MESSAGES_ATTACHMENTS_LABEL("wrapped.sent_messages.attachments_label"),
  GRAMMYS_WRAPPED_VOICE_CHAT_TITLE("wrapped.voice_chat.title"),
  GRAMMYS_WRAPPED_VOICE_CHAT_JOINED_VCS_LABEL("wrapped.voice_chat.joined_vcs_label"),
  GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_VC_TIME_LABEL("wrapped.voice_chat.total_vc_time_label"),
  GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_MUTED_TIME_LABEL("wrapped.voice_chat.total_muted_time_label"),
  GRAMMYS_WRAPPED_VOICE_CHAT_TOTAL_DEAFENED_TIME_LABEL("wrapped.voice_chat.total_deafened_time_label"),
  GRAMMYS_WRAPPED_VOICE_CHAT_CHARTS_SENDING("wrapped.voice_chat.charts_sending"),
  GRAMMYS_WRAPPED_VOICE_CHAT_CHART_USER_TITLE("wrapped.voice_chat.chart.user_title"),
  GRAMMYS_WRAPPED_VOICE_CHAT_CHART_OVERALL_TITLE("wrapped.voice_chat.chart.overall_title"),
  GRAMMYS_WRAPPED_VOICE_CHAT_CHART_Y_AXIS("wrapped.voice_chat.chart.y_axis"),
  GRAMMYS_WRAPPED_VOICE_CHAT_CHART_X_AXIS("wrapped.voice_chat.chart.x_axis"),

  ;

  private final String key;

  private LocalizedMessage(String key) {
    this.key = key;
  }

  public String get() {
    return LocalizationService.getInstance().getString(key);
  }

  public String getFormatted(Object... args) {
    return LocalizationService.getInstance().getFormattedString(key, args);
  }
}
