package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class SetBridge extends AbstractIC {

	private int onMaterial = 1;
	private int onData = 0;

	private int offMaterial = 0;
	private int offData = 0;

	private int width = 1;
	private int depth = 1;

	private int offsetX = 0;
	private int offsetY = 0;
	private int offsetZ = 0;

	private Block center;
	private BlockFace faceing;

	public SetBridge(Server server, Sign block) {
		super(server, block);
		load();
	}

	private void load() {
		this.center = SignUtil.getBackBlock(getSign().getBlock());
		this.faceing = SignUtil.getFacing(getSign().getBlock());
		String line = getSign().getLine(2);
		if (!line.equals("")) {
			try {
				String[] split = line.split("-");
				// parse the material data
				if (split.length > 0) {
					try {
						// parse the data that gets set when the block is toggled off
						String[] strings = split[1].split(":");
						offMaterial = Integer.parseInt(strings[0]);
						if (strings.length > 0) offData = Integer.parseInt(strings[1]);
					} catch (NumberFormatException e) {
						// do nothing and use the defaults
					} catch (ArrayIndexOutOfBoundsException e) {
						// do nothing and use the defaults
					}
				}
				// parse the material and data for toggle on
				String[] strings = split[0].split(":");
				onMaterial = Integer.parseInt(strings[0]);
				if (strings.length > 0) onData = Integer.parseInt(strings[1]);
			} catch (NumberFormatException e) {
				// do nothing and use the defaults
			} catch (ArrayIndexOutOfBoundsException e) {
				// do nothing and use the defaults
			}
		}
		// parse the coordinates
		line = getSign().getLine(3);
		if (!line.equals("")) {
			boolean relativeOffset = line.contains("!") ? false : true;
			if (!relativeOffset) line.replace("!", "");
			String[] split = line.split(":");
			try {
				// parse the offset
				String[] offsetSplit = split[0].split(",");
				offsetX = Integer.parseInt(offsetSplit[0]);
				offsetY = Integer.parseInt(offsetSplit[1]);
				offsetZ = Integer.parseInt(offsetSplit[2]);
			} catch (NumberFormatException e) {
				// do nothing and use the defaults
			} catch (IndexOutOfBoundsException e) {
				// do nothing and use the defaults
			}
			try {
				// parse the size of the door
				String[] sizeSplit = split[1].split(",");
				width = Integer.parseInt(sizeSplit[0]);
				depth = Integer.parseInt(sizeSplit[1]);
			} catch (NumberFormatException e) {
				// do nothing and use the defaults
			} catch (ArrayIndexOutOfBoundsException e) {
				// do nothing and use the defaults
			}
			if (relativeOffset) {
				this.center = LocationUtil.getRelativeOffset(getSign(), offsetX, offsetY, offsetZ);
			} else {
				this.center = LocationUtil.getOffset(this.center, offsetX, offsetY, offsetZ);
			}
		} else {
			center = center.getRelative(BlockFace.UP);
		}
	}

	@Override
	public String getTitle() {
		return "Set P-Bridge";
	}

	@Override
	public String getSignTitle() {
		return "SET P-Bridge";
	}

	@Override
	public void trigger(ChipState chip) {
		if (chip.getInput(0)) {
			setDoor(true);
		} else {
			setDoor(false);
		}
	}

	private void setDoor(boolean open) {
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				Block block = LocationUtil.getRelativeOffset(center, faceing, x, 0, z);
				if (open) {
					block.setTypeIdAndData(onMaterial, (byte) onData, true);
				} else {
					block.setTypeIdAndData(offMaterial, (byte) offData, true);
				}
			}
		}
	}

	public static class Factory extends AbstractICFactory implements RestrictedIC {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new SetBridge(getServer(), sign);
		}
	}
}
