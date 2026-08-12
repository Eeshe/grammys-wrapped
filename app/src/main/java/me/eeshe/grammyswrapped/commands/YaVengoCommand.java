package me.eeshe.grammyswrapped.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.grammyswrapped.model.LocalizedMessage;
import me.eeshe.grammyswrapped.repository.YaVengoRepository;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.modals.Modal;

public class YaVengoCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(YaVengoCommand.class);

    private final YaVengoRepository yaVengoRepository;

    public YaVengoCommand(YaVengoRepository yaVengoRepository) {
        this.yaVengoRepository = yaVengoRepository;
    }

    public void handle(SlashCommandInteractionEvent event) {
        final OptionMapping userOptionMapping = event.getOption("user");
        final User target;
        if (userOptionMapping != null) {
            target = userOptionMapping.getAsUser();
        } else {
            target = event.getUser();
        }
        yaVengoRepository.put(event.getUser(), target);
        event.replyModal(createInputModal(target)).queue();
    }

    private Modal createInputModal(final User target) {
        final String targetName = target.getName();
        return Modal.create("ya_vengo_modal", LocalizedMessage.YA_VENGO_MODAL_TITLE.getFormatted(targetName))
                .addComponents(
                        Label.of(LocalizedMessage.YA_VENGO_MODAL_QUESTION.getFormatted(targetName),
                                TextInput.create("time",
                                        TextInputStyle.SHORT)
                                        .setPlaceholder(LocalizedMessage.YA_VENGO_MODAL_QUESTION_PLACEHOLDER.get())
                                        .setRequired(true).build()))
                .build();
    }
}
