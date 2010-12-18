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

import java.util.HashMap;
import java.util.Map;
import com.sk89q.craftbook.*;

/**
 * Delegate listener for vehicle-related hooks and features.
 * 
 * @author sk89q
 */
public class VehicleListener extends CraftBookDelegateListener {
	/**
     * Last minecart message sent from a sign to a player.
     */
    private Map<String,String> lastMinecartMsg =
            new HashMap<String,String>();
    /**
     * Last time minecart message sent from a sign to a player.
     */
    private Map<String,Long> lastMinecartMsgTime =
            new HashMap<String,Long>();
    
    // Settings
    private boolean minecartControlBlocks = true;
    private boolean slickPressurePlates = false;
    private boolean unoccupiedCoast = true;
    private boolean inCartControl = true;
    private boolean minecartDispensers = true;
    private boolean minecartTrackMessages = true;
    private int minecart25xBoostBlock = BlockType.GOLD_ORE;
    private int minecart100xBoostBlock = BlockType.GOLD_BLOCK;
    private int minecart50xSlowBlock = BlockType.SLOW_SAND;
    private int minecart20xSlowBlock = BlockType.GRAVEL;
    private int minecartStationBlock = BlockType.OBSIDIAN;
    private int minecartReverseBlock = BlockType.CLOTH;
    private int minecartTriggerBlock = BlockType.IRON_ORE;
    private int minecartEjectBlock = BlockType.IRON_BLOCK;

    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     * @param properties
     */
    public VehicleListener(CraftBook craftBook, CraftBookListener listener) {
		super(craftBook, listener);
	}

