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

    YA_VENGO_MODAL_TITLE("ya-vengo.modal.title"),
    YA_VENGO_MODAL_QUESTION("ya-vengo.modal.question"),
    YA_VENGO_MODAL_QUESTION_PLACEHOLDER("ya-vengo.modal.question-placeholder"),
    YA_VENGO_INVALID_TIME_FORMAT("ya-vengo.invalid-time-format"),

    YA_VENGO_EMBED_RUNNING_TITLE("ya-vengo.embed.running.title"),
    YA_VENGO_EMBED_RUNNING_IMAGE("ya-vengo.embed.running.image"),
    YA_VENGO_EMBED_RUNNING_DESCRIPTION_POSITIVE("ya-vengo.embed.running.description.positive"),
    YA_VENGO_EMBED_RUNNING_DESCRIPTION_NEGATIVE("ya-vengo.embed.running.description.negative"),
    YA_VENGO_EMBED_RUNNING_BUTTON("ya-vengo.embed.running.button"),

    YA_VENGO_EMBED_STOPPED_TITLE("ya-vengo.embed.stopped.title"),
    YA_VENGO_EMBED_STOPPED_IMAGE("ya-vengo.embed.stopped.image"),
    YA_VENGO_EMBED_STOPPED_DESCRIPTION_POSITIVE("ya-vengo.embed.stopped.description.positive"),
    YA_VENGO_EMBED_STOPPED_DESCRIPTION_NEGATIVE("ya-vengo.embed.stopped.description.negative"),
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
