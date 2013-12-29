package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class CookingPot extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!event.getLine(1).equalsIgnoreCase("[Cook]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.cook")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[Cook]");
        event.setLine(2, "0");
        event.setLine(3, CraftBookPlugin.inst().getConfiguration().cookingPotFuel ? "0" : "1");
        player.print("mech.cook.create");

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {

        if(!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        int lastTick = 0, oldTick;

        try {
            lastTick = Math.max(0, Integer.parseInt(sign.getLine(2).trim()));
        } catch (Exception e) {
            sign.setLine(2, String.valueOf(0));
            sign.update(false);
        }
        oldTick = lastTick;
        Block b = SignUtil.getBackBlock(event.getBlock());
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getType() == Material.CHEST) {
            Block fire = b.getRelative(0, 1, 0);
            if (fire.getType() == Material.FIRE) {
                Chest chest = (Chest) cb.getState();
                if (ItemUtil.containsRawFood(chest.getInventory()) || ItemUtil.containsRawMinerals(chest.getInventory()) && CraftBookPlugin.inst().getConfiguration().cookingPotOres) {
                    if(lastTick < 500) {
                        lastTick = CraftBookPlugin.inst().getConfiguration().cookingPotSuperFast ? lastTick + getMultiplier(sign) : lastTick + Math.min(getMultiplier(sign), 5);
                        if(getMultiplier(sign) > 0)
                            decreaseMultiplier(sign, 1);
                    }
                    if (lastTick >= 50) {
                        for (ItemStack i : chest.getInventory().getContents()) {

                            if (!ItemUtil.isStackValid(i)) continue;
                            ItemStack cooked = ItemUtil.getCookedResult(i);
                            if (cooked == null) {
                                if (CraftBookPlugin.inst().getConfiguration().cookingPotOres)
                                    cooked = ItemUtil.getSmeletedResult(i);
                                if (cooked == null) continue;
                            }
                            if (chest.getInventory().addItem(cooked).isEmpty()) {
                                ItemStack toRemove = i.clone();
                                toRemove.setAmount(1);
                                chest.getInventory().removeItem(toRemove);
                                chest.update();
                                lastTick -= 50;
                                break;
                            }
                        }
                    }
                }
            } else
                lastTick = 0;
        }

        if(oldTick != lastTick) {
            sign.setLine(2, String.valueOf(lastTick));
            sign.update(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if(!SignUtil.isSign(event.getClickedBlock())) return;

        ChangedSign sign = BukkitUtil.toChangedSign(event.getClickedBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getClickedBlock().getLocation());

        LocalPlayer p = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = SignUtil.getBackBlock(event.getClickedBlock());
            Block cb = b.getRelative(0, 2, 0);
            if (cb.getType() == Material.CHEST) {
                Player player = event.getPlayer();
                if(!player.hasPermission("craftbook.mech.cook.refuel")) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        p.printError("mech.restock-permission");
                    event.setCancelled(true);
                    return;
                }
                if (ItemUtil.isStackValid(player.getItemInHand()) && Ingredients.isIngredient(player.getItemInHand().getType())) {
                    Material itemID = player.getItemInHand().getType();
                    increaseMultiplier(sign, Ingredients.getTime(itemID));
                    if (player.getItemInHand().getAmount() <= 1) {
                        player.setItemInHand(null);
                    } else {
                        player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    }
                    if(itemID == Material.LAVA_BUCKET && !CraftBookPlugin.inst().getConfiguration().cookingPotDestroyBuckets)
                        player.getInventory().addItem(new ItemStack(Material.BUCKET, 1));
                    p.print("mech.cook.add-fuel");
                    event.setCancelled(true);
                } else if (CraftBookPlugin.inst().getConfiguration().cookingPotSignOpen) {
                    player.openInventory(((Chest) cb.getState()).getBlockInventory());
                    event.setCancelled(true);
                }
            }

            if(sign.hasChanged())
                sign.update(false);
        } else {
            event.getPlayer().setFireTicks(getMultiplier(sign)+40);
            LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
            player.printError("mech.cook.ouch");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());

        if (event.isOn() && !event.isMinor())
            increaseMultiplier(sign, event.getNewCurrent() - event.getOldCurrent());

        if(sign.hasChanged())
            sign.update(false);
    }

    public void setMultiplier(ChangedSign sign, int amount) {

        if(!CraftBookPlugin.inst().getConfiguration().cookingPotFuel)
            amount = Math.max(amount, 1);
        sign.setLine(3, String.valueOf(amount));
    }

    public void increaseMultiplier(ChangedSign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) + amount);
    }

    public void decreaseMultiplier(ChangedSign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) - amount);
    }

    public int getMultiplier(ChangedSign sign) {

        int multiplier;
        try {
            multiplier = Integer.parseInt(sign.getLine(3).trim());
        } catch (Exception e) {
            multiplier = CraftBookPlugin.inst().getConfiguration().cookingPotFuel ? 0 : 1;
            setMultiplier(sign, multiplier);
        }
        if (multiplier <= 0 && !CraftBookPlugin.inst().getConfiguration().cookingPotFuel) return 1;
        return Math.max(0, multiplier);
    }

    private enum Ingredients {
        COAL(Material.COAL, 40), COALBLOCK(Material.COAL_BLOCK, 360), LAVA(Material.LAVA_BUCKET, 6000), BLAZE(Material.BLAZE_ROD, 500), BLAZEDUST(Material.BLAZE_POWDER, 250), SNOWBALL(Material.SNOW_BALL, -40), SNOW(Material.SNOW_BLOCK, -100), ICE(Material.ICE, -1000);

        private Material id;
        private int mult;

        private Ingredients(Material id, int mult) {

            this.id = id;
            this.mult = mult;
        }

        public static boolean isIngredient(Material id) {

            for (Ingredients in : values()) { if (in.id == id) return true; }
            return false;
        }

        public static int getTime(Material id) {

            for (Ingredients in : values()) { if (in.id == id) return in.mult; }
            return 0;
        }
    }
}