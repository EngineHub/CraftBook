package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Me4502
 */
public class SetBlockChest extends SetBlock {

    public SetBlockChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Chest";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK CHEST";
    }

    @Override
    protected void doSet(Block body, ItemInfo item, boolean force) {

        if(((Factory)getFactory()).blockBlacklist.contains(item))
            return;

        BlockFace toPlace = ((Factory)getFactory()).above ? BlockFace.UP : BlockFace.DOWN;
        BlockFace chest = !((Factory)getFactory()).above ? BlockFace.UP : BlockFace.DOWN;

        if (force || body.getRelative(toPlace).getType() == Material.AIR) {
            if (takeFromChest(body.getRelative(chest), item)) {
                body.getRelative(toPlace).setType(item.getType());
                if (item.getData() != -1) {
                    body.getRelative(toPlace).setData((byte) item.getData());
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        @SuppressWarnings("serial")
        public List<ItemInfo> blockBlacklist = new ArrayList<ItemInfo>(){{
            add(new ItemInfo(Material.BEDROCK, -1));
        }};

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockChest(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if(sign.getLine(2) == null || sign.getLine(2).isEmpty())
                throw new ICVerificationException("A block must be provided on line 2!");
            ItemInfo item = new ItemInfo(sign.getLine(2));
            if(item.getType() == null)
                throw new ICVerificationException("An invalid block was provided on line 2!");
            if(blockBlacklist.contains(item))
                throw new ICVerificationException("A blacklisted block was provided on line 2!");
        }

        @Override
        public String getShortDescription() {

            return "Sets " + (above ? "above" : "below") + " block from " + (!above ? "above" : "below") + " chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"id{:data}", "+oFORCE if it should force"};
        }

        @Override
        public void addConfiguration (YAMLProcessor config, String path) {

            config.setComment(path + "blacklist", "Stops the IC from placing the listed blocks.");
            blockBlacklist.addAll(ItemInfo.parseListFromString(config.getStringList(path + "blacklist", ItemInfo.toStringList(blockBlacklist))));
        }
    }
}
