package com.sk89q.craftbook;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BaseItemStack;

/**
 * Author: Turtle9598
 */
public class CraftItemStack extends BaseItemStack {

    private final BlockWorldVector location;

    public CraftItemStack(int id, BlockWorldVector location) {

        super(id);
        this.location = location;
    }

    public CraftItemStack(int id, int amount, BlockWorldVector location) {

        super(id, amount);
        this.location = location;
    }

    public CraftItemStack(int id, int amount, short data, BlockWorldVector location) {

        super(id, amount, data);
        this.location = location;
    }

    public BlockWorldVector getLocation() {

        return location;
    }
}
