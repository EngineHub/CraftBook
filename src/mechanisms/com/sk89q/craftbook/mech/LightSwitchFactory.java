package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;

public class LightSwitchFactory implements MechanicFactory<LightSwitch> {

	protected MechanismsPlugin plugin;

	public LightSwitchFactory(MechanismsPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public LightSwitch detect(BlockWorldVector pt) {
		Block block = pt.getWorld().getBlockAt(BukkitUtil.toLocation(pt));
		if (block.getTypeId() == BlockID.WALL_SIGN) {
			BlockState state = block.getState();
			if (state instanceof Sign
					&& ((Sign) state).getLine(1).equalsIgnoreCase("[|]")
					|| ((Sign) state).getLine(1).equalsIgnoreCase("[I]")) {
				return new LightSwitch(pt, plugin);
			}
			return null;
		}
	}
}
