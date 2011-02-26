package mechanisms.com.sk89q.craftbook.mech;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.craftbook.util.WorldVector;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone
 * to normal torches. This is done every time a sign with [|] or [I] is right 
 * clicked by a player.
 *
 * @author sk89q
 */
public class LightSwitch {
	/**
     * Store a list of recent light toggles to prevent spamming. Someone
     * clever can just use two signs though.
     */
    private HistoryHashMap<BlockWorldVector,Long> recentLightToggles = new HistoryHashMap<BlockWorldVector,Long>(20);

    /**
     * Toggle lights in the immediate area.
     * 
     * @param pt
     * @return
     */
 
    public boolean toggleLights(BlockWorldVector pt) {
    	
    	World world = pt.getWorld();
    	
    	int wx = pt.getX();
        int wy = pt.getY();
        int wz = pt.getZ();
        int aboveID = world.getBlockTypeIdAt(wx, wy, wz);
        

        if (aboveID == BlockID.TORCH || aboveID == BlockID.REDSTONE_TORCH_OFF
                || aboveID == BlockID.REDSTONE_TORCH_ON) {

        	// Check if block above is a redstone torch.
        	// Used to get what to change torches to.
            boolean on = (aboveID != BlockID.TORCH);
            
            // Prevent spam
            Long lastUse = recentLightToggles.remove(pt);
            long currTime = System.currentTimeMillis();
            if (lastUse != null && currTime - lastUse < 500) {
                recentLightToggles.put(pt, lastUse);
                return true;
            }
            recentLightToggles.put(pt, currTime);
            
            int changed = 0;
            for (int x = -10 + wx; x <= 10 + wx; x++) {
                for (int y = -10 + wy; y <= 10 + wy; y++) {
                    for (int z = -5 + wz; z <= 5 + wz; z++) {
                        int id = world.getBlockTypeIdAt(x, y, z);
                        if (id == BlockID.TORCH || id == BlockID.REDSTONE_TORCH_OFF
                                || id == BlockID.REDSTONE_TORCH_ON) {
                            // Limit the maximum number of changed lights
                            if (changed >= 20) {
                                return true;
                            }
                            if (on) {
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.TORCH);
                            } else {
                            	world.getBlockAt(x, y, z).setTypeId(BlockID.TORCH);
                            }
                            changed++;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}
