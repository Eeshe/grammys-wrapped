package me.eeshe.grammyswrapped;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.listeners.StatsListener;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.util.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
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
        .addEventListeners(new StatsListener(statsService))
        .enableCache(CacheFlag.ACTIVITY)
        .setMemberCachePolicy(MemberCachePolicy.ONLINE)
        .build();
  }

  @Override
  public void onSessionDisconnect(SessionDisconnectEvent event) {
    LOGGER.warn("Disconnected from Discord Gateway. Code: {} Message: {}", event.getCloseCode(),
        event.getCloseCode().getMeaning());
  }

  @Override
  public void onSessionResume(@Nonnull SessionResumeEvent event) {
    LOGGER.info("Session has been resumed successfully!");
  }

  @Override
  public void onSessionRecreate(SessionRecreateEvent event) {
    LOGGER.info("Session has been recreated successfully!");
  }

  @Override
  public void onShutdown(@Nonnull ShutdownEvent event) {
    LOGGER.warn("JDA has been shut down. Code: {} Message: {}", event.getCloseCode(),
        event.getCloseCode().getMeaning());
  }
}
