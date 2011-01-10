package com.sk89q.craftbook.mech.area;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookCore;
import com.sk89q.craftbook.blockbag.BlockBag;
import com.sk89q.craftbook.blockbag.BlockBagException;
import com.sk89q.craftbook.blockbag.OutOfBlocksException;
import com.sk89q.craftbook.exception.CuboidCopyException;
import com.sk89q.craftbook.util.Vector;
import com.sk89q.worldedit.DoubleArrayList;

/**
 * Stores a copy of a cuboid.
 *
 * @author sk89q
 */
public class CuboidCopy {
    private Vector origin;
    private int width;
    private int height;
    private int length;
    private byte[] blocks;
    private byte[] data;
    private Vector testOffset;

    /**
     * Construct the object. This is to create a new copy at a certain
     * location.
     * 
     * @param origin
     * @param size
     */
    public CuboidCopy(Vector origin, Vector size) {
        this.origin = origin;
        width = size.getBlockX();
        height = size.getBlockY();
        length = size.getBlockZ();
        blocks = new byte[width * height * length];
        data = new byte[width * height * length];
    }

    /**
     * Used to create a copy when loaded from file.
     */
    private CuboidCopy() {
        
    }

    /**
     * Save the copy to file.
     * 
     * @param dest
     * @throws IOException
     */
    public void save(File dest) throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        DataOutputStream writer = new DataOutputStream(out);
        writer.writeByte(1);
        writer.writeInt(origin.getBlockX());
        writer.writeInt(origin.getBlockY());
        writer.writeInt(origin.getBlockZ());
        writer.writeInt(width);
        writer.writeInt(height);
        writer.writeInt(length);
        writer.write(blocks, 0, blocks.length);
        writer.write(data, 0, data.length);
        writer.close();
        out.close();
    }

    /**
     * Save the copy to a file.
     * 
     * @param path
     * @throws IOException
     */
    public void save(String path) throws IOException {
        save(new File(path));
    }

    /**
     * Load a copy.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws CuboidCopyException
     */
    public static CuboidCopy load(File file) throws IOException, CuboidCopyException {
        FileInputStream in = new FileInputStream(file);
        DataInputStream reader = new DataInputStream(in);

        int x, y, z;
        int width, height, length;
        byte[] blocks;
        byte[] data;

        try {
            @SuppressWarnings("unused")
            byte version = reader.readByte();
            x = reader.readInt();
            y = reader.readInt();
            z = reader.readInt();
            width = reader.readInt();
            height = reader.readInt();
            length = reader.readInt();
            int size = width * height * length;
            blocks = new byte[size];
            data = new byte[size];
            if (reader.read(blocks, 0, size) != size) {
                throw new CuboidCopyException("File error: Bad size");
            }
            data = new byte[size];
            if (reader.read(data, 0, size) != size) {
                throw new CuboidCopyException("File error: Bad size");
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        
        CuboidCopy copy = new CuboidCopy();
        copy.origin = new Vector(x, y, z);
        copy.width = width;
        copy.height = height;
        copy.length = length;
        copy.blocks = blocks;
        copy.data = data;
        copy.findTestOffset();
        
        return copy;
    }

    /**
     * Load a copy from a file.
     * 
     * @param path
     * @return
     * @throws IOException
     * @throws CuboidCopyException
     */
    public static CuboidCopy load(String path) throws IOException, CuboidCopyException {
        return load(new File(path));
    }

    /**
     * Make the copy from world.
     */
    public void copy() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    blocks[index] = (byte)CraftBookCore.getBlockID(origin.add(x, y, z));
                    data[index] = (byte)CraftBookCore.getBlockData(origin.add(x, y, z));
                }
            }
        }
        
        findTestOffset();
    }

    /**
     * Paste to world.
     */
    public void paste(BlockBag bag) throws BlockBagException {
        DoubleArrayList<Vector,byte[]> queueAfter =
            new DoubleArrayList<Vector,byte[]>(false);
        DoubleArrayList<Vector,byte[]> queueLast =
            new DoubleArrayList<Vector,byte[]>(false);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    Vector pt = origin.add(x, y, z);
                    
                    if (BlockType.shouldPlaceLast(CraftBookCore.getBlockID(pt))) {
                        CraftBookCore.setBlockID(pt, 0);
                    }
                    
                    if (BlockType.shouldPlaceLast(blocks[index])) {
                        queueLast.put(pt, new byte[] {blocks[index], data[index]});
                    } else {
                        queueAfter.put(pt, new byte[] {blocks[index], data[index]});
                    }
                }
            }
        }

        for (Map.Entry<Vector,byte[]> entry : queueAfter) {
            byte[] v = entry.getValue();
            try {
                bag.setBlockID(entry.getKey(), v[0]);
                if (BlockType.usesData(v[0])) {
                    CraftBookCore.setBlockData(entry.getKey(), v[1]);
                }
            } catch (OutOfBlocksException e) {
                // Eat error
            }
        }

        for (Map.Entry<Vector,byte[]> entry : queueLast) {
            byte[] v = entry.getValue();
            try {
                bag.setBlockID(entry.getKey(), v[0]);
                if (BlockType.usesData(v[0])) {
                    CraftBookCore.setBlockData(entry.getKey(), v[1]);
                }
            } catch (OutOfBlocksException e) {
                // Eat error
            }
        }

        bag.flushChanges();
    }

    /**
     * Clear the area.
     */
    public void clear(BlockBag bag) throws BlockBagException {
        List<Vector> queued = new ArrayList<Vector>();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Vector pt = origin.add(x, y, z);
                    if (BlockType.shouldPlaceLast(CraftBookCore.getBlockID(pt))) {
                        bag.setBlockID(pt, 0);
                    } else {
                        // Can't destroy these blocks yet
                        queued.add(pt);
                    }
                }
            }
        }

        for (Vector pt : queued) {
            bag.setBlockID(pt, 0);
        }

        bag.flushChanges();
    }

    /**
     * Toggles the area.
     *
     * @return
     */
    public void toggle(BlockBag bag) throws BlockBagException {
        if (shouldClear()) {
            clear(bag);
        } else {
            paste(bag);
        }
    }

    /**
     * Returns true if the bridge should be turned 'off'.
     * 
     * @return
     */
    public boolean shouldClear() {
        Vector v = origin.add(testOffset);
        return CraftBookCore.getBlockID(v) != 0;
    }

    /**
     * Find a good position to test if an area is active.
     */
    private void findTestOffset() {
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    if (blocks[index] != 0) {
                        testOffset = new Vector(x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Get the distance between a point and this cuboid.
     * 
     * @param pos
     * @return
     */
    public double distance(Vector pos) {
        Vector max = origin.add(new Vector(width, height, length));
        int closestX = Math.max(origin.getBlockX(),
                Math.min(max.getBlockX(), pos.getBlockX()));
        int closestY = Math.max(origin.getBlockY(),
                Math.min(max.getBlockY(), pos.getBlockY()));
        int closestZ = Math.max(origin.getBlockZ(),
                Math.min(max.getBlockZ(), pos.getBlockZ()));
        return pos.distance(new Vector(closestX, closestY, closestZ));
    }
}