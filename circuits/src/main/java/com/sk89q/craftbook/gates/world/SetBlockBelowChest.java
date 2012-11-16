package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Server;
import org.bukkit.block.Block;

/**
 * @author Me4502
 */
public class SetBlockBelowChest extends SetBlock {

    public SetBlockBelowChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Below";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK BELOW";
    }

    protected void doSet(Block body, int block, byte meta, boolean force) {

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (force || body.getWorld().getBlockAt(x, y - 1, z).getTypeId() == BlockID.AIR) {
            if (takeFromChest(body.getRelative(0, 1, 0), block, meta)) {
                body.getWorld().getBlockAt(x, y - 1, z).setTypeId(block);
                if (meta != -1) {
                    body.getWorld().getBlockAt(x, y - 1, z).setData(meta);
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockBelowChest(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Sets below block from above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:data",
                    "forced or not"
            };
            return lines;
        }
    }
}
