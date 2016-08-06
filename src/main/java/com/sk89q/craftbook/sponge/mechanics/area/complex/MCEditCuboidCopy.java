/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import com.sk89q.worldedit.world.DataException;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;

public class MCEditCuboidCopy extends CuboidCopy {

    private CuboidClipboard clipboard;

    public MCEditCuboidCopy(Vector origin, Vector size, World world) {
        super(origin, size, world);
        clipboard = new CuboidClipboard(size, origin);
    }

    MCEditCuboidCopy(World world) {
        // for loading from file
        this.world = world;
    }

    @Override
    public void save(File file) throws IOException, DataException {
        SchematicFormat.MCEDIT.save(clipboard, file);
    }

    @Override
    protected void loadFromFile(File file) throws IOException, CuboidCopyException, DataException {
        clipboard = SchematicFormat.MCEDIT.load(file);
        origin = clipboard.getOrigin();
        size = clipboard.getSize();
        width = size.getBlockX();
        height = size.getBlockY();
        length = size.getBlockZ();
    }

    @Override
    public void paste() {
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(SpongeWorldEdit.inst().getWorld(world), -1);
            editSession.enableQueue();
            clipboard.place(editSession, origin, false);
            editSession.flushQueue();
        } catch (MaxChangedBlocksException e) {
            // is never thrown because we are on infinite mode
        }
    }

    @Override
    public void clear() {
        try {
            CuboidRegion region = new CuboidRegion(origin, origin.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(SpongeWorldEdit.inst().getWorld(world), -1);
            editSession.enableQueue();
            editSession.setBlocks(region, new BaseBlock(0));
            editSession.flushQueue();
        } catch (MaxChangedBlocksException e) {
            // is never thrown
        }
    }

    @Override
    public void copy() {
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(SpongeWorldEdit.inst().getWorld(world), -1);
        editSession.enableQueue();
        clipboard.copy(editSession);
        editSession.flushQueue();
    }
}