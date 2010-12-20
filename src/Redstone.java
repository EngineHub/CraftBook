// $Id$
/*
 * CraftBook
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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.Vector;

/**
 * Library for Redstone-related logic.
 * 
 * @author sk89q
 */
public class Redstone {
	/**
	 * Tests to see if a block is high, possibly including redstone wires. If
	 * there was no redstone at that location, null will be returned.
	 *
	 * @param pt
	 * @param icName
	 * @param considerWires
	 * @return
	 */
	static boolean isHighBinary(Vector pt, boolean considerWires) {
	    Boolean result = Redstone.isHigh(pt, CraftBook.getBlockID(pt), considerWires);
	    if (result != null && result) {
	        return true;
	    } else {
	        return false;
	    }
	}

	/**
	 * Attempts to detect redstone input. If there are many inputs to one
	 * block, only one of the inputs has to be high.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	static Boolean testAnyInput(Vector pt) {
		return testAnyInput(pt, true, false);
	}

	/**
	 * Attempts to detect redstone input. If there are many inputs to one
	 * block, only one of the inputs has to be high.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	static Boolean testAnyInput(Vector pt, boolean checkWiresAbove,
			boolean checkOnlyHorizontal) {
	    Boolean result = null;
	    Boolean temp = null;
	
	    int x = pt.getBlockX();
	    int y = pt.getBlockY();
	    int z = pt.getBlockZ();
	
	    if (checkWiresAbove) {
		    temp = testAnyInput(new Vector(x, y + 1, z), false, true);
		    if (temp != null) {
		        if (temp == true) {
		            return true;
		        } else {
		            result = false;
		        }
		    }
	    }
	    
	    if (!checkOnlyHorizontal) {
		    // Check block above
		    int above = CraftBook.getBlockID(x, y + 1, z);
		    temp = Redstone.isHigh(new Vector(x, y + 1, z), above, true);
		    if (temp != null) {
		        if (temp == true) {
		            return true;
		        } else {
		            result = false;
		        }
		    }
	    }
	
	    if (!checkOnlyHorizontal) {
		    // Check block below
		    int below = CraftBook.getBlockID(x, y - 1, z);
		    temp = Redstone.isHigh(new Vector(x, y - 1, z), below, true);
		    if (temp != null) {
		        if (temp == true) {
		            return true;
		        } else {
		            result = false;
		        }
		    }
	    }
		
	    int north = CraftBook.getBlockID(x - 1, y, z);
	    int south = CraftBook.getBlockID(x + 1, y, z);
	    int west = CraftBook.getBlockID(x, y, z + 1);
	    int east = CraftBook.getBlockID(x, y, z - 1);
	
	    // For wires that lead up to only this block
	    if (north == BlockType.REDSTONE_WIRE) {
	        temp = Redstone.isWireHigh(new Vector(x - 1, y, z),
	                                  new Vector(x - 1, y, z - 1),
	                                  new Vector(x - 1, y, z + 1));
	        if (temp != null) {
	            if (temp == true) {
	                return true;
	            } else {
	                result = false;
	            }
	        }
	    }
	
	    if (south == BlockType.REDSTONE_WIRE) {
	        temp = Redstone.isWireHigh(new Vector(x + 1, y, z),
	                                  new Vector(x + 1, y, z - 1),
	                                  new Vector(x + 1, y, z + 1));
	        if (temp != null) {
	            if (temp == true) {
	                return true;
	            } else {
	                result = false;
	            }
	        }
	    }
	
	    if (west == BlockType.REDSTONE_WIRE) {
	        temp = Redstone.isWireHigh(new Vector(x, y, z + 1),
	                                  new Vector(x + 1, y, z + 1),
	                                  new Vector(x - 1, y, z + 1));
	        if (temp != null) {
	            if (temp == true) {
	                return true;
	            } else {
	                result = false;
	            }
	        }
	    }
	
	    if (east == BlockType.REDSTONE_WIRE) {
	        temp = Redstone.isWireHigh(new Vector(x, y, z - 1),
	                                  new Vector(x + 1, y, z - 1),
	                                  new Vector(x - 1, y, z - 1));
	        if (temp != null) {
	            if (temp == true) {
	                return true;
	            } else {
	                result = false;
	            }
	        }
	    }
	
	    // The sides of the block
	    temp = Redstone.isHigh(new Vector(x - 1, y, z), north, false);
	    if (temp != null) {
	        if (temp == true) {
	            return true;
	        } else {
	            result = false;
	        }
	    }
	
	    temp = Redstone.isHigh(new Vector(x + 1, y, z), south, false);
	    if (temp != null) {
	        if (temp == true) {
	            return true;
	        } else {
	            result = false;
	        }
	    }
	    
	    temp = Redstone.isHigh(new Vector(x, y, z + 1), west, false);
	    if (temp != null) {
	        if (temp == true) {
	            return true;
	        } else {
	            result = false;
	        }
	    }
	
	    temp = Redstone.isHigh(new Vector(x, y, z - 1), east, false);
	    if (temp != null) {
	        if (temp == true) {
	            return true;
	        } else {
	            result = false;
	        }
	    }
	
	    return result;
	}

	/**
	 * Checks to see whether a wire is high and directed.
	 * 
	 * @param pt
	 * @param sidePt1
	 * @param sidePt2
	 * @return
	 */
	static Boolean isWireHigh(Vector pt, Vector sidePt1, Vector sidePt2) {
	    int side1 = CraftBook.getBlockID(sidePt1);
	    int side1Above = CraftBook.getBlockID(sidePt1.add(0, 1, 0));
	    int side1Below = CraftBook.getBlockID(sidePt1.add(0, -1, 0));
	    int side2 = CraftBook.getBlockID(sidePt2);
	    int side2Above = CraftBook.getBlockID(sidePt2.add(0, 1, 0));
	    int side2Below = CraftBook.getBlockID(sidePt2.add(0, -1, 0));
	
	    if (!BlockType.isRedstoneBlock(side1)
	            && !BlockType.isRedstoneBlock(side1Above)
	            && (!BlockType.isRedstoneBlock(side1Below) || side1 != 0)
	            && !BlockType.isRedstoneBlock(side2)
	            && !BlockType.isRedstoneBlock(side2Above)
	            && (!BlockType.isRedstoneBlock(side2Below) || side2 != 0)) {
	        return CraftBook.getBlockData(pt) > 0;
	    }
	
	    return null;
	}

