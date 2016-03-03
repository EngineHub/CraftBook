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
package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashSet;
import java.util.Set;

@Module(moduleName = "Gate", onEnable="onInitialize", onDisable="onDisable")
public class Gate extends SimpleArea implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> searchRadius = new ConfigValue<>("search-radius", "The maximum area around the sign the gate can search.", 5);

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);
        super.registerCommonPermissions();

        searchRadius.load(config);
    }

    @Override
    public void onDisable() {
        super.saveCommonConfig(config);

        searchRadius.save(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Humanoid human) {

        super.onPlayerInteract(event, human);

        if (event.getTargetBlock().getLocation().get().getBlockType() == BlockTypes.FENCE) {

            int x = event.getTargetBlock().getLocation().get().getBlockX();
            int y = event.getTargetBlock().getLocation().get().getBlockY();
            int z = event.getTargetBlock().getLocation().get().getBlockZ();

            for (int x1 = x - searchRadius.getValue(); x1 <= x + searchRadius.getValue(); x1++) {
                for (int y1 = y - searchRadius.getValue(); y1 <= y + searchRadius.getValue() * 2; y1++) {
                    for (int z1 = z - searchRadius.getValue(); z1 <= z + searchRadius.getValue(); z1++) {

                        if (SignUtil.isSign(event.getTargetBlock().getLocation().get().getExtent().getLocation(x1, y1, z1))) {

                            Sign sign = (Sign) event.getTargetBlock().getLocation().get().getExtent().getLocation(x1, y1, z1).getTileEntity().get();

                            triggerMechanic(event.getTargetBlock().getLocation().get().getExtent().getLocation(x1, y1, z1), sign, human, null);
                        }
                    }
                }
            }
        }
    }

    public void findColumns(Location block, Set<GateColumn> columns) {

        int x = block.getBlockX();
        int y = block.getBlockY();
        int z = block.getBlockZ();

        for (int x1 = x - searchRadius.getValue(); x1 <= x + searchRadius.getValue(); x1++) {
            for (int y1 = y - searchRadius.getValue(); y1 <= y + searchRadius.getValue() * 2; y1++) {
                for (int z1 = z - searchRadius.getValue(); z1 <= z + searchRadius.getValue(); z1++) {

                    searchColumn(block.getExtent().getLocation(x1, y1, z1), columns);
                }
            }
        }
    }

    public void searchColumn(Location block, Set<GateColumn> columns) {

        int y = block.getBlockY();

        if (block.getExtent().getBlock(block.getBlockX(), y, block.getBlockZ()).getType() == BlockTypes.FENCE) {

            GateColumn column = new GateColumn(block);

            columns.add(column);

            Location temp = column.topBlock;

            while (temp.getBlockType() == BlockTypes.FENCE || temp.getBlockType() == BlockTypes.AIR) {
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.NORTH)))) searchColumn(temp.getRelative(Direction.NORTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.SOUTH)))) searchColumn(temp.getRelative(Direction.SOUTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.EAST)))) searchColumn(temp.getRelative(Direction.EAST), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.WEST)))) searchColumn(temp.getRelative(Direction.WEST), columns);

                temp = temp.getRelative(Direction.DOWN);
            }
        }
    }

    public void toggleColumn(Location block, boolean on) {

        Direction dir = Direction.DOWN;

        block = block.getRelative(dir);

        if (on) {
            while (block.getBlockType() == BlockTypes.AIR) {
                block.setBlockType(BlockTypes.FENCE);
                block = block.getRelative(dir);
            }
        } else {
            while (block.getBlockType() == BlockTypes.FENCE) {
                block.setBlockType(BlockTypes.AIR);
                block = block.getRelative(dir);
            }
        }
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Humanoid human, Boolean forceState) {

        if (SignUtil.getTextRaw(sign, 1).equals("[Gate]")) {

            Set<GateColumn> columns = new HashSet<>();

            findColumns(block, columns);

            if (columns.size() > 0) {
                Boolean on = forceState;
                for (GateColumn vec : columns) {
                    Location col = vec.getBlock();
                    if (on == null) {
                        on = col.getRelative(Direction.DOWN).getBlockType() != BlockTypes.FENCE;
                    }
                    toggleColumn(col, on);
                }
            } else {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Can't find a gate!").build());
            }
        } else return false;

        return true;
    }

    @Override
    public String getPath() {
        return "mechanics/gate";
    }

    @Override
    public String[] getMainDocumentation() {
        return new String[]{
                "=====",
                "Gates",
                "====="
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowedBlocks,
                searchRadius
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                createPermissions
        };
    }

    public static class GateColumn {

        Location topBlock;

        public GateColumn(Extent extent, int x, int y, int z) {
            this(extent.getLocation(x, y, z));
        }

        public GateColumn(Location topBlock) {

            while (topBlock.getBlockType() == BlockTypes.FENCE) {
                topBlock = topBlock.getRelative(Direction.UP);
            }

            topBlock = topBlock.getRelative(Direction.DOWN);

            this.topBlock = topBlock;
        }

        public Location getBlock() {
            return topBlock;
        }

        @Override
        public int hashCode() {
            return topBlock.getExtent().hashCode() + topBlock.getBlockX() + topBlock.getBlockZ();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GateColumn && ((GateColumn) o).topBlock.getX() == topBlock.getX() && ((GateColumn) o).topBlock.getZ() == topBlock.getZ();

        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Gate]"};
    }

    @Override
    public Set<BlockFilter> getDefaultBlocks() {
        Set<BlockFilter> states = Sets.newHashSet();
        states.add(new BlockFilter("FENCE"));
        states.add(new BlockFilter("NETHER_BRICK_FENCE"));
        states.add(new BlockFilter("GLASS_PANE"));
        states.add(new BlockFilter("STAINED_GLASS_PANE"));
        states.add(new BlockFilter("IRON_BARS"));
        return states;
    }
}
