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

public class SetVariableCommand implements CommandExecutor {

    Variables variables;

    public SetVariableCommand(Variables variables) {
        this.variables = variables;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();

        if(!Variables.isValidVariableKey(key)) {
            src.sendMessage(Text.of(TextColors.RED, "Key contains invalid characters!"));
            return CommandResult.empty();
        }

        String value = args.<String>getOne("value").get();

        variables.addVariable(((Player) src).getUniqueId().toString(), key, value);
        src.sendMessage(Text.of(TextColors.YELLOW, "Set variable " + key + " to value " + variables.getVariable(((Player) src).getUniqueId().toString(), key)));

        return CommandResult.success();
    }
}
