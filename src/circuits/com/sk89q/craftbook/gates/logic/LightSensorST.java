package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class LightSensorST extends AbstractIC implements SelfTriggeredIC {

	public LightSensorST(Server server, Sign block) {
		super(server, block);
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public String getTitle() {
		return "Self-Triggering Light Sensor";
	}

	@Override
	public String getSignTitle() {
		return "ST LIGHT SENSOR";
	}

	@Override
	public void trigger(ChipState chip) {
		// Empty ( No Inputs )
	}

	@Override
	public void unload() {

	}

	@Override
	public void think(ChipState state) {
		state.setOutput(0, hasLight());
	}
	
	// Copied from it's triggered equiv. 
    /**
     * Returns true if the sign has a light level above the specified.
     * 
     * @return
     */
    private boolean hasLight() {
        int lightLevel = (int) getSign()
                .getWorld()
                .getBlockAt(getSign().getBlock().getLocation().getBlockX(),
                        getSign().getBlock().getLocation().getBlockY() + 1,
                        getSign().getBlock().getLocation().getBlockZ())
                .getLightLevel();
        int specifiedLevel = 0;
        try {
            String specified = getSign().getLine(2);
            if (specified.length() > 0) {
                specifiedLevel = Integer.parseInt(specified);
            }
        } catch (NumberFormatException e) {
            // eat the exception.
        }

        return lightLevel >= specifiedLevel;
    }
    
    public static class Factory extends AbstractICFactory
    {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new LightSensorST(getServer(), sign);
		}
    	
    }

}
