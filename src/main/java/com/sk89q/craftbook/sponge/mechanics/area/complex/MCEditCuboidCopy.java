package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.schematic.SchematicFormat;
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

    protected MCEditCuboidCopy(World world) {

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
        /*try {
            EditSession editSession = new EditSession(new SpongeWorld(world), -1);
            editSession.enableQueue();
            clipboard.place(editSession, origin, false);
            editSession.flushQueue();
        } catch (MaxChangedBlocksException e) {
            // is never thrown because we are on infinite mode
        }*/
    }

    @Override
    public void clear() {
        /*try {
            CuboidRegion region = new CuboidRegion(origin, origin.add(size.getX() - 1, size.getY() - 1,
                    size.getZ() - 1));
            EditSession editSession = new EditSession(new BukkitWorld(world), -1);
            editSession.enableQueue();
            editSession.setBlocks(region, new BaseBlock(0));
            editSession.flushQueue();
        } catch (MaxChangedBlocksException e) {
            // is never thrown
        }*/
    }

    @Override
    public void copy() {
        /*EditSession editSession = new EditSession(new BukkitWorld(world), -1);
        editSession.enableQueue();
        // -1 means no block limit
        clipboard.copy(editSession);
        editSession.flushQueue();*/
    }
}