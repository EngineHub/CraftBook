package com.sk89q.craftbook.gates.world;

import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * Sapling planter
 * Hybrid variant of MCX206 and MCX203 chest collector
 * When there is a sapling or seed item drop in range it will auto plant it above the IC.
 *
 * @author Drathus
 */
public class Planter extends AbstractIC {

    public Planter(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
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

        World world = getSign().getWorld();
        Vector onBlock = BukkitUtil.toVector(SignUtil.getBackBlock(
                getSign().getBlock()).getLocation());
        Vector target;
        int[] info = null;
        int yOffset;

        if (getSign().getLine(2).length() != 0) {
            String[] lineParts = getSign().getLine(2).split(":");
            info = new int[] {Integer.parseInt(lineParts[0]), Integer.parseInt(lineParts[1])};
        }

        if (info == null || !plantableItem(info[0])) return;

        try {
            yOffset = Integer.parseInt(getSign().getLine(3));
        } catch (NumberFormatException e) {
            return;
        }
        if (yOffset < 1) return;

        target = onBlock.add(0, yOffset, 0);

        if (world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY(),
                target.getBlockZ()) == 0
                && itemPlantableOnBlock(info[0], world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY() - 1,
                        target.getBlockZ()))) {

            BlockPlanter run = new BlockPlanter(world, target, info[0], info[1]);
            run.run();
        }
    }

    protected boolean plantableItem(int itemId) {

        return itemId == BlockID.SAPLING || itemId == ItemID.SEEDS || itemId == ItemID.NETHER_WART_SEED
                || itemId == ItemID.MELON_SEEDS || itemId == ItemID.PUMPKIN_SEEDS || itemId == BlockID.CACTUS;
    }

    protected boolean itemPlantableOnBlock(int itemId, int blockId) {

        boolean isPlantable = false;

        if (itemId == BlockID.SAPLING && (blockId == BlockID.DIRT || blockId == BlockID.GRASS)) {
            isPlantable = true;
        }
        else if ((itemId == ItemID.SEEDS || itemId == ItemID.MELON_SEEDS || itemId == ItemID.PUMPKIN_SEEDS) && blockId == BlockID.SOIL) {
            isPlantable = true;
        }
        else if (itemId == ItemID.NETHER_WART_SEED && blockId == BlockID.SLOW_SAND) {
            isPlantable = true;
        }
        else if (itemId == BlockID.CACTUS && blockId == BlockID.SAND) {
            isPlantable = true;
        }

        return isPlantable;
    }

    protected class BlockPlanter implements Runnable {

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
                Collection<Item> items = world.getEntitiesByClass(Item.class);

                if (items == null) return;

                for (Item itemEnt : items)
                    if (!itemEnt.isDead()
                            && itemEnt.getItemStack().getAmount() > 0
                            && itemEnt.getItemStack().getTypeId() == itemId
                            && (damVal == -1 || damVal == -1 || itemEnt
                            .getItemStack().getDurability() == damVal)) {
                        double diffX = target.getBlockX()
                                - itemEnt.getLocation().getX();
                        double diffY = target.getBlockY()
                                - itemEnt.getLocation().getY();
                        double diffZ = target.getBlockZ()
                                - itemEnt.getLocation().getZ();

                        if (diffX * diffX + diffY * diffY + diffZ * diffZ < 6) {
                            itemEnt.remove();

                            world.getBlockAt(target.getBlockX(),
                                    target.getBlockY(), target.getBlockZ())
                                    .setTypeId(getBlockByItem(itemId));
                            world.getBlockAt(target.getBlockX(),
                                    target.getBlockY(), target.getBlockZ())
                                    .setData(
                                            (byte) (damVal == -1 ? 0
                                                    : damVal));

                            break;
                        }
                    }
            } catch (Exception ignored) {
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
        public IC create(Sign sign) {

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