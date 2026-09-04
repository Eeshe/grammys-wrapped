package me.eeshe.grammyswrapped.model;

import me.eeshe.grammyswrapped.service.LocalizationService;

public enum LocalizedMessage {
    BOT_TOKEN_NOT_CONFIGURED("bot.token.not_configured"),
    INVALID_TIME_FORMAT("invalid-time-format"),

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
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_TITLE("wrapped.electricity_status.title"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_POWER_OUTAGES_OVERALL_LABEL(
            "wrapped.electricity_status.power_outages_overall_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_POWER_OUTAGE_TIME_OVERALL_LABEL(
            "wrapped.electricity_status.power_outage_time_overall_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_LONGEST_POWER_OUTAGE_OVERALL_LABEL(
            "wrapped.electricity_status.longest_power_outage_overall_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_SHORTEST_POWER_OUTAGE_OVERALL_LABEL(
            "wrapped.electricity_status.shortest_power_outage_overall_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_TARNISHED_AWARD_LABEL(
            "wrapped.electricity_status.tarnished_award_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_POWER_OUTAGES_LABEL("wrapped.electricity_status.power_outages_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_TOTAL_POWER_OUTAGE_TIME_OVERALL_LABEL(
            "wrapped.electricity_status.total_power_outage_time_overall_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_TOTAL_POWER_OUTAGE_TIME_LABEL(
            "wrapped.electricity_status.total_power_outage_time_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_AVERAGE_POWER_OUTAGE_DURATION_LABEL(
            "wrapped.electricity_status.average_power_outage_duration_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_LONGEST_POWER_OUTAGE_LABEL(
            "wrapped.electricity_status.longest_power_outage_label"),
    GRAMMYS_WRAPPED_ELECTRICITY_STATUS_SHORTEST_POWER_OUTAGE_LABEL(
            "wrapped.electricity_status.shortest_power_outage_label"),

    YA_VENGO_MODAL_TITLE("ya-vengo.modal.title"),
    YA_VENGO_MODAL_QUESTION("ya-vengo.modal.question"),
    YA_VENGO_MODAL_QUESTION_PLACEHOLDER("ya-vengo.modal.question-placeholder"),

    YA_VENGO_EMBED_RUNNING_TITLE("ya-vengo.embed.running.title"),
    YA_VENGO_EMBED_RUNNING_IMAGE("ya-vengo.embed.running.image"),
    YA_VENGO_EMBED_RUNNING_DESCRIPTION_POSITIVE("ya-vengo.embed.running.description.positive"),
    YA_VENGO_EMBED_RUNNING_DESCRIPTION_NEGATIVE("ya-vengo.embed.running.description.negative"),
    YA_VENGO_EMBED_RUNNING_BUTTON("ya-vengo.embed.running.button"),

    YA_VENGO_EMBED_STOPPED_TITLE("ya-vengo.embed.stopped.title"),
    YA_VENGO_EMBED_STOPPED_IMAGE("ya-vengo.embed.stopped.image"),
    YA_VENGO_EMBED_STOPPED_DESCRIPTION_POSITIVE("ya-vengo.embed.stopped.description.positive"),
    YA_VENGO_EMBED_STOPPED_DESCRIPTION_NEGATIVE("ya-vengo.embed.stopped.description.negative"),

    ELECTRICITY_STATUS_EMBED_TITLE("electricity-status.embed.title"),
    ELECTRICITY_STATUS_EMBED_EMPTY_DESCRIPTION("electricity-status.embed.empty-description"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_NICKNAME_WITH_ELECTRICITY(
            "electricity-status.embed.description.nickname.with-electricity"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_NICKNAME_WITHOUT_ELECTRICITY(
            "electricity-status.embed.description.nickname.without-electricity"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITH_ELECTRICITY_PRE_OUTAGE(
            "electricity-status.embed.description.status.with-electricity-pre-outage"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITH_ELECTRICITY_POST_OUTAGE(
            "electricity-status.embed.description.status.with-electricity-post-outage"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITHOUT_ELECTRICITY(
            "electricity-status.embed.description.status.without-electricity"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITH_ELECTRICITY_PRE_OUTAGE(
            "electricity-status.embed.description.estimate.with-electricity-pre-outage"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITH_ELECTRICITY_POST_OUTAGE(
            "electricity-status.embed.description.estimate.with-electricity-post-outage"),
    ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITHOUT_ELECTRICITY(
            "electricity-status.embed.description.estimate.without-electricity"),
    ELECTRICITY_STATUS_EMBED_FOOTER("electricity-status.embed.footer"),
    ELECTRICITY_STATUS_EMBED_IMAGE_URL("electricity-status.embed.image-url"),
    ELECTRICITY_STATUS_EMBED_ELECTRICITY_OUT_BUTTON("electricity-status.embed.electricity-out-button"),
    ELECTRICITY_STATUS_EMBED_ELECTRICITY_IN_BUTTON("electricity-status.embed.electricity-in-button"),

    ELECTRICITY_IN_MODAL_TITLE("electricity-in.modal.title"),
    ELECTRICITY_IN_MODAL_QUESTION("electricity-in.modal.question"),
    ELECTRICITY_IN_MODAL_QUESTION_PLACEHOLDER("electricity-in.modal.question-placeholder"),

    ELECTRICITY_STATUS_ALERT_ELECTRICITY_OUT("electricity-status.alert.electricity-out"),
    ELECTRICITY_STATUS_ALERT_ELECTRICITY_IN("electricity-status.alert.electricity-in"),

    ELECTRICITY_STATUS_REMINDER("electricity-status.reminder"),
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
