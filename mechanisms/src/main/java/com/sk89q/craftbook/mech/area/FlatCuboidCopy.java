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

import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.World;

import java.io.*;
import java.util.ArrayList;

/**
 * Stores a copy of a cuboid.
 *
 * @author sk89q
 */
public class FlatCuboidCopy extends CuboidCopy {

    private byte[] blocks;
    private byte[] data;

    /**
     * Construct the object. This is to create a new copy at a certain
     * location.
     *
     * @param origin
     * @param size
     */
    public FlatCuboidCopy(Vector origin, Vector size, World world) {

        super(origin, size, world);
        blocks = new byte[width * height * length];
        data = new byte[width * height * length];
    }

    /**
     * Used to create a copy when loaded from file.
     */
    protected FlatCuboidCopy() {

    }

    /**
     * Save the copy to file.
     *
     * @param dest
     *
     * @throws IOException
     */
    @Override
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
     * Load a copy.
     *
     * @param file
     *
     * @return
     *
     * @throws IOException
     * @throws CuboidCopyException
     */
    @Override
    public void loadFromFile(File file) throws IOException, CuboidCopyException {

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
            } catch (IOException ignored) {
            }
        }

        this.origin = new Vector(x, y, z);
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
        this.data = data;
    }

    /**
     * Make the copy from world.
     */
    @Override
    public void copy() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    blocks[index] = (byte) world.getBlockTypeIdAt(BukkitUtil.toLocation(world, origin.add(x, y, z)));
                    data[index] = world.getBlockAt(BukkitUtil.toLocation(world, origin.add(x, y, z))).getData();
                }
            }
        }
    }

    /**
     * Paste to world.
     */
    @Override
    public void paste() {

        ArrayList<Tuple2<Vector, byte[]>> queueAfter =
                new ArrayList<Tuple2<Vector, byte[]>>();
        ArrayList<Tuple2<Vector, byte[]>> queueLast =
                new ArrayList<Tuple2<Vector, byte[]>>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    Vector pt = origin.add(x, y, z);

                    if (BlockType.shouldPlaceLast(world.getBlockTypeIdAt(BukkitUtil.toLocation(world, pt)))) {
                        world.getBlockAt(BukkitUtil.toLocation(world, pt)).setTypeId(0);
                    }

                    if (BlockType.shouldPlaceLast(blocks[index])) {
                        queueLast.add(new Tuple2<Vector, byte[]>(pt, new byte[] {blocks[index], data[index]}));
                    } else {
                        queueAfter.add(new Tuple2<Vector, byte[]>(pt, new byte[] {blocks[index], data[index]}));
                    }
                }
            }
        }

        for (Tuple2<Vector, byte[]> entry : queueAfter) {
            byte[] v = entry.b;
            world.getBlockAt(BukkitUtil.toLocation(world, entry.a)).setTypeId(v[0]);
            if (BlockType.usesData(v[0])) {
                world.getBlockAt(BukkitUtil.toLocation(world, entry.a)).setData(v[1]);
            }
        }

        for (Tuple2<Vector, byte[]> entry : queueLast) {
            byte[] v = entry.b;
            world.getBlockAt(BukkitUtil.toLocation(world, entry.a)).setTypeId(v[0]);
            if (BlockType.usesData(v[0])) {
                world.getBlockAt(BukkitUtil.toLocation(world, entry.a)).setData(v[1]);
            }
        }

    }
}