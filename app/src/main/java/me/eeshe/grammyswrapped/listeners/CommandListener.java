package me.eeshe.grammyswrapped.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.commands.WrappedCommand;
import me.eeshe.grammyswrapped.commands.YaVengoCommand;
import me.eeshe.grammyswrapped.commands.electricitystatus.PostElectricityStatusCommand;
import me.eeshe.grammyswrapped.repository.YaVengoRepository;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.service.StatsService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);
    private final WrappedCommand wrappedCommand;
    private final YaVengoCommand yaVengoCommand;
    private final PostElectricityStatusCommand postElectricityStatusCommand;

    public CommandListener(
            JDA bot,
            StatsService statsService,
            YaVengoRepository yaVengoRepository,
            ElectricityStatusEmbedService electricityStatusEmbedService) {
        this.wrappedCommand = new WrappedCommand(bot, statsService);
        this.yaVengoCommand = new YaVengoCommand(yaVengoRepository);
        this.postElectricityStatusCommand = new PostElectricityStatusCommand(electricityStatusEmbedService);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        LOGGER.info("{} executed command {} with arguments {}.",
                event.getUser().getAsTag(),
                commandName,
                event.getOptions().isEmpty() ? "" : event.getOptions().toString());

        switch (commandName) {
            case "wrapped" -> wrappedCommand.handle(event);
            case "yavengo" -> yaVengoCommand.handle(event);
            case "postelectricitystatusembed" -> postElectricityStatusCommand.handle(event);
        }
    }
}
