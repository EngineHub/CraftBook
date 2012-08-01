package com.sk89q.craftbook.mech.area;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.IOException;

/**
 * @author Silthus
 */
public class MCEditCuboidCopy extends CuboidCopy {

    private CuboidClipboard clipboard;
    private boolean on;

    public MCEditCuboidCopy(Vector origin, Vector size) {
        super(origin, size);
        this.clipboard = new CuboidClipboard(size, origin);
        on = true;
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
    public void paste(Sign sign) {
        try {
            clipboard.paste(new EditSession(new BukkitWorld(sign.getWorld()), -1), origin, false);
            sign.setLine(1, sign.getLine(1) + "#");
            sign.update();
        } catch (MaxChangedBlocksException e) {
            // is never thrown because we are on infinite mode
        }
    }

    @Override
    public void clear(Sign sign) {
        super.clear(sign);
        sign.setLine(1, sign.getLine(1).replace("#", ""));
        sign.update();
    }

    @Override
    public void copy(World world) {
        // -1 means no block limit
        clipboard.copy(new EditSession(new BukkitWorld(world), -1));
        on = true;
    }

    @Override
    public boolean shouldClear(Sign sign) {
        on = sign.getLine(1).contains("#");
        return on;
    }
}
