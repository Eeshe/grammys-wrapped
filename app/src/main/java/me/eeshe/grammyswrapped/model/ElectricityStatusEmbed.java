package me.eeshe.grammyswrapped.model;

import java.time.LocalDateTime;
import java.util.Map;

public class ElectricityStatusEmbed {
    private final String guildId;
    private final String channelId;
    private final String messageId;
    private final Map<String, UserElectricityStatus> participants;

    private LocalDateTime updatedAt;

    public ElectricityStatusEmbed(
            String guildId,
            String channelId,
            String messageId,
            Map<String, UserElectricityStatus> participants) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.participants = participants;

        updateUpdatedAt();
    }

    public ElectricityStatusEmbed(
            String guildId,
            String channelId,
            String messageId,
            Map<String, UserElectricityStatus> participants,
            LocalDateTime updatedAt) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.participants = participants;
        this.updatedAt = updatedAt;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Map<String, UserElectricityStatus> getParticipants() {
        return participants;
    }

    public void addParticipant(UserElectricityStatus userElectricityStatus) {
        participants.put(userElectricityStatus.getUserId(), userElectricityStatus);
        updateUpdatedAt();
    }

    public void removeParticipant(UserElectricityStatus userElectricityStatus) {
        participants.remove(userElectricityStatus.getUserId());
        updateUpdatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
