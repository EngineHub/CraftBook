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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.st.BukkitSelfTriggerManager;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.util.events.SelfTriggerThinkEvent;
import org.enginehub.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

public class CookingPot extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.getLine(1).equalsIgnoreCase("[Cook]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.cookingpot.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.create-permission");
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        event.setLine(1, "[Cook]");
        event.setLine(2, "0");
        event.setLine(3, requireFuel ? "0" : "1");
        player.printInfo(TranslatableComponent.of("craftbook.cookingpot.create"));

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(SelfTriggerPingEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Cook]")) {
            return;
        }

        event.setHandled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Cook]")) {
            return;
        }

        event.setHandled(true);

        if (emptyCooldown && sign.getLine(0).equals("COOLDOWN")) {
            if (ThreadLocalRandom.current().nextInt(100) != 0) {
                // Ticks 10 times per second. This will re-check every 10 seconds on average.
                return;
            }

            sign.setLine(0, "");
            sign.update(false);
        }

        int currentCookProgress = 0, previousCookProgress;

        try {
            currentCookProgress = Math.max(0, Integer.parseInt(sign.getLine(2)));
        } catch (Exception e) {
            sign.setLine(2, String.valueOf(0));
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
                if (allowSmelting)
                    items = ItemUtil.getRawMaterials(inventory);
                else
                    items = ItemUtil.getRawFood(inventory);

                if (items.size() == 0) {
                    if (emptyCooldown) {
                        sign.setLine(0, "COOLDOWN");
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
            sign.setLine(2, String.valueOf(currentCookProgress));
        }

        sign.update(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !EventUtil.passesFilter(event)) {
            return;
        }

        ChangedSign sign = event.getSign();

        if (!sign.getLine(1).equals("[Cook]")) {
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
                    p.printError("area.use-permissions");
                }
                return;
            }

            CookingPotFuel fuel = CookingPotFuel.getByMaterial(player.getInventory().getItemInMainHand().getType());

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

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Cook]")) {
            return;
        }

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).unregisterSelfTrigger(event.getBlock().getLocation(), UnregisterReason.BREAK);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!allowRedstone || !requireFuel || !SignUtil.isSign(event.getBlock()) || !EventUtil.passesFilter(event)) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Cook]")) {
            return;
        }

        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getBlock().getLocation());

        if (event.isOn() && !event.isMinor()) {
            increaseFuelLevel(sign, event.getNewCurrent());
            sign.update(false);
        }
    }

    public void setFuelLevel(ChangedSign sign, int amount) {
        if (!requireFuel) {
            amount = Math.max(amount, 1);
        }

        sign.setLine(3, String.valueOf(amount));
    }

    public void increaseFuelLevel(ChangedSign sign, int amount) {
        setFuelLevel(sign, getFuelLevel(sign) + amount);
    }

    public int getFuelLevel(ChangedSign sign) {
        int multiplier;

        try {
            multiplier = Integer.parseInt(sign.getLine(3));
        } catch (Exception e) {
            multiplier = requireFuel ? 0 : 1;
            setFuelLevel(sign, multiplier);
        }

        return Math.max(requireFuel ? 0 : 1, multiplier);
    }

    private enum CookingPotFuel {
        COAL(Material.COAL, 40),
        CHARCOAL(Material.CHARCOAL, 40),
        COALBLOCK(Material.COAL_BLOCK, 360),
        BLAZEDUST(Material.BLAZE_POWDER, 250),
        BLAZE(Material.BLAZE_ROD, 500),
        LAVA(Material.LAVA_BUCKET, 6000);

        private final Material id;
        private final int fuelCount;

        CookingPotFuel(Material id, int fuelCount) {
            this.id = id;
            this.fuelCount = fuelCount;
        }

        public int getFuelCount() {
            return this.fuelCount;
        }

        @Nullable
        public static CookingPotFuel getByMaterial(Material id) {
            for (CookingPotFuel in : values()) {
                if (in.id == id) {
                    return in;
                }
            }

            return null;
        }
    }

    private boolean allowRedstone;
    private boolean requireFuel;
    private boolean allowSmelting;
    private boolean openSign;
    private int progressPerFuel;
    private int fuelPerTick;
    private boolean emptyCooldown;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allows for redstone to be used as a fuel source.");
        allowRedstone = config.getBoolean("allow-redstone", false);

        config.setComment("require-fuel", "Require fuel to cook.");
        requireFuel = config.getBoolean("require-fuel", true);

        config.setComment("allow-smelting", "Allows the cooking pot to cook ores and other smeltable items.");
        allowSmelting = config.getBoolean("allow-smelting", false);

        config.setComment("sign-click-open", "When enabled, right clicking the [Cook] sign will open the cooking pot.");
        openSign = config.getBoolean("sign-click-open", true);

        config.setComment("progress-per-fuel", "How much the current smelt progress increases per unit of fuel (line 4). Decreases fuel per cooked item and increases cooking speed.");
        progressPerFuel = config.getInt("progress-per-fuel", 2);

        config.setComment("fuel-per-tick", "How many fuel units (line 4) are used per tick. Increases cooking speed.");
        fuelPerTick = config.getInt("fuel-per-tick", 5);

        config.setComment("empty-cooldown", "Put the cooking pot in a \"low power\" mode while the chest is empty. Useful for low-performance machines or overloaded servers.");
        emptyCooldown = config.getBoolean("empty-cooldown", false);
    }
}
