package me.eeshe.grammyswrapped.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;
import me.eeshe.grammyswrapped.model.UserElectricityStatus;
import me.eeshe.grammyswrapped.repository.ElectricityStatusEmbedRepository;

public class ElectricityStatusEmbedRepositoryImpl implements ElectricityStatusEmbedRepository {
    private static final String CREATE_EMBEDS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS electricity_status_embeds (
                message_id VARCHAR(255) PRIMARY KEY,
                guild_id VARCHAR(255) NOT NULL,
                channel_id VARCHAR(255) NOT NULL,
                updated_at TIMESTAMPTZ NOT NULL
            )""";

    private static final String CREATE_PARTICIPANTS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS user_electricity_status (
                message_id VARCHAR(255) NOT NULL
                    REFERENCES electricity_status_embeds(message_id) ON DELETE CASCADE,
                user_id VARCHAR(255) NOT NULL,
                nickname VARCHAR(255),
                last_electricity_out_time TIMESTAMPTZ,
                last_electricity_in_time TIMESTAMPTZ,
                last_reminder_time TIMESTAMPTZ,
                electricity_in_estimate TIMESTAMPTZ,
                PRIMARY KEY (message_id, user_id)
            )""";

    private static final String SELECT_ALL_EMBEDS_SQL = """
            SELECT e.message_id, e.guild_id, e.channel_id, e.updated_at,
                   u.user_id, u.nickname, u.last_electricity_out_time, u.last_electricity_in_time,
                   u.last_reminder_time, u.electricity_in_estimate
            FROM electricity_status_embeds e
            LEFT JOIN user_electricity_status u ON u.message_id = e.message_id
            ORDER BY e.message_id
            """;

    private static final String SELECT_EMBED_BY_MESSAGE_ID_SQL = """
            SELECT e.message_id, e.guild_id, e.channel_id, e.updated_at,
                   u.user_id, u.nickname, u.last_electricity_out_time, u.last_electricity_in_time,
                   u.last_reminder_time, u.electricity_in_estimate
            FROM electricity_status_embeds e
            LEFT JOIN user_electricity_status u ON u.message_id = e.message_id
            WHERE e.message_id = ?
            """;

    private static final String UPSERT_EMBED_SQL = """
            INSERT INTO electricity_status_embeds (message_id, guild_id, channel_id, updated_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (message_id) DO UPDATE SET
                guild_id = EXCLUDED.guild_id,
                channel_id = EXCLUDED.channel_id,
                updated_at = EXCLUDED.updated_at
            """;

    private static final String DELETE_PARTICIPANTS_SQL = "DELETE FROM user_electricity_status WHERE message_id = ?";

