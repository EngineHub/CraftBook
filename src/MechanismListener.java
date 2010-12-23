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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.sk89q.craftbook.*;

/**
 * Listener for mechanisms.
 * 
 * @author sk89q
 */
public class MechanismListener extends CraftBookDelegateListener {
	/**
     * Tracks copy saves to prevent flooding.
     */
    private Map<String,Long> lastCopySave =
            new HashMap<String,Long>();
    
    private boolean checkPermissions;
    private boolean checkCreatePermissions;
    private boolean redstoneToggleAreas = true;
    private int maxToggleAreaSize;
    private int maxUserToggleAreas;
    private boolean useBookshelves = true;
    private String bookReadLine;
    private Cauldron cauldronModule;
    private boolean useElevators = true;
    private boolean useGates = true;
    private boolean redstoneGates = true;
    private boolean useLightSwitches = true;
    private boolean useBridges = true;
    private boolean redstoneBridges = true;
    private boolean useDoors = true;
    private boolean redstoneDoors = true;
    private boolean useHiddenSwitches = true;
    private boolean useToggleAreas;
    private boolean dropBookshelves = true;
    private double dropAppleChance = 0;
    private boolean enableAmmeter = true;

    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public MechanismListener(CraftBook craftBook, CraftBookListener listener) {
		super(craftBook, listener);
	}

    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        maxToggleAreaSize = Math.max(0, properties.getInt("toggle-area-max-size", 5000));
    	maxUserToggleAreas = Math.max(0, properties.getInt("toggle-area-max-per-user", 30));

        useBookshelves = properties.getBoolean("bookshelf-enable", true);
        bookReadLine = properties.getString("bookshelf-read-text", "You pick out a book...");
        useLightSwitches = properties.getBoolean("light-switch-enable", true);
        useGates = properties.getBoolean("gate-enable", true);
        redstoneGates = properties.getBoolean("gate-redstone", true);
        useElevators = properties.getBoolean("elevators-enable", true);
        useBridges = properties.getBoolean("bridge-enable", true);
        redstoneBridges = properties.getBoolean("bridge-redstone", true);
        useHiddenSwitches = properties.getBoolean("door-enable", true);
        Bridge.allowableBridgeBlocks = Util.toBlockIDSet(properties.getString("bridge-blocks", "4,5,20,43"));
        Bridge.maxBridgeLength = properties.getInt("bridge-max-length", 30);
        useDoors = properties.getBoolean("door-enable", true);
        redstoneDoors = properties.getBoolean("door-redstone", true);
        Door.allowableDoorBlocks = Util.toBlockIDSet(properties.getString("door-blocks", "1,3,4,5,17,20,35,43,44,45,47,80,82"));
        Door.maxDoorLength = properties.getInt("door-max-length", 30);
        dropBookshelves = properties.getBoolean("drop-bookshelves", true);
        try {
            dropAppleChance = Double.parseDouble(properties.getString("apple-drop-chance", "0.5")) / 100.0;
        } catch (NumberFormatException e) {
            dropAppleChance = -1;
            logger.log(Level.WARNING, "Invalid apple drop chance setting in craftbook.properties");
        }
        useHiddenSwitches = properties.getBoolean("hidden-switches-enable", true);
        useToggleAreas = properties.getBoolean("toggle-areas-enable", true);
        redstoneToggleAreas = properties.getBoolean("toggle-areas-redstone", true);
        checkPermissions = properties.getBoolean("check-permissions", false);
        checkCreatePermissions = properties.getBoolean("check-create-permissions", false);
        cauldronModule = null;
        enableAmmeter = properties.getBoolean("ammeter", true);

