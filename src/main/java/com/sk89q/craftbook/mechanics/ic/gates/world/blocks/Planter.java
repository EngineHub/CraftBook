package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SearchArea;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        if (item != null && !plantableItem(item)) return false;

        if (getBackBlock().getRelative(0, 1, 0).getType() == Material.CHEST || getBackBlock().getRelative(0, 1, 0).getType() == Material.TRAPPED_CHEST) {

            Chest c = (Chest) getBackBlock().getRelative(0, 1, 0).getState();
            for (ItemStack it : c.getInventory().getContents()) {

                if (!ItemUtil.isStackValid(it)) continue;
                if (!plantableItem(it)) continue;

                if (item != null && !ItemUtil.areItemsIdentical(it, item)) continue;

                Block b;

                if ((b = searchBlocks(it)) != null) {
                    if (c.getInventory().removeItem(new ItemStack(it.getType(), 1, it.getDurability())).isEmpty()) {
                        return plantBlockAt(it, b);
                    }
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
                            return plantBlockAt(stack, b);
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block searchBlocks(ItemStack stack) {

        Block b = area.getRandomBlockInArea();

        if (b == null || b.getType() != Material.AIR)
            return null;

        if (itemPlantableAtBlock(stack, b))
            return b;

        return null;
    }

    protected boolean plantableItem(ItemStack item) {
        switch (item.getType()) {
            case WHEAT_SEEDS:
            case NETHER_WART:
            case MELON_SEEDS:
            case PUMPKIN_SEEDS:
            case CACTUS:
            case POTATO:
            case CARROT:
            case POPPY:
            case DANDELION:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case LILY_PAD:
            case BEETROOT_SEEDS:
            case COCOA_BEANS:
                return true;
            default:
                return Tag.SAPLINGS.isTagged(item.getType());
        }
    }

    protected boolean itemPlantableAtBlock(ItemStack item, Block block) {

        switch (item.getType()) {
            case POPPY:
            case DANDELION:
                return block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS_BLOCK || block.getRelative(0, -1, 0).getType() == Material.PODZOL;
            case WHEAT_SEEDS:
            case MELON_SEEDS:
            case PUMPKIN_SEEDS:
            case POTATO:
            case CARROT:
            case BEETROOT_SEEDS:
                return block.getRelative(0, -1, 0).getType() == Material.FARMLAND;
            case NETHER_WART:
                return block.getRelative(0, -1, 0).getType() == Material.SOUL_SAND;
            case CACTUS:
                return block.getRelative(0, -1, 0).getType() == Material.SAND;
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
                return block.getRelative(0, -1, 0).getType().isSolid();
            case LILY_PAD:
                return block.getRelative(0, -1, 0).getType() == Material.WATER;
            case COCOA_BEANS:
                BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
                for(BlockFace face : faces) {
                    if(block.getRelative(face).getType() == Material.JUNGLE_LOG)
                        return true;
                }
                return false;
            default:
                if (Tag.SAPLINGS.isTagged(item.getType())) {
                    return block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS_BLOCK || block.getRelative(0, -1, 0).getType() == Material.PODZOL;
                }
                return false;
        }
    }

    protected boolean plantBlockAt(ItemStack item, Block block) {

        switch (item.getType()) {
            case POPPY:
            case DANDELION:
            case CACTUS:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case LILY_PAD:
                block.setType(item.getType());
                return true;
            case WHEAT_SEEDS:
                block.setType(Material.WHEAT);
                return true;
            case MELON_SEEDS:
                block.setType(Material.MELON_STEM);
                return true;
            case PUMPKIN_SEEDS:
                block.setType(Material.PUMPKIN_STEM);
                return true;
            case NETHER_WART:
                block.setType(Material.NETHER_WART);
                return true;
            case POTATO:
                block.setType(Material.POTATOES);
                return true;
            case CARROT:
                block.setType(Material.CARROTS);
                return true;
            case BEETROOT_SEEDS:
                block.setType(Material.BEETROOTS);
                return true;
            case COCOA_BEANS:
                List<BlockFace> faces =
                        new ArrayList<>(Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH));
                Collections.shuffle(faces, CraftBookPlugin.inst().getRandom());
                for(BlockFace face : faces) {
                    if(block.getRelative(face).getType() == Material.JUNGLE_LOG) {
                        block.setType(Material.COCOA);
                        ((Cocoa) block.getBlockData()).setFacing(face);
                        return true;
                    }
                }
                return false;
            default:
                if (Tag.SAPLINGS.isTagged(item.getType())) {
                    block.setType(item.getType());
                    return true;
                }
                return false;
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

            return new String[] {"+oItem to plant id{:data}", "SearchArea"};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(3)))
                throw new ICVerificationException("Invalid SearchArea on 4th line!");
        }
    }
}