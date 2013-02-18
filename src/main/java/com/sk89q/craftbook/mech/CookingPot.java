package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

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

        public Factory() {

        }

        @Override
        public CookingPot detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                        sign.update();
                        return new CookingPot(pt);
                    }
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
        public CookingPot detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) throw new InsufficientPermissionsException();

                sign.setLine(2, "0");
                sign.setLine(1, "[Cook]");
                if (CraftBookPlugin.inst().getConfiguration().cookingPotFuel) {
                    sign.setLine(3, "0");
                } else {
                    sign.setLine(3, "1");
                }
                sign.update(false);
                player.print("mech.cook.create");
            } else return null;

            throw new ProcessedMechanismException();
        }

    }

    @Override
    public void think() {

        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            int lastTick = 0, oldTick;
            try {
                lastTick = Integer.parseInt(sign.getLine(2));
            } catch (Exception e) {
                sign.setLine(2, String.valueOf(lastTick));
                sign.update();
            }
            oldTick = lastTick;
            if (lastTick < 0) lastTick = 0;
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY() + 2;
            int z = b.getZ();
            Block cb = sign.getWorld().getBlockAt(x, y, z);
            if (cb.getTypeId() == BlockID.CHEST) {
                if (ItemUtil.containsRawFood(((Chest) cb.getState()).getInventory())
                        || ItemUtil.containsRawMinerals(((Chest) cb.getState()).getInventory())
                        && plugin.getConfiguration().cookingPotOres) {
                    if(getMultiplier(sign) < 0)
                        return;
                    lastTick += getMultiplier(sign);
                    decreaseMultiplier(sign, 1);
                }
                if (lastTick >= 50) {
                    Block fire = sign.getWorld().getBlockAt(x, y - 1, z);
                    if (fire.getTypeId() == BlockID.FIRE) {
                        Chest chest = (Chest) cb.getState();
                        for (ItemStack i : chest.getInventory().getContents()) {
                            if (i == null) {
                                continue;
                            }
                            ItemStack cooked = ItemUtil.getCookedResult(i);
                            if (cooked == null) {
                                if (plugin.getConfiguration().cookingPotOres)
                                    cooked = ItemUtil.getSmeletedResult(i);
                                if (cooked == null) continue;
                            }
                            if (chest.getInventory().addItem(cooked).isEmpty())
                                chest.getInventory().removeItem(new ItemStack(i.getType(), 1, i.getDurability()));
                            chest.update();
                            break;
                        }
                        lastTick -= 50;
                    }
                }
            }
            if (lastTick != oldTick) {
                sign.setLine(2, String.valueOf(lastTick));
                sign.update();
            }
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY() + 2;
            int z = b.getZ();
            Block cb = sign.getWorld().getBlockAt(x, y, z);
            if (cb.getTypeId() == BlockID.CHEST) {
                Player player = event.getPlayer();
                ItemStack itemInHand = player.getItemInHand();
                if (itemInHand != null && Ingredients.isIngredient(itemInHand.getTypeId()) && itemInHand.getAmount()
                        > 0) {
                    increaseMultiplier(sign, Ingredients.getTime(itemInHand.getTypeId()));
                    if (itemInHand.getAmount() <= 1) {
                        itemInHand.setTypeId(0);
                        player.setItemInHand(null);
                    } else {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }
                    player.sendMessage("You give the pot fuel!");
                } else if (plugin.getConfiguration().cookingPotSignOpen) {
                    player.openInventory(((Chest) cb.getState()).getBlockInventory());
                }
            }
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        if(!(event.getClickedBlock().getState() instanceof Sign))
            return;
        event.getPlayer().setFireTicks(getMultiplier((Sign) event.getClickedBlock().getState()));
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        player.printError("mech.cook.ouch");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (event.getNewCurrent() > event.getOldCurrent()) {
                increaseMultiplier(sign, 1);
            }
        }
    }

    public void setMultiplier(Sign sign, int amount) {

        int min = 1;
        if (amount < min && !plugin.getConfiguration().cookingPotFuel) {
            amount = min;
        }
        sign.setLine(3, String.valueOf(amount));
        sign.update();
    }

    public void increaseMultiplier(Sign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) + amount);
    }

    public void decreaseMultiplier(Sign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) - amount);
    }

    public int getMultiplier(Sign sign) {

        int multiplier;
        try {
            multiplier = Integer.parseInt(sign.getLine(3));
        } catch (Exception e) {
            multiplier = plugin.getConfiguration().cookingPotFuel ? 0 : 1;
            setMultiplier(sign, multiplier);
        }
        if (multiplier < 0) return plugin.getConfiguration().cookingPotFuel ? 0 : 1;
        return multiplier;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        List<BlockWorldVector> bwv = new ArrayList<BlockWorldVector>();
        bwv.add(pt);
        return bwv;
    }

    private enum Ingredients {
        COAL(ItemID.COAL, 20), LAVA(ItemID.LAVA_BUCKET, 6000), BLAZE(ItemID.BLAZE_ROD, 500), BLAZEDUST(ItemID.BLAZE_POWDER, 250), SNOWBALL(ItemID.SNOWBALL, -40), SNOW(BlockID.SNOW_BLOCK, -100), ICE(BlockID.ICE, -1000);

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