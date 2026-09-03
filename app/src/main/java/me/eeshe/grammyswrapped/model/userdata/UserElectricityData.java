package me.eeshe.grammyswrapped.model.userdata;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import me.eeshe.grammyswrapped.util.TimeUtil;
import net.dv8tion.jda.api.entities.User;

public class UserElectricityData extends UserData {
    private final Map<LocalDate, Duration> dailyPowerOutageTime;

    private int powerOutages;
    private long powerOutageTimeMillis;
    private long longestPowerOutageDurationMillis;
    private long shortestPowerOutageDurationMillis;

    public UserElectricityData(User user) {
        super(user);

        this.dailyPowerOutageTime = new TreeMap<>();
        this.powerOutages = 0;
        this.powerOutageTimeMillis = 0;
        this.longestPowerOutageDurationMillis = 0;
        this.shortestPowerOutageDurationMillis = Long.MAX_VALUE;
    }

    public long calculateAveragePowerOutageDurationMillis() {
        return powerOutageTimeMillis / powerOutages;
    }

    public int getPowerOutages() {
        return powerOutages;
    }

    public void increasePowerOutages() {
        this.powerOutages += 1;
    }

    public long getPowerOutageTimeMillis() {
        return powerOutageTimeMillis;
    }

    public void addPowerOutageTime(Date electricityOutDate, Date electricityInDate) {
        final long powerOutageDurationMillis = electricityInDate.getTime() - electricityOutDate.getTime();
        this.powerOutageTimeMillis += powerOutageDurationMillis;

        this.longestPowerOutageDurationMillis = Math.max(
                this.longestPowerOutageDurationMillis,
                powerOutageDurationMillis);
        this.shortestPowerOutageDurationMillis = Math.min(
                this.shortestPowerOutageDurationMillis,
                powerOutageDurationMillis);

        TimeUtil.computeDailyDuration(
                dailyPowerOutageTime,
                electricityOutDate,
                electricityInDate);
    }

    public long getLongestPowerOutageDurationMillis() {
        return longestPowerOutageDurationMillis;
    }

    public long getShortestPowerOutageDurationMillis() {
        return shortestPowerOutageDurationMillis;
    }

    public Map<LocalDate, Duration> getDailyPowerOutageTime() {
        return dailyPowerOutageTime;
    }
}
