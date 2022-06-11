/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.headdrops;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitCraftBookPlayer;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class HeadDropsCommands {

    private final HeadDrops headDrops;

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration, HeadDrops headDrops) {
        registration.register(
            commandManager,
            HeadDropsCommandsRegistration.builder(),
            new HeadDropsCommands(headDrops)
        );
    }

    private HeadDropsCommands(HeadDrops headDrops) {
        this.headDrops = headDrops;
    }

    @Command(
        name = "give",
        desc = "Gives the player the headdrops item."
    )
    @CommandPermissions({ "craftbook.headdrops.give" })
    public void giveItem(Actor actor,
                         @Arg(name = "Entity Type", desc = "The entity type to spawn the head of") com.sk89q.worldedit.world.entity.EntityType entityType,
                         @ArgFlag(name = 'p', desc = "The player to target") String otherPlayer,
                         @ArgFlag(name = 'a', desc = "Amount to give", def = "1") int amount,
                         @Switch(name = 's', desc = "Silence output") boolean silent
    ) throws AuthorizationException,
        CraftBookException {
        Player player;

        if (otherPlayer != null) {
            player = Bukkit.getPlayer(otherPlayer);
        } else if (!(actor instanceof CraftBookPlayer)) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.command.player-required"));
        } else {
            player = ((BukkitCraftBookPlayer) actor).getPlayer();
        }

        if (player == null) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.command.unknown-player"));
        }

        if (!actor.hasPermission("craftbook.headdrops.give" + (otherPlayer != null ? ".others" : "") + '.' + entityType.getId())) {
            throw new AuthorizationException();
        }

        ItemStack toDrop = headDrops.createFromEntityType(BukkitAdapter.adapt(entityType));
        if (toDrop != null) {
            toDrop.setAmount(amount);
        }

        if (toDrop == null || !player.getInventory().addItem(toDrop).isEmpty()) {
            // TODO Re-enable word-casing of mob names.
            actor.printError(TranslatableComponent.of(
                "craftbook.headdrops.give.failed",
                TextComponent.of(player.getName(), TextColor.WHITE),
                TextComponent.of(entityType.getName().replace("_ ", " "), TextColor.WHITE)
            ));
            return;
        }

        if (!silent) {
            // TODO Re-enable word-casing of mob names.
            actor.printInfo(TranslatableComponent.of(
                "craftbook.headdrops.give",
                TextComponent.of(entityType.getName().replace("_ ", " "), TextColor.WHITE),
                TextComponent.of(player.getName(), TextColor.WHITE)
            ));
        }
    }
}
