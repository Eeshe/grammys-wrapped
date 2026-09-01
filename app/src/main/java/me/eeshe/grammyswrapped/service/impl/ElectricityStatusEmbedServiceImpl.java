package me.eeshe.grammyswrapped.service.impl;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;
import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.model.UserElectricityStatus;
import me.eeshe.grammyswrapped.repository.ElectricityStatusEmbedRepository;
import me.eeshe.grammyswrapped.service.ElectricityStatusEmbedService;
import me.eeshe.grammyswrapped.service.StatsService;
import me.eeshe.grammyswrapped.util.AppConfig;
import me.eeshe.grammyswrapped.util.EmbedUtil;
import me.eeshe.grammyswrapped.util.TimeUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

public class ElectricityStatusEmbedServiceImpl implements ElectricityStatusEmbedService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElectricityStatusEmbedServiceImpl.class);

    private final JDA bot;
    private final ElectricityStatusEmbedRepository electricityStatusEmbedRepository;
    private final StatsService statsService;

    private ScheduledExecutorService electricityStatusEmbedUpdateScheduler;
    private ScheduledExecutorService electricityStatusEmbedReminderScheduler;

    public ElectricityStatusEmbedServiceImpl(
            JDA bot,
            ElectricityStatusEmbedRepository electricityStatusEmbedRepository,
            StatsService statsService) {
        this.bot = bot;
        this.electricityStatusEmbedRepository = electricityStatusEmbedRepository;
        this.statsService = statsService;
    }

    @Override
    public void onBotStart() {
        startElectricityStatusEmbedUpdateScheduler();
        startElectricityStatusEmbedReminderScheduler();
    }

    private void startElectricityStatusEmbedUpdateScheduler() {
        this.electricityStatusEmbedUpdateScheduler = Executors.newScheduledThreadPool(1);
        this.electricityStatusEmbedUpdateScheduler.scheduleAtFixedRate(
                this::updateAllElectricityStatusEmbeds,
                10L,
                60L,
                TimeUnit.SECONDS);
    }

    private void updateAllElectricityStatusEmbeds() {
        for (ElectricityStatusEmbed embed : electricityStatusEmbedRepository.findAll()) {
            updateElectricityStatusEmbed(embed);
        }
    }

    private void startElectricityStatusEmbedReminderScheduler() {
        this.electricityStatusEmbedReminderScheduler = Executors.newScheduledThreadPool(1);

        this.electricityStatusEmbedReminderScheduler.scheduleAtFixedRate(
                this::sendElectricityStatusEmbedReminders,
                10L,
                60L,
                TimeUnit.SECONDS);
    }

    private void sendElectricityStatusEmbedReminders() {
        for (ElectricityStatusEmbed embed : electricityStatusEmbedRepository.findAll()) {
            for (UserElectricityStatus participant : embed.getParticipants().values()) {
                if (participant.hasElectricity()) {
                    continue;
                }
                if (participant.calculateTimeSinceLastReminder().toHours() < 2) {
                    continue;
                }
                final User user = bot.getUserById(participant.getUserId());
                if (user == null) {
                    continue;
                }
                LOGGER.info("Sending reminder to user '{}'", user.getName());
                LOGGER.info("Time since last reminder: {}", participant.calculateTimeSinceLastReminder());
                user.openPrivateChannel().queue(
                        privateChannel -> {
                            privateChannel.sendMessage(LocalizedMessage.ELECTRICITY_STATUS_REMINDER.getFormatted(
                                    embed.createMessageLink())).queue();
                            participant.setLastReminderTime(Instant.now());

                            electricityStatusEmbedRepository.save(embed);
                        },
                        error -> {
                            LOGGER.error("Error sending reminder to {}. Message: {}",
                                    user.getName(),
                                    error.getMessage());
                        });
            }
        }
    }

    @Override
    public void onBotStop() {
        this.electricityStatusEmbedUpdateScheduler.shutdown();
        this.electricityStatusEmbedReminderScheduler.shutdown();
    }

    @Override
    public void postElectricityStatusEmbed(String guildId, String channelId) {
        final TextChannel textChannel = bot.getTextChannelById(channelId);
        if (textChannel == null) {
            return;
        }
        textChannel.sendMessageEmbeds(createElectricityStatusEmbed(new ArrayList<>()))
                .addComponents(createElectrictyStatusButtons())
                .queue(message -> {
                    final ElectricityStatusEmbed electricityStatusEmbed = new ElectricityStatusEmbed(
                            guildId,
                            channelId,
                            message.getId());
                    electricityStatusEmbedRepository.save(electricityStatusEmbed);
                });
    }

    private ActionRow createElectrictyStatusButtons() {
        return ActionRow.of(
                Button.danger(
                        "electricity_out",
                        LocalizedMessage.ELECTRICITY_STATUS_EMBED_ELECTRICITY_OUT_BUTTON.get()),
                Button.primary(
                        "electricity_in",
                        LocalizedMessage.ELECTRICITY_STATUS_EMBED_ELECTRICITY_IN_BUTTON.get()));
    }

    @Override
    public void sendElectricityInEstimateModal(ButtonInteractionEvent event) {
        event.replyModal(createElectricityInEstimateModal()).queue();
    }

    private Modal createElectricityInEstimateModal() {
        return Modal.create("electricity_in_estimate",
                LocalizedMessage.ELECTRICITY_IN_MODAL_TITLE.get())
                .addComponents(
                        Label.of(LocalizedMessage.ELECTRICITY_IN_MODAL_QUESTION.get(),
                                TextInput.create("time", TextInputStyle.SHORT)
                                        .setPlaceholder(
                                                LocalizedMessage.ELECTRICITY_IN_MODAL_QUESTION_PLACEHOLDER.get())
                                        .setRequired(false).build()))
                .build();
    }

    @Override
    public void addElectricityOutEntry(String messageId, Member member, Instant electricityInEstimate) {
        final ElectricityStatusEmbed electricityStatusEmbed = electricityStatusEmbedRepository
                .getByMessageId(messageId);
        if (electricityStatusEmbed == null) {
            return;
        }
        final UserElectricityStatus userElectricityStatus = electricityStatusEmbed
                .getParticipant(member.getUser().getId());
        String nickname = member.getNickname();
        if (nickname == null) {
            nickname = member.getUser().getGlobalName();
        }

        userElectricityStatus.setNickname(nickname);
        userElectricityStatus.setLastElectricityOutTime(Instant.now());
        userElectricityStatus.setElectricityInEstimate(electricityInEstimate);
        userElectricityStatus.setLastReminderTime(Instant.now());

        electricityStatusEmbed.addParticipant(userElectricityStatus);

        electricityStatusEmbedRepository.save(electricityStatusEmbed);
        updateElectricityStatusEmbed(electricityStatusEmbed);

        statsService.logElectricityStatusChange(member.getUser(), false);
        sendElectricityOutAlert(electricityStatusEmbed, userElectricityStatus);
    }

    private void sendElectricityOutAlert(
            ElectricityStatusEmbed electricityStatusEmbed,
            UserElectricityStatus userElectricityStatus) {
        final String alertMessage = LocalizedMessage.ELECTRICITY_STATUS_ALERT_ELECTRICITY_OUT.getFormatted(
                bot.getUserById(userElectricityStatus.getUserId()),
                TimeUtil.formatMilliseconds(
                        userElectricityStatus.calculateTimeSinceLastElectricityOutage().toMillis()));

        sendElectricityAlert(alertMessage);
    }

    @Override
    public void addElectricityInEntry(String messageId, Member member) {
        final ElectricityStatusEmbed electricityStatusEmbed = electricityStatusEmbedRepository
                .getByMessageId(messageId);
        if (electricityStatusEmbed == null) {
            return;
        }
        final UserElectricityStatus userElectricityStatus = electricityStatusEmbed
                .getParticipant(member.getUser().getId());
        String nickname = member.getNickname();
        if (nickname == null) {
            nickname = member.getUser().getGlobalName();
        }

        userElectricityStatus.setNickname(nickname);
        userElectricityStatus.setLastElectricityInTime(Instant.now());

        electricityStatusEmbed.addParticipant(userElectricityStatus);

        electricityStatusEmbedRepository.save(electricityStatusEmbed);
        updateElectricityStatusEmbed(electricityStatusEmbed);

        statsService.logElectricityStatusChange(member.getUser(), true);
        sendElectricityInAlert(userElectricityStatus);
    }

    private void sendElectricityInAlert(UserElectricityStatus userElectricityStatus) {
        if (userElectricityStatus.getLastElectricityOutTime() == null) {
            return;
        }
        final String alertMessage = LocalizedMessage.ELECTRICITY_STATUS_ALERT_ELECTRICITY_IN.getFormatted(
                bot.getUserById(userElectricityStatus.getUserId()),
                TimeUtil.formatHHMMTimestamp(userElectricityStatus.getLastElectricityOutTime().toEpochMilli()),
                TimeUtil.formatHHMMTimestamp(userElectricityStatus.getLastElectricityInTime().toEpochMilli()),
                TimeUtil.formatMilliseconds(userElectricityStatus.calculateLastElectricityOutageDuration().toMillis()));

        sendElectricityAlert(alertMessage);
    }

    private void sendElectricityAlert(final String alertMessage) {
        final String channelId = new AppConfig().getElectricityStatusChangeAlertChannelId();
        if (channelId == null) {
            LOGGER.warn("Electricity status alert channel ID not provided");
            return;
        }
        final TextChannel alertChannel = bot.getTextChannelById(channelId);
        if (alertChannel == null) {
            LOGGER.warn("Invalid electricity status alert channel ID '{}'", channelId);
            return;
        }
        alertChannel.sendMessage(alertMessage).queue();
    }

    private void updateElectricityStatusEmbed(ElectricityStatusEmbed electricityStatusEmbed) {
        final TextChannel textChannel = bot.getTextChannelById(electricityStatusEmbed.getChannelId());
        textChannel.retrieveMessageById(electricityStatusEmbed.getMessageId()).queue(
                message -> {
                    message.editMessageEmbeds(
                            createElectricityStatusEmbed(electricityStatusEmbed.getParticipants().values()))
                            .queue();
                },
                error -> {
                    LOGGER.error("Error updating embed with message ID {}. Message: {}",
                            electricityStatusEmbed.getMessageId(),
                            error.getMessage());
                });
    }

    private MessageEmbed createElectricityStatusEmbed(Collection<UserElectricityStatus> participants) {
        return EmbedUtil.createEmbed(
                Color.YELLOW,
                LocalizedMessage.ELECTRICITY_STATUS_EMBED_TITLE.get(),
                generateEmbedDescription(new ArrayList<>(participants)))
                .setFooter(LocalizedMessage.ELECTRICITY_STATUS_EMBED_FOOTER.get())
                .setTimestamp(null)
                .setThumbnail(LocalizedMessage.ELECTRICITY_STATUS_EMBED_IMAGE_URL.get())
                .build();
    }

    private String generateEmbedDescription(List<UserElectricityStatus> participants) {
        if (participants.isEmpty()) {
            return LocalizedMessage.ELECTRICITY_STATUS_EMBED_EMPTY_DESCRIPTION.get();
        }
        participants.sort(Comparator.comparing(participant -> participant.getNickname().toLowerCase()));

        final StringBuilder descriptionBuilder = new StringBuilder();
        for (UserElectricityStatus participant : participants) {
            descriptionBuilder.append(generateNicknameLine(participant)).append("\n");
            descriptionBuilder.append(generateElectricityStatusLine(participant)).append("\n");
            descriptionBuilder.append(generateElectricityEstimateLine(participant)).append("\n");

            descriptionBuilder.append("\n");
        }
        return descriptionBuilder.toString();
    }

    private String generateNicknameLine(UserElectricityStatus userElectricityStatus) {
        final String nickname = userElectricityStatus.getNickname();

        return userElectricityStatus.hasElectricity()
                ? LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_NICKNAME_WITH_ELECTRICITY
                        .getFormatted(nickname)
                : LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_NICKNAME_WITHOUT_ELECTRICITY
                        .getFormatted(nickname);

    }

    private String generateElectricityStatusLine(UserElectricityStatus userElectricityStatus) {
        if (!userElectricityStatus.hasElectricity()) {
            return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITHOUT_ELECTRICITY.getFormatted(
                    TimeUtil.formatHHMMTimestamp(userElectricityStatus.getLastElectricityOutTime().toEpochMilli()),
                    TimeUtil.formatRelativeTimestamp(userElectricityStatus.getLastElectricityOutTime().toEpochMilli()));
        }
        if (!userElectricityStatus.hasHadEletricityOutToday()) {
            return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITH_ELECTRICITY_PRE_OUTAGE.get();
        }
        return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_STATUS_WITH_ELECTRICITY_POST_OUTAGE.getFormatted(
                TimeUtil.formatHHMMTimestamp(userElectricityStatus.getLastElectricityOutTime().toEpochMilli()),
                TimeUtil.formatHHMMTimestamp(userElectricityStatus.getLastElectricityInTime().toEpochMilli()),
                TimeUtil.formatMilliseconds(userElectricityStatus.calculateLastElectricityOutageDuration().toMillis()));
    }

    private String generateElectricityEstimateLine(UserElectricityStatus userElectricityStatus) {
        if (!userElectricityStatus.hasElectricity()) {
            return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITHOUT_ELECTRICITY.getFormatted(
                    TimeUtil.formatHHMMTimestamp(userElectricityStatus.getElectricityInEstimate().toEpochMilli()),
                    TimeUtil.formatRelativeTimestamp(userElectricityStatus.getElectricityInEstimate().toEpochMilli()));
        }
        if (userElectricityStatus.hasHadEletricityOutToday()) {
            return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITH_ELECTRICITY_POST_OUTAGE.get();
        }
        return LocalizedMessage.ELECTRICITY_STATUS_EMBED_DESCRIPTION_ESTIMATE_WITH_ELECTRICITY_PRE_OUTAGE.getFormatted(
                TimeUtil.formatMilliseconds(
                        userElectricityStatus.calculateTimeSinceLastElectricityOutage().toMillis()));
    }

    @Override
    public void deleteElectricityStatusEmbed(String messageId) {
        if (electricityStatusEmbedRepository.getByMessageId(messageId) == null) {
            return;
        }
        electricityStatusEmbedRepository.delete(messageId);
    }
}
