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
package org.enginehub.craftbook.sponge.mechanics.ics.chips.world.entity;

import com.google.common.collect.Lists;
import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.InvalidICException;
import org.enginehub.craftbook.sponge.mechanics.ics.RestrictedIC;
import org.enginehub.craftbook.sponge.mechanics.ics.SerializedICData;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.enginehub.craftbook.sponge.util.prompt.ItemStackSnapshotDataPrompt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class ItemDispenser extends IC {

    private static ItemStackSnapshotDataPrompt ITEMS_PROMPT = new ItemStackSnapshotDataPrompt(
            1, 9, "Enter Items to Dispense"
    );

    private ItemDispenser.Factory.ItemStackData itemStackData;

    public ItemDispenser(ICFactory<ItemDispenser> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        itemStackData = new ItemDispenser.Factory.ItemStackData();

        String line2 = SignUtil.getTextRaw(lines.get(2));
        if (!line2.isEmpty()) {
            Sponge.getRegistry().getType(ItemType.class, line2).ifPresent(itemType ->
                    itemStackData.itemStacks = Lists.newArrayList(itemType.getTemplate())
            );
        }

        if (itemStackData.itemStacks.isEmpty()) {
            ITEMS_PROMPT.getData(player, entityArchetypes -> {
                itemStackData.itemStacks = entityArchetypes;
                setLine(2, Text.of(itemStackData.itemStacks.get(0).getType().getName()));
            });
        }
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            if (!itemStackData.itemStacks.isEmpty()) {
                Location<World> block = getBackBlock();
                while (block.getBlockType().getProperty(MatterProperty.class).map(MatterProperty::getValue).orElse(MatterProperty.Matter.GAS) == MatterProperty.Matter.SOLID) {
                    if (block.getY() >= 255) {
                        break;
                    }
                    block = block.getRelative(Direction.UP);
                }
                for (ItemStackSnapshot stack : itemStackData.itemStacks) {
                    Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, block.getPosition());
                    item.offer(Keys.REPRESENTED_ITEM, stack);
                    block.getExtent().spawnEntity(item);
                }
            }
        }
    }

    public static class Factory extends SerializedICFactory<ItemDispenser, Factory.ItemStackData> implements RestrictedIC {

        public Factory() {
            super(ItemDispenser.Factory.ItemStackData.class, 1);
        }

        @Override
        public ItemDispenser createInstance(Location<World> location) {
            return new ItemDispenser(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "item type, or blank to set later",
                    ""
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
        public void setData(ItemDispenser ic, ItemDispenser.Factory.ItemStackData data) {
            ic.itemStackData = data;
        }

        @Override
        public ItemDispenser.Factory.ItemStackData getData(ItemDispenser ic) {
            return ic.itemStackData;
        }

        @Override
        protected Optional<ItemDispenser.Factory.ItemStackData> buildContent(DataView container) throws InvalidDataException {
            ItemDispenser.Factory.ItemStackData itemStackData = new ItemDispenser.Factory.ItemStackData();

            itemStackData.itemStacks = container.getSerializableList(DataQuery.of("ItemStacks"), ItemStackSnapshot.class).orElse(Lists.newArrayList());

            return Optional.of(itemStackData);
        }

        public static class ItemStackData extends SerializedICData {

            public List<ItemStackSnapshot> itemStacks = Lists.newArrayList();

            @Override
            public int getContentVersion() {
                return 1;
            }

            @Override
            public DataContainer toContainer() {
                return super.toContainer()
                        .set(DataQuery.of("ItemStacks"), this.itemStacks);
            }
        }
    }
}