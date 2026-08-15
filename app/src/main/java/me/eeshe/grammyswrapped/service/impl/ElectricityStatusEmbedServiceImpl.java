package me.eeshe.grammyswrapped.service.impl;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;
import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.model.UserElectricityStatus;
import me.eeshe.grammyswrapped.repository.ElectricityStatusEmbedRepository;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.util.EmbedUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

public class ElectricityStatusEmbedServiceImpl implements ElectricityStatusEmbedService {
    private final JDA bot;
    private final ElectricityStatusEmbedRepository electricityStatusEmbedRepository;

    public ElectricityStatusEmbedServiceImpl(
            JDA bot,
            ElectricityStatusEmbedRepository electricityStatusEmbedRepository) {
        this.bot = bot;
        this.electricityStatusEmbedRepository = electricityStatusEmbedRepository;
    }

    @Override
    public void postElectricityStatusEmbed(String guildId, String channelId) {
        final TextChannel textChannel = bot.getTextChannelById(channelId);
        if (textChannel == null) {
            return;
        }
        textChannel.sendMessageEmbeds(createElectricityStatusEmbed())
                .addComponents(createElectrictyStatusButtons())
                .queue(message -> {
                    final ElectricityStatusEmbed electricityStatusEmbed = new ElectricityStatusEmbed(
                            guildId,
                            channelId,
                            message.getId());

                    electricityStatusEmbedRepository.save(electricityStatusEmbed);
                });
    }

    private ActionRow createElectrictyStatusButtons() {
        return ActionRow.of(
                Button.danger(
                        "electricity_out",
                        LocalizedMessage.ELECTRICITY_STATUS_EMBED_ELECTRICITY_OUT_BUTTON.get()),
                Button.primary(
                        "electricity_in",
                        LocalizedMessage.ELECTRICITY_STATUS_EMBED_ELECTRICITY_IN_BUTTON.get()));
    }

    @Override
    public void sendElectricityInEstimateModal(ButtonInteractionEvent event) {
        event.replyModal(createElectricityInEstimateModal()).queue();
    }

    private Modal createElectricityInEstimateModal() {
        return Modal.create("electricity_in_estimate",
                LocalizedMessage.ELECTRICITY_IN_MODAL_TITLE.get())
                .addComponents(
                        Label.of(LocalizedMessage.ELECTRICITY_IN_MODAL_QUESTION.get(),
                                TextInput.create("time", TextInputStyle.SHORT)
                                        .setPlaceholder(
                                                LocalizedMessage.ELECTRICITY_IN_MODAL_QUESTION_PLACEHOLDER.get())
                                        .setRequired(true).build()))
                .build();
    }

    @Override
    public void addElectricityOutEntry(String messageId, User user, Duration electricityInEstimate) {
        final ElectricityStatusEmbed electricityStatusEmbed = electricityStatusEmbedRepository
                .getByMessageId(messageId);
        if (electricityStatusEmbed == null) {
            return;
        }
        final UserElectricityStatus userElectricityStatus = electricityStatusEmbed.getParticipant(user.getId());

        userElectricityStatus.setLastElectricityOutTime(Instant.now());
        userElectricityStatus.setElectricityInEstimate(electricityInEstimate);
        userElectricityStatus.setLastReminderTime(Instant.now());

        electricityStatusEmbed.addParticipant(userElectricityStatus);

        electricityStatusEmbedRepository.save(electricityStatusEmbed);
        updateElectricityStatusEmbed(electricityStatusEmbed);
    }

    @Override
    public void addElectricityInEntry(String messageId, User user) {
        final ElectricityStatusEmbed electricityStatusEmbed = electricityStatusEmbedRepository
                .getByMessageId(messageId);
        if (electricityStatusEmbed == null) {
            return;
        }
        final UserElectricityStatus userElectricityStatus = electricityStatusEmbed.getParticipant(user.getId());

        userElectricityStatus.setLastElectricityInTime(Instant.now());

        electricityStatusEmbed.addParticipant(userElectricityStatus);

        electricityStatusEmbedRepository.save(electricityStatusEmbed);
        updateElectricityStatusEmbed(electricityStatusEmbed);
    }

    private void updateElectricityStatusEmbed(ElectricityStatusEmbed electricityStatusEmbed) {
        final TextChannel textChannel = bot.getTextChannelById(electricityStatusEmbed.getChannelId());
        textChannel.retrieveMessageById(electricityStatusEmbed.getMessageId()).queue(message -> {
            message.editMessageEmbeds(createElectricityStatusEmbed()).queue();
        });
    }

    private MessageEmbed createElectricityStatusEmbed() {
        return EmbedUtil.createEmbed(
                Color.YELLOW,
                LocalizedMessage.ELECTRICITY_STATUS_EMBED_TITLE.get(),
                LocalizedMessage.ELECTRICITY_STATUS_EMBED_EMPTY_DESCRIPTION.get())
                .build();
    }

    @Override
    public void deleteElectricityStatusEmbed(String messageId) {
        if (electricityStatusEmbedRepository.getByMessageId(messageId) == null) {
            return;
        }
        electricityStatusEmbedRepository.delete(messageId);
    }
}
