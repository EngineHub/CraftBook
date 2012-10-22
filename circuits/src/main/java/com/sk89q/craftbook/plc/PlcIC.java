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

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlcIC<StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements IC {

    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    private static final int PLC_STORE_VERSION = 1;

    private Lang lang;
    private StateT state;
    private String codeString;
    private CodeT code;

    private Sign sign;

    private boolean error = false;
    private String errorString = "no error";

    PlcIC(Sign s, Lang l) throws ICVerificationException {

        sign = s;
        try {
            codeString = getCode();
        } catch (CodeNotFoundException e) {
            throw new ICVerificationException("Error retrieving code: " + e.getMessage());
        }
        l.compile(codeString);
    }

    public PlcIC(Server sv, Sign s, Lang l) {

        lang = l;
        sign = s;
        try {
            codeString = getCode();
        } catch (CodeNotFoundException e) {
            error("code missing", "Code went missing!!");
        }
        try {
            if (codeString != null) {
                code = lang.compile(codeString);
            }
        } catch (ICVerificationException e) {
            throw new RuntimeException("inconsistent compile check!", e);
        }
        state = lang.initState();
        tryLoadState();
    }

    private boolean isShared() {

        return !sign.getLine(3).isEmpty() && sign.getLine(3).startsWith("id:");
    }

    private String getID() {

        return sign.getLine(2);
    }

    private String getFileName() {

        if (!isShared()) {
            Location l = sign.getLocation();
            return lang.getName() + "$$" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ();
        } else return lang.getName() + "$" + sign.getLine(3);
    }

    private File getStorageLocation() {

        World w = sign.getWorld();
        File worldDir = w.getWorldFolder();
        File targetDir = new File(worldDir, "craftbook-plcs");
        targetDir.mkdirs();
        return new File(targetDir, getFileName());
    }

    private String hashCode(String code) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(code.getBytes("UTF-8"));
            String hex = "";
            for (byte aDigest : digest) {
                String byteHex = Integer.toHexString(aDigest & 0xFF);
                if (byteHex.length() == 1) {
                    byteHex = "0" + byteHex;
                }
                hex += byteHex;
            }
            return hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("insane JVM implementation", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("insane JVM implementation", e);
        }
    }

    private void tryLoadState() {

        try {
            loadState();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load PLC state", e);
            state = lang.initState();
            getStorageLocation().delete();
        }
    }

    private void loadState() throws IOException {

        if (!getStorageLocation().exists()) return; // Prevent error spam

        DataInputStream in = new DataInputStream(new FileInputStream(getStorageLocation()));
        try {
            switch (in.readInt()) {
                case 1:
                    error = in.readBoolean();
                    errorString = in.readUTF();
                case 0:
                    String langName = in.readUTF();
                    String id = in.readUTF();
                    String code = hashCode(in.readUTF());
                    if ((lang.getName().equals(langName) || lang.supports(langName)) &&
                            (isShared() || id.equals(getID()) && hashCode(codeString).equals(code))) {
                        lang.loadState(state, in);
                    } else {
                        // Prevent errors from different ICs from affecting this one.
                        error = false;
                        errorString = "no error";
                    }
                    break;
                default:
                    throw new IOException("incompatible version");
            }
        } finally {
            in.close();
        }
    }

    private void trySaveState() {

        try {
            saveState();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save PLC state", e);
            state = lang.initState();
        }
    }

    private void saveState() throws IOException {

        DataOutputStream out = new DataOutputStream(new FileOutputStream(getStorageLocation()));
        try {
            out.writeInt(PLC_STORE_VERSION);
            out.writeBoolean(error);
            out.writeUTF(errorString);
            out.writeUTF(lang.getName());
            out.writeUTF(error ? "(error)" : getID());
            out.writeUTF(hashCode(codeString));
            lang.writeState(state, out);
        } finally {
            out.close();
        }
    }

    private String getBookCode(Block chestBlock) throws CodeNotFoundException {

        Chest c = (Chest) chestBlock.getState();
        Inventory i = c.getBlockInventory();
        ItemStack book = null;
        for (ItemStack s : i.getContents())
            if (s != null &&
            s.getAmount() > 0 &&
            (s.getType() == Material.BOOK_AND_QUILL ||
            s.getType() == Material.WRITTEN_BOOK)) {
                if (book != null)
                    throw new CodeNotFoundException("More than one written book found in chest!!");
                book = s;
            }
        if (book == null)
            throw new CodeNotFoundException("No written books found in chest.");
        BookItem data = new BookItem(book);
        String code = "";
        for (String s : data.getPages()) {
            code += s + "\n";
        }
        System.out.println(code);
        return code;
    }

    private String getCode() throws CodeNotFoundException {

        Block above = sign.getLocation().add(new Vector(0, 1, 0)).getBlock();
        if (above.getType() == Material.CHEST) return getBookCode(above);
        Block below = sign.getLocation().add(new Vector(0, -1, 0)).getBlock();
        if (below.getType() == Material.CHEST) return getBookCode(below);

        Location l = sign.getLocation();
        World w = l.getWorld();

        int x = l.getBlockX();
        int z = l.getBlockZ();

        for (int y = 0; y < w.getMaxHeight(); y++)
            if (y != l.getBlockY())
                if (w.getBlockAt(x, y, z).getState() instanceof Sign) {
                    Sign s = (Sign) w.getBlockAt(x, y, z).getState();
                    if (s.getLine(1).equalsIgnoreCase("[Code Block]")) {
                        y--;
                        BlockState b = w.getBlockAt(x, y, z).getState();
                        String code = "";
                        while (b instanceof Sign) {
                            s = (Sign) b;
                            for (int li = 0; li < 4 && y != l.getBlockY(); li++) {
                                code += s.getLine(li) + "\n";
                            }
                            b = w.getBlockAt(x, --y, z).getState();
                        }
                        return code;
                    }
                }
        throw new CodeNotFoundException("No code source found.");
    }

    @Override
    public String getTitle() {

        return lang.getName() + " PLC";
    }

    @Override
    public String getSignTitle() {

        return lang.getName().toUpperCase();
    }

    public void error(String shortMessage, String detailedMessage) {

        sign.setLine(2, ChatColor.RED + "!Error!");
        sign.setLine(3, shortMessage);
        sign.update();

        error = true;
        errorString = detailedMessage;

        trySaveState();
    }

    @Override
    public void trigger(ChipState chip) {

        try {
            if (isShared()) {
                tryLoadState();
            }

            lang.execute(chip, state, code);

            trySaveState();
        } catch (PlcException e) {
            error(e.getMessage(), e.detailedMessage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Internal error while executing PLC", e);
            error(e.getClass().getName(), "Internal error encountered: " + e.getClass().getName());
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
            public void trigger(ChipState chip) {

            }

            @Override
            public void think(ChipState chip) {

                self.trigger(chip);
            }

            @Override
            public boolean isActive() {

                return true;
            }

            @Override
            public void onRightClick(Player p) {

                self.onRightClick(p);
            }

            @Override
            public void unload() {

            }
        };
    }

    @Override
    public void onRightClick(Player p) {

        if (p.hasPermission("craftbook.plc.debug")) {
            p.sendMessage(ChatColor.GREEN + "Programmable Logic Controller debug information");
            Location l = sign.getLocation();
            p.sendMessage(ChatColor.RED + "Status:" + ChatColor.RESET + " " + (error ? "Error Encountered" : "OK"));
            p.sendMessage(ChatColor.RED + "Location:" + ChatColor.RESET +
                    " (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")");
            p.sendMessage(ChatColor.RED + "Language:" + ChatColor.RESET + " " + lang.getName());
            p.sendMessage(ChatColor.RED + "Full Storage Name:" + ChatColor.RESET + " " + getFileName());
            if (error) {
                p.sendMessage(errorString);
            } else {
                p.sendMessage(lang.dumpState(state));
            }
        } else {
            p.sendMessage(ChatColor.RED + "You do not have the necessary permissions to do that.");
        }
    }

    @Override
    public void unload() {

    }
}
