package me.eeshe.grammyswrapped.listeners;

import me.eeshe.grammyswrapped.service.StatsService;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StatsListener extends ListenerAdapter {
  private final StatsService statsService;

  public StatsListener(StatsService statsService) {
    this.statsService = statsService;
  }

  @Override
  public void onUserActivityStart(UserActivityStartEvent event) {
    Member member = event.getMember();
    if (!hasEsquizogangRole(member)) {
      return;
    }
    Activity newActivity = event.getNewActivity();
    RichPresence richPresence = newActivity.asRichPresence();
    if (richPresence == null) {
      return;
    }
    statsService.logPresence(
        member.getUser(),
        true,
        richPresence);
  }

  @Override
  public void onUserActivityEnd(UserActivityEndEvent event) {
    Member member = event.getMember();
    if (!hasEsquizogangRole(member)) {
      return;
    }
    Activity newActivity = event.getOldActivity();
    RichPresence richPresence = newActivity.asRichPresence();
    if (richPresence == null) {
      return;
    }
    statsService.logPresence(
        member.getUser(),
        false,
        richPresence);
  }

  @Override
  public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
    if (!hasEsquizogangRole(event.getMember())) {
      return;
    }
    boolean joined = event.getChannelJoined() != null;
    User user = event.getEntity().getUser();
    GuildVoiceState voiceState = event.getVoiceState();
    statsService.logVoiceChatConnection(
        user,
        joined);
    if (!joined) {
      // Upon leaving, muted and deafened status are always false. Log this to
      // avoid carrying muted/deafened time to the next VC connection
      logVoiceChatEvent(
          user,
          voiceState);
    }
    if (!joined || (!voiceState.isSelfMuted() && !voiceState.isSelfDeafened())) {
      return;
    }
    // Member joined while being muted/deafened, log the state
    logVoiceChatEvent(
        user,
        voiceState);
  }

  @Override
  public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
    if (!hasEsquizogangRole(event.getMember())) {
      return;
    }
    logVoiceChatEvent(
        event.getMember().getUser(),
        event.getVoiceState());
  }

  @Override
  public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
    if (!hasEsquizogangRole(event.getMember())) {
      return;
    }
    logVoiceChatEvent(
        event.getMember().getUser(),
        event.getVoiceState());
  }

  private void logVoiceChatEvent(User user, GuildVoiceState voiceState) {
    statsService.logVoiceChatEvent(
        user,
        voiceState.isSelfMuted(),
        voiceState.isSelfDeafened());
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (!hasEsquizogangRole(event.getMember())) {
      return;
    }
    statsService.logMessage(event.getMessage());
  }

  private boolean hasEsquizogangRole(Member member) {
    Role esquizogangRole = member.getGuild().getRoleById("1046078281981624380");

    return member.getRoles().contains(esquizogangRole);
  }

}
