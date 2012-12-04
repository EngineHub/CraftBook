package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
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
import com.sk89q.worldedit.BlockWorldVector;
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

    World world;
    int itemID = 295;
    byte data = -1;
    int yOffset = 2;
    Vector target;
    Vector onBlock;

    @Override
    public void load() {
        world = BukkitUtil.toSign(getSign()).getWorld();
        onBlock = BukkitUtil.toVector(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getLocation());
        if (!getSign().getLine(2).isEmpty()) {
            String[] lineParts = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            itemID = Integer.parseInt(lineParts[0]);
            try {
                data = Byte.parseByte(lineParts[1]);
            }
            catch(Exception e){}
        }
        try {
            yOffset = Integer.parseInt(getSign().getLine(3));
        } catch (NumberFormatException e) {
        }

        if (yOffset < 1) return;

        target = onBlock.add(0, yOffset, 0);
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

        if (world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY(), target.getBlockZ()) == 0 && itemPlantableOnBlock(itemID, world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY() - 1, target.getBlockZ()))) {

            BlockPlanter planter = new BlockPlanter(world, target, itemID, data);
            planter.run();
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

    protected static class BlockPlanter implements Runnable {

        private final World world;
        private final Vector target;
        private final int itemId;
        private final int damVal;

        public BlockPlanter(World world, Vector target, int itemId, int damVal) {

            this.world = world;
            this.target = target;
            this.itemId = itemId;
            this.damVal = damVal;
        }

        @Override
        public void run() {

            try {
                for (Entity ent : BukkitUtil.toBlock(new BlockWorldVector(BukkitUtil.getLocalWorld(world), target)).getChunk().getEntities()) {
                    if(!(ent instanceof Item))
                        continue;

                    Item itemEnt = (Item) ent;

                    if(!ItemUtil.isStackValid(itemEnt.getItemStack()))
                        continue;

                    if (itemEnt.getItemStack().getTypeId() == itemId && (damVal == -1 || itemEnt.getItemStack().getDurability() == damVal)) {
                        Location loc = itemEnt.getLocation();
                        double diffX = target.getBlockX() - loc.getX();
                        double diffY = target.getBlockY() - loc.getY();
                        double diffZ = target.getBlockZ() - loc.getZ();

                        if (diffX * diffX + diffY * diffY + diffZ * diffZ < 36) {
                            itemEnt.remove();

                            world.getBlockAt(target.getBlockX(), target.getBlockY(), target.getBlockZ()).setTypeIdAndData(getBlockByItem(itemId), (byte) (damVal == -1 ? 0 : damVal), true);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
        }

        private int getBlockByItem(int itemId) {

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