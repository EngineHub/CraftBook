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

package org.enginehub.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.BlockParser;

import java.util.Locale;

public abstract class SetBlock extends AbstractSelfTriggeredIC {

    public SetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    BlockStateHolder item;
    String force;

    @Override
    public void load() {

        force = getSign().getLine(3).toUpperCase(Locale.ENGLISH).trim();

        item = BlockParser.getBlock(getLine(2));
    }

    public void onTrigger() {

        Block body = getBackBlock();

        doSet(body, item, force.equals("FORCE"));
    }

    @Override
    public void trigger(ChipState chip) {

        chip.setOutput(0, chip.getInput(0));

        onTrigger();
    }

    @Override
    public void think(ChipState chip) {

        onTrigger();
    }

    protected abstract void doSet(Block block, BlockStateHolder blockData, boolean force);

    public boolean takeFromChest(Block bl, ItemType item) {

        if (bl.getType() != Material.CHEST) {
            return false;
        }
        Material material = BukkitAdapter.adapt(item);
        BlockState state = bl.getState();
        if (!(state instanceof Chest)) {
            return false;
        }
        Chest c = (Chest) state;
        ItemStack[] is = c.getInventory().getContents();
        for (int i = 0; i < is.length; i++) {
            final ItemStack stack = is[i];
            if (stack == null) {
                continue;
            }
            if (stack.getAmount() > 0 && stack.getType() == material) {
                if (stack.getAmount() == 1) {
                    is[i] = new ItemStack(Material.AIR, 0);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                c.getInventory().setContents(is);
                //c.update();
                return true;
            }
        }
        return false;
    }
}
