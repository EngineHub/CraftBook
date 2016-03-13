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

public class GetVariableCommand implements CommandExecutor {

    Variables variables;

    public GetVariableCommand(Variables variables) {
        this.variables = variables;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();

        src.sendMessage(Text.of(TextColors.YELLOW, "Variable " + key +  " is set to " + variables.getVariable(((Player) src).getUniqueId().toString(), key)));

        return CommandResult.success();
    }
}
