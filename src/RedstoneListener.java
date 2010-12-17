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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import lymia.customic.*;
import lymia.perlstone.Perlstone_1_0;

/**
 * Event listener for redstone enhancements such as redstone pumpkins and
 * integrated circuits. Redstone hooks for mechanisms are in
 * MechanismListener.
 * 
 * @author sk89q
 */
public class RedstoneListener extends CraftBookDelegateListener
		implements CustomICAccepter {
    
    /**
     * Currently registered ICs
     */
    private Map<String,RegisteredIC> icList = 
            new HashMap<String,RegisteredIC>();

    private boolean checkCreatePermissions;
    private boolean redstonePumpkins = true;
    private boolean redstoneICs = true;
    private boolean redstonePLCs = true;
    private boolean redstonePLCsRequirePermission = true;
    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public RedstoneListener(CraftBook craftBook, CraftBookListener listener) {
		super(craftBook, listener);
	}

    /**
     * Loads relevant configuration.
     */
	@Override
	public void loadConfiguration() {
		checkCreatePermissions = properties.getBoolean(
				"check-create-permissions", false);

		redstonePumpkins = properties.getBoolean("redstone-pumpkins", true);
		redstoneICs = properties.getBoolean("redstone-ics", true);
		redstonePLCs = properties.getBoolean("redstone-plcs", true);
		redstonePLCsRequirePermission = properties.getBoolean(
				"redstone-plcs-require-permission", false);

		if (properties.getBoolean("custom-ics", true)) {
			try {
				icList.clear();
				CustomICLoader.load("custom-ics.txt", this);
				addDefaultICs();
			} catch (CustomICException e) {
				logger.log(Level.SEVERE,
						"Failed to load custom IC file: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	


	private void addDefaultICs() {
		internalRegisterIC("MC1000", new MC1000(), ICType.SISO);
		internalRegisterIC("MC1001", new MC1001(), ICType.SISO);
		internalRegisterIC("MC1017", new MC1017(), ICType.SISO);
		internalRegisterIC("MC1018", new MC1018(), ICType.SISO);
		internalRegisterIC("MC1020", new MC1020(), ICType.SISO);
		internalRegisterIC("MC1025", new MC1025(), ICType.SISO);
		internalRegisterIC("MC1110", new MC1110(), ICType.SISO);
		internalRegisterIC("MC1111", new MC1111(), ICType.SISO);
		internalRegisterIC("MC1200", new MC1200(), ICType.SISO);
		internalRegisterIC("MC1201", new MC1201(), ICType.SISO);
		internalRegisterIC("MC1205", new MC1205(), ICType.SISO);
		internalRegisterIC("MC1206", new MC1206(), ICType.SISO);
		internalRegisterIC("MC1230", new MC1230(), ICType.SISO);
		internalRegisterIC("MC1231", new MC1231(), ICType.SISO);
		internalRegisterIC("MC2020", new MC2020(), ICType.SI3O);
		internalRegisterIC("MC3020", new MC3020(), ICType._3ISO);
		internalRegisterIC("MC3002", new MC3002(), ICType._3ISO);
		internalRegisterIC("MC3003", new MC3003(), ICType._3ISO);
		internalRegisterIC("MC3021", new MC3021(), ICType._3ISO);
		internalRegisterIC("MC3030", new MC3030(), ICType._3ISO);
		internalRegisterIC("MC3031", new MC3031(), ICType._3ISO);
		internalRegisterIC("MC3032", new MC3032(), ICType._3ISO);
		internalRegisterIC("MC3034", new MC3034(), ICType._3ISO);
		internalRegisterIC("MC3036", new MC3036(), ICType._3ISO);
		internalRegisterIC("MC3040", new MC3040(), ICType._3ISO);
		internalRegisterIC("MC3101", new MC3101(), ICType._3ISO);
		internalRegisterIC("MC3231", new MC3231(), ICType._3ISO);
		internalRegisterIC("MC4000", new MC4000(), ICType._3I3O);
		internalRegisterIC("MC4010", new MC4010(), ICType._3I3O);
		internalRegisterIC("MC4100", new MC4100(), ICType._3I3O);
		internalRegisterIC("MC4110", new MC4110(), ICType._3I3O);

		internalRegisterIC("MC5000", new DefaultPLC(new Perlstone_1_0()),
				ICType.VIVO, true);
		internalRegisterIC("MC5001", new DefaultPLC(new Perlstone_1_0()),
				ICType._3I3O, true);
	}

    /**
     * Called when either a sign, chest or furnace is changed.
     *
     * @param player
     *            player who changed it
     * @param cblock
     *            complex block that changed
     * @return true if you want any changes to be reverted
     */
    public boolean onComplexBlockChange(Player player, ComplexBlock cblock) {
        if (cblock instanceof Sign) {
            Sign sign = (Sign)cblock;
            int type = CraftBook.getBlockID(
                    cblock.getX(), cblock.getY(), cblock.getZ());
            
            String line2 = sign.getText(1);
            int len = line2.length();

            // ICs
            if (line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                    line2.charAt(len - 1) == ']') {

                // Check to see if the player can even create ICs
                if (checkCreatePermissions
                        && !player.canUseCommand("/makeic")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make ICs.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }

                String id = line2.substring(1, len - 1).toUpperCase();
                RegisteredIC ic = icList.get(id);

                if (ic != null) {
                    if (ic.isPlc) {
                        if (!redstonePLCs) {
                            player.sendMessage(Colors.Rose + "PLCs are not enabled.");
                            CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                            return false;
                        }
                    }
                    
                    if (canCreateIC(player,id,ic)) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make " + id + ".");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    } else {
                        // To check the environment
                        Vector pos = new Vector(cblock.getX(), cblock.getY(), cblock.getZ());
                        SignText signText = new SignText(
                            sign.getText(0), sign.getText(1), sign.getText(2),
                            sign.getText(3));

                        // Maybe the IC is setup incorrectly
                        String envError = ic.ic.validateEnvironment(pos, signText);

                        if (signText.isChanged()) {
                            sign.setText(0, signText.getLine1());
                            sign.setText(1, signText.getLine2());
                            sign.setText(2, signText.getLine3());
                            sign.setText(3, signText.getLine4());
                        }

                        if (envError != null) {
                            player.sendMessage(Colors.Rose
                                    + "Error: " + envError);
                            CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                            return true;
                        } else {
                            sign.setText(0, ic.ic.getTitle());
                            sign.setText(1, "[" + id + "]");
                        }
                    }
                } else {
                    sign.setText(1, Colors.Red + line2);
                    player.sendMessage(Colors.Rose + "Unrecognized IC: " + id);
                }

                if (!redstoneICs) {
                    player.sendMessage(Colors.Rose + "Warning: ICs are disabled.");
                } else if (type == BlockType.SIGN_POST) {
                    player.sendMessage(Colors.Rose + "Warning: IC signs must be on a wall.");
                }
                
                sign.update();

                return false;
            }
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
    public void onDirectWireInput(Vector pt, boolean isOn, Vector changed) {
        int type = CraftBook.getBlockID(pt);
        
        // Redstone pumpkins
        if (redstonePumpkins
                && (type == BlockType.PUMPKIN || type == BlockType.JACKOLANTERN)) {
            Boolean useOn = Redstone.testAnyInput(pt);

            if (useOn != null && useOn) {
                CraftBook.setBlockID(pt, BlockType.JACKOLANTERN);
            } else if (useOn != null) {
                CraftBook.setBlockID(pt, BlockType.PUMPKIN);
            }
        // Sign gates
    	} else if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof Sign)) {
                return;
            }

            Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);
            int len = line2.length();
            
            // ICs
            if (redstoneICs
                    && type == BlockType.WALL_SIGN
                    && line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                    line2.charAt(len - 1) == ']') {
                String id = line2.substring(1, len - 1).toUpperCase();
                SignText signText = new SignText(sign.getText(0),sign.getText(1),
                                                 sign.getText(2),sign.getText(3));

                RegisteredIC icType = icList.get(id);
                if(icType==null) {
                    sign.setText(1, Colors.Red + line2);
                    sign.update();
                    return;
                }
                
                if(icType.isPlc&&!redstonePLCs) {
                    sign.setText(1, Colors.Red + line2);
                    sign.setText(2, "!ERROR!");
                    sign.setText(3, "plcs disabled");
                    sign.update();
                    return;
                }
                
                icType.think(pt, changed, signText, sign, craftBook.getDelay());

                if (signText.isChanged()) {
                    sign.setText(0, signText.getLine1());
                    sign.setText(1, signText.getLine2());
                    sign.setText(2, signText.getLine3());
                    sign.setText(3, signText.getLine4());
                    if(signText.update()) sign.update();
                }
            }
        }
    }
    
    /**
     * Checks if the player can create an IC.
     */
    private boolean canCreateIC(Player player, String id, RegisteredIC ic) {
        return (ic.ic.requiresPermission()
        		|| (ic.isPlc && redstonePLCsRequirePermission))
        		&& !player.canUseCommand("/allic")
                && !player.canUseCommand("/" + id.toLowerCase());
    }

	/**
	 * Register a new IC. Defined by the interface CustomICAccepter
	 */
	public void registerIC(String name, IC ic, String type)
			throws CustomICException {
		if (icList.containsKey(name)) {
			throw new CustomICException("IC already defined");
		}
		
		registerIC(name, ic, getIcType(type), false);
	}

	private ICType getIcType(String type) throws CustomICException {
		ICType typeObject = ICType.forName(type);
		
		if (typeObject == null) {
			throw new CustomICException("Invalid IC type: " + type);
		}
		
		return typeObject;
	}

	private void internalRegisterIC(String name, IC ic, ICType type) {
		if (!icList.containsKey(name))
			registerIC(name, ic, type, false);
	}

	private void internalRegisterIC(String name, IC ic, ICType type,
			boolean isPlc) {
		if (!icList.containsKey(name))
			registerIC(name, ic, type, isPlc);
	}

	/**
	 * Registers a new IC.
	 */
	public void registerIC(String name, IC ic, ICType type) {
		registerIC(name, ic, type, false);
	}

	/**
	 * Registers a new IC.
	 */
	public void registerIC(String name, IC ic, ICType type, boolean isPlc) {
		icList.put(name, new RegisteredIC(ic, type, isPlc));
	}

	/**
	 * Storage class for registered ICs.
	 */
	private static class RegisteredIC {
		final ICType type;
		final IC ic;
		final boolean isPlc;

		RegisteredIC(IC ic, ICType type, boolean isPlc) {
			this.type = type;
			this.ic = ic;
			this.isPlc = isPlc;
		}

		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, RedstoneDelayer r) {
			type.think(pt, changedRedstoneInput, signText, sign, ic, r);
		}
	}

}
