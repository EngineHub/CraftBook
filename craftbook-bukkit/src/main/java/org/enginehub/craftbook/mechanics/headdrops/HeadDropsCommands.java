/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.enginehub.craftbook.CraftBook;
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

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
            commandManager,
            HeadDropsCommandsRegistration.builder(),
            new HeadDropsCommands()
        );
    }

    public HeadDropsCommands() {
    }

    @Command(name = "give", desc = "Gives the player the headdrops item.")
    @CommandPermissions({ "craftbook.mech.headdrops.give" })
    public void giveItem(Actor actor,
                         @Arg(desc = "The entity type") com.sk89q.worldedit.world.entity.EntityType entityType,
                         @ArgFlag(name = 'p', desc = "The player to target") String otherPlayer,
                         @ArgFlag(name = 'a', desc = "Amount to give", def = "1") int amount,
                         @Switch(name = 's', desc = "Silence output") boolean silent
    ) throws AuthorizationException,
        CraftBookException {
        Player player;

        if (otherPlayer != null)
            player = Bukkit.getPlayer(otherPlayer);
        else if (!(actor instanceof CraftBookPlayer))
            throw new CraftBookException("Please provide a player! (-p flag)");
        else
            player = ((BukkitCraftBookPlayer) actor).getPlayer();

        if (player == null)
            throw new CraftBookException("Unknown Player!");

        if (HeadDrops.instance == null)
            throw new CraftBookException("HeadDrops are not enabled!");

        if (!actor.hasPermission("craftbook.mech.headdrops.give" + (otherPlayer != null ? ".others" : "") + '.' + entityType.getId()))
            throw new AuthorizationException();

        HeadDrops.MobSkullType skullType = HeadDrops.MobSkullType.getFromEntityType(BukkitAdapter.adapt(entityType));
        if (skullType == null)
            throw new CraftBookException("Invalid Skull Type!");

        String mobName = skullType.getPlayerName();

        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, amount);
        ItemMeta metaD = stack.getItemMeta();
        if (metaD instanceof SkullMeta) {
            SkullMeta itemMeta = (SkullMeta) metaD;
            itemMeta.setDisplayName(ChatColor.RESET + entityType.getName().toUpperCase() + " Head");
            itemMeta.setOwner(mobName);
            stack.setItemMeta(itemMeta);
        } else {
            CraftBook.logger.warn("Bukkit has failed to set a HeadDrop item to a head!");
        }

        if (!player.getInventory().addItem(stack).isEmpty()) {
            throw new CraftBookException("Failed to add item to inventory!");
        }

        if (!silent)
            actor.print("Gave HeadDrop for " + ChatColor.BLUE + entityType.getName() + ChatColor.YELLOW + " to " + player.getName());
    }
}
