/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.entity;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.prompt.EntityArchetypeDataPrompt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class EntitySpawner extends IC {

    private static EntityArchetypeDataPrompt ENTITY_TYPE_PROMPT = new EntityArchetypeDataPrompt(
            1, 9, "Enter Entity Type"
    );

    private Factory.EntityTypeData entityTypeData;

    private int amount;

    public EntitySpawner(ICFactory<EntitySpawner> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        amount = 1;

        try {
            amount = Integer.parseInt(SignUtil.getTextRaw(lines.get(3)));
        } catch (Exception e) {
        }

        lines.set(3, Text.of(amount));

        entityTypeData = new Factory.EntityTypeData();

        String line2 = SignUtil.getTextRaw(lines.get(2));
        if (!line2.isEmpty()) {
            Sponge.getRegistry().getType(EntityType.class, line2).ifPresent(entityType ->
                    entityTypeData.entityTypes = Lists.newArrayList(EntityArchetype.of(entityType))
            );
        }

        if (entityTypeData.entityTypes.isEmpty()) {
            ENTITY_TYPE_PROMPT.getData(player, entityArchetypes -> {
                entityTypeData.entityTypes = entityArchetypes;
                setLine(2, Text.of(entityTypeData.entityTypes.get(0).getType().getName()));
            });
        }
    }

    @Override
    public void load() {
        super.load();

        if (!getLine(3).isEmpty()) {
            amount = Integer.parseInt(getLine(3));
        } else {
            amount = 1;
        }
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            if (!entityTypeData.entityTypes.isEmpty()) {
                Location<World> block = getBackBlock();
                while (block.getBlockType().getProperty(MatterProperty.class).map(MatterProperty::getValue).orElse(MatterProperty.Matter.GAS) == MatterProperty.Matter.SOLID) {
                    if (block.getY() >= 255) {
                        break;
                    }
                    block = block.getRelative(Direction.UP);
                }
                for (EntityArchetype type : entityTypeData.entityTypes) {
                    for (int i = 0; i < amount; i++) {
                        type.apply(block);
                    }
                }
            }
        }
    }

    public static class Factory extends SerializedICFactory<EntitySpawner, Factory.EntityTypeData> {

        public Factory() {
            super(EntityTypeData.class, 1);
        }

        @Override
        public EntitySpawner createInstance(Location<World> location) {
            return new EntitySpawner(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "entity type, or blank to set later",
                    "amount"
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
        public void setData(EntitySpawner ic, EntityTypeData data) {
            ic.entityTypeData = data;
        }

        @Override
        public EntityTypeData getData(EntitySpawner ic) {
            return ic.entityTypeData;
        }

        @Override
        protected Optional<EntityTypeData> buildContent(DataView container) throws InvalidDataException {
            EntityTypeData entityTypeData = new EntityTypeData();

            entityTypeData.entityTypes = container.getSerializableList(DataQuery.of("EntityTypes"), EntityArchetype.class).orElse(Lists.newArrayList());

            return Optional.of(entityTypeData);
        }

        public static class EntityTypeData extends SerializedICData {

            public List<EntityArchetype> entityTypes = Lists.newArrayList();

            @Override
            public int getContentVersion() {
                return 1;
            }

            @Override
            public DataContainer toContainer() {
                return super.toContainer()
                        .set(DataQuery.of("EntityTypes"), this.entityTypes);
            }
        }
    }
}