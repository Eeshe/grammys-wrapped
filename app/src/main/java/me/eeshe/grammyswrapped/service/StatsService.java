package me.eeshe.grammyswrapped.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.User;

public class StatsService {
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
            "details TEXT" +
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

  public void logMessage(User user, Message message) {
    String content = message.getContentRaw();
    List<String> attachmentUrls = new ArrayList<>();
    String channelId = message.getChannelId();
    for (Attachment attachment : message.getAttachments()) {
      attachmentUrls.add(attachment.getUrl());
    }

    String sql = "INSERT INTO " + MESSAGES_TABLE + " (user_id, channel_id, content, attachments)" +
        "VALUES (?, ?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setString(2, channelId);
      preparedStatement.setString(3, content);
      preparedStatement.setArray(4, connection.createArrayOf("text", attachmentUrls.toArray()));

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void logVoiceChatConnection(User user, boolean joined) {
    String sql = "INSERT INTO " + VOICE_CHAT_CONNECTIONS_TABLE + " (user_id, joined) VALUES (?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setBoolean(2, joined);

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void logVoiceChatEvent(User user, boolean muted, boolean deafened) {
    String sql = "INSERT INTO " + VOICE_CHAT_EVENTS_TABLE + " (user_id, muted, deafened) VALUES (?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setBoolean(2, muted);
      preparedStatement.setBoolean(3, deafened);

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void logPresence(User user, boolean starting, RichPresence richPresence) {
    String presenceType = null;
    if (richPresence.getType() != null) {
      presenceType = richPresence.getType().name();
    }

    String sql = "INSERT INTO " + PRESENCES_TABLE + " (user_id, starting, type, name, state, details) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, user.getId());
      preparedStatement.setBoolean(2, starting);

      preparedStatement.setString(3, presenceType);
      preparedStatement.setString(4, richPresence.getName());
      preparedStatement.setString(5, richPresence.getState());
      preparedStatement.setString(6, richPresence.getDetails());

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
