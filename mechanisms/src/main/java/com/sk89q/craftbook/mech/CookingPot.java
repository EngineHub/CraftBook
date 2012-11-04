package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CookingPot extends PersistentMechanic implements SelfTriggeringMechanic {

    /**
     * Plugin.
     */
    protected final MechanismsPlugin plugin;

    /**
     * Location.
     */
    protected final BlockWorldVector pt;

    /**
     * Construct a cooking pot for a location.
     *
     * @param pt
     * @param plugin
     */
    public CookingPot(BlockWorldVector pt, MechanismsPlugin plugin) {

        super();
        this.pt = pt;
        this.plugin = plugin;
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {

        return true; //is now.
    }

    public static class Factory extends AbstractMechanicFactory<CookingPot> {

        protected final MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
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
                        return new CookingPot(pt, plugin);
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
                Sign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) throw new InsufficientPermissionsException();

                sign.setLine(2, "0");
                sign.setLine(1, "[Cook]");
                if (plugin.getLocalConfiguration().cookingPotSettings.requiresfuel) {
                    sign.setLine(3, "0");
                }
                else {
                    sign.setLine(3, "1");
                }
                sign.update();
                player.print("mech.cook.create");
            } else
                return null;

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
                sign.setLine(2, lastTick + "");
                sign.update();
            }
            oldTick = lastTick;
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY() + 2;
            int z = b.getZ();
            Block cb = sign.getWorld().getBlockAt(x, y, z);
            if (cb.getTypeId() == BlockID.CHEST) {
                if (ItemUtil.containsRawFood(((Chest) cb.getState()).getInventory())) {
                    decreaseMultiplier(sign, 1);
                    lastTick += getMultiplier(sign);
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
                                continue;
                            }
                            chest.getInventory().addItem(new ItemStack(cooked.getType(), 1));
                            chest.getInventory().removeItem(new ItemStack(i.getType(), 1));
                            chest.update();
                            break;
                        }
                        lastTick = 0;
                    }
                }
            }
            if (lastTick != oldTick) {
                sign.setLine(2, lastTick + "");
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
            if (cb.getTypeId() == BlockID.CHEST)
                if (event.getPlayer().getItemInHand() != null && Ingredients.isIngredient(event.getPlayer()
                        .getItemInHand().getTypeId()) && event.getPlayer().getItemInHand().getAmount() > 0) {
                    increaseMultiplier(sign, Ingredients.getTime(event.getPlayer().getItemInHand().getTypeId()));
                    if (event.getPlayer().getItemInHand().getAmount() <= 1) {
                        event.getPlayer().getItemInHand().setTypeId(0);
                        event.getPlayer().setItemInHand(null);
                    } else {
                        event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
                    }
                    event.getPlayer().sendMessage("You give the pot fuel!");
                } else {
                    event.getPlayer().openInventory(((Chest) cb.getState()).getBlockInventory());
                }
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        event.getPlayer().setFireTicks(20);
        LocalPlayer player = plugin.wrap(event.getPlayer());
        player.printError("mech.cook.ouch");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            try {
                if (event.getNewCurrent() > event.getOldCurrent()) {
                    increaseMultiplier(sign, 1);
                }
                sign.update();
            } catch (Exception ignored) {
            }
        }
    }

    public void setMultiplier(Sign sign, int amount) {

        if (amount < 1 && !plugin.getLocalConfiguration().cookingPotSettings.requiresfuel) {
            amount = 1;
        }
        sign.setLine(3, amount + "");
        sign.update();
    }

    public void increaseMultiplier(Sign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) + amount);
    }

    public void decreaseMultiplier(Sign sign, int amount) {

        setMultiplier(sign, getMultiplier(sign) - amount);
    }

    public int getMultiplier(Sign sign) {

        int multiplier = 1;
        if (plugin.getLocalConfiguration().cookingPotSettings.requiresfuel) {
            multiplier = 0;
        }
        try {
            multiplier = Integer.parseInt(sign.getLine(3));
        } catch (Exception e) {
            multiplier = 1;
            if (plugin.getLocalConfiguration().cookingPotSettings.requiresfuel) {
                multiplier = 0;
            }
            setMultiplier(sign, multiplier);
        }
        return multiplier;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        List<BlockWorldVector> bwv = new ArrayList<BlockWorldVector>();
        bwv.add(pt);
        return bwv;
    }

    private enum Ingredients {
        COAL(ItemID.COAL, 10), LAVA(ItemID.LAVA_BUCKET, 500), BLAZE(ItemID.BLAZE_ROD, 200),
        SNOWBALL(ItemID.SNOWBALL, -20), SNOW(BlockID.SNOW_BLOCK, -100);

        private int id;
        private int mult;

        private Ingredients(int id, int mult) {

            this.id = id;
            this.mult = mult;
        }

        public static boolean isIngredient(int id) {

            for (Ingredients in : values())
                if (in.id == id)
                    return true;
            return false;
        }

        public static int getTime(int id) {

            for (Ingredients in : values())
                if (in.id == id)
                    return in.mult;
            return 0;
        }
    }
}