package me.eeshe.grammyswrapped.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

public class SessionTimeUtil {
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
