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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.block.Chest;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ItemInfo;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.RailUtil;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CartDeposit extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (event.isMinor()) return;
        if (!event.getBlocks().matches(getBlock())) return;
        if (!(event.getMinecart() instanceof StorageMinecart)) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        // collect/deposit set?
        if (!event.getBlocks().hasSign()) return;
        if (!event.getBlocks().matches("collect") && !event.getBlocks().matches("deposit")) return;
        boolean collecting = event.getBlocks().matches("collect");

        // go
        List<Tuple2<ItemInfo, Integer>> items = new ArrayList<>();
        for (String data : RegexUtil.COMMA_PATTERN.split(event.getBlocks().getChangedSign().getLine(2))) {
            int itemID = -1;
            short itemData = -1;
            int amount = -1;
            try {
                String[] splitLine = RegexUtil.COLON_PATTERN.split(RegexUtil.ASTERISK_PATTERN.split(data)[0]);
                itemID = Integer.parseInt(splitLine[0]);
                if (splitLine.length > 1)
                    itemData = Short.parseShort(splitLine[1]);
                try {
                    amount = Integer.parseInt(RegexUtil.ASTERISK_PATTERN.split(data)[1]);
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
                continue;
            }

            items.add(new Tuple2<>(new ItemInfo(itemID, itemData), amount));
        }

        Inventory cartinventory = ((StorageMinecart) event.getMinecart()).getInventory();
        ArrayList<ItemStack> leftovers = new ArrayList<>();

        // search for containers
        List<Chest> containers = new ArrayList<>(RailUtil.getNearbyChests(event.getBlocks().base()));
        containers.addAll(RailUtil.getNearbyChests(event.getBlocks().rail()));

        // are there any containers?
        if (containers.isEmpty()) return;

        if (collecting) {
            // collecting
            ArrayList<ItemStack> transferItems = new ArrayList<>();
            if (!items.isEmpty()) {
                for (ItemStack item : cartinventory.getContents()) {
                    if (!ItemUtil.isStackValid(item))
                        continue;
                    Iterator<Tuple2<ItemInfo, Integer>> iter = items.iterator();
                    while (iter.hasNext()) {
                        Tuple2<ItemInfo, Integer> inf = iter.next();
                        if (!inf.a.isTypeValid() || inf.a.getType() == item.getType()) {
                            if (inf.a.getData() < 0 || inf.a.getData() == item.getDurability()) {
                                if (inf.b < 0) {
                                    transferItems.add(item.clone());
                                    cartinventory.remove(item);
                                } else {
                                    ItemStack stack = item.clone();
                                    if (item.getAmount() > inf.b) {
                                        stack.setAmount(inf.b);
                                        iter.remove();
                                        items.add(new Tuple2<>(inf.a, 0));
                                    } else {
                                        iter.remove();
                                        items.add(new Tuple2<>(inf.a, inf.b - stack.getAmount()));
                                    }
                                    transferItems.add(stack.clone());
                                    cartinventory.removeItem(stack);
                                }
                            }
                        }
                    }
                }
            } else {
                transferItems.addAll(Arrays.asList(cartinventory.getContents()));
                cartinventory.clear();
            }

            transferItems.removeAll(Collections.singleton(null));

            // is cart non-empty?
            if (transferItems.isEmpty()) return;

            CraftBookPlugin.logDebugMessage("collecting " + transferItems.size() + " item stacks", "cart-deposit.collect");
            for (ItemStack stack : transferItems)
                CraftBookPlugin.logDebugMessage("collecting " + stack.getAmount() + " items of type " + stack.getType().toString(), "cart-deposit.collect");

            for (Chest container : containers) {
                if (transferItems.isEmpty()) {
                    break;
                }
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferItems.toArray(new ItemStack[transferItems.size()
                    ])).values());
                transferItems.clear();
                transferItems.addAll(leftovers);
                leftovers.clear();

                //container.update();
            }

            CraftBookPlugin.logDebugMessage("collected items. " + transferItems.size() + " stacks left over.", "cart-deposit.collect");

            leftovers.addAll(cartinventory.addItem(transferItems.toArray(new ItemStack[transferItems.size()])).values());
            transferItems.clear();
            transferItems.addAll(leftovers);
            leftovers.clear();

            CraftBookPlugin.logDebugMessage("collection done. " + transferItems.size() + " stacks wouldn't fit back.", "cart-deposit.collect");
        } else {
            // depositing
            ArrayList<ItemStack> transferitems = new ArrayList<>();

            for (Chest container : containers) {
                Inventory containerinventory = container.getInventory();
                if (!items.isEmpty()) {
                    for (ItemStack item : containerinventory.getContents()) {
                        if (!ItemUtil.isStackValid(item))
                            continue;
                        Iterator<Tuple2<ItemInfo, Integer>> iter = items.iterator();
                        while (iter.hasNext()) {
                            Tuple2<ItemInfo, Integer> inf = iter.next();
                            if (!inf.a.isTypeValid() || inf.a.getType() == item.getType())
                                if (inf.a.getData() < 0 || inf.a.getData() == item.getDurability()) {
                                    if (inf.b < 0) {
                                        transferitems.add(item.clone());
                                        containerinventory.remove(item);
                                    } else {
                                        ItemStack stack = item.clone();
                                        if (item.getAmount() > inf.b) {
                                            stack.setAmount(inf.b);
                                            iter.remove();
                                            items.add(new Tuple2<>(inf.a, 0));
                                        } else {
                                            iter.remove();
                                            items.add(new Tuple2<>(inf.a, inf.b - stack.getAmount()));
                                        }
                                        transferitems.add(stack.clone());
                                        containerinventory.removeItem(stack);
                                    }
                                }
                        }
                    }
                } else {
                    transferitems.addAll(Arrays.asList(containerinventory.getContents()));
                    containerinventory.clear();
                }
                // container.update();
            }

            transferitems.removeAll(Collections.singleton(null));

            // are chests empty?
            if (transferitems.isEmpty()) return;

            CraftBookPlugin.logDebugMessage("depositing " + transferitems.size() + " stacks", "cart-deposit.deposit");
            for (ItemStack stack : transferitems)
                CraftBookPlugin.logDebugMessage("depositing " + stack.getAmount() + " items oftype " + stack.getType().toString(), "cart-deposit.deposit");

            leftovers.addAll(cartinventory.addItem(transferitems.toArray(new ItemStack[transferitems.size()])).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();

            CraftBookPlugin.logDebugMessage("deposited, " + transferitems.size() + " items left over.", "cart-deposit.deposit");

            for (Chest container : containers) {
                if (transferitems.isEmpty()) {
                    break;
                }
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferitems.toArray(new ItemStack[transferitems.size()])).values());
                containerinventory.clear();
                for (ItemStack item : leftovers)
                    containerinventory.addItem(item);
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();
            }

            CraftBookPlugin.logDebugMessage("deposit done. " + transferitems.size() + " items wouldn't fit back.", "cart-deposit.deposit");
        }
    }

    @Override
    public List<String> getApplicableSigns() {

        return ImmutableList.copyOf(new String[] { "Collect", "Deposit" });
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the deposit mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.IRON_ORE.getId()), true));
    }
}