        loadCauldron();
    }
    
    /**
     * Load the cauldron.
     */
    private void loadCauldron() {
		if (properties.getBoolean("cauldron-enable", true)) {
			try {
				CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");

				if (recipes.size() != 0) {
					cauldronModule = new Cauldron(recipes);
					logger.log(Level.INFO, recipes.size()
							+ " cauldron recipe(s) loaded");
				} else {
					logger.log(Level.WARNING,
							"cauldron-recipes.txt had no recipes");
				}
			} catch (IOException e) {
				logger.log(Level.INFO,
						"cauldron-recipes.txt not loaded: " + e.getMessage());
			}
		} else {
			cauldronModule = null;
		}
    }

    /**
     * Called before the console command is parsed. Return true if you don't
     * want the server command to be parsed by the server.
     * 
     * @param split
     * @return false if you want the command to be parsed.
     */
    public boolean onConsoleCommand(String[] split) {
    	if (split[0].equalsIgnoreCase("reload-cauldron")) {
    		loadCauldron();
    		return true;
    	}
    	
        return false;
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
    public void onDirectWireInput(final Vector pt,
    		final boolean isOn, final Vector changed) {
    	
        int type = CraftBook.getBlockID(pt);
        
        // Sign gates
        if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof Sign)) {
                return;
            }

            final Sign sign = (Sign)cblock;
            final String line2 = sign.getText(1);

            // Gate
            if (useGates && redstoneGates
                    && line2.equalsIgnoreCase("[Gate]")) {
                BlockBag bag = getBlockBag(pt);
                bag.addSourcePosition(pt);

                // A gate may toggle or not
                try {
                    GateSwitch.setGateState(pt, bag, isOn);
                } catch (BlockSourceException e) {
                }

            // Bridges
            } else if (useBridges != false
                    && redstoneBridges
                    && type == BlockType.SIGN_POST
                    && line2.equalsIgnoreCase("[Bridge]")) {
                craftBook.getDelay().delayAction(
                		new TickDelayer.Action(pt.toBlockVector(), 2) {
							@Override
							public void run() {
								int data = CraftBook.getBlockData(pt);
		
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
                		});

            // Doors
            } else if (useDoors != false
                    && redstoneDoors
                    && type == BlockType.SIGN_POST
                    && (line2.equalsIgnoreCase("[Door Up]")
                    	|| line2.equalsIgnoreCase("[Door Down]"))) {
                craftBook.getDelay().delayAction(
                		new TickDelayer.Action(pt.toBlockVector(), 2) {
							@Override
							public void run() {
			                    int data = CraftBook.getBlockData(pt);
			                    boolean upwards = line2.equalsIgnoreCase("[Door Up]");
			                    try {
			                        BlockBag bag = getBlockBag(pt);
			                        bag.addSourcePosition(pt);
			                        
			                        if (data == 0x0 || data == 0x8) { // East-west
			                            Door.toggleDoor(pt,
			                            		Door.Direction.NORTH_SOUTH, upwards, bag);
			                        } else if (data == 0x4 || data == 0xC) { // North-south
			                            Door.toggleDoor(pt,
			                            		Door.Direction.WEST_EAST, upwards, bag);
			                        }
			                    } catch (OperationException e) {
			                    } catch (BlockSourceException e) {
			                    }
							}
                		});

            // Toggle areas
            } else if (useToggleAreas && redstoneToggleAreas
                    && (line2.equalsIgnoreCase("[Toggle]")
                    || line2.equalsIgnoreCase("[Area]"))) {
            	final boolean isNewSign = line2.equalsIgnoreCase("[Area]");
                final String id = sign.getText(0);
                final String namespace = sign.getText(2);
            	
                craftBook.getDelay().delayAction(
            		new TickDelayer.Action(pt.toBlockVector(), 2) {
	    					@Override
	    					public void run() {
								BlockBag bag = listener.getBlockBag(pt);
								bag.addSourcePosition(pt);
	
								try {
	    			                CuboidCopy copy = null;
	    			                
	    			                if (isNewSign) {
	    			                	if (CopyManager.isValidNamespace(namespace)) {
	    			                		copy = listener.getCopyManager().load(
	            			                		"~" + namespace, id);
	    			                	} else if (namespace.equals("")) {
	    			                		copy = listener.getCopyManager().load(
	            			                		"global", id);
	    			                	}
	    			                } else {
	    			                	copy = listener.getCopyManager().load(
	    			                		"global", id);
	    			           		}
	    			           
	    			                if (copy != null && copy.distance(pt) <= 4) {
	    			                    if (isOn) {
	    			                        copy.paste(bag);
	    			                    } else {
	    			                    	copy.clear(bag);
	    			                    }
	    			                } 
	    			           } catch (CuboidCopyException e) {} 
	    			             catch (IOException e2) {}
	    			             catch (BlockSourceException e) {}
	    					}
	            		});
            }
        }
    }

    /**
     * Called when a block is hit with the primary attack.
     * 
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(Player player, Block block) {
        // Random apple drops
        if (dropAppleChance > 0 && block.getType() == BlockType.LEAVES
                && checkPermission(player, "/appledrops")) {
            if (block.getStatus() == 3) {
                if (Math.random() <= dropAppleChance) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            ItemType.APPLE);
                }
            }

        // Bookshelf drops
        } else if (dropBookshelves && block.getType() == BlockType.BOOKCASE
                && checkPermission(player, "/bookshelfdrops")) {
            if (block.getStatus() == 3) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            BlockType.BOOKCASE);
            }
        }

        return false;
    }

    /**
     * Called when either a sign, chest or furnace is changed.
     *
     * @param player player who changed it
     * @param cblock complex block that changed
     * @return true if you want any changes to be reverted
     */
    public boolean onComplexBlockChange(Player player, ComplexBlock cblock) {
        if (cblock instanceof Sign) {
            Sign sign = (Sign)cblock;
            int type = CraftBook.getBlockID(
            		cblock.getX(), cblock.getY(), cblock.getZ());
            
            String line2 = sign.getText(1);
            
            // Gate
            if (line2.equalsIgnoreCase("[Gate]")) {
                if (checkCreatePermissions && !player.canUseCommand("/makegate")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make gates.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                sign.setText(1, "[Gate]");
            	sign.update();
            	
            	listener.informUser(player);
            	
            	if (useGates) {
                	player.sendMessage(Colors.Gold + "Gate created!");
                } else {
                	player.sendMessage(Colors.Rose + "Gates are disabled on this server.");
                }
            	
            // Light switch
            } else if (line2.equalsIgnoreCase("[|]")
                    || line2.equalsIgnoreCase("[I]")) {
                if (checkCreatePermissions && !player.canUseCommand("/makelightswitch")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make light switches.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                sign.setText(1, "[I]");
            	sign.update();
            	
            	listener.informUser(player);
            	
            	if (useLightSwitches) {
                	player.sendMessage(Colors.Gold + "Light switch created!");
                } else {
                	player.sendMessage(Colors.Rose + "Light switches are disabled on this server.");
                }

            // Elevator
            } else if (line2.equalsIgnoreCase("[Lift Up]")
                    || line2.equalsIgnoreCase("[Lift Down]")
                    || line2.equalsIgnoreCase("[Lift]")) {
                if (checkCreatePermissions && !player.canUseCommand("/makeelevator")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make elevators.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }

                if (line2.equalsIgnoreCase("[Lift Up]")) {
                    sign.setText(1, "[Lift Up]");
                } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                	sign.setText(1, "[Lift Down]");
                } else if (line2.equalsIgnoreCase("[Lift]")) {
                	sign.setText(1, "[Lift]");
                }
                sign.update();
                
            	listener.informUser(player);
            	
            	if (useElevators) {
                    Vector pt = new Vector(cblock.getX(), cblock.getY(), cblock.getZ());
                    
                    if (line2.equalsIgnoreCase("[Lift Up]")) {
                        if (Elevator.hasLinkedLift(pt, true)) {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created and linked!");
                        } else {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created but not yet linked to an existing lift sign.");
                        }
                    } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                        if (Elevator.hasLinkedLift(pt, false)) {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created and linked!");
                        } else {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created but not yet linked to an existing lift sign.");
                        }
                    } else if (line2.equalsIgnoreCase("[Lift]")) {
                        if (Elevator.hasLinkedLift(pt, true)
                        		|| Elevator.hasLinkedLift(pt, false)) {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created and linked!");
                        } else {
                        	player.sendMessage(Colors.Gold
                        			+ "Elevator created but not yet linked to an existing lift sign.");
                        }
                    }
                } else {
                	player.sendMessage(Colors.Rose + "Elevators are disabled on this server.");
                }

            // Toggle areas
            } else if (line2.equalsIgnoreCase("[Toggle]")) {
                if (checkCreatePermissions && !player.canUseCommand("/maketogglearea")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make toggle areas.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                sign.setText(1, "[Toggle]");
            	sign.update();
            	
            	listener.informUser(player);
            	
            	if (useToggleAreas) {
                	player.sendMessage(Colors.Gold + "Area toggle created!");
                } else {
                	player.sendMessage(Colors.Rose + "Area toggles are disabled on this server.");
                }

            // Toggle areas
            } else if (line2.equalsIgnoreCase("[Area]")) {
                if (checkCreatePermissions && !player.canUseCommand("/maketogglearea")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make toggle areas.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                String namespace = sign.getText(2);
                
                String expected = player.getName();
                if (expected.length() > 15) {
                	expected = expected.substring(0, 15);
                }
                
                if (namespace.equals("")
                		|| namespace.equalsIgnoreCase(expected)) {
                	sign.setText(2, player.getName());
                } else if (namespace.equals("@")) {
                    if (!player.canUseCommand("/savensarea")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make global area toggles.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }

                	sign.setText(2, "");
                } else {
                    if (!player.canUseCommand("/savensarea")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make area toggles for other namespaces.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }
                }
                
                sign.setText(1, "[Area]");
            	sign.update();
            	
            	listener.informUser(player);
            	
            	if (useToggleAreas) {
                	player.sendMessage(Colors.Gold + "Area toggle created!");
                } else {
                	player.sendMessage(Colors.Rose + "Area toggles are disabled on this server.");
                }

            // Bridges
            } else if (line2.equalsIgnoreCase("[Bridge]")) {
                if (checkCreatePermissions && !player.canUseCommand("/makebridge")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make bridges.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                sign.setText(1, "[Bridge]");
            	sign.update();

            	listener.informUser(player);

            	if (useBridges) {
                    int data = CraftBook.getBlockData(
                    		cblock.getX(), cblock.getY(), cblock.getZ());
                    
                    if (type == BlockType.WALL_SIGN) {
                    	player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                	} else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
	                	player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
	                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
	                    return true;
					}
					
                	player.sendMessage(Colors.Gold + "Bridge created!");
                } else {
                	player.sendMessage(Colors.Rose + "Bridges are disabled on this server.");
                }

            // Doors
            } else if (line2.equalsIgnoreCase("[Door Up]")
            		|| line2.equalsIgnoreCase("[Door Down]")) {
                if (checkCreatePermissions && !player.canUseCommand("/makedoor")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make doors.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }
                
                sign.setText(1, line2.equalsIgnoreCase("[Door Up]") ?
                		"[Door Up]" : "[Door Down]");
            	sign.update();

            	listener.informUser(player);

            	if (useDoors) {
                    int data = CraftBook.getBlockData(
                    		cblock.getX(), cblock.getY(), cblock.getZ());
                    
                    if (type == BlockType.WALL_SIGN) {
                    	player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                	} else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
	                	player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
	                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
	                    return true;
					}
					
                	player.sendMessage(Colors.Gold + "Door created!");
                } else {
                	player.sendMessage(Colors.Rose + "Doors are disabled on this server.");
                }
			}
		}

		return false;
	}

	/**
	 * Called when a block is being attempted to be placed.
	 * 
	 * @param player
	 * @param blockClicked
	 * @param itemInHand
	 * @return
	 */
    @Override
    public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
        try {
            handleBlockUse(player, blockClicked, item.getItemId());
        } catch (OutOfBlocksException e) {
            player.sendMessage(Colors.Rose + "Uh oh! Ran out of: " + Util.toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby block sources have the necessary");
            player.sendMessage(Colors.Rose + "materials.");
        } catch (OutOfSpaceException e) {
            player.sendMessage(Colors.Rose + "No room left to put: " + Util.toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby block sources have free slots.");
        } catch (BlockSourceException e) {
            player.sendMessage(Colors.Rose + "Error: " + e.getMessage());
        }
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    private boolean handleBlockUse(Player player, Block blockClicked,
    		int itemInHand)
    		throws BlockSourceException {

        int current = -1;

        // Ammeter
        if (enableAmmeter && itemInHand == 263) { // Coal
            int type = blockClicked.getType();
            int data = CraftBook.getBlockData(blockClicked.getX(),
                    blockClicked.getY(), blockClicked.getZ());
            
            if (type == BlockType.LEVER) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_TORCH_ON) {
                current = 15;
            } else if (type == BlockType.REDSTONE_TORCH_OFF) {
                current = 0;
            } else if (type == BlockType.STONE_BUTTON) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_WIRE) {
                current = data;
            }

            if (current > -1) {
                player.sendMessage(Colors.Yellow + "Ammeter: "
                        + Colors.Yellow + "["
                        + Colors.Yellow + Util.repeatString("|", current)
                        + Colors.Black + Util.repeatString("|", 15 - current)
                        + Colors.Yellow + "] "
                        + Colors.White
                        + current + " A");
            } else {
                player.sendMessage(Colors.Yellow + "Ammeter: " + Colors.Red + "Not supported.");
            }

            return false;
        }

        int plyX = (int)Math.floor(player.getLocation().x);
        int plyY = (int)Math.floor(player.getLocation().y);
        int plyZ = (int)Math.floor(player.getLocation().z);

        // Book reading
        if (useBookshelves
                && blockClicked.getType() == BlockType.BOOKCASE
                && checkPermission(player, "/readbooks")) {
            BookReader.readBook(player, bookReadLine);
            return true;

        // Sign buttons
        } else if (blockClicked.getType() == BlockType.WALL_SIGN ||
                blockClicked.getType() == BlockType.SIGN_POST ||
                CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN ||
                CraftBook.getBlockID(plyX, plyY, plyZ) == BlockType.WALL_SIGN) {
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            // Because sometimes the player is *inside* the block with a sign,
            // it becomes impossible for the player to select the sign but
            // may try anyway, so we're fudging detection for this case
            Vector pt;
            if (blockClicked.getType() == BlockType.WALL_SIGN
                    || blockClicked.getType() == BlockType.SIGN_POST) {
                pt = new Vector(x, y, z);
            } else if (CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN) {
                pt = new Vector(plyX, plyY + 1, plyZ);
                x = plyX;
                y = plyY + 1;
                z = plyZ;
            } else {
                pt = new Vector(plyX, plyY, plyZ);
                x = plyX;
                y = plyY;
                z = plyZ;
            }

            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;
                String line2 = sign.getText(1);

                // Gate
                if (useGates && line2.equalsIgnoreCase("[Gate]")
                        && checkPermission(player, "/gate")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);

                    // A gate may toggle or not
                    if (GateSwitch.toggleGates(pt, bag)) {
                        player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
                    }
                
                // Light switch
                } else if (useLightSwitches &&
                        (line2.equalsIgnoreCase("[|]") || line2.equalsIgnoreCase("[I]"))
                        && checkPermission(player, "/lightswitch")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);
                    
                    return LightSwitch.toggleLights(pt, bag);

                // Elevator
                } else if (useElevators
                        && (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]"))
                        && checkPermission(player, "/elevator")) {

                    // Go up or down?
                    boolean up = line2.equalsIgnoreCase("[Lift Up]");
                    Elevator.performLift(player, pt, up);
                    
                    return true;

                // Toggle areas
                } else if (useToggleAreas != false
                        && (line2.equalsIgnoreCase("[Toggle]")
                        		|| line2.equalsIgnoreCase("[Area]"))
                        && checkPermission(player, "/togglearea")) {
                    String id = sign.getText(0);
                    String namespace = sign.getText(2);

                    if (id.trim().length() == 0) {
                        player.sendMessage(Colors.Rose + "Area name must be the first line.");
                        return true;
                    } else if (!CopyManager.isValidName(id)) {
                        player.sendMessage(Colors.Rose + "Not a valid area name (1st sign line)!");
                        return true;
                    }

                    try {
                    	boolean isNewArea = line2.equalsIgnoreCase("[Area]");
                    	
                        BlockBag bag = getBlockBag(pt);
                        bag.addSourcePosition(pt);
                        CuboidCopy copy = null;
                        
                        if (isNewArea && CopyManager.isValidNamespace(namespace)) {
                        	copy = listener.getCopyManager().load("~" + namespace, id);
                        } else if (namespace.equals("")) {
                        	copy = listener.getCopyManager().load("global", id);
                        }
                        
                        if (copy == null) {
                            player.sendMessage(Colors.Rose + "Specified area (" + id + ") " +
                            		"doesn't exist.");
                            return true;
                        }
                        
                        if (isNewArea || copy.distance(pt) <= 4) {
                            copy.toggle(bag);
                            
                            // Get missing
                            Map<Integer,Integer> missing = bag.getMissing();
                            if (missing.size() > 0) {
                                for (Map.Entry<Integer,Integer> entry : missing.entrySet()) {
                                    player.sendMessage(Colors.Rose + "Missing "
                                            + entry.getValue() + "x "
                                            + Util.toBlockName(entry.getKey()));
                                }
                            } else {
                                player.sendMessage(Colors.Gold + "Toggled!");
                            }
                        } else {
                            player.sendMessage(Colors.Rose + "This sign is too far away!");
                        }
                    } catch (CuboidCopyException e) {
                        player.sendMessage(Colors.Rose + "Could not load area: " + e.getMessage());
                    } catch (IOException e2) {
                        player.sendMessage(Colors.Rose + "Could not load area: " + e2.getMessage());
                    }
                    
                    return true;

                // Bridges
                } else if (useBridges
                        && blockClicked.getType() == BlockType.SIGN_POST
                        && line2.equalsIgnoreCase("[Bridge]")
                        && checkPermission(player, "/bridge")) {
                    int data = CraftBook.getBlockData(x, y, z);

                    try {
                        BlockBag bag = getBlockBag(pt);
                        bag.addSourcePosition(pt);
                        
                        if (data == 0x0) {
                            Bridge.toggleBridge(new Vector(x, y, z), Bridge.Direction.EAST, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0x4) {
                        	Bridge.toggleBridge(new Vector(x, y, z), Bridge.Direction.SOUTH, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0x8) {
                        	Bridge.toggleBridge(new Vector(x, y, z), Bridge.Direction.WEST, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0xC) {
                        	Bridge.toggleBridge(new Vector(x, y, z), Bridge.Direction.NORTH, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else {
                            player.sendMessage(Colors.Rose + "That sign is not in a right direction.");
                        }
                    } catch (OperationException e) {
                        player.sendMessage(Colors.Rose + e.getMessage());
                    }
                    
                    return true;

                // Doors
                } else if (useDoors
                        && blockClicked.getType() == BlockType.SIGN_POST
                        && (line2.equalsIgnoreCase("[Door Up]")
                        		|| line2.equalsIgnoreCase("[Door Down]"))
                        && checkPermission(player, "/door")) {
                    int data = CraftBook.getBlockData(x, y, z);
                    boolean upwards = line2.equalsIgnoreCase("[Door Up]");

                    try {
                        BlockBag bag = getBlockBag(pt);
                        bag.addSourcePosition(pt);
                        
                        if (data == 0x0 || data == 0x8) { // East-west
                            Door.toggleDoor(new Vector(x, y, z),
                            		Door.Direction.NORTH_SOUTH, upwards, bag);
                            player.sendMessage(Colors.Gold + "Door toggled.");
                        } else if (data == 0x4 || data == 0xC) { // North-south
                            Door.toggleDoor(new Vector(x, y, z),
                            		Door.Direction.WEST_EAST, upwards, bag);
                            player.sendMessage(Colors.Gold + "Door toggled.");
                        } else {
                            player.sendMessage(Colors.Rose + "That sign is not in a right direction.");
                        }
                    } catch (OperationException e) {
                        player.sendMessage(Colors.Rose + e.getMessage());
                    }
                    
                    return true;
                }
            }

        // Cauldron
        } else if (cauldronModule != null
                && checkPermission(player, "/cauldron")) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            cauldronModule.preCauldron(new Vector(x, y, z), player);

        }

        // Hidden switches
        if (useHiddenSwitches
        		&& itemInHand <= 0
                && blockClicked.getType() != BlockType.SIGN_POST
                && blockClicked.getType() != BlockType.WALL_SIGN
                && !BlockType.isRedstoneBlock(blockClicked.getType())) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

        	toggleHiddenSwitch(x, y - 1, z);
        	toggleHiddenSwitch(x, y + 1, z);
        	toggleHiddenSwitch(x - 1, y, z);
        	toggleHiddenSwitch(x + 1, y, z);
        	toggleHiddenSwitch(x, y, z - 1);
        	toggleHiddenSwitch(x, y, z + 1);
            
            return true;
        }

        return false;
    }
    
    /**
     * Toggle a hidden switch.
     * 
     * @param pt
     */
    private void toggleHiddenSwitch(int x, int y, int z) {
    	ComplexBlock cblock = etc.getServer().getComplexBlock(x, y, z);
    	
    	if (cblock instanceof Sign) {
    		Sign sign = (Sign)cblock;
    		
    		if (sign.getText(1).equalsIgnoreCase("[X]")) {
    			Redstone.toggleOutput(new Vector(x, y - 1, z));
    			Redstone.toggleOutput(new Vector(x, y + 1, z));
    			Redstone.toggleOutput(new Vector(x - 1, y, z));
    			Redstone.toggleOutput(new Vector(x + 1, y, z));
    			Redstone.toggleOutput(new Vector(x, y, z - 1));
    			Redstone.toggleOutput(new Vector(x, y, z + 1));
    		}
    	}
    }
    
    /**
     * Called when a command is run
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCheckedCommand(Player player, String[] split)
            throws InsufficientArgumentsException, LocalWorldEditBridgeException {
    	
        if ((split[0].equalsIgnoreCase("/savearea")
        		&& Util.canUse(player, "/savearea"))
        		|| (split[0].equalsIgnoreCase("/savensarea")
        		&& Util.canUse(player, "/savensarea"))) {
            boolean namespaced = split[0].equalsIgnoreCase("/savensarea");
            
            Util.checkArgs(split, namespaced ? 2 : 1, -1, split[0]);

            String id;
            String namespace;
            
            if (namespaced) {
            	id = Util.joinString(split, " ", 2);
            	namespace = split[1];

            	if (namespace.equalsIgnoreCase("@")) {
            		namespace = "global";
            	} else {
	                if (!CopyManager.isValidNamespace(namespace)) {
	                    player.sendMessage(Colors.Rose + "Invalid namespace name. For the global namespace, use @");
	                    return true;
	                }
	                namespace = "~" + namespace;
            	}
            } else {
            	id = Util.joinString(split, " ", 1);
            	String nameNamespace = player.getName();
            	
            	// Sign lines can only be 15 characters long while names
            	// can be up to 16 characters long
            	if (nameNamespace.length() > 15) {
            		nameNamespace = nameNamespace.substring(0, 15);
            	}

                if (!CopyManager.isValidNamespace(nameNamespace)) {
                    player.sendMessage(Colors.Rose + "You have an invalid player name.");
                    return true;
                }
                
                namespace = "~" + nameNamespace;
            }

            if (!CopyManager.isValidName(id)) {
                player.sendMessage(Colors.Rose + "Invalid area name.");
                return true;
            }
            
            try {
                Vector min = LocalWorldEditBridge.getRegionMinimumPoint(player);
                Vector max = LocalWorldEditBridge.getRegionMaximumPoint(player);
                Vector size = max.subtract(min).add(1, 1, 1);

                // Check maximum size
                if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > maxToggleAreaSize) {
                    player.sendMessage(Colors.Rose + "Area is larger than allowed "
                            + maxToggleAreaSize + " blocks.");
                    return true;
                }
                
                // Check to make sure that a user doesn't have too many toggle
                // areas (to prevent flooding the server with files)
                if (maxUserToggleAreas >= 0 && !namespace.equals("global")) {
                	int count = listener.getCopyManager().meetsQuota(
                			namespace, id, maxUserToggleAreas);

                	if (count > -1) {
	                    player.sendMessage(Colors.Rose + "You are limited to "
	                    		+ maxUserToggleAreas + " toggle area(s). You have "
	                    		+ count + " areas.");
	                    return true;
                	}
                }

                // Prevent save flooding
                Long lastSave = lastCopySave.get(player.getName());
                long now = System.currentTimeMillis();
                
                if (lastSave != null) {
                	if (now - lastSave < 1000 * 3) {
	                    player.sendMessage(Colors.Rose + "Please wait before saving again.");
	                    return true;
                	}
                }
                
                lastCopySave.put(player.getName(), now);
                
                // Copy
                CuboidCopy copy = new CuboidCopy(min, size);
                copy.copy();
                
                logger.info(player.getName() + " saving toggle area with folder '"
                		+ namespace + "' and ID '" + id + "'.");
                
                // Save
                try {
                    listener.getCopyManager().save(namespace, id, copy);
                    if (namespaced) {
                        player.sendMessage(Colors.Gold + "Area saved as '"
                        		+ id + "' under the specified namespace.");
                    } else {
                        player.sendMessage(Colors.Gold + "Area saved as '"
                        		+ id + "' under your player.");
                    }
                } catch (IOException e) {
                    player.sendMessage(Colors.Rose + "Could not save area: " + e.getMessage());
                }
            } catch (NoClassDefFoundError e) {
                player.sendMessage(Colors.Rose + "WorldEdit.jar does not exist in plugins/.");
            }

            return true;
        }

        return false;
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static CauldronCookbook readCauldronRecipes(String path)
            throws IOException {
    	
        File file = new File(path);
        FileReader input = null;
        CauldronCookbook cookbook = new CauldronCookbook();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank lines
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
                    continue;
                }

                String[] parts = line.split(":");
                
                if (parts.length < 3) {
                    logger.log(Level.WARNING, "Invalid cauldron recipe line in "
                            + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<Integer> ingredients = parseCauldronItems(parts[1]);
                    List<Integer> results = parseCauldronItems(parts[2]);
                    String[] groups = null;
                    
                    if (parts.length >= 4 && parts[3].trim().length() > 0) {
                    	groups = parts[3].split(",");
                    }
                    
                    CauldronRecipe recipe =
                            new CauldronRecipe(name, ingredients, results, groups);
                    cookbook.add(recipe);
                }
            }

            return cookbook;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Parse a list of cauldron items.
     * 
     * @param list
     * @return
     */
    private static List<Integer> parseCauldronItems(String list) {
        String[] parts = list.split(",");

        List<Integer> out = new ArrayList<Integer>();

        for (String part : parts) {
            int multiplier = 1;

            try {
                // Multiplier
                if (part.matches("^.*\\*([0-9]+)$")) {
                    int at = part.lastIndexOf("*");
                    multiplier = Integer.parseInt(
                            part.substring(at + 1, part.length()));
                    part = part.substring(0, at);
                }

                try {
                    for (int i = 0; i < multiplier; i++) {
                        out.add(Integer.valueOf(part));
                    }
                } catch (NumberFormatException e) {
                    int item = etc.getDataSource().getItem(part);

                    if (item > 0) {
                        for (int i = 0; i < multiplier; i++) {
                            out.add(item);
                        }
                    } else {
                        logger.log(Level.WARNING, "Cauldron: Unknown item " + part);
                    }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                logger.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
            }
        }

        return out;
    }
    
    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkPermission(Player player, String command) {
        return !checkPermissions || player.canUseCommand(command);
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
    	lastCopySave.remove(player.getName());
    }
}
