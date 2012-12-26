package com.sk89q.craftbook.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class ItemSensor extends AbstractIC {

    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private int item;
    private short data;

    private Block center;
    private Set<Chunk> chunks;
    private int radius;

    public ItemSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        Block block = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        String[] split = COLON_PATTERN.split(getSign().getLine(3).trim());
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
        chunks = LocationUtil.getSurroundingChunks(block, radius);
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

        for (Chunk chunk : chunks)
            if (chunk.isLoaded()) {
                for (Entity entity : chunk.getEntities())
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
        public String getDescription() {

            return "Detects items within a given radius";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius=x:y:z offset", "id:data"};
            return lines;
        }
    }
}
