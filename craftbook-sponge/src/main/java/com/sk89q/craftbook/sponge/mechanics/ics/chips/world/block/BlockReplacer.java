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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.block;

import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlockReplacer extends IC {

    private Factory.BlockTypeData blockTypeData;

    private int delay;
    private int mode;
    private boolean physics;

    public BlockReplacer(ICFactory<BlockReplacer> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        delay = 20;
        mode = 0;
        physics = true;

        try {
            String[] data = RegexUtil.COLON_PATTERN.split(SignUtil.getTextRaw(lines.get(3)));
            delay = Math.max(0, Integer.parseInt(data[0]));
            if (data.length > 1)
                mode = Integer.parseInt(data[1]);
            else
                mode = 0;
            physics = data.length <= 2 || data[2].equalsIgnoreCase("1") || data[2].equalsIgnoreCase("true");
        } catch (Exception e) {
            throw new InvalidICException("Last line must be of delay:mode:physics format");
        }

        lines.set(3, Text.of(delay, ":", mode, ":", physics));

        blockTypeData = new BlockReplacer.Factory.BlockTypeData();

        Inventory offBlockInventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Enter Off Block")))
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, 1))
                .listener(InteractInventoryEvent.Close.class, close -> {
                    Inventory inventory = close.getTargetInventory();
                    inventory.peek().ifPresent(itemStack ->
                            blockTypeData.offBlock = itemStack.get(Keys.ITEM_BLOCKSTATE).orElse(itemStack.getItem().getBlock().orElse(BlockTypes.AIR).getDefaultState()));
                })
                .build(CraftBookPlugin.spongeInst().container);

        Inventory onBlockInventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Enter On Block")))
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, 1))
                .listener(InteractInventoryEvent.Close.class, close -> {
                    Inventory inventory = close.getTargetInventory();
                    inventory.peek().ifPresent(itemStack -> {
                        blockTypeData.onBlock = itemStack.get(Keys.ITEM_BLOCKSTATE).orElse(itemStack.getItem().getBlock().orElse(BlockTypes.AIR).getDefaultState());

                        player.openInventory(offBlockInventory, CraftBookPlugin.spongeInst().getCause().build());
                    });
                })
                .build(CraftBookPlugin.spongeInst().container);

        player.openInventory(onBlockInventory, CraftBookPlugin.spongeInst().getCause().build());
    }

    @Override
    public void load() {
        super.load();

        if (getLine(3).isEmpty()) {
            String[] data = RegexUtil.COLON_PATTERN.split(getLine(3));
            delay = Integer.parseInt(data[0]);
            if (data.length > 1)
                mode = Integer.parseInt(data[1]);
            else
                mode = 0;
            physics = data.length <= 2 || data[2].equalsIgnoreCase("1") || data[2].equalsIgnoreCase("true");
        } else {
            delay = 20;
            mode = 0;
            physics = true;
        }
    }

    @Override
    public void trigger() {
        if (blockTypeData.onBlock != null && blockTypeData.offBlock != null) {
            Location<World> block = getBackBlock();
            if(block.getBlock().equals(blockTypeData.onBlock)) {
                if(!getPinSet().getInput(0, this)) {
                    block.setBlock(blockTypeData.offBlock, physics ? BlockChangeFlag.ALL : BlockChangeFlag.NONE, CraftBookPlugin.spongeInst().getCause().build());
                }
            } else if (block.getBlock().equals(blockTypeData.offBlock)) {
                if (getPinSet().getInput(0, this)) {
                    block.setBlock(blockTypeData.onBlock, physics ? BlockChangeFlag.ALL : BlockChangeFlag.NONE, CraftBookPlugin.spongeInst().getCause().build());
                }
            }
            Set<Location> traversedBlocks = new HashSet<>();
            traversedBlocks.add(block);
            getPinSet().setOutput(0, replaceBlocks(getPinSet().getInput(0, this), block, traversedBlocks), this);

        }
    }

    private boolean replaceBlocks(final boolean on, final Location<World> block, final Set<Location> traversedBlocks) {
        if (traversedBlocks.size() > 15000) {
            return true;
        }

        if (mode == 0) {
            for (Direction direction : BlockUtil.getDirectFaces()) {
                final Location<World> b = block.getRelative(direction);
                if (traversedBlocks.contains(b)) {
                    continue;
                }
                traversedBlocks.add(b);

                if (b.getBlock().equals(blockTypeData.onBlock)) {
                    if (!on) {
                        b.setBlock(blockTypeData.offBlock, physics ? BlockChangeFlag.ALL : BlockChangeFlag.NONE, CraftBookPlugin.spongeInst().getCause().build());
                    }
                    Sponge.getScheduler().createTaskBuilder().delayTicks(delay).execute(() -> replaceBlocks(on, b, traversedBlocks)).submit(CraftBookPlugin.spongeInst().container);
                } else if (b.getBlock().equals(blockTypeData.offBlock)) {
                    if (on) {
                        b.setBlock(blockTypeData.onBlock, physics ? BlockChangeFlag.ALL : BlockChangeFlag.NONE, CraftBookPlugin.spongeInst().getCause().build());
                    }
                    Sponge.getScheduler().createTaskBuilder().delayTicks(delay).execute(() -> replaceBlocks(on, b, traversedBlocks)).submit(CraftBookPlugin.spongeInst().container);
                }
            }
        }

        return traversedBlocks.size() > 0;
    }

    public static class Factory extends SerializedICFactory<BlockReplacer, Factory.BlockTypeData> {

        public Factory() {
            super(BlockTypeData.class, 1);
        }

        @Override
        public BlockReplacer createInstance(Location<World> location) {
            return new BlockReplacer(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "",
                    "delay:mode:physics"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Clock"
                    },
                    new String[] {
                            "None"
                    }
            };
        }

        @Override
        public void setData(BlockReplacer ic, BlockTypeData data) {
            ic.blockTypeData = data;
        }

        @Override
        public BlockTypeData getData(BlockReplacer ic) {
            return ic.blockTypeData;
        }

        @Override
        protected Optional<BlockTypeData> buildContent(DataView container) throws InvalidDataException {
            BlockTypeData blockTypeData = new BlockTypeData();

            blockTypeData.onBlock = container.getSerializable(DataQuery.of("OnBlock"), BlockState.class).orElse(null);
            blockTypeData.offBlock = container.getSerializable(DataQuery.of("OffBlock"), BlockState.class).orElse(null);

            if (blockTypeData.onBlock == null || blockTypeData.offBlock == null) {
                return Optional.empty();
            }

            return Optional.of(blockTypeData);
        }

        public static class BlockTypeData extends SerializedICData {

            public BlockState onBlock = BlockTypes.STONEBRICK.getDefaultState();
            public BlockState offBlock = BlockTypes.COBBLESTONE.getDefaultState();

            @Override
            public int getContentVersion() {
                return 1;
            }

            @Override
            public DataContainer toContainer() {
                return super.toContainer()
                        .set(DataQuery.of("OnBlock"), this.onBlock)
                        .set(DataQuery.of("OffBlock"), this.offBlock);
            }
        }
    }
}