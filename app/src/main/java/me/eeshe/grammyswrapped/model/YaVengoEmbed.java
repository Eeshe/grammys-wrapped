package me.eeshe.grammyswrapped.model;

import java.awt.Color;
import java.util.Timer;

import me.eeshe.grammyswrapped.util.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class YaVengoEmbed {
    private final User target;
    private final long awayTimeMillis;
    private final long arrivalTimeMillis;
    private boolean hasBeenEdited;
    private Timer updateTask;

    public YaVengoEmbed(User target, long awayTimeMillis) {
        this.target = target;
        this.awayTimeMillis = awayTimeMillis;
        this.arrivalTimeMillis = System.currentTimeMillis() + awayTimeMillis;
    }

    public MessageEmbed createRunningEmbed() {
        final Color embedColor;
        final LocalizedMessage description;
        if (!isPastArrivalTime()) {
            embedColor = Color.GREEN;
            description = LocalizedMessage.YA_VENGO_EMBED_RUNNING_DESCRIPTION_POSITIVE;
        } else {
            embedColor = Color.RED;
            description = LocalizedMessage.YA_VENGO_EMBED_RUNNING_DESCRIPTION_NEGATIVE;
        }
        return new EmbedBuilder()
                .setTitle(LocalizedMessage.YA_VENGO_EMBED_RUNNING_TITLE.getFormatted(target.getAsMention()))
                .setDescription(description.getFormatted(
                        target.getAsMention(),
                        TimeUtil.formatMilliseconds(awayTimeMillis),
                        generateTimestampString()))
                .setColor(embedColor)
                .setImage(LocalizedMessage.YA_VENGO_EMBED_RUNNING_IMAGE.get())
                .build();
    }

    public MessageEmbed createStoppedEmbed() {
        long spareTimeMillis = arrivalTimeMillis - System.currentTimeMillis();
        final Color embedColor;
        final LocalizedMessage description;
        if (spareTimeMillis >= 0) {
            embedColor = Color.GREEN;
            description = LocalizedMessage.YA_VENGO_EMBED_STOPPED_DESCRIPTION_POSITIVE;
        } else {
            embedColor = Color.RED;
            description = LocalizedMessage.YA_VENGO_EMBED_STOPPED_DESCRIPTION_NEGATIVE;
        }
        return new EmbedBuilder()
                .setTitle(LocalizedMessage.YA_VENGO_EMBED_STOPPED_TITLE.getFormatted(target.getAsMention()))
                .setDescription(description.getFormatted(
                        target.getAsMention(),
                        TimeUtil.formatMilliseconds(awayTimeMillis),
                        TimeUtil.formatMilliseconds(Math.abs(spareTimeMillis))))
                .setColor(embedColor)
                .setImage(LocalizedMessage.YA_VENGO_EMBED_STOPPED_IMAGE.get())
                .build();
    }

    public ActionRow createEmbedActionRow() {
        return ActionRow.of(Button.danger("stop_ya_vengo", LocalizedMessage.YA_VENGO_EMBED_RUNNING_BUTTON.get()));
    }

    private String generateTimestampString() {
        return String.format("<t:%s:R>", arrivalTimeMillis / 1000);
    }

    public boolean isPastArrivalTime() {
        return arrivalTimeMillis < System.currentTimeMillis();
    }

    public boolean hasBeenEdited() {
        return hasBeenEdited;
    }

    public void setHasBeenEdited(boolean hasBeenEdited) {
        this.hasBeenEdited = hasBeenEdited;
    }

    public Timer getUpdateTask() {
        return updateTask;
    }

    public void setUpdateTask(Timer updateTask) {
        this.updateTask = updateTask;
    }
}
