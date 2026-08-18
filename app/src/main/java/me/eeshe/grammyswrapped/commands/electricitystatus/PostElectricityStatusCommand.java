package me.eeshe.grammyswrapped.commands.electricitystatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.commands.WrappedCommand;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PostElectricityStatusCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(WrappedCommand.class);

    private final ElectricityStatusEmbedService electricityStatusService;

    public PostElectricityStatusCommand(ElectricityStatusEmbedService electricityStatusEmbedService) {
        this.electricityStatusService = electricityStatusEmbedService;
    }

    public void handle(SlashCommandInteractionEvent event) {
        final String guildId = event.getGuild().getId();
        final String channelId = event.getChannelId();

        electricityStatusService.postElectricityStatusEmbed(guildId, channelId);
        event.deferReply(true).queue();
        event.getHook().deleteOriginal().queue();
    }
}
