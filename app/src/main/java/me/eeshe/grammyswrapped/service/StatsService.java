package me.eeshe.grammyswrapped.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import me.eeshe.grammyswrapped.model.LoggableMessage;
import me.eeshe.grammyswrapped.model.LoggablePresence;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatConnection;
import me.eeshe.grammyswrapped.model.LoggableVoiceChatEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

public class StatsService {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

  private static final String MESSAGES_TABLE = "messages";
  private static final String VOICE_CHAT_CONNECTIONS_TABLE = "voice_chat_connections";
  private static final String VOICE_CHAT_EVENTS_TABLE = "voice_chat_events";
  private static final String PRESENCES_TABLE = "presences";

  private final PostgreSQLDatabase database;

  public StatsService() {
    this.database = PostgreSQLDatabase.getInstance();
  }

  public void createStatsTables() {
    createMessagesTable();
    createVoiceChatConnectionsTable();
    createVoiceChatEventsTable();
    createPresencesTable();
  }

  private void createMessagesTable() {
    createTable(
        "CREATE TABLE IF NOT EXISTS " + MESSAGES_TABLE + " (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "user_id VARCHAR(255) NOT NULL, " +
            "date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
            "channel_id VARCHAR(255) NOT NULL, " +
            "content TEXT, " +
            "attachments TEXT[]" +
            ")");
  }

  private void createVoiceChatConnectionsTable() {
    createTable(
        "CREATE TABLE IF NOT EXISTS " + VOICE_CHAT_CONNECTIONS_TABLE + " (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "user_id VARCHAR(255) NOT NULL, " +
            "date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
            "joined BOOL NOT NULL" +
            ")");
  }

  private void createVoiceChatEventsTable() {
    createTable(
        "CREATE TABLE IF NOT EXISTS " + VOICE_CHAT_EVENTS_TABLE + " (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "user_id VARCHAR(255) NOT NULL, " +
            "date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
            "muted BOOL NOT NULL, " +
            "deafened BOOL NOT NULL" +
            ")");
  }

  private void createPresencesTable() {
    createTable(
        "CREATE TABLE IF NOT EXISTS " + PRESENCES_TABLE + " (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "user_id VARCHAR(255) NOT NULL, " +
            "date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
            "starting BOOL NOT NULL, " +
            "type VARCHAR(255), " +
            "name TEXT, " +
            "state TEXT, " +
            "details TEXT, " +
            "large_image_text TEXT, " +
            "small_image_text TEXT" +
            ")");
  }

  private void createTable(String createTableSql) {
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void logMessage(Message message) {
    LoggableMessage loggableMessage = LoggableMessage.fromMessage(message);
    LOGGER.info("Logging message: {}", loggableMessage);

    String sql = "INSERT INTO " + MESSAGES_TABLE + " (user_id, channel_id, content, attachments)" +
        "VALUES (?, ?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, loggableMessage.userId());
      preparedStatement.setString(2, loggableMessage.channelId());
      preparedStatement.setString(3, loggableMessage.content());
      preparedStatement.setArray(4, connection.createArrayOf("text", loggableMessage.attachmentUrls().toArray()));

      preparedStatement.executeUpdate();
      LOGGER.info("Message logged.");
    } catch (SQLException e) {
      LOGGER.error("Failed to log voice message. Data: {}", loggableMessage, e);
    }
  }

  public void logVoiceChatConnection(User user, boolean joined) {
    LoggableVoiceChatConnection loggableVoiceChatConnection = new LoggableVoiceChatConnection(
        user.getId(),
        joined);
    LOGGER.info("Logging voice chat connection: {}", loggableVoiceChatConnection);

    String sql = "INSERT INTO " + VOICE_CHAT_CONNECTIONS_TABLE + " (user_id, joined) VALUES (?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setBoolean(2, joined);

      preparedStatement.executeUpdate();
      LOGGER.info("Voice chat connection logged.");
    } catch (SQLException e) {
      LOGGER.error("Failed to log voice chat connection. Data: {}", loggableVoiceChatConnection, e);
    }
  }

  public void logVoiceChatEvent(User user, boolean muted, boolean deafened) {
    LoggableVoiceChatEvent loggableVoiceChatEvent = new LoggableVoiceChatEvent(
        user.getId(),
        muted,
        deafened);
    LOGGER.info("Logging voice chat event: {}", loggableVoiceChatEvent);

    String sql = "INSERT INTO " + VOICE_CHAT_EVENTS_TABLE + " (user_id, muted, deafened) VALUES (?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setBoolean(2, muted);
      preparedStatement.setBoolean(3, deafened);

      preparedStatement.executeUpdate();
      LOGGER.info("Successfully logged voice chat event.");
    } catch (SQLException e) {
      LOGGER.error("Failed to log voice chat event. Data: {}", loggableVoiceChatEvent, e);
    }
  }

  public void logPresence(User user, boolean starting, RichPresence richPresence) {
    LoggablePresence loggablePresence = LoggablePresence.fromPresence(user, starting, richPresence);
    LOGGER.info("Logging presence: {}", loggablePresence);

    String sql = "INSERT INTO " + PRESENCES_TABLE +
        " (user_id, starting, type, name, state, details, large_image_text, small_image_text) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, loggablePresence.userId());
      preparedStatement.setBoolean(2, loggablePresence.starting());
      preparedStatement.setString(3, loggablePresence.type());
      preparedStatement.setString(4, loggablePresence.name());
      preparedStatement.setString(5, loggablePresence.state());
      preparedStatement.setString(6, loggablePresence.details());
      preparedStatement.setString(7, loggablePresence.largeImageText());
      preparedStatement.setString(8, loggablePresence.smallImageText());

      preparedStatement.executeUpdate();

      LOGGER.info("Successfully logged presence.");
    } catch (SQLException e) {
      LOGGER.error("Failed to log presence. Data: {}", loggablePresence, e);
    }
  }

  public List<LoggablePresence> fetchPresences(Date startingDate, Date endingDate) {
    LOGGER.info("Fetching presences entries from {} to {}.", startingDate, endingDate);

    List<LoggablePresence> presences = new ArrayList<>();
    String sql = "SELECT * FROM " + PRESENCES_TABLE + " WHERE date BETWEEN ? AND ?";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setDate(1, new java.sql.Date(startingDate.getTime()));
      preparedStatement.setDate(2, new java.sql.Date(endingDate.getTime()));

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          Date date = resultSet.getDate("date");
          String userId = resultSet.getString("user_id");
          boolean starting = resultSet.getBoolean("starting");
          String type = resultSet.getString("type");
          String name = resultSet.getString("name");
          String state = resultSet.getString("state");
          String details = resultSet.getString("details");
          String largeImageText = resultSet.getString("large_image_text");
          String smallImageText = resultSet.getString("small_image_text");

          presences.add(new LoggablePresence(
              date,
              userId,
              starting,
              type,
              name,
              state,
              details,
              largeImageText,
              smallImageText));
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to fetch presences. ", e);
    }
    return presences;
  }
}
