package me.eeshe.grammyswrapped;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.database.PostgreSQLDatabase;
import me.eeshe.grammyswrapped.listeners.CommandListener;
import me.eeshe.grammyswrapped.listeners.ElectricityStatusListener;
import me.eeshe.grammyswrapped.listeners.StatsListener;
import me.eeshe.grammyswrapped.listeners.YaVengoListener;
import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.repository.ElectricityStatusEmbedRepository;
import me.eeshe.grammyswrapped.repository.Repository;
import me.eeshe.grammyswrapped.repository.YaVengoRepository;
import me.eeshe.grammyswrapped.repository.impl.ElectricityStatusEmbedRepositoryImpl;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.service.LocalizationService;
import me.eeshe.grammyswrapped.service.Service;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.service.impl.ElectricityStatusEmbedServiceImpl;
import me.eeshe.grammyswrapped.util.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    private final ConcurrentHashMap<String, User> yaVengoTargets = new ConcurrentHashMap<>();
    private final List<Repository> repositories = new ArrayList<>();
    private final List<Service> services = new ArrayList<>();

    private YaVengoRepository yaVengoRepository;
    private ElectricityStatusEmbedRepository electricityStatusEmbedRepository;

    private StatsService statsService;
    private ElectricityStatusEmbedService electricityStatusService;

    private JDA bot;

    public void start() {
        AppConfig botConfig = new AppConfig();
        LocalizationService.initialize(botConfig.getLanguageCode());

        String botToken = botConfig.getBotToken();
        if (botToken == null) {
            LOGGER.error(LocalizedMessage.BOT_TOKEN_NOT_CONFIGURED.get());
            return;
        }
        this.bot = JDABuilder.createDefault(
                botToken,
                EnumSet.of(
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT))
                .enableCache(CacheFlag.ACTIVITY)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        initializeRepositories();
        initializeServices();

        addListeners();
        addCommands();
    }

    private void initializeRepositories() {
        this.yaVengoRepository = new YaVengoRepository();
        this.electricityStatusEmbedRepository = new ElectricityStatusEmbedRepositoryImpl(
                PostgreSQLDatabase.getInstance());

        repositories.addAll(List.of(
                electricityStatusEmbedRepository));
        for (Repository repository : repositories) {
            repository.onStart();
        }
    }

    private void initializeServices() {
        this.statsService = new StatsService();
        this.electricityStatusService = new ElectricityStatusEmbedServiceImpl(
                bot,
                electricityStatusEmbedRepository,
                statsService);

        services.addAll(List.of(
                statsService,
                electricityStatusService));

        for (Service service : services) {
            service.onBotStart();
        }
    }

    private void addListeners() {
        bot.addEventListener(this);
        bot.addEventListener(new StatsListener(statsService));
        bot.addEventListener(new YaVengoListener(yaVengoRepository));
        bot.addEventListener(new ElectricityStatusListener(electricityStatusService));

        bot.addEventListener(new CommandListener(
                bot,
                statsService,
                yaVengoRepository,
                electricityStatusService));
    }

    /**
     * Adds all the commands used by the bot.
     */
    private void addCommands() {
        try {
            bot.awaitReady();
            bot.getGuildById(new AppConfig().getTestGuildId()).updateCommands().addCommands(
                    Commands.slash("wrapped", "Shows a summary of the Esquizogang within the specified dates")
                            .addOption(OptionType.STRING, "starting-date", "Date at which the summary will start", true)
                            .addOption(OptionType.STRING, "ending-date", "Date at which the summary will end", true),
                    Commands.slash("yavengo", "Inicia un contador del tiempo en el que viene alguien")
                            .addOption(OptionType.USER, "user",
                                    "Usuario que ya viene. Dejar en blanco para usarlo en uno mismo", false),
                    Commands.slash("postelectricitystatusembed", "Publica el seguidor de Corpoelec"))
                    .queue(
                            success -> LOGGER.info("Successfully registered commands."),
                            failure -> LOGGER.error("Failed to register commands. {}", failure.getMessage()));
            LOGGER.info("Registering commands...");
        } catch (Exception e) {
            LOGGER.error("Adding commands got interrupted. {}", e.getMessage());
        }
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        LOGGER.warn("Disconnected from Discord Gateway. Code: {} Message: {}", event.getCloseCode(),
                event.getCloseCode().getMeaning());
    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {
        LOGGER.info("Session has been resumed successfully!");
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        LOGGER.info("Session has been recreated successfully!");
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        LOGGER.warn("JDA has been shut down. Code: {} Message: {}", event.getCloseCode(),
                event.getCloseCode().getMeaning());
    }

    public ConcurrentHashMap<String, User> getYaVengoTargets() {
        return yaVengoTargets;
    }
}