	/**
	 * Tests to see if a block is high, possibly including redstone wires. If
	 * there was no redstone at that location, null will be returned.
	 * 
	 * @param pt
	 * @param type
	 * @param considerWires
	 * @return
	 */
	static Boolean isHigh(Vector pt, int type, boolean considerWires) {
	    if (type == BlockType.LEVER) {
	        return (CraftBook.getBlockData(pt) & 0x8) == 0x8;
	    } else if (type == BlockType.STONE_PRESSURE_PLATE) {
	        return (CraftBook.getBlockData(pt) & 0x1) == 0x1;
	    } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
	        return (CraftBook.getBlockData(pt) & 0x1) == 0x1;
	    } else if (type == BlockType.REDSTONE_TORCH_ON) {
	        return true;
	    } else if (type == BlockType.REDSTONE_TORCH_OFF) {
	        return false;
	    } else if (type == BlockType.STONE_BUTTON) {
	        return (CraftBook.getBlockData(pt) & 0x8) == 0x8;
	    } else if (considerWires && type == BlockType.REDSTONE_WIRE) {
	        return CraftBook.getBlockData(pt) > 0;
	    }
	
	    return null;
	}

	/**
	 * Tests to see if a block is high, possibly including redstone wires. If
	 * there was no redstone at that location, null will be returned.
	 *
	 * @param pt
	 * @param icName
	 * @param considerWires
	 * @return
	 */
	static Boolean isHigh(Vector pt, boolean considerWires) {
	    return isHigh(pt, CraftBook.getBlockID(pt), considerWires);
	}

	/**
	 * Tests the simple input at a block.
	 * 
	 * @param pt
	 * @return
	 */
	public static Boolean testSimpleInput(Vector pt) {
	    Boolean result = null;
	    Boolean temp;
	
	    temp = isHigh(pt.add(1, 0, 0), true);
	    if (temp != null) if (temp) return true; else result = false;
	    temp = isHigh(pt.add(-1, 0, 0), true);
	    if (temp != null) if (temp) return true; else result = false;
	    temp = isHigh(pt.add(0, 0, 1), true);
	    if (temp != null) if (temp) return true; else result = false;
	    temp = isHigh(pt.add(0, 0, -1), true);
	    if (temp != null) if (temp) return true; else result = false;
	    temp = isHigh(pt.add(0, -1, 0), true);
	    if (temp != null) if (temp) return true; else result = false;
	    return result;
	}

    /**
     * Sets the output state of a redstone IC at a location.
     *
     * @param getPosition
     * @param state
     */
    static void setOutput(Vector pos, boolean state) {
        if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
            int data = CraftBook.getBlockData(pos);
            int newData = data & 0x7;

            if (!state) {
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
    }

	/**
	 * Gets the output state of a redstone IC at a location.
	 *
	 * @param getPosition
	 * @param state
	 */
	static boolean getOutput(Vector pos) {
	    if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
	        return (CraftBook.getBlockData(pos) & 0x8) == 0x8;
	    } else {
	        return false;
	    }
	}

	/**
	 * Sets the output state of a minecart trigger at a location.
	 *
	 * @param getPosition
	 * @param state
	 */
	static void setTrackTrigger(Vector pos) {
	    if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
	        int data = CraftBook.getBlockData(pos);
	        int newData = 0;
	        boolean state = (data & 0x8) == 0x8;
	
	        if (state) {
	            newData = data & 0x7;
	        } else {
	            newData = data | 0x8;
	        }
	
	        CraftBook.setBlockData(pos, newData);
	        etc.getServer().updateBlockPhysics(
	                pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newData);
	    }
	}

}
