package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.worldedit.blocks.BlockType;

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

    SearchArea area;

    @Override
    public void load() {

        if(getLine(2).isEmpty())
            item = null;
        else
            item = ItemSyntax.getItem(getLine(2));

        area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
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

        if(state.getInput(0)) return;

        for(int i = 0; i < 10; i++)
            plant();
    }

    public boolean plant() {

        if (item != null && !plantableItem(item.getType())) return false;

        if (getBackBlock().getRelative(0, 1, 0).getType() == Material.CHEST || getBackBlock().getRelative(0, 1, 0).getType() == Material.TRAPPED_CHEST) {

            Chest c = (Chest) getBackBlock().getRelative(0, 1, 0).getState();
            for (ItemStack it : c.getInventory().getContents()) {

                if (!ItemUtil.isStackValid(it)) continue;
                if (!plantableItem(it.getType())) continue;

                if (item != null && !ItemUtil.areItemsIdentical(it, item)) continue;

                Block b = null;

                if ((b = searchBlocks(it)) != null) {
                    if (c.getInventory().removeItem(new ItemStack(it.getType(), 1, it.getDurability())).isEmpty()) {
                        b.setTypeIdAndData(getBlockByItem(it.getType()).getId(), (byte) it.getDurability(), true);
                        return true;
                    } else
                        continue;
                }
            }
        } else {
            for (Entity ent : area.getEntitiesInArea()) {
                if (!(ent instanceof Item)) continue;

                Item itemEnt = (Item) ent;
                ItemStack stack = itemEnt.getItemStack();

                if (!ItemUtil.isStackValid(stack)) continue;

                if (item == null || ItemUtil.areItemsIdentical(item, stack)) {

                    Block b = null;
                    if ((b = searchBlocks(stack)) != null) {
                        if (ItemUtil.takeFromItemEntity(itemEnt, 1)) {
                            b.setTypeIdAndData(getBlockByItem(stack.getType()).getId(), stack.getData().getData(), true);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block searchBlocks(ItemStack stack) {

        Block b = area.getRandomBlockInArea();

        if (b == null || b.getType() != Material.AIR) return null;

        if (itemPlantableOnBlock(stack.getType(), b.getRelative(0, -1, 0).getType())) {

            return b;
        }
        return null;
    }

    protected boolean plantableItem(Material itemId) {

        switch (itemId) {
            case SAPLING:
            case SEEDS:
            case NETHER_STALK:
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
            case POTATO_ITEM:
            case CARROT_ITEM:
                return blockId == Material.SOIL;
            case NETHER_STALK:
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

    protected Material getBlockByItem(Material itemId) {

        switch (itemId) {
            case SEEDS:
                return Material.CROPS;
            case MELON_SEEDS:
                return Material.MELON_STEM;
            case PUMPKIN_SEEDS:
                return Material.PUMPKIN_STEM;
            case NETHER_STALK:
                return Material.NETHER_WARTS;
            case POTATO_ITEM:
                return Material.POTATO;
            case CARROT_ITEM:
                return Material.CARROT;
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

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(BukkitUtil.toSign(sign).getBlock(), sign.getLine(3)))
                throw new ICVerificationException("Invalid SearchArea on 4th line!");
        }
    }
}