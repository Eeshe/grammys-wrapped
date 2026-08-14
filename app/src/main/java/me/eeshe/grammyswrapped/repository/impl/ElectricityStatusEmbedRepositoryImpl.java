package me.eeshe.grammyswrapped.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;
import me.eeshe.grammyswrapped.model.UserElectricityStatus;
import me.eeshe.grammyswrapped.repository.ElectricityStatusEmbedRepository;
import me.eeshe.grammyswrapped.repository.UserElectricityStatusRepository;

public class ElectricityStatusEmbedRepositoryImpl implements ElectricityStatusEmbedRepository {
    private static final String TABLE_NAME = "electricity_status_embeds";
    private final Map<String, ElectricityStatusEmbed> byMessageId = new ConcurrentHashMap<>();

    private final PostgreSQLDatabase database;
    private final UserElectricityStatusRepository userElectricityStatusRepository;

    public ElectricityStatusEmbedRepositoryImpl(PostgreSQLDatabase database,
            UserElectricityStatusRepository userElectricityStatusRepository) {
        this.database = database;
        this.userElectricityStatusRepository = userElectricityStatusRepository;
    }

    @Override
    public void onStart() {
        createTableElectricityStatusEmbedTable();
        fetchAllElectricityStatusEmbeds();
    }

    private void createTableElectricityStatusEmbedTable() {
        final String createTableSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "message_id VARCHAR(255) PRIMARY KEY, " +
                "guild_id VARCHAR(255) NOT NULL, " +
                "channel_id VARCHAR(255) NOT NULL, " +
                "participant_ids TEXT[] NOT NULL, " +
                "updated_at TIMESTAMP NOT NULL" +
                ")";
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchAllElectricityStatusEmbeds() {
        final String sql = "SELECT * FROM " + TABLE_NAME;
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final ResultSet queryResult = preparedStatement.executeQuery();
            while (queryResult.next()) {
                final ElectricityStatusEmbed electricityStatusEmbed = parseElectricityStatusEmbed(queryResult);
                if (electricityStatusEmbed == null) {
                    continue;
                }
                byMessageId.put(electricityStatusEmbed.getMessageId(), electricityStatusEmbed);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ElectricityStatusEmbed parseElectricityStatusEmbed(final ResultSet queryResult) {
        try {
            final String messageId = queryResult.getString("message_id");
            final String guildId = queryResult.getString("guild_id");
            final String channelId = queryResult.getString("channel_id");
            final String[] participantIds = (String[]) queryResult.getArray("participant_ids").getArray();
            final LocalDateTime updatedAt = queryResult.getTimestamp("updated_at").toLocalDateTime();

            final Map<String, UserElectricityStatus> participants = new ConcurrentHashMap<>();
            for (String participantId : participantIds) {
                final UserElectricityStatus participant = userElectricityStatusRepository.getById(participantId);
                if (participant == null) {
                    continue;
                }
                participants.put(participantId, participant);
            }
            final ElectricityStatusEmbed electricityStatusEmbed = new ElectricityStatusEmbed(
                    guildId,
                    channelId,
                    messageId,
                    participants,
                    updatedAt);

            return electricityStatusEmbed;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStop() {
        byMessageId.clear();
    }

    @Override
    public ElectricityStatusEmbed getByMessageId(String messageId) {
        return byMessageId.get(messageId);
    }

    @Override
    public void save(ElectricityStatusEmbed electricityStatusEmbed) {
        byMessageId.put(electricityStatusEmbed.getMessageId(), electricityStatusEmbed);

        final String upsertSql = "INSERT INTO " + TABLE_NAME + " (" +
                "message_id, " +
                "guild_id, " +
                "channel_id, " +
                "participant_ids, " +
                "updated_at) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (message_id) DO UPDATE SET " +
                "participant_ids = EXCLUDED.participant_ids, " +
                "updated_at = EXCLUDED.updated_at";
        try (Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
            final String[] participantIds = electricityStatusEmbed.getParticipants().keySet()
                    .toArray(new String[0]);

            preparedStatement.setString(1, electricityStatusEmbed.getMessageId());
            preparedStatement.setString(2, electricityStatusEmbed.getGuildId());
            preparedStatement.setString(3, electricityStatusEmbed.getChannelId());
            preparedStatement.setArray(4, connection.createArrayOf("text", participantIds));
            preparedStatement.setTimestamp(5,
                    Timestamp.valueOf(electricityStatusEmbed.getUpdatedAt()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
