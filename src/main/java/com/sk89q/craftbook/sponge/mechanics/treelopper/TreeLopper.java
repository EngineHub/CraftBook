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
package com.sk89q.craftbook.sponge.mechanics.treelopper;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.treelopper.command.ToggleCommand;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.type.BlockFilterListTypeToken;
import com.sk89q.craftbook.sponge.util.type.UUIDListTypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Module(moduleName = "TreeLopper", onEnable="onInitialize", onDisable="onDisable")
public class TreeLopper extends SpongeMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that are logs.", getDefaultBlocks(), new BlockFilterListTypeToken());
    public ConfigValue<List<UUID>> disabledPlayers = new ConfigValue<>("disabled-users", "A list of users that have disabled the mechanic.", new ArrayList<>(), new UUIDListTypeToken());

    private SpongePermissionNode togglePermission = new SpongePermissionNode("craftbook.treelopper.toggle", "Allows the user to toggle TreeLopper on and off.", PermissionDescription.ROLE_USER);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowedBlocks.load(config);
        disabledPlayers.load(config);

        togglePermission.register();

        CommandSpec toggleCommand = CommandSpec.builder()
                .description(Text.of("Toggles TreeLopper being enabled."))
                .permission(togglePermission.getNode())
                .arguments(GenericArguments.optional(GenericArguments.bool(Text.of("state"))))
                .executor(new ToggleCommand(this))
                .build();

        CommandSpec treeLopperCommand = CommandSpec.builder()
                .description(Text.of("Base TreeLopper command"))
                .child(toggleCommand, "toggle")
                .build();

        Sponge.getCommandManager().register(CraftBookPlugin.inst(), treeLopperCommand, "treelopper", "timber");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        disabledPlayers.save(config);
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Named(NamedCause.SOURCE) Player player) {
        event.getTransactions().stream().filter((t) -> BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), t.getOriginal().getState())).forEach((transaction) -> {
            checkBlocks(transaction.getOriginal().getLocation().get(), player, new ArrayList<>());
        });
    }

    private void checkBlocks(Location<World> block, Player player, List<Location> traversed) {
        if(traversed.contains(block)) return;

        traversed.add(block);

        Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, block.getBlockPosition().add(0.5, 0.5, 0.5)).get();
        item.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().fromBlockState(block.getBlock()).build().createSnapshot());
        block.getExtent().spawnEntity(item, Cause.of(NamedCause.source(CraftBookPlugin.inst())));

        block.setBlockType(BlockTypes.AIR);

        for(Direction dir : BlockUtil.getDirectFaces()) {
            if (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), block.getRelative(dir).getBlock()))
                checkBlocks(block.getRelative(dir), player, traversed);
        }
    }

    private static List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.add(new BlockFilter("LOG"));
        states.add(new BlockFilter("LOG2"));
        return states;
    }
}
