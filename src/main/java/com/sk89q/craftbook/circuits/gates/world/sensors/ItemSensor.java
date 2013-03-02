package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;

/**
 * @author Silthus
 */
public class ItemSensor extends AbstractIC {

    private int item;
    private short data;

    private Block center;
    private Vector radius;

    public ItemSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(3).trim());
        // lets get the type to detect first
        try {
            item = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            // seems to be the name of the item
            BlockType material = BlockType.lookup(split[0]);
            if (material != null) {
                item = material.getID();
            } else {
                ItemType it = ItemType.lookup(split[0]);
                if (it != null) {
                    item = it.getID();
                }
            }
        }

        if (item == 0) {
            item = BlockID.STONE;
        }

        if (split.length > 1) {
            data = Short.parseShort(split[1]);
        } else data = -1;

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        if (getSign().getLine(2).contains("=")) {
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            center = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        }
    }

    @Override
    public String getTitle() {

        return "Item Detection";
    }

    @Override
    public String getSignTitle() {

        return "ITEM DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {

        for (Entity entity : LocationUtil.getNearbyEntities(center.getLocation(), radius)) {
            if (entity instanceof Item) {
                ItemStack itemStack = ((Item) entity).getItemStack();
                if (itemStack.getTypeId() == item) {
                    if (data != -1 && !(itemStack.getDurability() == data)) return false;
                    if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius))
                        return true;
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getShortDescription() {

            return "Detects items within a given radius";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius=x:y:z offset", "id:data"};
            return lines;
        }
    }
}
