package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;

public abstract class FloatSensorBase extends AbstractIC implements SelfTriggeredIC {

    private int idFloat;
    private int idStationary;
    
    public FloatSensorBase(Server server, Sign block, int idFloat, int idStationary) {
        super(server, block);
        this.idFloat = idFloat;
        this.idStationary = idStationary;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void trigger(ChipState chip) {}
    
    /**
     * Returns true if the sign has water at the specified location.
     * 
     * @return
     */
    private boolean hasFloat() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int yOffset = b.getY();
        int z = b.getZ();
        try {
            String yOffsetLine = getSign().getLine(2);
            if (yOffsetLine.length() > 0) {
                yOffset += Integer.parseInt(yOffsetLine);
            } else {
                yOffset -= 1;
            }
        } catch (NumberFormatException e) {
            yOffset -= 1;
        }
        int blockID = getSign().getBlock().getWorld()
                .getBlockTypeIdAt(x, yOffset, z);

        return (blockID == idFloat || blockID == idStationary);
    }

    @Override
    public void think(ChipState chip) {
        chip.setOutput(0, hasFloat());
    }

}
