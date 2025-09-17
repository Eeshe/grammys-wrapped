package me.eeshe.grammyswrapped.util;

import java.awt.Color;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtil {

  public static EmbedBuilder createEmbed(Color color, String title, String description) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setColor(color);
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(description);
    embedBuilder.setTimestamp(Instant.now());

    return embedBuilder;
  }

  public static EmbedBuilder createEmbed(String title, String description) {
    return createEmbed(Color.BLUE, title, description);
  }

  public static EmbedBuilder createSuccessEmbed(String title, String description) {
    return createEmbed(Color.GREEN, title, description);
  }

  public static EmbedBuilder createErrorEmbed(String title, String description) {
    return createEmbed(Color.RED, title, description);
  }
}
