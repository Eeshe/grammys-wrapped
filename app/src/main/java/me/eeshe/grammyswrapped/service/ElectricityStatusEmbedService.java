package me.eeshe.grammyswrapped.service;

import java.time.Duration;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ElectricityStatusEmbedService {

    void postElectricityStatusEmbed(String guildId, String channelId);

    void sendElectricityInEstimateModal(ButtonInteractionEvent event);

    void addElectricityOutEntry(String messageId, User user, Duration electricityInEstimate);

    void addElectricityInEntry(String messageId, User user);

    void deleteElectricityStatusEmbed(String messageId);
}
