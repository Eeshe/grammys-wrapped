package me.eeshe.grammyswrapped.model;

import java.util.Date;

import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.RichPresence.Image;
import net.dv8tion.jda.api.entities.User;

public record LoggablePresence(
    Date date,
    String userId,
    boolean starting,
    String type,
    String name,
    String state,
    String details,
    String largeImageText,
    String smallImageText) {

  public static LoggablePresence fromPresence(
      User user,
      boolean starting,
      RichPresence richPresence) {
    return new LoggablePresence(
        new Date(),
        user.getId(),
        starting,
        richPresence.getType() != null ? richPresence.getType().name() : null,
        richPresence.getName(),
        richPresence.getState(),
        richPresence.getDetails(),
        getImageText(richPresence.getLargeImage()),
        getImageText(richPresence.getSmallImage()));
  }

  private static String getImageText(Image image) {
    if (image == null) {
      return null;
    }
    return image.getText();
  }
}
