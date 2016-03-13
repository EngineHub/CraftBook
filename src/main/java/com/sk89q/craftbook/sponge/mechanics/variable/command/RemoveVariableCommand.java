package com.sk89q.craftbook.sponge.mechanics.variable.command;

import com.sk89q.craftbook.sponge.mechanics.variable.Variables;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class RemoveVariableCommand implements CommandExecutor {

    Variables variables;

    public RemoveVariableCommand(Variables variables) {
        this.variables = variables;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();

        variables.removeVariable(((Player)src).getUniqueId().toString(), key);
        src.sendMessage(Text.of(TextColors.YELLOW, "Removed variable " + key));

        return CommandResult.success();
    }
}
