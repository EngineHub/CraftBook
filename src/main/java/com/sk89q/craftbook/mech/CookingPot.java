package com.sk89q.craftbook.mech;

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class CookingPot extends PersistentMechanic implements SelfTriggeringMechanic {

    /**
     * Plugin.
     */
    private final CraftBookPlugin plugin = CraftBookPlugin.inst();

    /**
     * Location.
     */
    private final BlockWorldVector pt;

    /**
     * Construct a cooking pot for a location.
     */
    public CookingPot(BlockWorldVector pt) {

        super();
        this.pt = pt;
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends AbstractMechanicFactory<CookingPot> {

        @Override
        public CookingPot detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toLocation(pt).getBlock();
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                ChangedSign sign = BukkitUtil.toChangedSign(block);
                if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                    return new CookingPot(pt);
                }
            }

            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public CookingPot detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) throw new InsufficientPermissionsException();

                sign.setLine(1, "[Cook]");
                sign.setLine(2, "0");
                sign.setLine(3, CraftBookPlugin.inst().getConfiguration().cookingPotFuel ? "0" : "1");
                sign.update(false);
                player.print("mech.cook.create");
            } else return null;

            throw new ProcessedMechanismException();
        }

    }

    @Override
    public void think() {

        int lastTick = 0, oldTick;
        Block block = BukkitUtil.toLocation(pt).getBlock();
        ChangedSign sign = BukkitUtil.toChangedSign(block);

        if(sign == null)
            return;

        try {
            lastTick = Math.max(0, Integer.parseInt(sign.getLine(2).trim()));
        } catch (Exception e) {
            sign.setLine(2, String.valueOf(0));
            sign.update(false);
        }
        oldTick = lastTick;
        Block b = SignUtil.getBackBlock(block);
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getTypeId() == BlockID.CHEST) {
            Block fire = b.getRelative(0, 1, 0);
            if (fire.getTypeId() == BlockID.FIRE) {
                Chest chest = (Chest) cb.getState();
                if (ItemUtil.containsRawFood(chest.getInventory()) || ItemUtil.containsRawMinerals(chest.getInventory()) && plugin.getConfiguration().cookingPotOres) {
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
                                if (plugin.getConfiguration().cookingPotOres)
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

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();
        ChangedSign sign = BukkitUtil.toChangedSign(block);
        LocalPlayer p = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(sign == null)
            return;

        Block b = SignUtil.getBackBlock(block);
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getTypeId() == BlockID.CHEST) {
            Player player = event.getPlayer();
            if(!player.hasPermission("craftbook.mech.cook.refuel")) {
                p.printError("mech.restock-permission");
                event.setCancelled(true);
                return;
            }
            if (ItemUtil.isStackValid(player.getItemInHand()) && Ingredients.isIngredient(player.getItemInHand().getTypeId())) {
                int itemID = player.getItemInHand().getTypeId();
                increaseMultiplier(sign, Ingredients.getTime(itemID));
                if (player.getItemInHand().getAmount() <= 1) {
                    player.setItemInHand(null);
                } else {
                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                }
                if(itemID == ItemID.LAVA_BUCKET && !plugin.getConfiguration().cookingPotDestroyBuckets)
                    player.getInventory().addItem(new ItemStack(ItemID.BUCKET, 1));
                p.print("mech.cook.add-fuel");
                event.setCancelled(true);
            } else if (plugin.getConfiguration().cookingPotSignOpen) {
                player.openInventory(((Chest) cb.getState()).getBlockInventory());
                event.setCancelled(true);
            }
        }

        if(sign.hasChanged())
            sign.update(false);
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        ChangedSign sign = BukkitUtil.toChangedSign(event.getClickedBlock());

        if(sign == null)
            return;

        event.getPlayer().setFireTicks(getMultiplier(sign)+40);
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        player.printError("mech.cook.ouch");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

        if(sign == null)
            return;

        if (event.getNewCurrent() > event.getOldCurrent())
            increaseMultiplier(sign, event.getNewCurrent() - event.getOldCurrent());

        if(sign.hasChanged())
            sign.update(false);
    }

    public void setMultiplier(ChangedSign sign, int amount) {

        if(!plugin.getConfiguration().cookingPotFuel)
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
            multiplier = plugin.getConfiguration().cookingPotFuel ? 0 : 1;
            setMultiplier(sign, multiplier);
        }
        if (multiplier <= 0 && !plugin.getConfiguration().cookingPotFuel) return 1;
        return Math.max(0, multiplier);
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(pt);
    }

    private enum Ingredients {
        COAL(ItemID.COAL, 40), COALBLOCK(BlockID.COAL_BLOCK, 360), LAVA(ItemID.LAVA_BUCKET, 6000), BLAZE(ItemID.BLAZE_ROD, 500), BLAZEDUST(ItemID.BLAZE_POWDER, 250), SNOWBALL(ItemID.SNOWBALL, -40), SNOW(BlockID.SNOW_BLOCK, -100), ICE(BlockID.ICE, -1000);

        private int id;
        private int mult;

        private Ingredients(int id, int mult) {

            this.id = id;
            this.mult = mult;
        }

        public static boolean isIngredient(int id) {

            for (Ingredients in : values()) { if (in.id == id) return true; }
            return false;
        }

        public static int getTime(int id) {

            for (Ingredients in : values()) { if (in.id == id) return in.mult; }
            return 0;
        }
    }
}