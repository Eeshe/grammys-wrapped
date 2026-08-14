package me.eeshe.grammyswrapped.model;

import java.time.Duration;
import java.time.Instant;

import net.dv8tion.jda.api.entities.User;

public class UserElectricityStatus {
    private final String userId;

    private Instant lastElectricityOutTime;
    private Instant lastElectricityInTime;
    private Instant lastReminderTime;
    private Duration electricityInEstimate;

    public UserElectricityStatus(User user) {
        this.userId = user.getId();
    }

    public UserElectricityStatus(String userId, Instant lastElectricityOutTime,
            Instant lastElectricityInTime, Instant lastReminderTime,
            Duration electricityInEstimate) {
        this.userId = userId;
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

    public String getUserId() {
        return userId;
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

    public Instant getLastReminderTime() {
        return lastReminderTime;
    }

    public void setLastReminderTime(Instant lastReminderTime) {
        this.lastReminderTime = lastReminderTime;
    }

    public Duration getElectricityInEstimate() {
        return electricityInEstimate;
    }

    public void setElectricityInEstimate(Duration electricityInEstimate) {
        this.electricityInEstimate = electricityInEstimate;
    }
}
