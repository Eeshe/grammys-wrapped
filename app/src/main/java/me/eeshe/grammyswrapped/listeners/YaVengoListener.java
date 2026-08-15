package me.eeshe.grammyswrapped.listeners;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.model.YaVengoEmbed;
import me.eeshe.grammyswrapped.repository.YaVengoRepository;
import me.eeshe.grammyswrapped.util.TimeUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class YaVengoListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);
    private final ConcurrentHashMap<String, YaVengoEmbed> yaVengoEmbeds = new ConcurrentHashMap<>();

    private final YaVengoRepository yaVengoRepository;

    public YaVengoListener(YaVengoRepository yaVengoRepository) {
        this.yaVengoRepository = yaVengoRepository;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("ya_vengo_modal")) {
            return;
        }
        final User user = event.getUser();
        final User target = yaVengoRepository.get(user);
        if (target == null) {
            event.reply("ERROR").setEphemeral(true).queue();
            return;
        }
        final String timeInput = event.getValue("time").getAsString();
        final Long awayTime = TimeUtil.parseTime(timeInput);
        if (awayTime == null) {
            event.reply(LocalizedMessage.INVALID_TIME_FORMAT.get()).setEphemeral(true).queue();
            return;
        }
        final YaVengoEmbed yaVengoEmbed = new YaVengoEmbed(target, awayTime);

        event.deferReply().queue();
        event.getHook().sendMessageEmbeds(yaVengoEmbed.createRunningEmbed())
                .addComponents(yaVengoEmbed.createEmbedActionRow())
                .queue(message -> {
                    yaVengoEmbeds.put(message.getId(), yaVengoEmbed);
                    startEmbedUpdateTask(message, yaVengoEmbed);
                });
    }

    private void startEmbedUpdateTask(final Message embedMessage, final YaVengoEmbed yaVengoEmbed) {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (yaVengoEmbed.hasBeenEdited()) {
                    timer.cancel();
                    return;
                }
                if (!yaVengoEmbed.isPastArrivalTime()) {
                    return;
                }
                embedMessage.editMessageEmbeds(yaVengoEmbed.createRunningEmbed()).queue();
                timer.cancel();
            }
        }, 0L, 1000L);

        yaVengoEmbed.setUpdateTask(timer);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getCustomId().equals("stop_ya_vengo")) {
            return;
        }
        final String messageId = event.getMessageId();
        final YaVengoEmbed yaVengoEmbed = yaVengoEmbeds.remove(messageId);
        if (yaVengoEmbed == null) {
            event.deferEdit().queue();
            return;
        }
        yaVengoEmbed.getUpdateTask().cancel();
        event.editMessageEmbeds(yaVengoEmbed.createStoppedEmbed()).queue();
        event.getMessage().editMessageComponents().queue();
    }
}
