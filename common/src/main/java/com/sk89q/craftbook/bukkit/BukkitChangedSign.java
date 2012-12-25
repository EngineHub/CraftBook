// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import org.bukkit.block.Sign;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;

public class BukkitChangedSign implements ChangedSign {

    protected final Sign sign;
    protected String[] lines;

    public BukkitChangedSign (Sign sign, String[] lines) {

        this.sign = sign;
        this.lines = lines;
    }

    @Override
    public BlockWorldVector getBlockVector () {

        return BukkitUtil.toWorldVector(sign.getBlock());
    }

    @Override
    public int getTypeId () {

        return sign.getTypeId();
    }

    @Override
    public byte getLightLevel () {

        return sign.getLightLevel();
    }

    @Override
    public LocalWorld getLocalWorld () {

        return BukkitUtil.getLocalWorld(sign.getWorld());
    }

    @Override
    public int getX () {

        return sign.getX();
    }

    @Override
    public int getY () {

        return sign.getY();
    }

    @Override
    public int getZ () {

        return sign.getZ();
    }

    /*
     * @Override public Chunk getChunk() {
     * 
     * return BukkitUtil.sign.getChunk(); }
     */

    @Override
    public String[] getLines () {

        return lines;
    }

    @Override
    public String getLine (int index) throws IndexOutOfBoundsException {

        return lines[index];
    }

    @Override
    public void setLine (int index, String line) throws IndexOutOfBoundsException {

        sign.setLine(index, line);
        lines[index] = line;
    }

    @Override
    public boolean setTypeId (int type) {

        return sign.setTypeId(type);
    }

    @Override
    public boolean update (boolean force) {

        return sign.update(force);
    }

    @Override
    public byte getRawData () {

        return sign.getRawData();
    }

    @Override
    public void setRawData (byte b) {

        sign.setRawData(b);
    }

    @Override
    public void setLines (String[] lines) {
        this.lines = lines;
    }
}