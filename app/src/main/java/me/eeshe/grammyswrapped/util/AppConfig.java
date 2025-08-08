package me.eeshe.grammyswrapped.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
  private final Properties properties;

  public AppConfig() {
    this.properties = new Properties();

    File propertiesFile = new File("config.properties");
    if (!propertiesFile.exists()) {
      System.out.println("UNABLE TO FIND CONFIG.PROPERTIES FILE.");
      return;
    }
    try (InputStream inputStream = new FileInputStream(propertiesFile)) {
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
