package com.sk89q.craftbook;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.foundation.Block;

/**
 * Author: Turtle9598
 */
public class CraftBlock extends Block {

    private final BlockWorldVector location;

    public CraftBlock(int id, BlockWorldVector location) {

        super(id);
        this.location = location;
    }

    public CraftBlock(int id, int data, BlockWorldVector location) {

        super(id, data);
        this.location = location;
    }

    public CraftBlock(int id, int data, CompoundTag nbtData, BlockWorldVector location) throws DataException {

        super(id, data, nbtData);
        this.location = location;
    }

    public BlockWorldVector getLocation() {

        return location;
    }
}
