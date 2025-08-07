package me.eeshe.grammyswrapped;

import java.util.EnumSet;

import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.util.AppConfig;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Role;
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
  private final StatsService statsService;

  public Bot() {
    this.statsService = new StatsService();
  }

  public void start() {
    AppConfig botConfig = new AppConfig();
    String botToken = botConfig.getBotToken();
    if (botToken == null) {
      System.out.println("Bot token not provided.");
      return;
    }
    statsService.createStatsTables();

    JDABuilder.createDefault(
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

  private boolean hasEsquizogangRole(Member member) {
    Role esquizogangRole = member.getGuild().getRoleById("1046078281981624380");

    return member.getRoles().contains(esquizogangRole);
  }

  @Override
  public void onUserActivityStart(UserActivityStartEvent event) {
    Member member = event.getMember();
    if (!hasEsquizogangRole(member)) {
      return;
    }
    System.out.println("-------------------------");
    System.out.println(member.getUser().getName() + " started an activity");

    Activity newActivity = event.getNewActivity();
    RichPresence richPresence = newActivity.asRichPresence();
    System.out.println(richPresence.getName());
    System.out.println(richPresence.getState());
    System.out.println(richPresence.getDetails());
  }

  @Override
  public void onUserActivityEnd(UserActivityEndEvent event) {
    Member member = event.getMember();
    if (!hasEsquizogangRole(member)) {
      return;
    }
    System.out.println("------------------------------");
    System.out.println(member.getUser().getName() + " ended an activity");

    Activity newActivity = event.getOldActivity();
    RichPresence richPresence = newActivity.asRichPresence();
    System.out.println(richPresence.getName());
    System.out.println(richPresence.getState());
    System.out.println(richPresence.getDetails());
  }

  @Override
  public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
    if (event.getChannelJoined() != null) {
      System.out.println(event.getEntity().getNickname() + " JOINED VC CHANNEL");
    }
    if (event.getChannelLeft() != null) {
      System.out.println(event.getEntity().getNickname() + " LEFT VC CHANNEL");
    }
    for (Member member : event.getGuild().getMembers()) {
      System.out.println(member.getNickname());

      for (Activity activity : member.getActivities()) {
        System.out.println(activity.getName());
      }
    }
  }

  @Override
  public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
    String user = event.getMember().getNickname();
    if (event.getVoiceState().isSelfMuted()) {
      System.out.println(user + " MUTED");
    } else {
      System.out.println(user + " UNMUTED");
    }
  }

  @Override
  public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
    String user = event.getMember().getNickname();
    if (event.getVoiceState().isSelfDeafened()) {
      System.out.println(user + " DEAFENED");
    } else {
      System.out.println(user + " UNDEAFENDED");
    }
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    System.out
        .println("[" + event.getChannel() + "]: #" + event.getAuthor() + ":" + event.getMessage().getContentDisplay());
  }
}
