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
package com.sk89q.craftbook.sponge.mechanics.blockbags;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Module(moduleName = "BlockBag", onEnable="onInitialize", onDisable="onDisable")
public class BlockBagManager extends SpongeMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private BlockBag[] blockBags;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        try {
            List<String> blockBagList = config.getNode("blockbags").getList(TypeToken.of(String.class));
            blockBags = new BlockBag[blockBagList.size()];
            for(int i = 0; i < blockBagList.size(); i++) {
                blockBags[i] = BlockBag.createFromString(blockBagList.get(i));
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        List<String> blockBagList = new ArrayList<>();
        for(BlockBag blockBag : blockBags)
            blockBagList.add(blockBag.toString());

        config.getNode("blockbags").setValue(blockBagList);
    }

    private Random random = new Random();

    private long getUnusedID() {
        long id = random.nextLong();
        while(getBlockBag(id) != null)
            id = random.nextLong();
        return id;
    }

    public BlockBag getBlockBag(long id) {
        for(BlockBag bag : blockBags)
            if(bag.blockBagId == id)
                return bag;
        return null;
    }

    public BlockBag getBlockBag(String name) {
        for(BlockBag bag : blockBags)
            if(bag.simpleName.equals(name))
                return bag;
        return null;
    }

    public void createBlockBag(Player creator, BlockBag blockBag) {
        if(getBlockBag(blockBag.simpleName) != null) {
            creator.sendMessage(Text.of(TextColors.RED, "A blockbag with this name already exists!"));
            return;
        }

        blockBag.blockBagId = getUnusedID();

        blockBags = Arrays.copyOf(blockBags, blockBags.length + 1);
        blockBags[blockBags.length-1] = blockBag;
    }
}
