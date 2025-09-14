package me.eeshe.grammyswrapped.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.commands.WrappedCommand;
import me.eeshe.grammyswrapped.service.StatsService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);
  private final WrappedCommand wrappedCommand;

  public CommandListener(JDA bot, StatsService statsService) {
    this.wrappedCommand = new WrappedCommand(bot, statsService);
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    String commandName = event.getName();
    LOGGER.info("{} executed command {} with arguments {}.",
        event.getUser().getAsTag(),
        commandName,
        event.getOptions().isEmpty() ? "" : event.getOptions().toString());

    switch (commandName) {
      case "wrapped" -> {
        wrappedCommand.handle(event);
      }
    }
  }
}
