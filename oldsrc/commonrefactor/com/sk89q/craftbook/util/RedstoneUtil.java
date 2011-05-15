package com.sk89q.craftbook.util;
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
import com.sk89q.craftbook.access.WorldInterface;

/**
 * Library for Redstone-related logic.
 * 
 * @author sk89q
 */
public class RedstoneUtil {
    /**
     * Tests to see if a block is high, possibly including redstone wires. If
     * there was no redstone at that location, null will be returned.
     *
     * @param pt
     * @param icName
     * @param considerWires
     * @return
     */
    public static boolean isHighBinary(WorldInterface w, Vector v, boolean considerWires) {
        Boolean result = isHigh(w, v, 
                w.getId(v), considerWires);
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
    public static Boolean testAnyInput(WorldInterface w, Vector pt) {
        return testAnyInput(w, pt, true, false);
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
    public static Boolean testAnyInput(WorldInterface w, Vector pt, boolean checkWiresAbove,
            boolean checkOnlyHorizontal) {
        Boolean result = null;
        Boolean temp = null;
    
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
    
        if (checkWiresAbove) {
            temp = testAnyInput(w, new Vector(x, y + 1, z), false, true);
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
            int above = w.getId(x, y + 1, z);
            temp = isHigh(w, new Vector(x, y + 1, z), above, true);
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
            int below = w.getId(x, y - 1, z);
            temp = isHigh(w, new Vector(x, y - 1, z), below, true);
            if (temp != null) {
                if (temp == true) {
                    return true;
                } else {
                    result = false;
                }
            }
        }
        
        int north = w.getId(x - 1, y, z);
        int south = w.getId(x + 1, y, z);
        int west = w.getId(x, y, z + 1);
        int east = w.getId(x, y, z - 1);
    
        // For wires that lead up to only this block
        if (north == BlockType.REDSTONE_WIRE) {
            temp = isWireHigh(w,new Vector(x - 1, y, z),
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
            temp = isWireHigh(w,new Vector(x + 1, y, z),
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
            temp = isWireHigh(w,new Vector(x, y, z + 1),
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
            temp = isWireHigh(w,new Vector(x, y, z - 1),
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
        temp = isHigh(w, new Vector(x - 1, y, z), north, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }
    
        temp = isHigh(w, new Vector(x + 1, y, z), south, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }
        
        temp = isHigh(w, new Vector(x, y, z + 1), west, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }
    
        temp = isHigh(w, new Vector(x, y, z - 1), east, false);
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
    public static Boolean isWireHigh(WorldInterface w, Vector pt, Vector sidePt1, Vector sidePt2) {
        int side1 = getID(w,sidePt1);
        int side1Above = getID(w,sidePt1.add(0, 1, 0));
        int side1Below = getID(w,sidePt1.add(0, -1, 0));
        int side2 = getID(w,sidePt2);
        int side2Above = getID(w,sidePt2.add(0, 1, 0));
        int side2Below = getID(w,sidePt2.add(0, -1, 0));
    
        if (!BlockType.isRedstoneBlock(side1)
                && !BlockType.isRedstoneBlock(side1Above)
                && (!BlockType.isRedstoneBlock(side1Below) || side1 != 0)
                && !BlockType.isRedstoneBlock(side2)
                && !BlockType.isRedstoneBlock(side2Above)
                && (!BlockType.isRedstoneBlock(side2Below) || side2 != 0)) {
            return getData(w,pt) > 0;
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
    public static Boolean isHigh(WorldInterface w, Vector pt, int type, boolean considerWires) {
        if (type == BlockType.LEVER) {
            return (getData(w,pt) & 0x8) == 0x8;
        } else if (type == BlockType.STONE_PRESSURE_PLATE) {
            return (getData(w,pt) & 0x1) == 0x1;
        } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
            return (getData(w,pt) & 0x1) == 0x1;
        } else if (type == BlockType.REDSTONE_TORCH_ON) {
            return true;
        } else if (type == BlockType.REDSTONE_TORCH_OFF) {
            return false;
        } else if (type == BlockType.STONE_BUTTON) {
            return (getData(w,pt) & 0x8) == 0x8;
        } else if (considerWires && type == BlockType.REDSTONE_WIRE) {
            return getData(w,pt) > 0;
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
    public static Boolean isHigh(WorldInterface w, Vector pt, boolean considerWires) {
        return isHigh(w, pt, getID(w,pt), considerWires);
    }

    /**
     * Tests the simple input at a block.
     * 
     * @param pt
     * @return
     */
    public static Boolean testSimpleInput(WorldInterface w, Vector pt) {
        Boolean result = null;
        Boolean temp;
    
        temp = isHigh(w,pt.add(1, 0, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isHigh(w,pt.add(-1, 0, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isHigh(w,pt.add(0, 0, 1), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isHigh(w,pt.add(0, 0, -1), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isHigh(w,pt.add(0, -1, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        return result;
    }

    /**
     * Sets the output state of a redstone IC at a location.
     *
     * @param getPosition
     * @param state
     */
    public static void setOutput(WorldInterface w, Vector pos, boolean state) {
        if (getID(w,pos) == BlockType.LEVER) {
            int data = getData(w,pos);
            int newData = data & 0x7;

            if (!state) {
                newData = data & 0x7;
            } else {
                newData = data | 0x8;
            }

            if (newData != data) {
                w.setDataAndUpdate(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newData);
            }
        }
    }

    /**
     * Gets the output state of a redstone IC at a location.
     *
     * @param getPosition
     * @param state
     */
    public static boolean getOutput(WorldInterface w, Vector pos) {
        if (getID(w,pos) == BlockType.LEVER) {
            return (getData(w,pos) & 0x8) == 0x8;
        } else {
            return false;
        }
    }
    
    /**
     * Toggles an output.
     * 
     * @param pos
     * @return
     */
    public static void toggleOutput(WorldInterface w, Vector pos) {
        if (getID(w,pos) == BlockType.LEVER) {
            setOutput(w, pos, (getData(w,pos) & 0x8) != 0x8);
        }
    }

    /**
     * Sets the output state of a minecart trigger at a location.
     *
     * @param getPosition
     * @param state
     */
    public static void setTrackTrigger(WorldInterface w, Vector pos) {
        int x = pos.getBlockX();
        int y = pos.getBlockY();
        int z = pos.getBlockZ();
        if (w.getId(x,y,z) == BlockType.LEVER) {
            int data = w.getId(x,y,z);
            int newData = 0;
            boolean state = (data & 0x8) == 0x8;
    
            if (state) {
                newData = data & 0x7;
            } else {
                newData = data | 0x8;
            }
    
            w.setDataAndUpdate(x, y, z, newData);
        }
    }

    private static int getID(WorldInterface w, Vector pt) {
        return w.getId(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    private static int getData(WorldInterface w, Vector pt) {
        return w.getData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
}
