package me.eeshe.grammyswrapped.service;

import java.time.Instant;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ElectricityStatusEmbedService {

    void postElectricityStatusEmbed(String guildId, String channelId);

    void sendElectricityInEstimateModal(ButtonInteractionEvent event);

    void addElectricityOutEntry(String messageId, Member member, Instant electricityInEstimate);

    void addElectricityInEntry(String messageId, Member member);

    void deleteElectricityStatusEmbed(String messageId);
}
