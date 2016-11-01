/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;

@Module(moduleName = "Bookshelf", onEnable="onInitialize", onDisable="onDisable")
public class ChunkAnchor extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermission = new SpongePermissionNode("craftbook.chunkanchor", "Allows creation of the Chunk Anchor", PermissionDescription.ROLE_ADMIN);

    private ConfigValue<Boolean> checkChunks = new ConfigValue<>("check-chunks", "Whether to check for existing chunk anchors in the chunk before placing.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        checkChunks.load(config);

        createPermission.register();
    }

    @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable  Player player) {
        if(checkChunks.getValue()) {
            for(TileEntity tile : location.getExtent().getChunkAtBlock(location.getBlockPosition()).get().getTileEntities()) {
                if(tile instanceof Sign) {
                    Sign s = (Sign) tile;
                    if(isMechanicSign(s)) {
                        if (player != null) {
                            player.sendMessage(Text.of(TextColors.RED, "A chunk anchor already exists in this chunk!"));
                        }
                        return false;
                    }
                }
            }
        }

        return super.verifyLines(location, lines, player);
    }

    @Listener
    public void onChunkUnload(UnloadChunkEvent event) {
        for(TileEntity tile : event.getTargetChunk().getTileEntities()) {
            if(tile instanceof Sign) {
                Sign s = (Sign) tile;
                if(isMechanicSign(s)) {

                    // TODO Prevent unloading.

                    break;
                }
            }
        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Chunk]"
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermission
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                checkChunks
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermission;
    }

    @Override
    public String getPath() {
        return "mechanics/chunk_anchor";
    }
}
