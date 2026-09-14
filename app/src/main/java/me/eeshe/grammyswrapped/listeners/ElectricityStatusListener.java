package me.eeshe.grammyswrapped.listeners;

import java.time.Duration;
import java.time.Instant;

import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.util.TimeUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ElectricityStatusListener extends ListenerAdapter {
    private final ElectricityStatusEmbedService electricityStatusService;

    public ElectricityStatusListener(
            ElectricityStatusEmbedService electricityStatusService) {
        this.electricityStatusService = electricityStatusService;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        final String messageId = event.getMessageId();

        electricityStatusService.deleteElectricityStatusEmbed(messageId);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final String messageId = event.getMessageId();
        final String buttonId = event.getButton().getCustomId();
        final Member member = event.getMember();
        if (buttonId.equals("electricity_in")) {
            if (electricityStatusService.hasElectricity(messageId, member)) {
                event.reply(LocalizedMessage.ELECTRICITY_STATUS_ALREADY_MARKED_ELECTRICITY_IN.get()).setEphemeral(true)
                        .queue();
                return;
            }
            electricityStatusService.addElectricityInEntry(messageId, event.getMember());
            event.deferEdit().queue();
        } else if (buttonId.equals("electricity_out")) {
            if (!electricityStatusService.hasElectricity(messageId, member)) {
                event.reply(LocalizedMessage.ELECTRICITY_STATUS_ALREADY_MARKED_ELECTRICITY_OUT.get()).setEphemeral(true)
                        .queue();
                return;
            }
            electricityStatusService.sendElectricityInEstimateModal(event);
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("electricity_in_estimate")) {
            return;
        }
        String timeInput = event.getValue("time").getAsOptionalString();
        if (timeInput == null) {
            timeInput = "6h";
        }
        final Long electricityInEstimateMillis = TimeUtil.parseTime(timeInput);
        if (electricityInEstimateMillis == null) {
            event.reply(LocalizedMessage.INVALID_TIME_FORMAT.get()).setEphemeral(true).queue();
            return;
        }
        final Instant electricityInEstimate = Instant.now().plus(Duration.ofMillis(electricityInEstimateMillis));

        event.deferEdit().queue();
        electricityStatusService.addElectricityOutEntry(
                event.getMessage().getId(),
                event.getMember(),
                electricityInEstimate);
    }
}
