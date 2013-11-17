package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ItemInfo;

/**
 * @author Me4502
 */
public class SetBlockBelowChest extends SetBlock {

    public SetBlockBelowChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Below (Chest)";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK BELOW";
    }

    @Override
    protected void doSet(Block body, ItemInfo item, boolean force) {

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (force || body.getWorld().getBlockAt(x, y - 1, z).getType() == Material.AIR) {
            if (takeFromChest(body.getRelative(0, 1, 0), item)) {
                body.getWorld().getBlockAt(x, y - 1, z).setType(item.getType());
                if (item.getData() != -1) {
                    body.getWorld().getBlockAt(x, y - 1, z).setData((byte) item.getData());
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
        public String getShortDescription() {

            return "Sets below block from above chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"id{:data}", "+oFORCE if it should be forced."};
        }
    }
}