    private static final String INSERT_PARTICIPANT_SQL = """
            INSERT INTO user_electricity_status (
                message_id, user_id, nickname, last_electricity_out_time, last_electricity_in_time,
                last_reminder_time, electricity_in_estimate)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_EMBED_SQL = "DELETE FROM electricity_status_embeds WHERE message_id = ?";

    private final PostgreSQLDatabase database;

    public ElectricityStatusEmbedRepositoryImpl(PostgreSQLDatabase database) {
        this.database = database;
    }

    @Override
    public void onStart() {
        try (Connection connection = database.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_EMBEDS_TABLE_SQL);
            statement.executeUpdate(CREATE_PARTICIPANTS_TABLE_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public void save(ElectricityStatusEmbed electricityStatusEmbed) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                upsertEmbed(connection, electricityStatusEmbed);
                deleteParticipants(connection, electricityStatusEmbed.getMessageId());
                insertParticipants(connection, electricityStatusEmbed);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void upsertEmbed(Connection connection, ElectricityStatusEmbed electricityStatusEmbed)
            throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPSERT_EMBED_SQL)) {
            preparedStatement.setString(1, electricityStatusEmbed.getMessageId());
            preparedStatement.setString(2, electricityStatusEmbed.getGuildId());
            preparedStatement.setString(3, electricityStatusEmbed.getChannelId());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(electricityStatusEmbed.getUpdatedAt()));
            preparedStatement.executeUpdate();
        }
    }

    private void deleteParticipants(Connection connection, String messageId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PARTICIPANTS_SQL)) {
            preparedStatement.setString(1, messageId);
            preparedStatement.executeUpdate();
        }
    }

    private void insertParticipants(Connection connection, ElectricityStatusEmbed electricityStatusEmbed)
            throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PARTICIPANT_SQL)) {
            for (UserElectricityStatus participant : electricityStatusEmbed.getParticipants().values()) {
                bindParticipant(preparedStatement, electricityStatusEmbed.getMessageId(), participant);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void bindParticipant(PreparedStatement preparedStatement, String messageId,
            UserElectricityStatus participant) throws SQLException {
        preparedStatement.setString(1, messageId);
        preparedStatement.setString(2, participant.getUserId());
        preparedStatement.setString(3, participant.getNickname());
        preparedStatement.setTimestamp(4, toTimestamp(participant.getLastElectricityOutTime()));
        preparedStatement.setTimestamp(5, toTimestamp(participant.getLastElectricityInTime()));
        preparedStatement.setTimestamp(6, toTimestamp(participant.getLastReminderTime()));
        preparedStatement.setObject(7, toTimestamp(participant.getElectricityInEstimate()));
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    @Override
    public List<ElectricityStatusEmbed> findAll() {
        final List<ElectricityStatusEmbed> electricityStatusEmbeds = new ArrayList<>();
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_EMBEDS_SQL)) {
            electricityStatusEmbeds.addAll(parseEmbeds(preparedStatement.executeQuery()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return electricityStatusEmbeds;
    }

    @Override
    public ElectricityStatusEmbed getByMessageId(String messageId) {
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        SELECT_EMBED_BY_MESSAGE_ID_SQL)) {
            preparedStatement.setString(1, messageId);
            final List<ElectricityStatusEmbed> electricityStatusEmbeds = parseEmbeds(
                    preparedStatement.executeQuery());
            return electricityStatusEmbeds.isEmpty() ? null : electricityStatusEmbeds.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ElectricityStatusEmbed> parseEmbeds(ResultSet resultSet) throws SQLException {
        final Map<String, ElectricityStatusEmbed> embedsByMessageId = new LinkedHashMap<>();
        while (resultSet.next()) {
            final String messageId = resultSet.getString("message_id");
            ElectricityStatusEmbed electricityStatusEmbed = embedsByMessageId.get(messageId);
            if (electricityStatusEmbed == null) {
                electricityStatusEmbed = new ElectricityStatusEmbed(
                        resultSet.getString("guild_id"),
                        resultSet.getString("channel_id"),
                        messageId,
                        new ConcurrentHashMap<>(),
                        resultSet.getTimestamp("updated_at").toLocalDateTime());
                embedsByMessageId.put(messageId, electricityStatusEmbed);
            }
            final String userId = resultSet.getString("user_id");
            if (userId != null) {
                electricityStatusEmbed.getParticipants().put(userId, parseParticipant(resultSet));
            }
        }
        return new ArrayList<>(embedsByMessageId.values());
    }

    private UserElectricityStatus parseParticipant(ResultSet resultSet) throws SQLException {
        return new UserElectricityStatus(
                resultSet.getString("user_id"),
                resultSet.getString("nickname"),
                toInstant(resultSet, "last_electricity_out_time"),
                toInstant(resultSet, "last_electricity_in_time"),
                toInstant(resultSet, "last_reminder_time"),
                toInstant(resultSet, "electricity_in_estimate"));
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        final Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp != null ? timestamp.toInstant() : null;
    }

    @Override
    public void delete(String messageId) {
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DELETE_EMBED_SQL)) {
            preparedStatement.setString(1, messageId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
