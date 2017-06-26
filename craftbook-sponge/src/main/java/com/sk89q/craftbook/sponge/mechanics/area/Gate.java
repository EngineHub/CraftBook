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
package com.sk89q.craftbook.sponge.mechanics.area;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.MultiBlockBag;
import com.sk89q.craftbook.sponge.util.data.mutable.EmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Module(id = "gate", name = "Gate", onEnable="onInitialize", onDisable="onDisable")
public class Gate extends SimpleArea implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> searchRadius = new ConfigValue<>("search-radius", "The maximum area around the sign the gate can search.", 5);
    private ConfigValue<Boolean> indirectAccess = new ConfigValue<>("indirect-access", "Allows toggling of gates by clicking the gate material "
            + "rather than the sign.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);
        super.registerCommonPermissions();

        searchRadius.load(config);
        indirectAccess.load(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Humanoid human) {
        super.onPlayerInteract(event, human);

        if (indirectAccess.getValue()) {
            event.getTargetBlock().getLocation().ifPresent((location) -> {
                if (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), location.getBlock())) {
                    if (((human instanceof Subject) && !usePermissions.hasPermission((Subject) human))) {
                        if (human instanceof CommandSource)
                            ((CommandSource) human).sendMessage(TranslationsManager.USE_PERMISSIONS);
                        return;
                    }

                    int x = location.getBlockX();
                    int y = location.getBlockY();
                    int z = location.getBlockZ();

                    for (int x1 = x - searchRadius.getValue(); x1 <= x + searchRadius.getValue(); x1++) {
                        for (int y1 = y - searchRadius.getValue(); y1 <= y + searchRadius.getValue() * 2; y1++) {
                            for (int z1 = z - searchRadius.getValue(); z1 <= z + searchRadius.getValue(); z1++) {
                                Location<World> searchLocation = location.getExtent().getLocation(x1, y1, z1);
                                Optional tileEntity = searchLocation.getTileEntity();

                                if (SignUtil.isSign(searchLocation) && tileEntity.isPresent()
                                        && "[Gate]".equals(SignUtil.getTextRaw((Sign) tileEntity.get(), 1))) {
                                    Set<GateColumn> columns = new HashSet<>();
                                    BlockState state = findColumns(location, columns, location.getBlock());
                                    toggleColumns(state, (Sign) tileEntity.get(), human, columns, null);
                                    return;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private BlockState findColumns(Location<World> block, Set<GateColumn> columns, BlockState state) {
        int x = block.getBlockX();
        int y = block.getBlockY();
        int z = block.getBlockZ();

        Location<World> closestColumn = null;
        Vector3d blockFlat = new Vector3d(block.getX(), 0, block.getZ());

        for (int x1 = x - searchRadius.getValue(); x1 <= x + searchRadius.getValue(); x1++) {
            for (int y1 = y - searchRadius.getValue(); y1 <= y + searchRadius.getValue() * 2; y1++) {
                for (int z1 = z - searchRadius.getValue(); z1 <= z + searchRadius.getValue(); z1++) {
                    if (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), block.getExtent().getBlock(x1, y1, z1))) {
                        if(closestColumn == null)
                            closestColumn = block.getExtent().getLocation(x1, y1, z1);
                        else {
                            Vector3d oldClosest = new Vector3d(closestColumn.getX(), 0, closestColumn.getZ());
                            Vector3d test = new Vector3d(x1, 0, z1);

                            if(blockFlat.distanceSquared(test) < blockFlat.distanceSquared(oldClosest))
                                closestColumn = block.getExtent().getLocation(x1, y1, z1);
                        }
                    }
                }
            }
        }

        if(closestColumn != null)
            state = searchColumn(closestColumn, columns, state);

        return state;
    }

    private BlockState searchColumn(Location<World> block, Set<GateColumn> columns, BlockState state) {
        if (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), block.getBlock())) {
            GateColumn column = new GateColumn(block, allowedBlocks);

            Location<World> temp = column.topBlock;

            if(temp.getBlockType() != BlockTypes.AIR) {
                if(state == null)
                    state = temp.getBlock();
                if(state.equals(temp.getBlock()))
                    columns.add(column);
                else
                    return state;
            }

            while (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), temp.getBlock()) || temp.getBlockType() == BlockTypes.AIR) {
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.NORTH), allowedBlocks))) state = searchColumn(temp.getRelative(Direction.NORTH), columns, state);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.SOUTH), allowedBlocks))) state = searchColumn(temp.getRelative(Direction.SOUTH), columns, state);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.EAST), allowedBlocks))) state = searchColumn(temp.getRelative(Direction.EAST), columns, state);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.WEST), allowedBlocks))) state = searchColumn(temp.getRelative(Direction.WEST), columns, state);

                temp = temp.getRelative(Direction.DOWN);
            }
        }

        return state;
    }

    private void toggleColumn(Location<World> block, Sign sign, @Nullable Humanoid human, boolean on, BlockState gateType) {
        Direction dir = Direction.DOWN;

        block = block.getRelative(dir);

        ItemStack blockBagItem = ItemStack.builder().fromBlockState(gateType).quantity(1).build();
        BlockBag blockBag = getBlockBag(sign.getLocation());

        if (on) {
            while (block.getBlockType() == BlockTypes.AIR) {
                if (blockBag.has(Lists.newArrayList(blockBagItem.copy()))) {
                    if (blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                        block.setBlock(gateType, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
                        block = block.getRelative(dir);
                    }
                } else {
                    if (human != null && human instanceof CommandSource) {
                        ((CommandSource) human).sendMessage(Text.of("Out of Blocks"));
                    }
                    break;
                }
            }
        } else {
            while (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), block.getBlock())) {
                for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                    Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                    item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                    block.getExtent().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                }
                block.setBlockType(BlockTypes.AIR, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
                block = block.getRelative(dir);
            }
        }

        if (blockBag instanceof EmbeddedBlockBag) {
            sign.getLocation().offer(new EmbeddedBlockBagData((EmbeddedBlockBag) blockBag));
        }
    }

    @Override
    public boolean triggerMechanic(Location<World> block, Sign sign, Humanoid human, Boolean forceState) {
        if ("[Gate]".equals(SignUtil.getTextRaw(sign, 1))) {

            Set<GateColumn> columns = new HashSet<>();

            BlockState state = findColumns(block, columns, null);

            if (!columns.isEmpty()) {
                toggleColumns(state, sign, human, columns, forceState);
            } else {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Can't find a gate!").build());
            }
        } else return false;

        return true;
    }

    private void toggleColumns(BlockState state, Sign sign, @Nullable Humanoid human, Set<GateColumn> columns, Boolean forceState) {
        if (state == null) {
            state = BlockTypes.FENCE.getDefaultState();
        }
        for (GateColumn vec : columns) {
            Location<World> col = vec.getBlock();
            if (forceState == null) {
                forceState = !BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), col.getRelative(Direction.DOWN).getBlock());
            }
            toggleColumn(col, sign, human, forceState, state);
        }
    }

    @Override
    public BlockBag getBlockBag(Location<World> location) {
        BlockBag mainBlockBag = super.getBlockBag(location);
        Location<World> next = BlockUtil.getNextMatchingSign(location, SignUtil.getBack(location), 2, this::isMechanicSign);
        if (next != null) {
            BlockBag nextBlockBag = super.getBlockBag(next);
            if (nextBlockBag != null) {
                return new MultiBlockBag(mainBlockBag, nextBlockBag);
            }
        }

        return mainBlockBag;
    }

    @Override
    public String getPath() {
        return "mechanics/gate";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowedBlocks,
                allowRedstone,
                searchRadius,
                indirectAccess
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                createPermissions,
                usePermissions
        };
    }

    private static final class GateColumn {

        Location<World> topBlock;

        GateColumn(Location<World> topBlock, ConfigValue<List<BlockFilter>> allowedBlocks) {
            while (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), topBlock.getBlock())) {
                topBlock = topBlock.getRelative(Direction.UP);
            }

            topBlock = topBlock.getRelative(Direction.DOWN);

            this.topBlock = topBlock;
        }

        public Location<World> getBlock() {
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
    public List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.add(new BlockFilter("FENCE"));
        states.add(new BlockFilter("NETHER_BRICK_FENCE"));
        states.add(new BlockFilter("GLASS_PANE"));
        states.add(new BlockFilter("STAINED_GLASS_PANE"));
        states.add(new BlockFilter("IRON_BARS"));
        return states;
    }
}
