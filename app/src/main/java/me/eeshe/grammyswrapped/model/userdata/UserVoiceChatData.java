package me.eeshe.grammyswrapped.model.userdata;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.User;

public class UserVoiceChatData extends UserData {
  private int joinedVoiceChats;
  private long voiceChatTimeMillis;
  private long mutedVoiceChatTimeMillis;
  private long deafenedVoiceChatTimeMillis;

  public UserVoiceChatData(User user) {
    super(user);

    this.joinedVoiceChats = 0;
    this.voiceChatTimeMillis = 0;
    this.mutedVoiceChatTimeMillis = 0;
    this.deafenedVoiceChatTimeMillis = 0;
  }

  public int getJoinedVoiceChats() {
    return joinedVoiceChats;
  }

  public void increaseJoinedVoiceChats() {
    this.joinedVoiceChats += 1;
  }

  public long getVoiceChatTimeMillis() {
    return voiceChatTimeMillis;
  }

  public void addVoiceChatTime(Date joinDate, Date leaveDate) {
    this.voiceChatTimeMillis += leaveDate.getTime() - joinDate.getTime();
  }

  public long getMutedVoiceChatTimeMillis() {
    return mutedVoiceChatTimeMillis;
  }

  public void addMutedVoiceChatTime(Date startingDate, Date endingDate) {
    long timeMillis = endingDate.getTime() - startingDate.getTime();
    if (TimeUnit.MILLISECONDS.toHours(timeMillis) > 6) {
      return;
    }
    this.mutedVoiceChatTimeMillis += timeMillis;
  }

  public long getDeafenedVoiceChatTimeMillis() {
    return deafenedVoiceChatTimeMillis;
  }

  public void addDeafenedVoiceChatTime(Date startingDate, Date endingDate) {
    long timeMillis = endingDate.getTime() - startingDate.getTime();
    if (TimeUnit.MILLISECONDS.toHours(timeMillis) > 6) {
      return;
    }
    this.deafenedVoiceChatTimeMillis += timeMillis;
  }
}
