package me.eeshe.grammyswrapped.service.userdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LoggableElectricityStatusChange;
import me.eeshe.grammyswrapped.model.userdata.UserData;
import me.eeshe.grammyswrapped.model.userdata.UserElectricityData;
import me.eeshe.grammyswrapped.service.ElectricityData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class UserElectricityDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserVoiceChatDataService.class);

    private final JDA bot;

    public UserElectricityDataService(JDA bot) {
        this.bot = bot;
    }

    /**
     * Computes the passed LoggableElectricityStatusChanges
     *
     * @param loggableElectricityStatusChanges LoggableVoiceChatConnections to
     *                                         compute.
     * @return Computed UserElectricityData.
     */
    public ElectricityData computeUserElectricityData(
            List<LoggableElectricityStatusChange> loggableElectricityStatusChanges) {
        LOGGER.info("Computing UserVoiceChatData from {} entries...",
                loggableElectricityStatusChanges.size());
        ElectricityData electricityData = new ElectricityData();

        computePowerOutages(electricityData, loggableElectricityStatusChanges);

        return new ElectricityData(
                (Map<String, UserElectricityData>) UserData.sortByUsername(electricityData.getUserElectricityData()));
    }

    /**
     * Computes the passed VoiceChatConnections and adds them to the passed
     * UserVoiceChatData Map.
     *
     * @param electricityData                 VoiceChatData to modify.
     * @param loggableElectricityStatusChange VoiceChatConnections to compute.
     */
    private void computePowerOutages(
            ElectricityData electricityData,
            List<LoggableElectricityStatusChange> loggableElectricityStatusChange) {
        Map<String, UserElectricityData> userElectricityDataMap = electricityData.getUserElectricityData();
        Map<String, LoggableElectricityStatusChange> previousElectricityStatusChanges = new HashMap<>();
        for (LoggableElectricityStatusChange electricityStatusChange : loggableElectricityStatusChange) {
            String userId = electricityStatusChange.userId();
            User user = bot.getUserById(userId);
            if (user == null) {
                LOGGER.error("User '{}' not found.", userId);
                continue;
            }
            boolean electricityIn = electricityStatusChange.electricityIn();

            UserElectricityData userElectricityData = userElectricityDataMap.getOrDefault(userId,
                    new UserElectricityData(user));
            if (electricityIn) {
                LoggableElectricityStatusChange previousElectricityStatusChange = previousElectricityStatusChanges
                        .remove(userId);
                if (previousElectricityStatusChange != null && !previousElectricityStatusChange.electricityIn()) {
                    // Previous electricity status change was in and current one is out, add voice
                    // chat time
                    userElectricityData.increasePowerOutages();
                    userElectricityData.addPowerOutageTime(
                            previousElectricityStatusChange.date(),
                            electricityStatusChange.date());
                }
            }
            userElectricityDataMap.put(userId, userElectricityData);
            previousElectricityStatusChanges.put(userId, electricityStatusChange);
        }
    }
}
