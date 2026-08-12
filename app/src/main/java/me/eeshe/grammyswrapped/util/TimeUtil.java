package me.eeshe.grammyswrapped.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final String TIME_ZONE_ID = "America/Caracas";

    /**
     * Converts a time in milliseconds into the format `XXhYYmZZs`.
     *
     * @param milliseconds The time duration in milliseconds.
     * @return A formatted string representing the duration.
     */
    public static String formatMilliseconds(long milliseconds) {
        if (milliseconds < 0) {
            return "";
        }
        long totalSeconds = milliseconds / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder stringBuilder = new StringBuilder();

        if (hours > 0) {
            stringBuilder.append(hours).append("h");
        }
        if (minutes > 0) {
            stringBuilder.append(minutes).append("m");
        } else if (hours > 0 && seconds > 0) {
            stringBuilder.append("0m");
        }
        if (seconds > 0) {
            stringBuilder.append(seconds).append("s");
        }
        if (stringBuilder.length() == 0) {
            return "0s";
        }
        return stringBuilder.toString();
    }

    public static Long parseTime(String timeInput) {
        if (timeInput == null || timeInput.trim().isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile(
                "^(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeInput.trim());
        if (!matcher.matches()) {
            return null;
        }
        long hours = parseLongOrZero(matcher.group(1));
        long minutes = parseLongOrZero(matcher.group(2));
        long seconds = parseLongOrZero(matcher.group(3));
        return hours * 3600_000L
                + minutes * 60_000L
                + seconds * 1_000L;
    }

    private static long parseLongOrZero(String group) {
        return group == null ? 0L : Long.parseLong(group);
    }

    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now(getZoneId());
    }

    public static void computeDailyVoiceChatTime(
            Map<LocalDate, Duration> dailyVoiceChatTimeMap,
            Date joinDate,
            Date leaveDate) {
        ZoneId zoneId = ZoneId.of(TIME_ZONE_ID);
        ZonedDateTime zonedJoinDate = joinDate.toInstant().atZone(zoneId);
        ZonedDateTime zonedLeaveDate = leaveDate.toInstant().atZone(zoneId);
        if (zonedJoinDate.getDayOfYear() == zonedLeaveDate.getDayOfYear()) {
            // User joined and left in the same day
            addDailyVoiceChatTime(
                    dailyVoiceChatTimeMap,
                    zonedJoinDate.toLocalDate(),
                    Duration.between(zonedJoinDate, zonedLeaveDate));
        } else {
            // User joined and left on different days
            ZonedDateTime startOfLeaveDate = zonedLeaveDate.toLocalDate().atStartOfDay(zoneId);

            addDailyVoiceChatTime(
                    dailyVoiceChatTimeMap,
                    zonedJoinDate.toLocalDate(),
                    Duration.between(zonedJoinDate, startOfLeaveDate));

            addDailyVoiceChatTime(
                    dailyVoiceChatTimeMap,
                    zonedLeaveDate.toLocalDate(),
                    Duration.between(startOfLeaveDate, zonedLeaveDate));
        }
    }

    private static void addDailyVoiceChatTime(
            Map<LocalDate, Duration> dailyVoiceChatTimeMap,
            LocalDate localDate,
            Duration duration) {
        Duration storedDuration = dailyVoiceChatTimeMap.getOrDefault(localDate, Duration.ZERO);
        dailyVoiceChatTimeMap.put(
                localDate,
                storedDuration.plus(duration));
    }

    public static ZoneId getZoneId() {
        return ZoneId.of(TIME_ZONE_ID);
    }
}
