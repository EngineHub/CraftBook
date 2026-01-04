/*
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

package org.enginehub.craftbook.mechanics.ic.gates.world.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.PipeInputIC;
import org.enginehub.craftbook.mechanics.pipe.PipePutEvent;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Distributer extends AbstractSelfTriggeredIC implements PipeInputIC {

    public Distributer(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block chestBlock;
    int right, left;
    int currentIndex;

    @Override
    public void load() {

        try {

            currentIndex = Integer.parseInt(getLine(3));
        } catch (Exception e) {
            currentIndex = -1;
        }
        left = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[0]);
        right = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[1]);
        chestBlock = getBackBlock().getRelative(0, 1, 0);
    }

    @Override
    public String getTitle() {

        return "Distributer";
    }

    @Override
    public String getSignTitle() {

        return "DISTRIBUTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, distribute());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, distribute());
    }

    public boolean distribute() {

        boolean returnValue = false;

        for (Item item : ItemUtil.getItemsAtBlock(getSign().getBlock())) {
            if (distributeItemStack(item.getItemStack())) {
                item.remove();
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean distributeItemStack(ItemStack item) {

        BlockFace back = SignUtil.getBack(getSign().getBlock());
        Block b;

        if (goRight())
            b = SignUtil.getRightBlock(getSign().getBlock()).getRelative(back);
        else
            b = SignUtil.getLeftBlock(getSign().getBlock()).getRelative(back);

        PipeRequestEvent event = new PipeRequestEvent(b, new ArrayList<>(Collections.singletonList(item)), getBackBlock());
        Bukkit.getPluginManager().callEvent(event);

        for (ItemStack it : event.getItems())
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), it);

        return true;
    }

    public boolean goRight() {

        currentIndex++;
        getSign().setLine(3, Component.text(currentIndex));
        if (currentIndex >= left && currentIndex < left + right)
            return true;
        else if (currentIndex < left)
            return false;
        else {
            currentIndex = 0;
            getSign().setLine(3, Component.text(currentIndex));
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Distributer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Distributes items to right and left based on sign.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "left quantity:right quantity", "Current distribution status" };
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {
            try {
                String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(line2)[0]);
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(line2)[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ICVerificationException("You need to specify both left and right quantities!");
            } catch (NumberFormatException e) {
                throw new ICVerificationException("Invalid quantities!");
            }
        }
    }

    @Override
    public void onPipeTransfer(PipePutEvent event) {

        List<ItemStack> leftovers = new ArrayList<>();

        for (ItemStack item : event.getItems())
            if (ItemUtil.isStackValid(item))
                if (!distributeItemStack(item))
                    leftovers.add(item);

        event.setItems(leftovers);
    }
}
