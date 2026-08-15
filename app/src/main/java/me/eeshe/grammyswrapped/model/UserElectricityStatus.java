package me.eeshe.grammyswrapped.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import me.eeshe.grammyswrapped.util.TimeUtil;

public class UserElectricityStatus {
    private final String userId;

    private String nickname;
    private Instant lastElectricityOutTime;
    private Instant lastElectricityInTime;
    private Instant lastReminderTime;
    private Instant electricityInEstimate;

    public UserElectricityStatus(String userId) {
        this.userId = userId;
    }

    public UserElectricityStatus(
            String userId,
            String nickname,
            Instant lastElectricityOutTime,
            Instant lastElectricityInTime,
            Instant lastReminderTime,
            Instant electricityInEstimate) {
        this.userId = userId;
        this.nickname = nickname;
        this.lastElectricityOutTime = lastElectricityOutTime;
        this.lastElectricityInTime = lastElectricityInTime;
        this.lastReminderTime = lastReminderTime;
        this.electricityInEstimate = electricityInEstimate;
    }

    public boolean hasElectricity() {
        if (lastElectricityOutTime == null) {
            return true;
        }
        if (lastElectricityInTime == null) {
            return false;
        }
        return lastElectricityInTime.isAfter(lastElectricityOutTime);
    }

    public boolean hasHadEletricityOutToday() {
        if (lastElectricityOutTime == null) {
            return false;
        }
        final LocalDate lastElectricityOutDate = lastElectricityOutTime
                .atZone(TimeUtil.getZoneId()).toLocalDate();

        return lastElectricityOutDate.equals(LocalDate.now());
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Instant getLastElectricityOutTime() {
        return lastElectricityOutTime;
    }

    public void setLastElectricityOutTime(Instant lastElectricityOutTime) {
        this.lastElectricityOutTime = lastElectricityOutTime;
    }

    public Instant getLastElectricityInTime() {
        return lastElectricityInTime;
    }

    public void setLastElectricityInTime(Instant lastElectricityInTime) {
        this.lastElectricityInTime = lastElectricityInTime;
    }

    public Duration calculateLastElectricityOutageDuration() {
        if (lastElectricityInTime == null || lastElectricityOutTime == null ||
                lastElectricityOutTime.isAfter(lastElectricityInTime)) {
            return Duration.ZERO;
        }
        return Duration.between(lastElectricityOutTime, lastElectricityInTime);
    }

    public Duration calculateTimeSinceLastElectricityOutage() {
        if (lastElectricityInTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(lastElectricityInTime, Instant.now());
    }

    public Instant getLastReminderTime() {
        return lastReminderTime;
    }

    public void setLastReminderTime(Instant lastReminderTime) {
        this.lastReminderTime = lastReminderTime;
    }

    public Instant getElectricityInEstimate() {
        return electricityInEstimate;
    }

    public void setElectricityInEstimate(Instant electricityInEstimate) {
        this.electricityInEstimate = electricityInEstimate;
    }
}
