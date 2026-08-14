package me.eeshe.grammyswrapped.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import me.eeshe.grammyswrapped.model.UserElectricityStatus;
import me.eeshe.grammyswrapped.repository.UserElectricityStatusRepository;

public class UserElectricityStatusRepositoryImpl implements UserElectricityStatusRepository {
    private static final String TABLE_NAME = "user_electricity_status";
    private final Map<String, UserElectricityStatus> byId = new ConcurrentHashMap<>();

    private final PostgreSQLDatabase database;

    public UserElectricityStatusRepositoryImpl(PostgreSQLDatabase database) {
        this.database = database;
    }

    @Override
    public void onStart() {
        createTableUserElectricityStatusTable();
        fetchAllUserElectricityStatus();
    }

    private void createTableUserElectricityStatusTable() {
        final String createTableSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id VARCHAR(255) NOT NULL UNIQUE, " +
                "last_electricity_out_time TIMESTAMP, " +
                "last_electricity_in_time TIMESTAMP, " +
                "last_reminder_time TIMESTAMP, " +
                "electricity_in_estimate long NOT NULL" +
                ")";
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchAllUserElectricityStatus() {
        final String sql = "SELECT * FROM " + TABLE_NAME;
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final ResultSet queryResult = preparedStatement.executeQuery();
            while (queryResult.next()) {
                final UserElectricityStatus userElectricityStatus = parseUserElectricityStatus(queryResult);
                if (userElectricityStatus == null) {
                    continue;
                }
                byId.put(userElectricityStatus.getUserId(), userElectricityStatus);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private UserElectricityStatus parseUserElectricityStatus(final ResultSet queryResult) {
        try {
            final String userId = queryResult.getString("user_id");
            final Instant lastElectricityOutTime = queryResult.getTimestamp("last_electricity_out_time") != null
                    ? queryResult.getTimestamp("last_electricity_out_time").toInstant()
                    : null;
            final Instant lastElectricityInTime = queryResult.getTimestamp("last_electricity_in_time") != null
                    ? queryResult.getTimestamp("last_electricity_in_time").toInstant()
                    : null;
            final Instant lastReminderTime = queryResult.getTimestamp("last_reminder_time") != null
                    ? queryResult.getTimestamp("last_reminder_time").toInstant()
                    : null;
            final long electricityInEstimateMs = queryResult.getLong("electricity_in_estimate");
            final Duration electricityInEstimate = Duration.ofMillis(electricityInEstimateMs);

            return new UserElectricityStatus(
                    userId,
                    lastElectricityOutTime,
                    lastElectricityInTime,
                    lastReminderTime,
                    electricityInEstimate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStop() {
        byId.clear();
    }

    @Override
    public void save(UserElectricityStatus userElectricityStatus) {
        byId.put(userElectricityStatus.getUserId(), userElectricityStatus);

        final String upsertSql = "INSERT INTO " + TABLE_NAME + " (" +
                "user_id, " +
                "last_electricity_out_time, " +
                "last_electricity_in_time, " +
                "last_reminder_time, " +
                "electricity_in_estimate) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET " +
                "last_electricity_out_time = EXCLUDED.last_electricity_out_time, " +
                "last_electricity_in_time = EXCLUDED.last_electricity_in_time, " +
                "last_reminder_time = EXCLUDED.last_reminder_time, " +
                "electricity_in_estimate = EXCLUDED.electricity_in_estimate";
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
            final Instant lastElectricityOutTime = userElectricityStatus.getLastElectricityOutTime();
            final Instant lastElectricityInTime = userElectricityStatus.getLastElectricityInTime();
            final Instant lastReminderTime = userElectricityStatus.getLastReminderTime();

            preparedStatement.setString(1, userElectricityStatus.getUserId());
            preparedStatement.setTimestamp(2,
                    lastElectricityOutTime != null ? Timestamp.from(lastElectricityOutTime) : null);
            preparedStatement.setTimestamp(3,
                    lastElectricityInTime != null ? Timestamp.from(lastElectricityInTime) : null);
            preparedStatement.setTimestamp(4, lastReminderTime != null ? Timestamp.from(lastReminderTime) : null);
            preparedStatement.setLong(5, userElectricityStatus.getElectricityInEstimate().toMillis());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserElectricityStatus getById(String userId) {
        return byId.get(userId);
    }
}
