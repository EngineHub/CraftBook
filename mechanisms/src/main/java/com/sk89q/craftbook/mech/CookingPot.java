package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CookingPot extends AbstractMechanic implements SelfTriggeringMechanic{
    
	int lastTick = 0;
	
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
        return true; 
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
        public CookingPot detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) {
                    throw new InsufficientPermissionsException();
                }
                
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
		lastTick++;
		if(lastTick<200) return;
		lastTick = 0;
		Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
		if (block.getTypeId() == BlockID.WALL_SIGN) {
			BlockState state = block.getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                int x = sign.getBlock().getX();
                int y = sign.getBlock().getY();
                int z = sign.getBlock().getZ();
                y += 2;
                Block cb = BukkitUtil.toWorld(pt).getBlockAt(x,y,z);
                if (cb.getTypeId() == BlockID.CHEST && BukkitUtil.toWorld(pt).getBlockAt(x,y-1,z).getTypeId() == BlockID.FIRE) {
        			BlockState s = cb.getState();
                    if (state instanceof Chest) {
                    	Chest chest = (Chest) s;
                    	int index = -1;
                    	for(ItemStack i : chest.getInventory().getContents())
                    	{
                    		index++;
                    		if(!i.getType().isEdible()) continue;
                    		if(i.getType().equals(Material.RAW_BEEF))
                    		{
                    			chest.getInventory().addItem(new ItemStack(Material.COOKED_BEEF,1));
                    			chest.getInventory().removeItem(new ItemStack(Material.RAW_BEEF,1));
                    			break;
                    		}
                    		if(i.getType().equals(Material.RAW_CHICKEN))
                    		{
                    			chest.getInventory().addItem(new ItemStack(Material.COOKED_CHICKEN,1));
                    			chest.getInventory().removeItem(new ItemStack(Material.RAW_CHICKEN,1));
                    			break;
                    		}
                    		if(i.getType().equals(Material.RAW_FISH))
                    		{
                    			chest.getInventory().addItem(new ItemStack(Material.COOKED_FISH,1));
                    			chest.getInventory().removeItem(new ItemStack(Material.RAW_FISH,1));
                    			break;
                    		}
                    	}
                    }
                }
            }
		}
	}
}
