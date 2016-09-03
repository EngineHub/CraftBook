package com.sk89q.craftbook.sponge.mechanics.treelopper.command;

import com.sk89q.craftbook.sponge.mechanics.treelopper.TreeLopper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class ToggleCommand implements CommandExecutor {

    private TreeLopper treeLopper;

    public ToggleCommand(TreeLopper treeLopper) {
        this.treeLopper = treeLopper;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            return CommandResult.empty();
        }
        boolean currentState = treeLopper.disabledPlayers.getValue().contains(((Player) src).getUniqueId());
        if (args.hasAny("state")) {
            currentState = args.<Boolean>getOne("state").get();
        } else {
            currentState = !currentState;
        }

        treeLopper.disabledPlayers.getValue().remove(((Player) src).getUniqueId());
        if (!currentState) {
            treeLopper.disabledPlayers.getValue().add(((Player) src).getUniqueId());
        }

        return CommandResult.success();
    }
}
