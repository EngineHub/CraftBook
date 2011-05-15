package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.gates.logic.BothTriggeredIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.util.SignUtil;

public class BlockSensor extends BothTriggeredIC {

    private final int[] ids;
    
    public BlockSensor(Server server, Sign block, boolean selfTriggered, Boolean risingEdge, String title, String signTitle, int... ids) {
        super(server, block, selfTriggered, risingEdge, title, signTitle);
        this.ids = ids;
    }

    private boolean hasBlock() {

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

        for (int id : this.ids) {
            if (id == blockID) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void work(ChipState chip) {
        chip.setOutput(0, this.hasBlock());
    }
}
