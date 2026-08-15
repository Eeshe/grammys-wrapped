package me.eeshe.grammyswrapped.listeners;

import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.util.TimeUtil;
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
        if (buttonId.equals("electricity_in")) {
            electricityStatusService.addElectricityInEntry(messageId, event.getUser());
        } else if (buttonId.equals("electricity_out")) {
            electricityStatusService.sendElectricityInEstimateModal(event);
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("electricity_in_estimate")) {
            return;
        }
        final String timeInput = event.getValue("time").getAsString();
        final Long awayTime = TimeUtil.parseTime(timeInput);
        if (awayTime == null) {
            event.reply(LocalizedMessage.INVALID_TIME_FORMAT.get()).setEphemeral(true).queue();
            return;
        }
    }
}
