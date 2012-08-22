package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CookingPot extends AbstractMechanic implements SelfTriggeringMechanic {

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
                        sign.setLine(2, "0");
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
                if (!player.hasPermission("craftbook.mech.cook")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(2, "0");
                sign.setLine(1, "[Cook]");
                sign.update();
                player.print("mech.cook.create");
            } else {
                return null;
            }

            throw new ProcessedMechanismException();
        }

    }

    @Override
    public void think() {

        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            int lastTick = 0, oldTick;
            int multiplier = 1;
            try {
                lastTick = Integer.parseInt(sign.getLine(2));
                try {
                    multiplier = Integer.parseInt(sign.getLine(3));
                }
                catch(Exception e) {
                }
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
            if (cb.getType() == Material.CHEST) {
                if (ItemUtil.containsRawFood(((Chest) cb.getState()).getInventory()))
                    lastTick += multiplier;
                if (lastTick >= 50) {
                    Block fire = sign.getWorld().getBlockAt(x, y - 1, z);
                    if (fire.getType() == Material.FIRE) {
                        if (cb.getState() instanceof Chest) {
                            Chest chest = (Chest) cb.getState();
                            for (ItemStack i : chest.getInventory().getContents()) {
                                if (i == null) continue;
                                ItemStack cooked = ItemUtil.getCookedResult(i);
                                if (cooked == null) continue;
                                chest.getInventory().addItem(new ItemStack(cooked.getType(), 1));
                                chest.getInventory().removeItem(new ItemStack(i.getType(), 1));
                                chest.update();
                                break;
                            }
                            lastTick = 0;
                        }
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
            if (cb.getType() == Material.CHEST)
                event.getPlayer().openInventory(((Chest) cb.getState()).getBlockInventory());
            think();
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
                sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "5" : "1");
                sign.update();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}