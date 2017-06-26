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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

@Module(id = "bookshelf", name = "Bookshelf", onEnable="onInitialize", onDisable="onDisable")
public class Bookshelf extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.bookshelf.use", "Allows the user to use the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<Boolean> readWhenHoldingBlock = new ConfigValue<>("read-with-block",
            "Whether to allow the player to read a book whilst holding a block", false);
    private ConfigValue<TernaryState> sneakState = new ConfigValue<>("sneak-state", "Sets how the player must be sneaking in order to use a bookshelf.", TernaryState.FALSE,
            TypeToken.of(TernaryState.class));

    private String[] bookLines;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        Asset books = CraftBookPlugin.spongeInst().getContainer().getAsset("bookshelf/books.txt").get();
        Path path = new File(CraftBookPlugin.inst().getWorkingDirectory(), "bookshelf/books.txt").toPath();

        new File(CraftBookPlugin.inst().getWorkingDirectory(), "bookshelf").mkdirs();

        if (Files.notExists(path)) {
            try {
                books.copyToFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bookLines = Files.readAllLines(path).toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        readWhenHoldingBlock.load(config);
        sneakState.load(config);

        usePermissions.register();
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getLocation().filter((this::isValid)).ifPresent(location -> {
            if (!sneakState.getValue().doesPass(player.get(Keys.IS_SNEAKING).orElse(false) || !usePermissions.hasPermission(player))) {
                return; //Don't alert the player with this mechanic.
            }

            if (!readWhenHoldingBlock.getValue()) {
                ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).filter((itemStack -> itemStack.getItem().getBlock().isPresent())).orElse(null);
                if (stack != null)
                    return;
            }
            String line = bookLines[ThreadLocalRandom.current().nextInt(bookLines.length)];

            player.sendMessage(Text.of(TextColors.YELLOW, "You pick up a book..."));
            player.sendMessage(Text.of(line));
        });
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.BOOKSHELF;
    }

    @Override
    public String getPath() {
        return "mechanics/bookshelf";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                usePermissions
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                readWhenHoldingBlock,
                sneakState
        };
    }
}
