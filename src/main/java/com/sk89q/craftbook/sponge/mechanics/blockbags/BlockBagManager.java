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
package com.sk89q.craftbook.sponge.mechanics.blockbags;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.blockbags.data.EmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.mechanics.blockbags.data.EmbeddedBlockBagDataBuilder;
import com.sk89q.craftbook.sponge.mechanics.blockbags.data.ImmutableEmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Module(id = "blockbag", name = "BlockBag", onEnable="onInitialize", onDisable="onDisable")
public class BlockBagManager extends SpongeMechanic {

    public static Key<Value<EmbeddedBlockBag>> EMBEDDED_BLOCK_BAG = makeSingleKey(new TypeToken<EmbeddedBlockBag>() {},
            new TypeToken<Value<EmbeddedBlockBag>>() {}, of("EmbeddedBlockBag"), "craftbook:embeddedblockbag",
            "EmbeddedBlockBag");

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private Set<BlockBag> blockBags = new HashSet<>();

    private Random random = new Random();

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        Sponge.getDataManager().registerBuilder(EmbeddedBlockBag.class, new EmbeddedBlockBag.EmbeddedBlockBagBuilder());
        Sponge.getDataManager().register(EmbeddedBlockBagData.class, ImmutableEmbeddedBlockBagData.class, new EmbeddedBlockBagDataBuilder());
    }

    private long getUnusedID() {
        long id = random.nextLong();
        while(getBlockBag(id) != null) {
            id = random.nextLong();
        }
        return id;
    }

    public BlockBag getBlockBag(long id) {
        return blockBags.stream().filter(bag -> bag.getId() == id).findFirst().orElse(null);
    }

    public BlockBag getBlockBag(UUID creator, String name) {
        return blockBags.stream().filter(bag -> bag.getSimpleName().equals(name))
                .filter(bag -> bag.getCreator().equals(creator))
                .findFirst().orElse(null);
    }

    public void createBlockBag(Player creator, BlockBag blockBag) {
        if(getBlockBag(creator.getUniqueId(), blockBag.getSimpleName()) != null) {
            creator.sendMessage(Text.of(TextColors.RED, "A blockbag with this name already exists!"));
            return;
        }

        blockBag.setId(getUnusedID());

        blockBags.add(blockBag);
    }
}
