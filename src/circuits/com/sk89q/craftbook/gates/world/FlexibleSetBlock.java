package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class FlexibleSetBlock extends AbstractIC {

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new FlexibleSetBlock(getServer(), sign);
        }

    }

    public FlexibleSetBlock(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Flexible Set Block";
    }

    @Override
    public String getSignTitle() {
        return "FLEX SET";
    }

    @Override
    public void trigger(ChipState chip) {
        // Clock trigger!
        FlexiBlockDescription desc = getDescription(chip);
        //Invalid description
        if(desc == null){
            return;
        }
        Block setBlock = SignUtil.getBackBlock(getSign().getBlock()).getRelative(desc.xOff, desc.yOff, desc.zOff);
        
        boolean clock = chip.get(0);
        if(clock){
            if(!desc.applyBlockType){
                setBlock.setTypeId(desc.blockId);
            }else{
                setBlock.setTypeIdAndData(desc.blockId, desc.blockType, true);
            }
        }else if(desc.applyToggleBlock){
            if(!desc.applyToggleBlockType){
                setBlock.setTypeId(desc.toggleBlockId);
            }else{
                setBlock.setTypeIdAndData(desc.toggleBlockId, desc.toggleBlockType, true);
            }
        }
    }

    private FlexiBlockDescription getDescription(ChipState chip) {
        FlexiBlockDescription d = new FlexiBlockDescription();
        try {
            Sign s = getSign();
            /* Parse position. For maximum flexibility within the one-character limit, offsets may be in hexadecimal. */
            String[] posAndBlock = s.getLine(2).split(":", 2);
            switch (posAndBlock[0].toLowerCase().charAt(0)) {
            case 'x':
                d.xOff = Integer.parseInt(posAndBlock[0].substring(1).replaceAll("^[+]", ""), 16);
                break;
            case 'y':
                d.yOff = Integer.parseInt(posAndBlock[0].substring(1).replaceAll("^[+]", ""), 16);
                break;
            case 'z':
                d.zOff = Integer.parseInt(posAndBlock[0].substring(1).replaceAll("^[+]", ""), 16);
                break;
            default:
                /* New alternative position syntax: [+-]xOff[+-]yOff[+-]zOff
                      For example: +0+1+0 is equivalent to Y+1
                                   -5+2-3 specifies the block at relative offset (-5, 2, -3)
                                   +0+F+0 specifies a block 15 above the IC block
                */
                if (posAndBlock[0].matches("([-+][0-9a-fA-F]){3}")) {
                    d.xOff = Integer.parseInt(posAndBlock[0].substring(0, 2).replaceAll("^[+]", ""), 16);
                    d.yOff = Integer.parseInt(posAndBlock[0].substring(2, 4).replaceAll("^[+]", ""), 16);
                    d.zOff = Integer.parseInt(posAndBlock[0].substring(4, 6).replaceAll("^[+]", ""), 16);
                } else {
                    /* If neither the old or the new syntax match, fail. */
                    return null;
                }
                break;
            }
            /* Parse block information: failure to provide block info will throw into
               surrounding try/catch block and abort */
            if (posAndBlock[1].contains(":")) {
                String[] blockParams = posAndBlock[1].split(":");
                d.blockId = Integer.parseInt(blockParams[0]);
                d.blockType = Byte.parseByte(blockParams[1]);
                d.applyBlockType = true;
            } else {
                d.blockId = Integer.parseInt(posAndBlock[1]);
                d.applyBlockType = false;
            }
            /* Parse optional hold and toggle settings */
            String line4 = s.getLine(3).trim().toLowerCase();
            if (line4.contains("h")) {
                /* Apply hold settings: toggle with air */
                d.applyToggleBlock = true;
                d.toggleBlockId = 0;
                d.applyToggleBlockType = false;
            } else {
                String[] toggleParams = line4.split(":");
                if (toggleParams.length > 0) {
                    try {
                        d.toggleBlockId = Integer.parseInt(toggleParams[0]);
                        d.applyToggleBlock = true;
                    } catch (Exception e) {
                        d.applyToggleBlock = false;
                    }
                } else {
                    d.applyToggleBlock = false;
                }
                if (toggleParams.length > 1) {
                    try {
                        d.toggleBlockType = Byte.parseByte(toggleParams[1]);
                        d.applyToggleBlockType = true;
                    } catch (Exception e) {
                        d.applyToggleBlockType = false;
                    }
                } else {
                    d.applyToggleBlockType = false;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return d;
    }

    private class FlexiBlockDescription {
        int xOff, yOff, zOff;
        int blockId;
        byte blockType;
        boolean applyBlockType;
        boolean applyToggleBlock;
        int toggleBlockId;
        byte toggleBlockType;
        boolean applyToggleBlockType;
    }

}
