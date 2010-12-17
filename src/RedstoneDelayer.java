// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.HashMap;

import com.sk89q.craftbook.BlockSourceException;
import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.BlockVector;
import com.sk89q.craftbook.OperationException;
import com.sk89q.craftbook.Vector;

/**
 * Thread for delaying redstone inputs.
 * 
 * @author Lymia
 * @author sk89q
 */
public class RedstoneDelayer implements Runnable {
	private HashMap<BlockVector,Boolean> delayedOutputs =
			new HashMap<BlockVector,Boolean>();
	private HashMap<BlockVector,Boolean> delayedBridges =
			new HashMap<BlockVector,Boolean>();
	private HashMap<BlockVector,Integer> delayedInputsOldValue =
			new HashMap<BlockVector,Integer>();
	private HashMap<BlockVector,Integer> delayedInputsNewValue =
			new HashMap<BlockVector,Integer>();

	private CraftBookListener listener;

	public RedstoneDelayer(CraftBookListener listner) {
		this.listener = listner;
	}

	public void setOut(Vector pos, boolean value) {
		delayedOutputs.put(pos.toBlockVector(), value);
	}

	public void toggleBridge(Vector v, boolean value) {
		delayedBridges.put(v.toBlockVector(), value);
	}

	public void delayRsChange(BlockVector v, int oldValue, int newValue) {
		delayedInputsOldValue.put(v, oldValue);
		delayedInputsNewValue.put(v, newValue);
	}

	/**
	 * Run thread.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		HashMap<BlockVector, Boolean> delayedOutputs =
			(HashMap<BlockVector,Boolean>)this.delayedOutputs.clone();
		HashMap<BlockVector, Boolean> delayedBridges =
			(HashMap<BlockVector,Boolean>)this.delayedBridges.clone();
		HashMap<BlockVector, Integer> delayedInputsOldValue =
			(HashMap<BlockVector,Integer>)this.delayedInputsOldValue.clone();
		HashMap<BlockVector, Integer> delayedInputsNewValue =
			(HashMap<BlockVector,Integer>)this.delayedInputsNewValue.clone();
		
		this.delayedOutputs.clear();
		this.delayedBridges.clear();
		this.delayedInputsOldValue.clear();
		this.delayedInputsNewValue.clear();
		
		listener.setRsLock(true);
		
		for (BlockVector pos : delayedOutputs.keySet()) {
			if (CraftBook.getBlockID(pos) != BlockType.LEVER)
				continue;
			int data = CraftBook.getBlockData(pos);
			int newData = data & 0x7;

			if (!delayedOutputs.get(pos)) {
				newData = data & 0x7;
			} else {
				newData = data | 0x8;
			}

			if (newData != data) {
				CraftBook.setBlockData(pos, newData);
				etc.getServer().updateBlockPhysics(pos.getBlockX(),
						pos.getBlockY(), pos.getBlockZ(), newData);
			}
		}
		
		for (BlockVector pt : delayedBridges.keySet()) {
			int data = CraftBook.getBlockData(pt);
			boolean isOn = delayedBridges.get(pt);

			try {
				BlockBag bag = listener.getBlockBag(pt);
				bag.addSourcePosition(pt);

				if (data == 0x0) {
					Bridge.setBridgeState(pt, Bridge.Direction.EAST, bag, !isOn);
				} else if (data == 0x4) {
					Bridge.setBridgeState(pt, Bridge.Direction.SOUTH, bag, !isOn);
				} else if (data == 0x8) {
					Bridge.setBridgeState(pt, Bridge.Direction.WEST, bag, !isOn);
				} else if (data == 0xC) {
					Bridge.setBridgeState(pt, Bridge.Direction.NORTH, bag, !isOn);
				}
			} catch (OperationException e) {
			} catch (BlockSourceException e) {
			}
		}
		
		listener.setRsLock(false);
		
		for (BlockVector pt : delayedInputsOldValue.keySet()) {
			listener.onRedstoneChange(pt, delayedInputsOldValue.get(pt),
					delayedInputsNewValue.get(pt));
		}
	}
	
	/**
	 * Action to delay.
	 * 
	 * @author sk89q
	 */
	public static interface Action {
		public void run();
	}
}
