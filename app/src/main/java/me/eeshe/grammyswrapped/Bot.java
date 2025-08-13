package me.eeshe.grammyswrapped;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.util.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot extends ListenerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

  private final StatsService statsService;
  private JDA bot;

  public Bot() {
    this.statsService = new StatsService();
  }

  public void start() {
    AppConfig botConfig = new AppConfig();
    String botToken = botConfig.getBotToken();
    if (botToken == null) {
      LOGGER.error("Bot token not provided.");
      return;
    }
    statsService.createStatsTables();

    this.bot = JDABuilder.createDefault(
        botToken,
        EnumSet.of(
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT))
        .addEventListeners(this)
        .enableCache(CacheFlag.ACTIVITY)
        .setMemberCachePolicy(MemberCachePolicy.ONLINE)
        .build();
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
    boolean joined = event.getChannelJoined() != null;
    statsService.logVoiceChatConnection(
        event.getEntity().getUser(),
        joined);
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
