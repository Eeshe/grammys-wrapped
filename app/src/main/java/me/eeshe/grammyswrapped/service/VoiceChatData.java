package me.eeshe.grammyswrapped.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import me.eeshe.grammyswrapped.model.userdata.UserVoiceChatData;

public class VoiceChatData {
  private final Map<String, UserVoiceChatData> userVoiceChatData;
  private final Map<LocalDate, Duration> overallDailyVoiceChatTime;

  public VoiceChatData() {
    this.userVoiceChatData = new HashMap<>();
    this.overallDailyVoiceChatTime = new TreeMap<>();
  }

  public VoiceChatData(Map<String, UserVoiceChatData> userVoiceChatData,
      Map<LocalDate, Duration> overallDailyVoiceChatTime) {
    this.userVoiceChatData = userVoiceChatData;
    this.overallDailyVoiceChatTime = overallDailyVoiceChatTime;
  }

  public Map<String, UserVoiceChatData> getUserVoiceChatData() {
    return userVoiceChatData;
  }

  public Map<LocalDate, Duration> getOverallDailyVoiceChatTime() {
    return overallDailyVoiceChatTime;
  }
}
