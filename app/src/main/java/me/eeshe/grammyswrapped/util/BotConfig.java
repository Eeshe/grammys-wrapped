package me.eeshe.grammyswrapped.util;

import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
  private final Properties properties;

  public BotConfig() {
    this.properties = new Properties();
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
      if (inputStream == null) {
        System.out.println("Unable to find config.properties file.");
        return;
      }
      properties.load(inputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getBotToken() {
    return properties.getProperty("discord.bot.token");
  }
}
