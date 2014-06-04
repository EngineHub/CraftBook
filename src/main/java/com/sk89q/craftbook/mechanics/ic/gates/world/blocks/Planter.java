package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Dye;
import org.bukkit.material.Tree;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SearchArea;

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

                Block b = null;

                if ((b = searchBlocks(it)) != null) {
                    if (c.getInventory().removeItem(new ItemStack(it.getType(), 1, it.getDurability())).isEmpty()) {
                        return plantBlockAt(it, b);
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
            case INK_SACK:
                return ((Dye)item.getData()).getColor() == DyeColor.BROWN;
            default:
                return false;
        }
    }

    protected boolean itemPlantableAtBlock(ItemStack item, Block block) {

        switch (item.getType()) {
            case SAPLING:
            case RED_ROSE:
            case YELLOW_FLOWER:
                return block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS;
            case SEEDS:
            case MELON_SEEDS:
            case PUMPKIN_SEEDS:
            case POTATO_ITEM:
            case CARROT_ITEM:
                return block.getRelative(0, -1, 0).getType() == Material.SOIL;
            case NETHER_STALK:
                return block.getRelative(0, -1, 0).getType() == Material.SOUL_SAND;
            case CACTUS:
                return block.getRelative(0, -1, 0).getType() == Material.SAND;
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
                return block.getRelative(0, -1, 0).getType().isSolid();
            case WATER_LILY:
                return block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER;
            case INK_SACK:
                if(((Dye)item.getData()).getColor() != DyeColor.BROWN) return false;
                BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
                for(BlockFace face : faces) {
                    if(block.getRelative(face).getType() == Material.LOG && ((Tree)block.getRelative(face).getState().getData()).getSpecies() == TreeSpecies.JUNGLE)
                        return true;
                }
                return false;
            default:
                break;
        }
        return false;
    }

    protected boolean plantBlockAt(ItemStack item, Block block) {

        switch (item.getType()) {
            case SEEDS:
                block.setTypeIdAndData(Material.CROPS.getId(), (byte) 0, true);
                return true;
            case MELON_SEEDS:
                block.setType(Material.MELON_STEM);
                return true;
            case PUMPKIN_SEEDS:
                block.setType(Material.PUMPKIN_STEM);
                return true;
            case NETHER_STALK:
                block.setTypeIdAndData(Material.NETHER_WARTS.getId(), (byte) 0, true);
                return true;
            case POTATO_ITEM:
                block.setTypeIdAndData(Material.POTATO.getId(), (byte) 0, true);
                return true;
            case CARROT_ITEM:
                block.setTypeIdAndData(Material.CARROT.getId(), (byte) 0, true);
                return true;
            case INK_SACK:
                if(((Dye)item.getData()).getColor() != DyeColor.BROWN) return false;
                List<BlockFace> faces = new ArrayList<BlockFace>(Arrays.asList(new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}));
                Collections.shuffle(faces, CraftBookPlugin.inst().getRandom());
                for(BlockFace face : faces) {
                    if(block.getRelative(face).getType() == Material.LOG && ((Tree)block.getRelative(face).getState().getData()).getSpecies() == TreeSpecies.JUNGLE) {
                        block.setTypeIdAndData(Material.COCOA.getId(), (byte) 0, true);
                        BlockState state = block.getState();
                        ((CocoaPlant)state.getData()).setFacingDirection(face);
                        state.update();
                        return true;
                    }
                }
                return false;
            default:
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
            if(!SearchArea.isValidArea(BukkitUtil.toSign(sign).getBlock(), sign.getLine(3)))
                throw new ICVerificationException("Invalid SearchArea on 4th line!");
        }
    }
}