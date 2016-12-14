package com.sk89q.craftbook.sponge.mechanics.signcopier.command;

import com.sk89q.craftbook.sponge.mechanics.signcopier.SignCopier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

public class EditSignCommand implements CommandExecutor {

    private SignCopier signCopier;

    public EditSignCommand(SignCopier signCopier) {
        this.signCopier = signCopier;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            if (!signCopier.getSigns().containsKey(((Player) src).getUniqueId())) {
                src.sendMessage(Text.of(TextColors.RED, "You haven't copied a sign!"));
            } else {
                int line = args.<Integer>getOne("line").orElse(-1) - 1;
                if (line < 0 || line > 3) {
                    src.sendMessage(Text.of(TextColors.RED, "Line must be between 1 and 4."));
                } else {
                    String text = args.<String>getOne("text").orElse("");

                    signCopier.getSigns().get(((Player) src).getUniqueId()).set(line, TextSerializers.FORMATTING_CODE.deserialize(text));
                    src.sendMessage(Text.of(TextColors.YELLOW, "Updated message!"));
                }
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "Only players can use this mechanic!"));
        }

        return CommandResult.success();
    }
}
