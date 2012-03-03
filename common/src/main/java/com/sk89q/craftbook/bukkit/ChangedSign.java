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

    protected Sign sign;
    protected String[] newLines;
    
    public ChangedSign(Sign sign, String[] newLines) {
        this.sign = sign;
        this.newLines = newLines;
    }

    public Block getBlock() {
        return sign.getBlock();
    }

    public MaterialData getData() {
        return sign.getData();
    }

    public Material getType() {
        return sign.getType();
    }

    public int getTypeId() {
        return sign.getTypeId();
    }

    public byte getLightLevel() {
        return sign.getLightLevel();
    }

    public World getWorld() {
        return sign.getWorld();
    }

    public int getX() {
        return sign.getX();
    }

    public int getY() {
        return sign.getY();
    }

    public int getZ() {
        return sign.getZ();
    }

    public Chunk getChunk() {
        return sign.getChunk();
    }
    
    public String[] getLines() {
        return newLines;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {
        return newLines[index];
    }

    public void setLine(int index, String line)
            throws IndexOutOfBoundsException {
        sign.setLine(index, line);
        newLines[index] = line;
    }

    public void setData(MaterialData data) {
        sign.setData(data);
    }

    public void setType(Material type) {
        sign.setType(type);
    }

    public boolean setTypeId(int type) {
        return sign.setTypeId(type);
    }

    public boolean update() {
        return sign.update();
    }

    public boolean update(boolean force) {
        return sign.update(force);
    }

    public byte getRawData() {
        return sign.getRawData();
    }

    public Location getLocation() {
        return sign.getLocation();
    }

    public void setRawData(byte b) {
        sign.setRawData(b);
    }

    public void setMetadata(String string, MetadataValue mv) {
        sign.setMetadata(string, mv);
    }

    public List<MetadataValue> getMetadata(String string) {
        return sign.getMetadata(string);
    }

    public boolean hasMetadata(String string) {
        return sign.hasMetadata(string);
    }

    public void removeMetadata(String string, Plugin plugin) {
        sign.removeMetadata(string, plugin);
    }

}
