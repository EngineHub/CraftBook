/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Module(moduleId = "paintingswitcher", moduleName = "PaintingSwitcher", onEnable="onInitialize", onDisable="onDisable")
public class PaintingSwitcher extends SpongeMechanic implements DocumentationProvider {

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.paintingswitcher.use", "Allows the user to switch paintings.", PermissionDescription.ROLE_USER);

    // <Player, Painting>
    private BiMap<UUID, UUID> paintings = HashBiMap.create();

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        usePermissions.register();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        paintings.clear();
    }

    @Listener
    public void onPlayerClick(InteractEntityEvent.Secondary.MainHand event, @First Player player, @Getter("getTargetEntity") Painting painting) {
        if (usePermissions.hasPermission(player)) {
            if (paintings.containsKey(player.getUniqueId()) && paintings.get(player.getUniqueId()).equals(painting.getUniqueId())) {
                paintings.remove(player.getUniqueId());
                player.sendMessage(Text.of(TextColors.YELLOW, "Finished editing painting."));
            } else {
                paintings.put(player.getUniqueId(), painting.getUniqueId());
                player.sendMessage(Text.of(TextColors.YELLOW, "Editing painting."));
            }

            event.setCancelled(true);
        } else {
            player.sendMessage(Text.of(TextColors.RED, "You don't have permission to edit paintings!"));
        }
    }

    @Listener
    public void onInventoryChange(ChangeInventoryEvent.Held event, @First Player player) {
        getPainting(player).ifPresent(painting -> {
            int offset = 1; // TODO when this is supported.
            List<Art> artsCollection = new ArrayList<>(Sponge.getRegistry().getAllOf(Art.class));

            int current = artsCollection.indexOf(painting.art().get());

            current += offset;
            current %= artsCollection.size();

            System.out.println(current);

            while (!painting.offer(Keys.ART, artsCollection.get(current)).isSuccessful()) {
                current += offset;
                current %= artsCollection.size();
            }
        });
    }

    private Optional<Painting> getPainting(Player player) {
        return player.getWorld().getEntity(paintings.get(player.getUniqueId())).filter(entity -> entity instanceof Painting).map(entity -> (Painting) entity);
    }

    @Override
    public String getPath() {
        return "mechanics/painting_switcher";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                usePermissions
        };
    }
}
