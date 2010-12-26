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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import lymia.customic.*;
import lymia.perlstone.Perlstone_1_0;
import lymia.util.Tuple2;

/**
 * Event listener for redstone enhancements such as redstone pumpkins and
 * integrated circuits. Redstone hooks for mechanisms are in
 * MechanismListener.
 * 
 * @author sk89q
 * @author Lymia
 */
public class RedstoneListener extends CraftBookDelegateListener
        implements CustomICAccepter, SignPatch.ExtensionListener, 
                   TickExtensionListener {
    
    /**
     * Currently registered ICs
     */
    private Map<String,RegisteredIC> icList = 
            new HashMap<String,RegisteredIC>(32);

    private Set<BlockVector> bv = 
            new HashSet<BlockVector>(32);
    
    private boolean checkCreatePermissions = false;
    private boolean redstonePumpkins = true;
    private boolean redstoneNetherstone = false;
    private boolean redstoneICs = true;
    private boolean redstonePLCs = true;
    private boolean redstonePLCsRequirePermission = true;
    private boolean listICs = true;
    private boolean listUnusuableICs = true;
    
    private boolean enableSelfTriggeredICs = true;
    private boolean restrictSelfTriggeredICs = false;
    
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
        redstoneNetherstone = properties.getBoolean("redstone-netherstone", false);
        redstoneICs = properties.getBoolean("redstone-ics", true);
        redstonePLCs = properties.getBoolean("redstone-plcs", true);
        redstonePLCsRequirePermission = properties.getBoolean(
                "redstone-plcs-require-permission", false);
        
        listICs = properties.getBoolean("enable-ic-list",true);
        listUnusuableICs = properties.getBoolean("ic-list-show-unusuable",true);
        
        enableSelfTriggeredICs = properties.getBoolean("enable-self-triggered-ics",true);
        restrictSelfTriggeredICs = properties.getBoolean("self-triggered-ics-require-premission",false);

        icList.clear();
        
        // Load custom ICs
        if (properties.getBoolean("custom-ics", true)) {
            try {
                CustomICLoader.load("custom-ics.txt", this);
                logger.info("Custom ICs for CraftBook loaded");
            } catch (CustomICException e) {
                Throwable cause = e.getCause();
                
                if (cause != null && !(cause instanceof FileNotFoundException)) {
                    logger.log(Level.WARNING,
                            "Failed to load custom IC file: " + e.getMessage());
                }
            }
        }
        
        addDefaultICs();
        
        Server s = etc.getServer();
        for(Tuple2<Integer,Integer> chunkCoord:ChunkFinder.getLoadedChunks(s.getMCServer().e)) {
            int xs = (chunkCoord.a+1)<<4;
            int ys = (chunkCoord.b+1)<<4;
            for(int x=chunkCoord.a<<4;x<xs;x++) 
                for(int y=0;y<128;y++) 
                    for(int z=chunkCoord.b<<4;z<ys;z++) 
                        if(s.getBlockIdAt(x, y, z)==BlockType.WALL_SIGN)
                            onSignAdded(x,y,z);
        }
    }
    
    /**
     * Populate the IC list with the default ICs.
     */
    private void addDefaultICs() {
        if(enableSelfTriggeredICs) {
            internalRegisterIC("MC0020", new MC0020(), ICType.ZISO);
            internalRegisterIC("MC0111", new MC1111(), ICType.ZISO);
            internalRegisterIC("MC0230", new MC1230(), ICType.ZISO);
            internalRegisterIC("MC0420", new MC1420(), ICType.ZISO);
        }
        
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
        internalRegisterIC("MC1202", new MC1202(), ICType.SISO);
        internalRegisterIC("MC1205", new MC1205(), ICType.SISO);
        internalRegisterIC("MC1206", new MC1206(), ICType.SISO);
        internalRegisterIC("MC1230", new MC1230(), ICType.SISO);
        internalRegisterIC("MC1231", new MC1231(), ICType.SISO);
        internalRegisterIC("MC1240", new MC1240(), ICType.SISO);
        internalRegisterIC("MC1241", new MC1241(), ICType.SISO);
        internalRegisterIC("MC1420", new MC1420(), ICType.SISO);
        
        internalRegisterIC("MC2020", new MC2020(), ICType.SI3O);
        
        internalRegisterIC("MC3020", new MC3020(), ICType._3ISO);
        internalRegisterIC("MC3002", new MC3002(), ICType._3ISO);
        internalRegisterIC("MC3003", new MC3003(), ICType._3ISO);
        internalRegisterIC("MC3021", new MC3021(), ICType._3ISO);
        internalRegisterIC("MC3030", new MC3030(), ICType._3ISO);
        internalRegisterIC("MC3031", new MC3031(), ICType._3ISO);
        internalRegisterIC("MC3032", new MC3032(), ICType._3ISO);
        internalRegisterIC("MC3033", new MC3033(), ICType._3ISO);
        internalRegisterIC("MC3034", new MC3034(), ICType._3ISO);
        internalRegisterIC("MC3036", new MC3036(), ICType._3ISO);
        internalRegisterIC("MC3040", new MC3040(), ICType._3ISO);
        internalRegisterIC("MC3101", new MC3101(), ICType._3ISO);
        internalRegisterIC("MC3231", new MC3231(), ICType._3ISO);
        internalRegisterIC("MC4000", new MC4000(), ICType._3I3O);
        internalRegisterIC("MC4010", new MC4010(), ICType._3I3O);
        internalRegisterIC("MC4100", new MC4100(), ICType._3I3O);
        internalRegisterIC("MC4110", new MC4110(), ICType._3I3O);
        internalRegisterIC("MC4200", new MC4200(), ICType._3I3O);

        internalRegisterIC("MC5000", new DefaultPLC(new Perlstone_1_0()),
                ICType.VIVO, true);
        internalRegisterIC("MC5001", new DefaultPLC(new Perlstone_1_0()),
                ICType._3I3O, true);
    }

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(Player player, Sign sign) {
        int type = CraftBook.getBlockID(
                sign.getX(), sign.getY(), sign.getZ());
        
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
                CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                return true;
            }

            String id = line2.substring(1, len - 1).toUpperCase();
            RegisteredIC ic = icList.get(id);

            if (ic != null) {
                if (ic.isPlc) {
                    if (!redstonePLCs) {
                        player.sendMessage(Colors.Rose + "PLCs are not enabled.");
                        CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                        return false;
                    }
                }
                
                if (!canCreateIC(player, id, ic)) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make " + id + ".");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else {
                    // To check the environment
                    Vector pos = new Vector(sign.getX(), sign.getY(), sign.getZ());
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
                        CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                        return true;
                    } else {
                        sign.setText(0, ic.ic.getTitle());
                        sign.setText(1, "[" + id + "]");
                    }
                    
                    if(enableSelfTriggeredICs && ic.type.isSelfTriggered) {
                        bv.add(pos.toBlockVector());
                    }
                    
                    sign.update();
                }
                
                if (ic.isPlc && !redstonePLCs && redstoneICs) {
                    player.sendMessage(Colors.Rose + "Warning: PLCs are disabled.");
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

            return false;
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
    public void onDirectWireInput(final Vector pt, boolean isOn, final Vector changed) {
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
        // Redstone netherstone
        } else if (redstoneNetherstone
                && (type == BlockType.NETHERSTONE)) {
            Boolean useOn = Redstone.testAnyInput(pt);
            Vector above = pt.add(0, 1, 0);

            if (useOn != null && useOn && CraftBook.getBlockID(above) == 0) {
                CraftBook.setBlockID(above, BlockType.FIRE);
            } else if (useOn != null && CraftBook.getBlockID(above) == BlockType.FIRE) {
                CraftBook.setBlockID(above, 0);
            }
        } else if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof Sign)) {
                return;
            }

            final Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);
            int len = line2.length();
            
            // ICs
            if (redstoneICs && type == BlockType.WALL_SIGN
                    && line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC")
                    && line2.charAt(len - 1) == ']') {

                String id = line2.substring(1, len - 1).toUpperCase();

                final SignText signText = new SignText(sign.getText(0),
                        sign.getText(1), sign.getText(2), sign.getText(3));

                final RegisteredIC ic = icList.get(id);
                
                if (ic == null) {
                    sign.setText(1, Colors.Red + line2);
                    sign.update();
                    return;
                }

                if (ic.type.isSelfTriggered) {
                    return;
                }

                craftBook.getDelay().delayAction(
                        new TickDelayer.Action(pt.toBlockVector(), 2) {
                    @Override
                    public void run() {
                        ic.think(pt, changed, signText, sign, craftBook.getDelay());

                        if (signText.isChanged()) {
                            sign.setText(0, signText.getLine1());
                            sign.setText(1, signText.getLine2());
                            sign.setText(2, signText.getLine3());
                            sign.setText(3, signText.getLine4());
                            
                            if (signText.shouldUpdate()) {
                                sign.update();
                            }
                        }
                    }
                });
            }
        }
    }

    public void onTick() {
        if(!enableSelfTriggeredICs) return;
        
        //XXX HACK: Do this in a more proper way later.
        if(etc.getServer().getTime()%2!=0) return;
        
        BlockVector[] bv = this.bv.toArray(new BlockVector[0]);
        for(BlockVector pt:bv) {
            Sign sign = (Sign)etc.getServer().getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if(sign==null) {
                this.bv.remove(pt);
                continue;
            }
            String line2 = sign.getText(1);
            if(!line2.startsWith("[MC")) {
                this.bv.remove(pt);
                continue;
            }
            
            String id = line2.substring(1, line2.length() - 1).toUpperCase();
            RegisteredIC ic = icList.get(id);
            if (ic == null) {
                sign.setText(1, Colors.Red + line2);
                sign.update();
                this.bv.remove(pt);
                continue;
            }

            if(!ic.type.isSelfTriggered) {
                this.bv.remove(pt);
                continue;
            }

            SignText signText = new SignText(sign.getText(0),
                    sign.getText(1), sign.getText(2), sign.getText(3));
            
            ic.think(pt, signText, sign);
            
            if (signText.isChanged()) {
                sign.setText(0, signText.getLine1());
                sign.setText(1, signText.getLine2());
                sign.setText(2, signText.getLine3());
                sign.setText(3, signText.getLine4());
                
                if (signText.shouldUpdate()) {
                    sign.update();
                }
            }
        }
    }
    public void onSignAdded(int x, int y, int z) {
        if(!enableSelfTriggeredICs) return;
            
        Sign sign = (Sign)etc.getServer().getComplexBlock(x,y,z);
        String line2 = sign.getText(1);
        if(!line2.startsWith("[MC")) return;
        
        String id = line2.substring(1, line2.length() - 1).toUpperCase();
        RegisteredIC ic = icList.get(id);
        if (ic == null) {
            sign.setText(1, Colors.Red + line2);
            sign.update();
            return;
        }

        if(!ic.type.isSelfTriggered) return;

        bv.add(new BlockVector(x,y,z));
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
            throws InsufficientArgumentsException,
            LocalWorldEditBridgeException {
        
        if (listICs && split[0].equalsIgnoreCase("/listics")
                && Util.canUse(player, "/listics")) {
            String[] lines = generateICText(player);
            int pages = ((lines.length - 1) / 10) + 1;
            int accessedPage;
            
            try {
                accessedPage = split.length == 1 ? 0 : Integer
                        .parseInt(split[1]) - 1;
                if (accessedPage < 0 || accessedPage >= pages) {
                    player.sendMessage(Colors.Rose + "Invalid page \""
                            + split[1] + "\"");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Colors.Rose + "Invalid page \"" + split[1]
                        + "\"");
                return true;
            }

            player.sendMessage(Colors.Blue + "CraftBook ICs (Page "
                    + (accessedPage + 1) + " of " + pages + "):");
            
            for (int i = accessedPage * 10; i < lines.length
                    && i < (accessedPage + 1) * 10; i++) {
                player.sendMessage(lines[i]);
            }

            return true;
        }

        return false;
    }

    /**
     * Used for the /listics command.
     * 
     * @param p
     * @return
     */
    private String[] generateICText(Player p) {
        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(icList.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<String>();
        for (String ic : icNameList) {
            RegisteredIC ric = icList.get(ic);
            boolean canUse = canCreateIC(p, ic, ric);
            boolean auto = ric.type.isSelfTriggered;
            if (listUnusuableICs) {
                strings.add(Colors.Rose + ic + " (" + ric.type.name + ")"
                        + (auto ? " (SELF-TRIGGERED)" : "") + ": "
                        + ric.ic.getTitle() + (canUse ? "" : " (RESTRICTED)"));
            } else if (canUse) {
                strings.add(Colors.Rose + ic + " (" + ric.type.name + ")"
                        + (auto ? " (SELF-TRIGGERED)" : "") + ": "
                        + ric.ic.getTitle());
            }
        }
        
        return strings.toArray(new String[0]);
    }

    /**
     * Checks if the player can create an IC.
     * 
     * @param player
     * @param id
     * @param ic
     */
    private boolean canCreateIC(Player player, String id, RegisteredIC ic) {
        return (!ic.ic.requiresPermission()
                && !(ic.isPlc && redstonePLCsRequirePermission)
                && !(ic.type.isSelfTriggered && restrictSelfTriggeredICs))
                || player.canUseCommand("/allic")
                || player.canUseCommand("/" + id.toLowerCase());
    }

    /**
     * Register a new IC. Defined by the interface CustomICAccepter.
     * 
     * @param name
     * @param ic
     * @param type
     */
    public void registerIC(String name, IC ic, String type)
            throws CustomICException {
        if (icList.containsKey(name)) {
            throw new CustomICException("IC already defined");
        }
        ICType icType = getICType(type);
        if(!enableSelfTriggeredICs && icType.isSelfTriggered) return;
        
        registerIC(name, ic, icType, false);
    }

    /**
     * Get an IC type from its type name.
     * 
     * @param type
     * @return
     * @throws CustomICException thrown if the type does not exist
     */
    private ICType getICType(String type) throws CustomICException {
        ICType typeObject = ICType.forName(type);
        
        if (typeObject == null) {
            throw new CustomICException("Invalid IC type: " + type);
        }
        
        return typeObject;
    }

    /**
     * Registers an non-PLC IC.
     * 
     * @param name
     * @param ic
     * @param type
     */
    private void internalRegisterIC(String name, IC ic, ICType type) {
        if (!icList.containsKey(name)) {
            registerIC(name, ic, type, false);
        }
    }

    /**
     * Registers PLC or non-PLC IC.
     * 
     * @param name
     * @param ic
     * @param type
     * @param isPlc
     */
    private void internalRegisterIC(String name, IC ic, ICType type,
            boolean isPlc) {
        if (!icList.containsKey(name)) {
            registerIC(name, ic, type, isPlc);
        }
    }

    /**
     * Registers a new non-PLC IC.
     * 
     * @param name
     * @param ic
     * @param type
     */
    public void registerIC(String name, IC ic, ICType type) {
        registerIC(name, ic, type, false);
    }

    /**
     * Registers a new IC.
     * 
     * @param name
     * @param ic
     * @param isPlc
     */
    public void registerIC(String name, IC ic, ICType type, boolean isPlc) {
        icList.put(name, new RegisteredIC(ic, type, isPlc));
    }

    public void run() {onTick();}

    /**
     * Storage class for registered ICs.
     */
    private static class RegisteredIC {
        final ICType type;
        final IC ic;
        final boolean isPlc;

        /**
         * Construct the object.
         * 
         * @param ic
         * @param type
         * @param isPlc
         */
        public RegisteredIC(IC ic, ICType type, boolean isPlc) {
            this.type = type;
            this.ic = ic;
            this.isPlc = isPlc;
        }

        /**
         * Think.
         * 
         * @param pt
         * @param changedRedstoneInput
         * @param signText
         * @param sign
         * @param r
         */
        void think(Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, TickDelayer r) {
            type.think(pt, changedRedstoneInput, signText, sign, ic, r);
        }

        /**
         * Think.
         * 
         * @param pt
         * @param changedRedstoneInput
         * @param signText
         * @param sign
         */
        void think(Vector pt, SignText signText, Sign sign) {
            type.think(pt, signText, sign, ic);
        }
    }
    
}
