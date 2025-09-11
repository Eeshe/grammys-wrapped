package me.eeshe.grammyswrapped.commands;

import me.eeshe.grammyswrapped.service.StatsService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class WrappedCommand {
  private final StatsService statsService;

  public WrappedCommand(StatsService statsService) {
    this.statsService = statsService;
  }

  /**
   * Handles the interaction with the /wrapped <StartingDate> <EndingDate>
   * command.
   *
   * @param event SlashCommandInteractionEvent.
   */
  public void handle(SlashCommandInteractionEvent event) {
    event.deferReply(true).queue();

    event.getHook().editOriginalFormat("TODO: Return stats").queue();
  }
}
