// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;

public class BukkitChangedSign implements ChangedSign {

    private Sign sign;
    private String[] lines;

    public BukkitChangedSign(Sign sign, String[] lines, LocalPlayer player) {

        Validate.notNull(sign);

        this.sign = sign;
        this.lines = lines;

        if(lines != null) {
            for(int i = 0; i < 4; i++) {

                String line = lines[i];
                for(String var : ParsingUtil.getPossibleVariables(line)) {

                    String key;

                    if(var.contains("|")) {
                        String[] bits = RegexUtil.PIPE_PATTERN.split(var);
                        key = bits[0];
                    } else
                        key = "global";

                    if(!player.hasPermission("craftbook.variables.use." + key))
                        setLine(i,line.replace("%" + var + "|" + key + "%", ""));
                }
            }
        }
    }

    public BukkitChangedSign(Sign sign, String[] lines) {

        Validate.notNull(sign);

        this.sign = sign;
        this.lines = lines;
    }

    @Override
    public BlockWorldVector getBlockVector() {

        return BukkitUtil.toWorldVector(sign.getBlock());
    }

    public Sign getSign() {

        return sign;
    }

    @Override
    public int getTypeId() {

        return sign.getTypeId();
    }

    @Override
    public byte getLightLevel() {

        return sign.getLightLevel();
    }

    @Override
    public LocalWorld getLocalWorld() {

        return BukkitUtil.getLocalWorld(sign.getWorld());
    }

    @Override
    public int getX() {

        return sign.getX();
    }

    @Override
    public int getY() {

        return sign.getY();
    }

    @Override
    public int getZ() {

        return sign.getZ();
    }

    /*
     * @Override public Chunk getChunk() {
     * 
     * return BukkitUtil.sign.getChunk(); }
     */

    @Override
    public String[] getLines() {

        return lines;
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {

        return ParsingUtil.parseVariables(lines[index], null);
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {

        lines[index] = line;
    }

    @Override
    public boolean setTypeId(int type) {

        return sign.setTypeId(type);
    }

    @Override
    public boolean update(boolean force) {

        if(!hasChanged() && !force)
            return false;
        for(int i = 0; i < 4; i++)
            sign.setLine(i, lines[i]);
        return sign.update(force);
    }

    @Override
    public byte getRawData() {

        return sign.getRawData();
    }

    @Override
    public void setRawData(byte b) {

        sign.setRawData(b);
    }

    @Override
    public void setLines(String[] lines) {

        this.lines = lines;
    }

    @Override
    public boolean hasChanged () {

        boolean ret = false;
        try {
            for(int i = 0; i < 4; i++)
                if(!sign.getLine(i).equals(lines[i])) {
                    ret = true;
                    break;
                }
        }
        catch(Exception ignored){}
        return ret;
    }

    @Override
    public void flushLines () {

        lines = sign.getLines();
    }

    @Override
    public boolean updateSign(ChangedSign sign) {

        if(!equals(sign)) {
            this.sign = ((BukkitChangedSign) sign).getSign();
            flushLines();
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {

        if(o instanceof BukkitChangedSign) {

            if(((BukkitChangedSign) o).getTypeId() != getTypeId())
                return false;
            if(((BukkitChangedSign) o).getRawData() != getRawData())
                return false;
            for(int i = 0; i < 4; i++)
                if(!((BukkitChangedSign) o).getLine(i).equals(getLine(i)))
                    return false;
            if(((BukkitChangedSign) o).getX() != getX())
                return false;
            if(((BukkitChangedSign) o).getY() != getY())
                return false;
            if(((BukkitChangedSign) o).getZ() != getZ())
                return false;
            if(!((BukkitChangedSign) o).getLocalWorld().getName().equals(getLocalWorld().getName()))
                return false;
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        return (getTypeId() * 1103515245 + 12345
                ^ Arrays.hashCode(getLines()) * 1103515245 + 12345
                ^ getX() * 1103515245 + 12345
                ^ getY() * 1103515245 + 12345
                ^ getZ() * 1103515245 + 12345
                ^ getLocalWorld().getName().hashCode() * 1103515245 + 12345
                ^ getRawData() * 1103515245 + 12345) * 1103515245 + 12345;
    }

    @Override
    public boolean hasVariable(String var) {

        var = var.toLowerCase();
        return lines[0].toLowerCase().contains("%" + var + "%") || lines[1].toLowerCase().contains("%" + var + "%") || lines[2].toLowerCase().contains("%" + var + "%") || lines[3].toLowerCase().contains("%" + var + "%");
    }
}