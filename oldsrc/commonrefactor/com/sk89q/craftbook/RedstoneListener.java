package com.sk89q.craftbook;
// $Id$

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;

import com.sk89q.craftbook.access.*;
import com.sk89q.craftbook.mech.ic.*;
import com.sk89q.craftbook.mech.ic.custom.*;
import com.sk89q.craftbook.mech.ic.logic.*;
import com.sk89q.craftbook.mech.ic.world.*;
import com.sk89q.craftbook.mech.ic.plc.*;
import com.sk89q.craftbook.mech.ic.plc.types.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.craftbook.util.Vector;

/**
 * Event listener for redstone enhancements such as redstone pumpkins and
 * integrated circuits. Redstone hooks for mechanisms are in
 * MechanismListener.
 * 
 * @author sk89q
 * @author Lymia
 */
public class RedstoneListener extends CraftBookDelegateListener
        implements CustomICAccepter{
    
    /**
     * Currently registered ICs
     */
    private Map<String,RegisteredIC> icList = 
            new HashMap<String,RegisteredIC>(32);

    private HashMap<String,HashSet<BlockVector>> instantICs = 
            new HashMap<String,HashSet<BlockVector>>(32);

    private HashMap<String,PlcLang> plcLanguageList = 
            new HashMap<String,PlcLang>();
    
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
    public RedstoneListener(CraftBookCore craftBook, ServerInterface server) {
        super(craftBook, server);
        registerLang("perlstone_v1.0",new Perlstone_1_0());
        registerLang("perlstone32_v1",new Perlstone32_1());
    }

    /**
     * Loads relevant configuration.
     */
    @Override
    public void loadConfiguration() {
        Configuration c = server.getConfiguration();
        
        checkCreatePermissions = c.getBoolean(
                "check-create-permissions", false);

        redstonePumpkins = c.getBoolean("redstone-pumpkins", true);
        redstoneNetherstone = c.getBoolean("redstone-netherstone", false);
        redstoneICs = c.getBoolean("redstone-ics", true);
        redstonePLCs = c.getBoolean("redstone-plcs", true);
        redstonePLCsRequirePermission = c.getBoolean(
                "redstone-plcs-require-permission", false);
        
        listICs = c.getBoolean("enable-ic-list",true);
        listUnusuableICs = c.getBoolean("ic-list-show-unusuable",true);
        
        enableSelfTriggeredICs = c.getBoolean("enable-self-triggered-ics",true);
        restrictSelfTriggeredICs = c.getBoolean("self-triggered-ics-require-premission",false);

        icList.clear();
        
        // Load custom ICs
        if (c.getBoolean("custom-ics", true)) {
            try {
                CustomICLoader.load("custom-ics.txt", this, plcLanguageList);
                server.getLogger().info("Custom ICs for CraftBook loaded");
            } catch (CustomICException e) {
                Throwable cause = e.getCause();
                
                if (cause != null && !(cause instanceof FileNotFoundException)) {
                    server.getLogger().log(Level.WARNING,
                            "Failed to load custom IC file: " + e.getMessage(),cause);
                }
            }
        }
        
        addDefaultICs();
        
        try {
            for (WorldInterface w:server.getWorlds())
                for(Tuple2<Integer,Integer> chunkCoord:w.getLoadedChunks()) {
                    int xs = (chunkCoord.a+1)<<4;
                    int ys = (chunkCoord.b+1)<<4;
                    for(int x=chunkCoord.a<<4;x<xs;x++) 
                        for(int y=0;y<128;y++) 
                            for(int z=chunkCoord.b<<4;z<ys;z++) 
                                if(w.getId(x, y, z)==BlockType.WALL_SIGN)
                                    onSignCreate(w,x,y,z);
                }
        } catch(Throwable t) {
            System.err.println("Chunk finder failed: "+t.getClass());
            t.printStackTrace();
        }
    }
    
    /**
     * Populate the IC list with the default ICs.
     */
    private void addDefaultICs() {
        if (enableSelfTriggeredICs) {
            internalRegisterIC("MC0020", new MC0020(), ICType.ZISO);
            internalRegisterIC("MC0111", new MC1111(), ICType.ZISO);
            internalRegisterIC("MC0230", new MC1230(), ICType.ZISO);
            internalRegisterIC("MC0420", new MC1420(), ICType.ZISO);
            internalRegisterIC("MC0500", new MC1500(), ICType.ZISO);
            internalRegisterIC("MC0260", new MC1260(false), ICType.ZISO);
            internalRegisterIC("MC0261", new MC1261(false), ICType.ZISO);
            internalRegisterIC("MC0262", new MC1262(false), ICType.ZISO);
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
        internalRegisterIC("MC1250", new MC1250(), ICType.SISO);
        internalRegisterIC("MC1260", new MC1260(true), ICType.SISO);
        internalRegisterIC("MC1261", new MC1261(true), ICType.SISO);
        internalRegisterIC("MC1262", new MC1262(true), ICType.SISO);
        internalRegisterIC("MC1420", new MC1420(), ICType.SISO);
        internalRegisterIC("MC1500", new MC1500(), ICType.SISO);
        internalRegisterIC("MC1510", new MC1510(), ICType.SISO);
        internalRegisterIC("MC1511", new MC1511(), ICType.SISO);
        internalRegisterIC("MC1512", new MC1512(), ICType.SISO);

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

        internalRegisterPLC("MC5000", "perlstone_v1.0", ICType.VIVO);
        internalRegisterPLC("MC5001", "perlstone_v1.0", ICType._3I3O);
        
        internalRegisterPLC("MC5032", "perlstone32_v1", ICType.VIVO);
        internalRegisterPLC("MC5033", "perlstone32_v1", ICType._3I3O);
    }

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(PlayerInterface player, WorldInterface world, Vector v, SignInterface s) {
        int type = world.getId(v);
        
        String line2 = s.getLine2();
        int len = line2.length();

        // ICs
        if (line2.length() > 4
                && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                line2.charAt(len - 1) == ']') {

            // Check to see if the player can even create ICs
            if (checkCreatePermissions
                    && !player.canCreateObject("ic")) {
                player.sendMessage(Colors.RED
                        + "You don't have permission to make ICs.");
                MinecraftUtil.dropSign(world, v.getBlockX(), v.getBlockY(), v.getBlockZ());
                return true;
            }

            String id = line2.substring(1, len - 1).toUpperCase();
            RegisteredIC ic = icList.get(id);

            if (ic != null) {
                if (ic.isPlc) {
                    if (!redstonePLCs) {
                        player.sendMessage(Colors.RED + "PLCs are not enabled.");
                        MinecraftUtil.dropSign(world, v.getBlockX(), v.getBlockY(), v.getBlockZ());
                        return false;
                    }
                }
                
                if (!canCreateIC(player, id, ic)) {
                    player.sendMessage(Colors.RED
                            + "You don't have permission to make " + id + ".");
                    MinecraftUtil.dropSign(world, v.getBlockX(), v.getBlockY(), v.getBlockZ());
                    return true;
                } else {
                    // To check the environment
                    Vector pos = new Vector(v.getBlockX(), v.getBlockY(), v.getBlockZ());

                    // Maybe the IC is setup incorrectly
                    String envError = ic.ic.validateEnvironment(server, world, pos, s);

                    s.flushChanges();

                    if (envError != null) {
                        player.sendMessage(Colors.RED
                                + "Error: " + envError);
                        MinecraftUtil.dropSign(world, v.getBlockX(), v.getBlockY(), v.getBlockZ());
                        return true;
                    } else {
                        s.setLine1(ic.ic.getTitle());
                        s.setLine2("[" + id + "]");
                    }
                    
                    if(enableSelfTriggeredICs && ic.type.isSelfTriggered) {
                        getInstantIcList(world).add(pos.toBlockVector());
                    }
                    
                    s.flushChanges();
                }
                
                if (ic.isPlc && !redstonePLCs && redstoneICs) {
                    player.sendMessage(Colors.RED + "Warning: PLCs are disabled.");
                }
            } else {
                s.setLine2(Colors.DARK_RED + line2);
                s.flushChanges();
                player.sendMessage(Colors.RED + "Unrecognized IC: " + id);
            }

            if (!redstoneICs) {
                player.sendMessage(Colors.RED + "Warning: ICs are disabled.");
            } else if (type == BlockType.SIGN_POST) {
                player.sendMessage(Colors.RED + "Warning: IC signs must be on a wall.");
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
    public void onWireInput(final WorldInterface world, final Vector pt, 
            final boolean isOn, final Vector changed) {
        int type = world.getId(pt);
        
        // Redstone pumpkins
        if (redstonePumpkins
                && (type == BlockType.PUMPKIN || type == BlockType.JACKOLANTERN)) {
            Boolean useOn = RedstoneUtil.testAnyInput(world, pt);

            if (useOn != null && useOn) {
                world.setId(pt, BlockType.JACKOLANTERN);
            } else if (useOn != null) {
                world.setId(pt, BlockType.PUMPKIN);
            }
        // Redstone netherstone
        } else if (redstoneNetherstone
                && (type == BlockType.NETHERSTONE)) {
            Boolean useOn = RedstoneUtil.testAnyInput(world,pt);
            Vector above = pt.add(0, 1, 0);

            if (useOn != null && useOn && world.getId(above) == 0) {
                world.setId(above, BlockType.FIRE);
            } else if (useOn != null && world.getId(above) == BlockType.FIRE) {
                world.setId(above, 0);
            }
        } else if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            BlockEntity cblock = world.getBlockEntity(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof SignInterface)) {
                return;
            }

            final SignInterface sign = (SignInterface)cblock;
            String line2 = sign.getLine2();
            int len = line2.length();
            
            // ICs
            if (redstoneICs && type == BlockType.WALL_SIGN
                    && line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC")
                    && line2.charAt(len - 1) == ']') {

                String id = line2.substring(1, len - 1).toUpperCase();

                final RegisteredIC ic = icList.get(id);
                
                if (ic == null) {
                    sign.setLine2(Colors.DARK_RED + line2);
                    sign.flushChanges();
                    return;
                }

                if (ic.type.isSelfTriggered) {
                    return;
                }

                world.delayAction(
                        new Action(world, pt.toBlockVector(), 2) {
                    @Override
                    public void run() {
                        ic.think(server, world, pt, changed, sign);

                        sign.flushChanges();
                    }
                });
            }
        }
    }

    public void onTick(WorldInterface world) {
        if(!enableSelfTriggeredICs) return;
        
        //XXX HACK: Do this in a more proper way later.
        if(world.getTime()%2!=0) return;
        
        HashSet<BlockVector> instantICs = getInstantIcList(world);
        
        BlockVector[] bv = instantICs.toArray(new BlockVector[0]);
        for(BlockVector pt:bv) {
            SignInterface sign = (SignInterface) 
                world.getBlockEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            
            if(sign==null) {
                instantICs.remove(pt);
                continue;
            }
            String line2 = sign.getLine2();
            if(!line2.startsWith("[MC")) {
                instantICs.remove(pt);
                continue;
            }
            
            String id = line2.substring(1, line2.length() - 1).toUpperCase();
            RegisteredIC ic = icList.get(id);
            if (ic == null) {
                sign.setLine2(Colors.RED + line2);
                sign.flushChanges();
                instantICs.remove(pt);
                continue;
            }

            if(!ic.type.isSelfTriggered) {
                instantICs.remove(pt);
                continue;
            }
            
            ic.think(server, world, pt, sign);
            
            sign.flushChanges();
        }
    }
    public void onSignCreate(WorldInterface world, int x, int y, int z) {
        if(!enableSelfTriggeredICs) return;
            
        SignInterface sign = (SignInterface) world.getBlockEntity(x,y,z);
        String line2 = sign.getLine2();
        if(!line2.startsWith("[MC")) return;
        
        String id = line2.substring(1, line2.length() - 1).toUpperCase();
        RegisteredIC ic = icList.get(id);
        if (ic == null) {
            sign.setLine2(Colors.DARK_RED + line2);
            sign.flushChanges();
            return;
        }

        if(!ic.type.isSelfTriggered) return;

        getInstantIcList(world).add(new BlockVector(x,y,z));
    }
    
    /**
     * Called when a command is run
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(PlayerInterface player, String[] split) {
        if (listICs && split[0].equalsIgnoreCase("/listics")
                && player.canUseCommand("listics")) {
            String[] lines = generateICText(player);
            int pages = ((lines.length - 1) / 10) + 1;
            int accessedPage;
            
            try {
                accessedPage = split.length == 1 ? 0 : Integer
                        .parseInt(split[1]) - 1;
                if (accessedPage < 0 || accessedPage >= pages) {
                    player.sendMessage(Colors.RED + "Invalid page \""
                            + split[1] + "\"");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Colors.RED + "Invalid page \"" + split[1]
                        + "\"");
                return true;
            }

            player.sendMessage(Colors.DARK_BLUE + "CraftBook ICs (Page "
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
    private String[] generateICText(PlayerInterface p) {
        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(icList.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<String>();
        for (String ic : icNameList) {
            RegisteredIC ric = icList.get(ic);
            boolean canUse = canCreateIC(p, ic, ric);
            boolean auto = ric.type.isSelfTriggered;
            if (listUnusuableICs) {
                strings.add(Colors.RED + ic + " (" + ric.type.name + ")"
                        + (auto ? " (SELF-TRIGGERED)" : "") + ": "
                        + ric.ic.getTitle() + (canUse ? "" : " (RESTRICTED)"));
            } else if (canUse) {
                strings.add(Colors.RED + ic + " (" + ric.type.name + ")"
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
    private boolean canCreateIC(PlayerInterface player, String id, RegisteredIC ic) {
        return (!ic.ic.requiresPermission()
                && !(ic.isPlc && redstonePLCsRequirePermission)
                && !(ic.type.isSelfTriggered && restrictSelfTriggeredICs))
                || player.canCreateIC(id);
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
     * Registers a PLC
     * 
     * @param name
     * @param ic
     * @param type
     * @param isPlc
     */
    private void internalRegisterPLC(String name, String plclang, ICType type) {
        if (!icList.containsKey(name)) {
            registerIC(name, new DefaultPLC(plcLanguageList.get(plclang)), type, true);
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

    public void registerLang(String name, PlcLang language) {
        plcLanguageList.put(name, language);
        server.getStateManager().addStateHolder(name, language);
    }
    
    private HashSet<BlockVector> getInstantIcList(WorldInterface world) {
        String name = world.getUniqueIdString();
        if(instantICs.containsKey(name)) return instantICs.get(name);
        HashSet<BlockVector> s = new HashSet<BlockVector>();
        instantICs.put(name,s);
        return s;
    }

    /**
     * Storage class for registered ICs.
     */
    private class RegisteredIC {
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


        void think(ServerInterface s, WorldInterface w, Vector v, Vector c, SignInterface t) {
            type.think(craftBook,s,w,v,c,t,ic);
        }

        void think(ServerInterface s, WorldInterface w, Vector v, SignInterface t) {
            type.think(craftBook,s,w,v,t,ic);
        }
    }

}
