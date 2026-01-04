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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.bukkit.events.SelfTriggerThinkEvent;
import org.enginehub.craftbook.bukkit.events.SelfTriggerUnregisterEvent;
import org.enginehub.craftbook.bukkit.events.SignClickEvent;
import org.enginehub.craftbook.bukkit.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.bukkit.st.BukkitSelfTriggerManager;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.CookingPot;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BukkitCookingPot extends CookingPot implements Listener {

    public BukkitCookingPot(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[Cook]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.cookingpot.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        event.line(1, Component.text("[Cook]"));
        event.line(2, Component.text("0"));
        event.line(3, Component.text(requireFuel ? "0" : "1"));
        player.printInfo(TranslatableComponent.of("craftbook.cookingpot.create"));

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(SelfTriggerPingEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState(false);

        for (Side side : Side.values()) {
            if (!sign.getSide(side).getLine(1).equals("[Cook]")) {
                continue;
            }

            event.setHandled(true);
            break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        BukkitChangedSign sign = null;
        Sign bukkitSign = (Sign) event.getBlock().getState(false);

        for (Side side : Side.values()) {
            if (bukkitSign.getSide(side).getLine(1).equals("[Cook]")) {
                sign = BukkitChangedSign.create(bukkitSign, side, null);
                break;
            }
        }
        if (sign == null) {
            return;
        }

        event.setHandled(true);

        String line0 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(0));
        if (emptyCooldown && line0.equals("COOLDOWN")) {
            if (ThreadLocalRandom.current().nextInt(100) != 0) {
                // Ticks 10 times per second. This will re-check every 10 seconds on average.
                return;
            }

            sign.setLine(0, Component.text(""));
            sign.update(false);
        }

        int currentCookProgress = 0;
        int previousCookProgress;

        try {
            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            currentCookProgress = Math.max(0, Integer.parseInt(line2));
        } catch (Exception e) {
            sign.setLine(2, Component.text(0));
        }

        previousCookProgress = currentCookProgress;
        Block b = SignUtil.getBackBlock(event.getBlock());
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getType() == Material.CHEST) {
            Material fireType = b.getRelative(0, 1, 0).getType();

            if (Tag.FIRE.isTagged(fireType) || Tag.CAMPFIRES.isTagged(fireType)) {
                Chest chest = (Chest) cb.getState(false);
                Inventory inventory = chest.getInventory();

                List<ItemStack> items;
                if (allowSmelting) {
                    items = ItemUtil.getRawMaterials(inventory);
                } else {
                    items = ItemUtil.getRawFood(inventory);
                }

                if (items.isEmpty()) {
                    if (emptyCooldown) {
                        sign.setLine(0, Component.text("COOLDOWN"));
                        sign.update(false);
                    }
                    return;
                }

                int fuelLevel = getFuelLevel(sign);

                if (fuelLevel > 0) {
                    currentCookProgress += progressPerFuel * Math.min(fuelPerTick, fuelLevel);
                    setFuelLevel(sign, fuelLevel - Math.min(fuelPerTick, fuelLevel));
                }

                if (currentCookProgress >= 50) {
                    for (ItemStack i : items) {
                        if (!ItemUtil.isStackValid(i)) {
                            continue;
                        }

                        ItemStack cooked = ItemUtil.getCookedResult(i);
                        if (cooked == null) {
                            if (allowSmelting) {
                                cooked = ItemUtil.getSmeltedResult(i);
                                if (cooked == null) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                        if (inventory.addItem(cooked).isEmpty()) {
                            ItemStack toRemove = i.clone();
                            toRemove.setAmount(1);
                            inventory.removeItem(toRemove);

                            currentCookProgress -= 50;
                            break;
                        }
                    }
                }
            } else {
                currentCookProgress = 0;
            }
        }

        if (previousCookProgress != currentCookProgress) {
            sign.setLine(2, Component.text(currentCookProgress));
        }

        sign.update(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !EventUtil.passesFilter(event)) {
            return;
        }

        BukkitChangedSign sign = event.getSign();

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[Cook]")) {
            return;
        }

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getClickedBlock().getLocation());

        CraftBookPlayer p = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        Block b = SignUtil.getBackBlock(event.getClickedBlock());
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getType() == Material.CHEST) {
            Player player = event.getPlayer();
            if (!player.hasPermission("craftbook.cookingpot.refuel")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    p.printError(TranslatableComponent.of("craftbook.cookingpot.no-refuel-permissions"));
                }
                event.setCancelled(true);
                return;
            }

            if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    p.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
                }
                return;
            }

            CookingPotFuel fuel = CookingPotFuel.getByItemType(p.getItemInHand(HandSide.MAIN_HAND).getType());

            if (ItemUtil.isStackValid(player.getInventory().getItemInMainHand()) && fuel != null) {
                increaseFuelLevel(sign, fuel.getFuelCount());

                player.getInventory().setItemInMainHand(ItemUtil.getUsedItem(player.getInventory().getItemInMainHand()));

                p.printInfo(TranslatableComponent.of("craftbook.cookingpot.fuel-added"));
                event.setCancelled(true);
            } else if (openSign) {
                player.openInventory(((Chest) cb.getState(false)).getBlockInventory());
                event.setCancelled(true);
            }
        }

        sign.update(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDestroy(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState(false);

        for (Side side : Side.values()) {
            String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getSide(side).line(1));
            if (!line1.equals("[Cook]")) {
                continue;
            }

            ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).unregisterSelfTrigger(event.getBlock().getLocation(), SelfTriggerUnregisterEvent.UnregisterReason.BREAK);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!allowRedstone || !requireFuel || !SignUtil.isSign(event.getBlock()) || !EventUtil.passesFilter(event)) {
            return;
        }

        Sign bukkitSign = (Sign) event.getBlock().getState(false);
        Side side = bukkitSign.getInteractableSideFor(event.getSource().getLocation());

        if (!bukkitSign.getSide(side).getLine(1).equals("[Cook]")) {
            return;
        }

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getBlock().getLocation());

        if (event.isOn() && !event.isMinor()) {
            BukkitChangedSign sign = BukkitChangedSign.create(bukkitSign, side);
            increaseFuelLevel(sign, event.getNewCurrent());
            sign.update(false);
        }
    }

    public void setFuelLevel(BukkitChangedSign sign, int amount) {
        if (!requireFuel) {
            amount = Math.max(amount, 1);
        }

        sign.setLine(3, Component.text(amount));
    }

    public void increaseFuelLevel(BukkitChangedSign sign, int amount) {
        setFuelLevel(sign, getFuelLevel(sign) + amount);
    }

    public int getFuelLevel(BukkitChangedSign sign) {
        int multiplier;

        try {
            String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
            multiplier = Integer.parseInt(line3);
        } catch (Exception e) {
            multiplier = requireFuel ? 0 : 1;
            setFuelLevel(sign, multiplier);
        }

        return Math.max(requireFuel ? 0 : 1, multiplier);
    }
}
