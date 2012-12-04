package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.GeneralUtil;
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
 * @author Drathus
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

    @Override
    public void load() {
        onBlock = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        try {
            String[] loc = ICUtil.COLON_PATTERN.split(getSign().getLine(3));
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

        } catch (Exception e) {
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
            plant();
    }

    public void plant() {

        if (!plantableItem(itemID)) return;

        if(target.getTypeId() != 0)
            return;

        if (itemPlantableOnBlock(itemID, target.getRelative(0, -1, 0).getTypeId())) {

            try {
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

                        if (diffX * diffX + diffY * diffY + diffZ * diffZ < 36) {
                            if(ItemUtil.takeFromEntity(itemEnt)) {
                                target.setTypeIdAndData(getBlockByItem(itemID), data == -1 ? 0 : data, true);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
        }
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
                    "Y Offset"
            };
            return lines;
        }
    }
}