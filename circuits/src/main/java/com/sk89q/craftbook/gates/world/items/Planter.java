package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Sapling planter
 * Hybrid variant of MCX206 and MCX203 chest collector
 * When there is a sapling or seed item drop in range it will auto plant it above the IC.
 *
 * @authors Drathus, Me4502
 */
public class Planter extends AbstractIC {

    public Planter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    int itemID = 295;
    byte data = -1;
    Block target;
    Block onBlock;
    Vector offset = new Vector(0,2,0);
    int radius = 10;

    @Override
    public void load() {

        onBlock = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        try {
            radius = Integer.parseInt(ICUtil.EQUALS_PATTERN.split(getSign().getLine(3))[0]);
            try {
                String[] loc = ICUtil.COLON_PATTERN.split(ICUtil.EQUALS_PATTERN.split(getSign().getLine(3))[1]);
                offset = new Vector(Integer.parseInt(loc[0]),Integer.parseInt(loc[1]),Integer.parseInt(loc[2]));
                if(offset.getX() > 16)
                    offset.setX(16);
                if(offset.getY() > 16)
                    offset.setY(16);
                if(offset.getZ() > 16)
                    offset.setZ(16);

                if(offset.getX() < -16)
                    offset.setX(-16);
                if(offset.getY() < -16)
                    offset.setY(-16);
                if(offset.getZ() < -16)
                    offset.setZ(-16);
            }
            catch(Exception e){
                offset = new Vector(0,2,0);
            }

        } catch (Exception e) {
            radius = 10;
            offset = new Vector(0,2,0);
        }

        target = onBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        if (!getSign().getLine(2).isEmpty()) {
            try {
                itemID = Integer.parseInt(ICUtil.COLON_PATTERN.split(getSign().getLine(2))[0]);
                try {
                    data = Byte.parseByte(ICUtil.COLON_PATTERN.split(getSign().getLine(2))[1]);
                }
                catch(Exception e){}
            }
            catch(Exception e){
                itemID = BlockType.lookup(ICUtil.COLON_PATTERN.split(getSign().getLine(2))[0]).getID();
                try {
                    data = Byte.parseByte(ICUtil.COLON_PATTERN.split(getSign().getLine(2))[1]);
                }
                catch(Exception ee){}
            }
        }
    }

    @Override
    public String getTitle() {

        return "Planter";
    }

    @Override
    public String getSignTitle() {

        return "PLANTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0,plant());
    }

    public boolean plant() {

        if (!plantableItem(itemID)) return false;

        if(onBlock.getRelative(0,1,0).getTypeId() == BlockID.CHEST) {

            Chest c = (Chest) onBlock.getRelative(0, 1, 0).getState();
            for(ItemStack it : c.getInventory().getContents()) {

                if(!ItemUtil.isStackValid(it))
                    continue;

                if(it.getTypeId() != itemID)
                    continue;

                if(data != -1 && it.getDurability() != data)
                    continue;

                Block b = null;

                if((b = searchBlocks(it)) != null) {
                    if(c.getInventory().removeItem(new ItemStack(it.getTypeId(),1,it.getDurability(),it.getData().getData())).isEmpty()) {
                        b.setTypeIdAndData(getBlockByItem(itemID), data == -1 ? 0 : data, true);
                        return true;
                    }
                }
            }
        }
        else {
            for (Entity ent : target.getChunk().getEntities()) {
                if(!(ent instanceof Item))
                    continue;

                Item itemEnt = (Item) ent;

                if(!ItemUtil.isStackValid(itemEnt.getItemStack()))
                    continue;

                if (itemEnt.getItemStack().getTypeId() == itemID && (data == -1 || itemEnt.getItemStack().getDurability() == data || itemEnt.getItemStack().getData().getData() == data)) {
                    Location loc = itemEnt.getLocation();
                    double diffX = target.getX() - loc.getX();
                    double diffY = target.getY() - loc.getY();
                    double diffZ = target.getZ() - loc.getZ();

                    if (diffX * diffX + diffY * diffY + diffZ * diffZ < radius*radius) {

                        Block b = null;

                        if((b = searchBlocks(itemEnt.getItemStack())) != null) {
                            if(ItemUtil.takeFromEntity(itemEnt)) {
                                b.setTypeIdAndData(getBlockByItem(itemID), data == -1 ? 0 : data, true);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block searchBlocks(ItemStack stack) {

        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++) {
                    int rx = target.getX() - x;
                    int ry = target.getY() - y;
                    int rz = target.getZ() - z;
                    Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);

                    if(b.getTypeId() != 0)
                        continue;

                    if (itemPlantableOnBlock(itemID, b.getRelative(0, -1, 0).getTypeId())) {

                        return b;
                    }
                }
            }
        }
        return null;
    }

    protected boolean plantableItem(int itemId) {

        switch (itemId) {
            case BlockID.SAPLING:
            case ItemID.SEEDS:
            case ItemID.NETHER_WART_SEED:
            case ItemID.MELON_SEEDS:
            case ItemID.PUMPKIN_SEEDS:
            case BlockID.CACTUS:
            case ItemID.POTATO:
            case ItemID.CARROT:
            case BlockID.RED_FLOWER:
            case BlockID.YELLOW_FLOWER:
            case BlockID.RED_MUSHROOM:
            case BlockID.BROWN_MUSHROOM:
                return true;
            default:
                return false;
        }
    }

    protected boolean itemPlantableOnBlock(int itemId, int blockId) {

        switch (itemId) {
            case BlockID.SAPLING:
            case BlockID.RED_FLOWER:
            case BlockID.YELLOW_FLOWER:
                return blockId == BlockID.DIRT || blockId == BlockID.GRASS;
            case ItemID.SEEDS:
            case ItemID.MELON_SEEDS:
            case ItemID.PUMPKIN_SEEDS:
            case ItemID.POTATO:
                return blockId == BlockID.SOIL;
            case ItemID.NETHER_WART_SEED:
                return blockId == BlockID.SLOW_SAND;
            case BlockID.CACTUS:
                return blockId == BlockID.SAND;
            case BlockID.RED_MUSHROOM:
            case BlockID.BROWN_MUSHROOM:
                return !BlockType.canPassThrough(blockId);
        }
        return false;
    }

    protected int getBlockByItem(int itemId) {

        switch (itemId) {
            case ItemID.SEEDS:
                return BlockID.CROPS;
            case ItemID.MELON_SEEDS:
                return BlockID.MELON_STEM;
            case ItemID.PUMPKIN_SEEDS:
                return BlockID.PUMPKIN_STEM;
            case BlockID.SAPLING:
                return BlockID.SAPLING;
            case ItemID.NETHER_WART_SEED:
                return BlockID.NETHER_WART;
            case BlockID.CACTUS:
                return BlockID.CACTUS;
            case ItemID.POTATO:
                return BlockID.POTATOES;
            case ItemID.CARROT:
                return BlockID.CARROTS;
            case BlockID.RED_FLOWER:
                return BlockID.RED_FLOWER;
            case BlockID.YELLOW_FLOWER:
                return BlockID.YELLOW_FLOWER;
            case BlockID.RED_MUSHROOM:
                return BlockID.RED_MUSHROOM;
            case BlockID.BROWN_MUSHROOM:
                return BlockID.BROWN_MUSHROOM;
            default:
                return BlockID.AIR;
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Planter(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Plants plantable things at set offset.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "Item to plant id:data",
                    "radius=x:y:z offset"
            };
            return lines;
        }
    }
}