package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CookingPot extends PersistentMechanic implements SelfTriggeringMechanic{

    /**
     * Plugin.
     */
    protected MechanismsPlugin plugin;

    /**
     * Location.
     */
    protected BlockWorldVector pt;

    /**
     * Construct a gate for a location.
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

        protected MechanismsPlugin plugin;

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
        public CookingPot detect(BlockWorldVector pt, LocalPlayer player, Sign sign) throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(2, "0");
                sign.setLine(1, "[Cook]");
                player.print("Cooking pot created.");
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
            int lastTick = 0;
            try {
                lastTick = Integer.parseInt(sign.getLine(2));
            }
            catch(Exception e){
                sign.setLine(2, lastTick + "");
            }
            lastTick++;
            if(lastTick<25) return;
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY()+2;
            int z = b.getZ();
            Block cb = sign.getWorld().getBlockAt(x,y,z);
            if (cb.getType() == Material.CHEST) {
                Block fire = sign.getWorld().getBlockAt(x,y-1,z);
                if(fire.getType() == Material.FIRE)
                {
                    if (cb.getState() instanceof Chest) {
                        Chest chest = (Chest) cb.getState();
                        for(ItemStack i : chest.getInventory().getContents())
                        {
                            if(i==null || ItemUtil.isItemCookable(i) == false) continue;
                            ItemStack cooked = ItemUtil.getCookedState(i);
                            if(cooked == null) continue;
                            chest.getInventory().addItem(new ItemStack(cooked.getType(),1));
                            chest.getInventory().removeItem(new ItemStack(i.getType(),1));
                            break;
                        }
                        lastTick = 0;
                    }
                }
            }
            sign.setLine(2, lastTick + "");
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if(event.getClickedBlock().getState() instanceof Sign)
        {
            Sign sign = (Sign) event.getClickedBlock().getState();
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY()+2;
            int z = b.getZ();
            Block cb = sign.getWorld().getBlockAt(x,y,z);
            if (cb.getType() == Material.CHEST)
                event.getPlayer().openInventory(((Chest)cb.getState()).getBlockInventory());
            int lastTick = Integer.parseInt(sign.getLine(2));
            lastTick++;
            sign.setLine(2, lastTick + "");
            think();
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {
        event.getPlayer().setFireTicks(5);
        event.getPlayer().sendMessage(ChatColor.RED + "Ouch! That was hot!");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {
        List<BlockWorldVector> bwv = new ArrayList<BlockWorldVector>();
        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
        bwv.add(pt);
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            Block b = SignUtil.getBackBlock(sign.getBlock());
            int x = b.getX();
            int y = b.getY()+2;
            int z = b.getZ();
            bwv.add(BukkitUtil.toWorldVector(b));
            Block cb = sign.getWorld().getBlockAt(x,y,z);
            if (cb.getType() == Material.CHEST) {
                bwv.add(BukkitUtil.toWorldVector(cb));
                Block fire = sign.getWorld().getBlockAt(x,y-1,z);
                if(fire.getType() == Material.FIRE)
                    bwv.add(BukkitUtil.toWorldVector(fire));
            }
        }

        return bwv;
    }
}
