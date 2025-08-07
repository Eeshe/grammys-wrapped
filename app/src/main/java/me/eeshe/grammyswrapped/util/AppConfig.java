package me.eeshe.grammyswrapped.util;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
  private final Properties properties;

  public AppConfig() {
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

  public String getPostgreSQLHost() {
    return properties.getProperty("postgresql.host");
  }

  public String getPostgreSQLPort() {
    return properties.getProperty("postgresql.port");
  }

  public String getPostgreSQLDatabase() {
    return properties.getProperty("postgresql.database");
  }

  public String getPostgreSQLUsername() {
    return properties.getProperty("postgresql.username");
  }

  public String getPostgreSQLPassword() {
    return properties.getProperty("postgresql.password");
  }
}