    /**
    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        minecartControlBlocks = properties.getBoolean("minecart-control-blocks", true);
        slickPressurePlates = properties.getBoolean("hinder-minecart-pressure-plate-slow", true);
        unoccupiedCoast = properties.getBoolean("minecart-hinder-unoccupied-slowdown", true);
        inCartControl = properties.getBoolean("minecart-in-cart-control", true);
        minecartDispensers = properties.getBoolean("minecart-dispensers", true);
        minecartTrackMessages = properties.getBoolean("minecart-track-messages", true);
        minecart25xBoostBlock = properties.getInt("minecart-25x-boost-block", BlockType.GOLD_ORE);
        minecart100xBoostBlock = properties.getInt("minecart-100x-boost-block", BlockType.GOLD_BLOCK);
        minecart50xSlowBlock = properties.getInt("minecart-50x-slow-block", BlockType.SLOW_SAND);
        minecart20xSlowBlock = properties.getInt("minecart-20x-slow-block", BlockType.GRAVEL);
        minecartStationBlock = properties.getInt("minecart-station-block", BlockType.OBSIDIAN);
        minecartReverseBlock = properties.getInt("minecart-reverse-block", BlockType.CLOTH);
        minecartTriggerBlock = properties.getInt("minecart-trigger-block", BlockType.IRON_ORE);
        minecartEjectBlock = properties.getInt("minecart-eject-block", BlockType.IRON_BLOCK);
    }

    /**
     * Handles the wire input at a block in the case when the wire is
     * directly connected to the block in question only.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onDirectWireInput(Vector pt, boolean isOn, Vector changed) {
        int type = CraftBook.getBlockID(pt);
        
        // Minecart dispenser
        if (minecartDispensers && type == BlockType.CHEST
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
        	
        	// Rising edge-triggered only
            if (!isOn) {
                return;
            }
            
            Vector signPos = pt.add(0, -2, 0);

            // Try two blocks underneath first
            if (!Util.doesSignSay(signPos, 1, "[Dispenser]")) {
                signPos = pt.add(0, -1, 0);
            }

            // Try three blocks underneath
            if (!Util.doesSignSay(signPos, 1, "[Dispenser]")) {
                return;
            }

            Vector dir = Util.getSignPostOrthogonalBack(signPos, 1)
                    .subtract(signPos);
            Vector depositPt = pt.add(dir.multiply(2.5));

            if (CraftBook.getBlockID(depositPt) != BlockType.MINECART_TRACKS) {
                return;
            }

            NearbyChestBlockSource blockBag = new NearbyChestBlockSource(pt);
            blockBag.addSingleSourcePosition(pt);
            blockBag.addSingleSourcePosition(pt.add(1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(-1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(0, 0, 1));
            blockBag.addSingleSourcePosition(pt.add(0, 0, -1));

            try {
                blockBag.fetchBlock(ItemType.MINECART);
                jo minecart = new jo(etc.getMCServer().e,
                        depositPt.getX(), depositPt.getY(),
                        depositPt.getZ(), 0);
                etc.getMCServer().e.a(minecart);
            } catch (BlockSourceException e) {
                // No minecarts
            }
        // Minecart station
        } else if (minecartControlBlocks && type == minecartStationBlock
                && CraftBook.getBlockID(pt.add(0, 1, 0)) == BlockType.MINECART_TRACKS
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());

            // Maybe it's the sign directly below
            if (cblock == null || !(cblock instanceof Sign)) {
                cblock = etc.getServer().getComplexBlock(
                        pt.getBlockX(), pt.getBlockY() - 1, pt.getBlockZ());
            }

            if (cblock == null || !(cblock instanceof Sign)) {
                return;
            }

            Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);

            if (!line2.equalsIgnoreCase("[Station]")) {
                return;
            }

            Vector motion;
            int data = CraftBook.getBlockData(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());
            
            if (data == 0x0) {
                motion = new Vector(0, 0, -0.3);
            } else if (data == 0x4) {
                motion = new Vector(0.3, 0, 0);
            } else if (data == 0x8) {
                motion = new Vector(0, 0, 0.3);
            } else if (data == 0xC) {
                motion = new Vector(-0.3, 0, 0);
            } else {
                return;
            }

            for (BaseEntity ent : etc.getServer().getEntityList()) {
                if (ent instanceof Minecart) {
                    Minecart minecart = (Minecart)ent;
                    int cartX = (int)Math.floor(minecart.getX());
                    int cartY = (int)Math.floor(minecart.getY());
                    int cartZ = (int)Math.floor(minecart.getZ());

                    if (cartX == pt.getBlockX()
                            && cartY == pt.getBlockY() + 1
                            && cartZ == pt.getBlockZ()) {
                        minecart.setMotion(motion.getX(), motion.getY(), motion.getZ());
                    }
                }
            }
        }
    }
    
    /**
     * Called when a vehicle changes block
     *
     * @param vehicle the vehicle
     * @param blockX coordinate x
     * @param blockY coordinate y
     * @param blockZ coordinate z
     */
    @Override
    public void onVehiclePositionChange(BaseVehicle vehicle,
            int blockX, int blockY, int blockZ) {
        if (!minecartControlBlocks && !minecartTrackMessages && !minecartDispensers) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;

            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == minecart25xBoostBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 1.25);
                        minecart.setMotionZ(minecart.getMotionZ() * 1.25);
                        return;
                    }
                } else if (under == minecart100xBoostBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 2);
                        minecart.setMotionZ(minecart.getMotionZ() * 2);
                        return;
                    }
                } else if (under == minecart50xSlowBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.5);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.5);
                        return;
                    }
                } else if (under == minecart20xSlowBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.8);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.8);
                        return;
                    }
                } else if (under == minecartReverseBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        Vector signPos = new Vector(blockX, blockY - 2, blockZ);
                        boolean reverseX = true;
                        boolean reverseZ = true;

                        // Directed reverse block
                        if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                && Util.doesSignSay(signPos, 1, "[Reverse]")) {
                            Vector dir = Util.getSignPostOrthogonalBack(signPos, 1)
                                    .subtract(signPos);

                            // Acceptable sign direction
                            if (dir != null) {
                                if (CBMath.isSameSign(minecart.getMotionX(),
                                        dir.getBlockX())) {
                                    reverseX = false;
                                }
                                if (CBMath.isSameSign(minecart.getMotionZ(),
                                        dir.getBlockZ())) {
                                    reverseZ = false;
                                }
                            }
                        }
                        
                        if (reverseX) {
                            minecart.setMotionX(minecart.getMotionX() * -1);
                        }
                        if (reverseZ) {
                            minecart.setMotionZ(minecart.getMotionZ() * -1);
                        }
                        
                        return;
                    }
                } else if (under == minecartTriggerBlock) {
                    Redstone.setTrackTrigger(underPt.add(1, 0, 0));
                    Redstone.setTrackTrigger(underPt.add(-1, 0, 0));
                    Redstone.setTrackTrigger(underPt.add(0, 0, 1));
                    Redstone.setTrackTrigger(underPt.add(0, 0, -1));

                    return;
                } else if (under == minecartEjectBlock) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test == null || test) {
                        Player player = minecart.getPassenger();
                        if (player != null) {
                            // Let's find a place to put the player
                            Location loc = player.getLocation();
                            Vector signPos = new Vector(blockX, blockY - 2, blockZ);

                            if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                    && Util.doesSignSay(signPos, 1, "[Eject]")) {
                                Vector pos = Util.getSignPostOrthogonalBack(signPos, 1);

                                // Acceptable sign direction
                                if (pos != null) {
                                    pos = pos.setY(blockY);

                                    // Is the spot free?
                                    if (BlockType.canPassThrough(CraftBook.getBlockID(pos.add(0, 1, 0)))
                                            && BlockType.canPassThrough(CraftBook.getBlockID(pos))) {
                                        loc = new Location(
                                                pos.getBlockX(),
                                                pos.getBlockY(),
                                                pos.getBlockZ());

                                        ComplexBlock cBlock = etc.getServer().getComplexBlock(
                                                blockX, blockY - 2, blockZ);

                                        if (cBlock instanceof Sign) {
                                            Sign sign = (Sign)cBlock;
                                            String text = sign.getText(0);
                                            if (text.length() > 0) {
                                                player.sendMessage(Colors.Gold + "You've arrived at: "
                                                        + text);
                                            }
                                        }
                                    }
                                }
                            }

                            loc.x = loc.x + 0.5;
                            loc.z = loc.z + 0.5;

                            player.teleportTo(loc);
                        }
                    }

                    return;
                }
            }

            if (minecartTrackMessages
                    && CraftBook.getBlockID(underPt.add(0, -1, 0)) == BlockType.SIGN_POST) {
                Vector signPos = underPt.add(0, -1, 0);
                
                Boolean test = Redstone.testSimpleInput(signPos);

                if (test == null || test) {
                    ComplexBlock cblock = etc.getServer().getComplexBlock(
                            signPos.getBlockX(), signPos.getBlockY(), signPos.getBlockZ());

                    if (!(cblock instanceof Sign)) {
                        return;
                    }

                    Sign sign = (Sign)cblock;
                    String line1 = sign.getText(0);

                    if (line1.equalsIgnoreCase("[Print]")) {
                        Player player = minecart.getPassenger();
                        if (player == null) { return; }
                        
                        String name = player.getName();
                        String msg = sign.getText(1) + sign.getText(2) + sign.getText(3);
                        long now = System.currentTimeMillis();

                        if (lastMinecartMsg.containsKey(name)) {
                            String lastMessage = lastMinecartMsg.get(name);
                            if (lastMessage.equals(msg)
                                    && now < lastMinecartMsgTime.get(name) + 3000) {
                                return;
                            }
                        }

                        lastMinecartMsg.put(name, msg);
                        lastMinecartMsgTime.put(name, now);
                        player.sendMessage("> " + msg);
                    }
                }
            }

            if (minecartDispensers) {
                Vector pt = new Vector(blockX, blockY, blockZ);
                Vector depositPt = null;

                if (CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(-1, -1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(1, 0, 0);
                } else if (CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(1, -1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(-1, 0, 0);
                } else if (CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, -1, -1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, 1);
                } else if (CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, -1, 1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, -1);
                }

                if (depositPt != null) {
                    NearbyChestBlockSource blockBag = new NearbyChestBlockSource(depositPt);
                    blockBag.addSingleSourcePosition(depositPt);
                    blockBag.addSingleSourcePosition(depositPt.add(1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(-1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, 1));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, -1));

                    Minecart.Type type = null;
                    if(etc.getInstance().getVersion()<=131) {
                        switch(minecart.getEntity().d) {
                            case 0: type = Minecart.Type.Minecart;
                                    break;
                            case 1: type = Minecart.Type.StorageCart;
                                    break;
                            case 2: type = Minecart.Type.PoweredMinecart;
                                    break;
                        }
                    } else type = minecart.getType();

                    if(type == Minecart.Type.Minecart) {
                        try {
                            blockBag.storeBlock(ItemType.MINECART);
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                        }
                    } else if(type == Minecart.Type.StorageCart) {
                        //Storage cart contents are client side at the moment. 

                        // we need to first try and move the items out of the storage cart
                        // one at a time, until we run out of items or space. If we run out
                        // of items, then try to move the storage cart into the chest.
                        
                        //StorageMinecart sm = minecart.getStorage();
                        //Item[] itemArray = sm.getContents();
                        try {
                            // loop through each filled position in the storage cart
                            /*    
                            for(int i = 0; itemArray.length > i; i++) {
                                if(itemArray[i] == null) {
                                    continue;
                                }
                                // move the items into the chest one at a time.
                                while(itemArray[i].getAmount() > 0) {
                                    blockBag.storeBlock(itemArray[i].getItemId());
                                    itemArray[i].setAmount(itemArray[i].getAmount() - 1);
                                }
                                // now that all of that item is gone, null the position
                                itemArray[i] = null;
                            }
                            */

                            // Split the cart into a Minecart, and an chest, as the dispenser cannot
                            // release storage minecarts.
                            blockBag.storeBlock(ItemType.MINECART);
                            blockBag.storeBlock(BlockType.CHEST);
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                        }
                    } else if(type == Minecart.Type.PoweredMinecart) {
                        // Split the cart into a Minecart, and an furnace, as the dispenser cannot
                        // release storage minecarts.
                        try {
                            blockBag.storeBlock(ItemType.MINECART);
                            blockBag.storeBlock(BlockType.FURNACE);
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when a vehicle enters or leaves a block
     *
     * @param vehicle the vehicle
     */
    @Override
    public void onVehicleUpdate(BaseVehicle vehicle) {
        if (!minecartControlBlocks && !unoccupiedCoast) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;

            int blockX = (int)Math.floor(minecart.getX());
            int blockY = (int)Math.floor(minecart.getY());
            int blockZ = (int)Math.floor(minecart.getZ());
            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == BlockType.OBSIDIAN) {
                    Boolean test = Redstone.testSimpleInput(underPt);

                    if (test != null) {
                        if (!test) {
                            minecart.setMotion(0, 0, 0);
                            return;
                        }
                    }
                }
            }
            
            int block = CraftBook.getBlockID(blockX, blockY, blockZ);
            if (slickPressurePlates
                    && block == BlockType.STONE_PRESSURE_PLATE
                    || block == BlockType.WOODEN_PRESSURE_PLATE) {
                // Numbers from code
                minecart.setMotion(minecart.getMotionX() / 0.55000000000000004D,
                                   0,
                                   minecart.getMotionZ() / 0.55000000000000004D);
            }

            if (unoccupiedCoast && minecart.getPassenger() == null) {
                minecart.setMotionX(minecart.getMotionX() * 1.0188250000000001D);
                minecart.setMotionZ(minecart.getMotionZ() * 1.0188250000000001D);
            }
        }
    }

    /**
     * Called when vehicle receives damage
     *
     * @param vehicle
     * @param attacker entity that dealt the damage
     * @param damage
     * @return false to set damage
     */
    @Override
    public boolean onVehicleDamage(BaseVehicle vehicle,
            BaseEntity attacker, int damage) {

        if (!inCartControl) {
            return false;
        }

        Player passenger = vehicle.getPassenger();

        // Player.equals() now works correctly as of recent hMod versions
        if (passenger != null && vehicle instanceof Minecart
                && attacker.isPlayer()
                && attacker.getPlayer().equals(passenger)) {
            double speed = Math.sqrt(Math.pow(vehicle.getMotionX(), 2)
                    + Math.pow(vehicle.getMotionY(), 2)
                    + Math.pow(vehicle.getMotionZ(), 2));

            if (speed > 0.01) { // Stop the cart
                vehicle.setMotion(0, 0, 0);
            } else {
                // From hey0's code, and then stolen from WorldEdit
                double rot = (passenger.getRotation() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }

                String dir = etc.getCompassPointForDirection(rot);
                
                if (dir.equals("N")) {
                    vehicle.setMotion(-0.1, 0, 0);
                } else if(dir.equals("NE")) {
                    vehicle.setMotion(-0.1, 0, -0.1);
                } else if(dir.equals("E")) {
                    vehicle.setMotion(0, 0, -0.1);
                } else if(dir.equals("SE")) {
                    vehicle.setMotion(0.1, 0, -0.1);
                } else if(dir.equals("S")) {
                    vehicle.setMotion(0.1, 0, 0);
                } else if(dir.equals("SW")) {
                    vehicle.setMotion(0.1, 0, 0.1);
                } else if(dir.equals("W")) {
                    vehicle.setMotion(0, 0, 0.1);
                } else if(dir.equals("NW")) {
                    vehicle.setMotion(-0.1, 0, 0.1);
                }
            }

            return true;
        }

        return false;
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        lastMinecartMsg.remove(player.getName());
        lastMinecartMsgTime.remove(player.getName());
    }
}
