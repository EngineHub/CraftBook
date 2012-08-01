package com.sk89q.craftbook.mech.area;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

/**
 * @author Silthus
 */
public class MCEditCuboidCopy extends CuboidCopy {

    private CuboidClipboard clipboard;

    public MCEditCuboidCopy(Vector origin, Vector size) {
        super(origin, size);
        this.clipboard = new CuboidClipboard(size, origin);
    }

    protected MCEditCuboidCopy() {
        // for loading from file
    }

    @Override
    public void save(File file) throws IOException, DataException {
        SchematicFormat.MCEDIT.save(clipboard, file);
    }

    @Override
    protected void loadFromFile(File file) throws IOException, CuboidCopyException, DataException {
        this.clipboard = SchematicFormat.MCEDIT.load(file);
    }

    @Override
    public void paste(World world) {
        try {
            clipboard.paste(new EditSession(new BukkitWorld(world), -1), origin, false);
        } catch (MaxChangedBlocksException e) {
            // is never thrown because we are on infinite mode
        }
    }

    @Override
    public void copy(World world) {
        // -1 means no block limit
        clipboard.copy(new EditSession(new BukkitWorld(world), -1));
    }

    @Override
    public boolean shouldClear(World world) {
        // TODO:
        return false;
    }
}
