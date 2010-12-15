/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.HashMap;

import com.sk89q.craftbook.BlockSourceException;
import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.BlockVector;
import com.sk89q.craftbook.OperationException;
import com.sk89q.craftbook.Vector;

public class RedstoneDelayer implements Runnable {
    private HashMap<BlockVector,Boolean> delayedOutputs = new HashMap<BlockVector,Boolean>();
    private HashMap<BlockVector,Boolean> delayedBridges = new HashMap<BlockVector,Boolean>();

    private HashMap<BlockVector,Integer> delayedInputsOldValue = new HashMap<BlockVector,Integer>();
    private HashMap<BlockVector,Integer> delayedInputsNewValue = new HashMap<BlockVector,Integer>();
    
    private CraftBookListener l;
    
    public RedstoneDelayer(CraftBookListener l) {
        this.l = l;
    }
    
    public void setOut(Vector pos, boolean value) {
        delayedOutputs.put(pos.toBlockVector(), value);
    }
    
    public void toggleBridge(Vector v, boolean value) {
        delayedBridges.put(v.toBlockVector(), value);
    }
    
    public void delayRsChange(BlockVector v, int oldValue, int newValue) {
        delayedInputsOldValue.put(v,oldValue);
        delayedInputsNewValue.put(v,newValue);
    }
    
    @SuppressWarnings("unchecked")
    public void run() {
        HashMap<BlockVector,Boolean> delayedOutputs = (HashMap<BlockVector, Boolean>) this.delayedOutputs.clone();
        HashMap<BlockVector,Boolean> delayedBridges = (HashMap<BlockVector, Boolean>) this.delayedBridges.clone();
        HashMap<BlockVector,Integer> delayedInputsOldValue = (HashMap<BlockVector, Integer>) this.delayedInputsOldValue.clone();
        HashMap<BlockVector,Integer> delayedInputsNewValue = (HashMap<BlockVector, Integer>) this.delayedInputsNewValue.clone();
        this.delayedOutputs.clear();
        this.delayedBridges.clear();
        this.delayedInputsOldValue.clear();
        this.delayedInputsNewValue.clear();
        l.setRsLock(true);
        for(BlockVector pos:delayedOutputs.keySet()) {
            if(CraftBook.getBlockID(pos)!=BlockType.LEVER) continue;
            int data = CraftBook.getBlockData(pos);
            int newData = data & 0x7;

            if (!delayedOutputs.get(pos)) {
                newData = data & 0x7;
            } else {
                newData = data | 0x8;
            }

            if (newData != data) {
                CraftBook.setBlockData(pos, newData);
                etc.getServer().updateBlockPhysics(
                        pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newData);
            }
        }
        for(BlockVector pt:delayedBridges.keySet()) {
            int data = CraftBook.getBlockData(pt);
            boolean isOn = delayedBridges.get(pt);
            
            try {
                BlockSource bag = l.getBlockSource(pt);
                bag.addSourcePosition(pt);

                if (data == 0x0) {
                    l.bridgeModule.setBridgeState(pt, Bridge.Direction.EAST, bag, !isOn);
                } else if (data == 0x4) {
                    l.bridgeModule.setBridgeState(pt, Bridge.Direction.SOUTH, bag, !isOn);
                } else if (data == 0x8) {
                    l.bridgeModule.setBridgeState(pt, Bridge.Direction.WEST, bag, !isOn);
                } else if (data == 0xC) {
                    l.bridgeModule.setBridgeState(pt, Bridge.Direction.NORTH, bag, !isOn);
                }
            } catch (OperationException e) {
            } catch (BlockSourceException e) {
            }
        }
        l.setRsLock(false);
        for(BlockVector pt:delayedInputsOldValue.keySet()) l.onRedstoneChange(pt, delayedInputsOldValue.get(pt), delayedInputsNewValue.get(pt));
    }
}
