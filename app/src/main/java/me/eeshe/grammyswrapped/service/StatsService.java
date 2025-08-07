package me.eeshe.grammyswrapped.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;

public class StatsService {

  public void createStatsTables() {
    createMessagesTable();
  }

  private void createMessagesTable() {
    String sql = "CREATE TABLE IF NOT EXISTS messages (" +
        "id BIGSERIAL PRIMARY KEY, " +
        "user_id VARCHAR(255) NOT NULL, " +
        "date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
        "message TEXT" +
        ")";
    try (Connection connection = PostgreSQLDatabase.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
