// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.plc;

import com.sk89q.craftbook.ic.*;
import org.bukkit.*;
import org.bukkit.block.*;

import java.io.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlcIC<StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements IC {
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    private static final int PLC_STORE_VERSION = 0;

    private Lang lang;
    private StateT state;
    private String codeString;
    private CodeT code;

    private Sign sign;

    PlcIC(Sign s, Lang l) throws ICVerificationException {
        sign = s;
        codeString = getCode();
        if(codeString == null)
            throw new ICVerificationException("Code block not found.");
        l.compile(codeString);
    }
    public PlcIC(Server sv, Sign s, Lang l) {
        lang = l;
        sign = s;
        codeString = getCode();
        try {
            code = lang.compile(codeString);
        } catch(ICVerificationException e) {
            throw new RuntimeException("inconsistent compile check!", e);
        }
        state = lang.initState();

        try {
            loadState();
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Failed to load PLC state", e);
            state = lang.initState();
        }
    }

    private String getID() {
        return sign.getLine(2);
    }

    private String getFileName() {
        Location l = sign.getLocation();
        return l.getBlockX()+"_"+l.getBlockY()+"_"+l.getBlockZ();
    }
    private File getStorageLocation() {
        World w = sign.getWorld();
        File worldDir = w.getWorldFolder();
        new File(worldDir, "craftbook-plcs").mkdirs();
        return new File(new File(worldDir, "craftbook-plcs"), getFileName());
    }

    private String hashCode(String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(code.getBytes("UTF-8"));
            String hex = "";
            for(int i=0;i<digest.length;i++) {
                String byteHex = Integer.toHexString(digest[i]&0xFF);
                if(byteHex.length() == 1) byteHex = "0"+byteHex;
                hex += byteHex;
            }
            return hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("insane JVM implementation", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("insane JVM implementation", e);
        }
    }

    private void loadState() throws IOException {
        if(!getStorageLocation().exists()) return; // Prevent error spam

        DataInputStream in = new DataInputStream(new FileInputStream(getStorageLocation()));
        try {
            if(PLC_STORE_VERSION!=in.readInt()) throw new IOException("incompatible save version");

            String langName = in.readUTF();
            if(lang.getName().equals(langName) || lang.supports(langName)) {
                String id = in.readUTF();
                String code = hashCode(in.readUTF());
                if(id.equals(getID()) && hashCode(codeString).equals(code)) {
                    lang.loadState(state, in);
                }
            }
        } finally {
            in.close();
        }
    }
    private void saveState() throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(getStorageLocation()));
        try {
            out.writeInt(PLC_STORE_VERSION);
            out.writeUTF(lang.getName());
            out.writeUTF(getID());
            out.writeUTF(hashCode(codeString));
            lang.writeState(state, out);
        } finally {
            out.close();
        }
    }

    private String getCode() {
        Location l = sign.getLocation();
        World w = l.getWorld();

        int x = l.getBlockX();
        int z = l.getBlockZ();

        for(int y=0;y<w.getMaxHeight();y++) {
            if(y!=l.getBlockY())
                if(w.getBlockAt(x,y,z).getState() instanceof Sign) {
                    Sign s = (Sign) w.getBlockAt(x,y,z).getState();
                    if(s.getLine(1).equalsIgnoreCase("[Code Block]")) {
                        y--;
                        BlockState b = w.getBlockAt(x, y, z).getState();
                        String code = "";
                        while(b instanceof Sign) {
                            s = (Sign) b;
                            for(int li=0;li<4 && y!=l.getBlockY();li++)
                                code += s.getLine(li)+"\n";
                            b = w.getBlockAt(x, --y, z).getState();
                        }
                        return code;
                    }
                }
        }
        return null;
    }

    @Override
    public String getTitle() {
        return lang.getName()+" PLC";
    }

    @Override
    public String getSignTitle() {
        return lang.getName().toUpperCase();
    }

    @Override
    public void trigger(ChipState chip) {
        try {
            lang.execute(chip, state, code);
            try {
                saveState();
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Failed to save PLC state",e);
                state = lang.initState();
            }
        } catch(PlcException e) {
            sign.setLine(1, ChatColor.DARK_RED+sign.getLine(1));
            sign.setLine(2, "Error!");
            sign.setLine(3, e.getMessage());
            sign.update();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Internal error while executing PLC", e);

            sign.setLine(1, ChatColor.DARK_RED+sign.getLine(1));
            sign.setLine(2, "Error!");
            sign.setLine(3, e.getClass().getName());
            sign.update();
        }
    }

    public IC selfTriggered() {
        final IC self = this;
        return new SelfTriggeredIC() {
            @Override
            public String getTitle() {
                return self.getTitle();
            }

            @Override
            public String getSignTitle() {
                return self.getSignTitle();
            }

            @Override
            public void trigger(ChipState chip) {}

            @Override
            public void think(ChipState chip) {
                self.trigger(chip);
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public void unload() {}
        };
    }

    @Override
    public void unload() {}
}
