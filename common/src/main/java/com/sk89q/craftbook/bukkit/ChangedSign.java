// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class ChangedSign implements Sign {

    protected final Sign sign;
    protected final String[] newLines;

    public ChangedSign(Sign sign, String[] newLines) {

        this.sign = sign;
        this.newLines = newLines;
    }

    @Override
    public Block getBlock() {

        return sign.getBlock();
    }

    @Override
    public MaterialData getData() {

        return sign.getData();
    }

    @Override
    public Material getType() {

        return sign.getType();
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
    public World getWorld() {

        return sign.getWorld();
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

    @Override
    public Chunk getChunk() {

        return sign.getChunk();
    }

    @Override
    public String[] getLines() {

        return newLines;
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {

        return newLines[index];
    }

    @Override
    public void setLine(int index, String line)
            throws IndexOutOfBoundsException {

        sign.setLine(index, line);
        newLines[index] = line;
    }

    @Override
    public void setData(MaterialData data) {

        sign.setData(data);
    }

    @Override
    public void setType(Material type) {

        sign.setType(type);
    }

    @Override
    public boolean setTypeId(int type) {

        return sign.setTypeId(type);
    }

    @Override
    public boolean update() {

        return sign.update();
    }

    @Override
    public boolean update(boolean force) {

        return sign.update(force);
    }

    @Override
    public byte getRawData() {

        return sign.getRawData();
    }

    @Override
    public Location getLocation() {

        return sign.getLocation();
    }

    @Override
    public void setRawData(byte b) {

        sign.setRawData(b);
    }

    @Override
    public void setMetadata(String string, MetadataValue mv) {

        sign.setMetadata(string, mv);
    }

    @Override
    public List<MetadataValue> getMetadata(String string) {

        return sign.getMetadata(string);
    }

    @Override
    public boolean hasMetadata(String string) {

        return sign.hasMetadata(string);
    }

    @Override
    public void removeMetadata(String string, Plugin plugin) {

        sign.removeMetadata(string, plugin);
    }

}
