package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Sapling planter Hybrid variant of MCX206 and MCX203 chest collector When there is a sapling or seed item drop in
 * range it will auto plant it above
 * the IC.
 *
 * @authors Drathus, Me4502
 */
public class Planter extends AbstractSelfTriggeredIC {

    public Planter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    ItemStack item;

    Block target;
    Block onBlock;
    Vector radius;

    @Override
    public void load() {

        item = ItemSyntax.getItem(getLine(2));

        onBlock = getBackBlock();

        radius = ICUtil.parseRadius(getSign(), 3);
        if (getLine(3).contains("=")) {
            target = ICUtil.parseBlockLocation(getSign(), 3);
        } else {
            target = getBackBlock();
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

        if (chip.getInput(0)) chip.setOutput(0, plant());
    }

    @Override
    public void think(ChipState state) {

        plant();
    }

    public boolean plant() {

        if (item != null && !plantableItem(item.getType())) return false;

        if (onBlock.getRelative(0, 1, 0).getType() == Material.CHEST || onBlock.getRelative(0, 1, 0).getType() == Material.TRAPPED_CHEST) {

            Chest c = (Chest) onBlock.getRelative(0, 1, 0).getState();
            for (ItemStack it : c.getInventory().getContents()) {

                if (!ItemUtil.isStackValid(it)) continue;
                if (!plantableItem(it.getType())) continue;

                if (item != null && !ItemUtil.areItemsIdentical(it, item)) continue;

                Block b = null;

                if ((b = searchBlocks(it)) != null) {
                    if (c.getInventory().removeItem(new ItemStack(it.getType(), 1, it.getDurability())).isEmpty()) {
                        b.setTypeIdAndData(getBlockByItem(it.getTypeId()), (byte) it.getDurability(), true);
                        return true;
                    } else
                        continue;
                }
            }
        } else {
            for (Entity ent : LocationUtil.getNearbyEntities(target.getLocation(), radius)) {
                if (!(ent instanceof Item)) continue;

                Item itemEnt = (Item) ent;
                ItemStack stack = itemEnt.getItemStack();

                if (!ItemUtil.isStackValid(stack)) continue;

                if (item == null || ItemUtil.areItemsIdentical(item, stack)) {

                    Block b = null;
                    if ((b = searchBlocks(stack)) != null) {
                        if (ItemUtil.takeFromItemEntity(itemEnt, 1)) {
                            b.setTypeIdAndData(getBlockByItem(stack.getTypeId()), stack.getData().getData(), true);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block searchBlocks(ItemStack stack) {

        for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
            for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                    int rx = target.getX() - x;
                    int ry = target.getY() - y;
                    int rz = target.getZ() - z;
                    Block b = onBlock.getWorld().getBlockAt(rx, ry, rz);

                    if (b.getType() != Material.AIR) continue;

                    if (itemPlantableOnBlock(stack.getType(), b.getRelative(0, -1, 0).getType())) {

                        return b;
                    }
                }
            }
        }
        return null;
    }

    protected boolean plantableItem(Material itemId) {

        switch (itemId) {
            case SAPLING:
            case SEEDS:
            case NETHER_WARTS:
            case MELON_SEEDS:
            case PUMPKIN_SEEDS:
            case CACTUS:
            case POTATO_ITEM:
            case CARROT_ITEM:
            case RED_ROSE:
            case YELLOW_FLOWER:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case WATER_LILY:
                return true;
            default:
                return false;
        }
    }

    protected boolean itemPlantableOnBlock(Material itemId, Material blockId) {

        switch (itemId) {
            case SAPLING:
            case RED_ROSE:
            case YELLOW_FLOWER:
                return blockId == Material.DIRT || blockId == Material.GRASS;
            case SEEDS:
            case MELON_SEEDS:
            case PUMPKIN_SEEDS:
            case POTATO:
            case CARROT:
                return blockId == Material.SOIL;
            case NETHER_WARTS:
                return blockId == Material.SOUL_SAND;
            case CACTUS:
                return blockId == Material.SAND;
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
                return !BlockType.canPassThrough(blockId.getId());
            case WATER_LILY:
                return blockId == Material.WATER || blockId == Material.STATIONARY_WATER;
            default:
                break;
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
            case BlockID.LILY_PAD:
                return BlockID.LILY_PAD;
            default:
                return itemId;
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
        public String getShortDescription() {

            return "Plants plantable things at set offset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oItem to plant id{:data}", "+oradius=x:y:z offset"};
        }
    }
}