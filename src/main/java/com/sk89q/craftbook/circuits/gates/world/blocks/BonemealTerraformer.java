package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.worldedit.blocks.BlockID;

public class BonemealTerraformer extends AbstractSelfTriggeredIC {

    SearchArea area;

    public BonemealTerraformer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Bonemeal Terraformer";
    }

    @Override
    public String getSignTitle() {

        return "TERRAFORMER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            terraform();
        }
    }

    @Override
    public void think(ChipState state) {

        for(int i = 0; i < 10; i++)
            terraform();
    }

    public void terraform() {

        Block b = area.getRandomBlockInArea();

        if (b.getType() == Material.CROPS && b.getData() < 0x7) {
            if (consumeBonemeal()) {
                b.setData((byte) (b.getData() + 0x1));
            }
            return;
        }
        if ((b.getType() == Material.CROPS || b.getType() == Material.CARROT || b.getType() == Material.POTATO || b.getType() == Material.MELON_STEM || b.getType() == Material.PUMPKIN_STEM) && b.getData() < 0x7) {
            if (consumeBonemeal()) {
                byte add = (byte) CraftBookPlugin.inst().getRandom().nextInt(3);
                if(b.getData() + add > 0x7)
                    b.setData((byte) 0x7);
                else
                    b.setData((byte) (b.getData() + add));
            }
            return;
        }
        if (b.getType() == Material.COCOA && ((b.getData() & 0x8) != 0x8 || (b.getData() & 0xC) != 0xC)) {
            if (consumeBonemeal()) {
                if (CraftBookPlugin.inst().getRandom().nextInt(30) == 0)
                    b.setData((byte) (b.getData() | 0xC));
                else b.setData((byte) (b.getData() | 0x8));
            }
            return;
        }
        if (b.getType() == Material.NETHER_WARTS && b.getData() < 0x3) {
            if (consumeBonemeal()) {
                b.setData((byte) (b.getData() + 0x1));
            }
            return;
        }
        if (b.getType() == Material.SAPLING) {
            if (consumeBonemeal()) {
                if (!growTree(b, CraftBookPlugin.inst().getRandom())) refundBonemeal();
                else return;
            }
        }
        if (b.getType() == Material.BROWN_MUSHROOM || b.getType() == Material.RED_MUSHROOM) {
            if (consumeBonemeal()) {
                if (b.getType() == Material.BROWN_MUSHROOM) {
                    b.setType(Material.AIR);
                    if (!b.getWorld().generateTree(b.getLocation(), TreeType.BROWN_MUSHROOM)) {
                        b.setType(Material.BROWN_MUSHROOM);
                        refundBonemeal();
                    } else return;
                }
                if (b.getType() == Material.RED_MUSHROOM) {
                    b.setType(Material.AIR);
                    if (!b.getWorld().generateTree(b.getLocation(), TreeType.RED_MUSHROOM)) {
                        b.setType(Material.RED_MUSHROOM);
                        refundBonemeal();
                    } else return;
                }
            }
        }
        if ((b.getType() == Material.SUGAR_CANE_BLOCK || b.getType() == Material.CACTUS) && b.getData() < 0x15 && b.getRelative(0, 1, 0).getType() == Material.AIR) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setType(b.getType());
            }
            return;
        }
        if (b.getType() == Material.DIRT && b.getRelative(0, 1, 0).getType() == Material.AIR) {
            if (consumeBonemeal()) {
                b.setType(b.getBiome() == Biome.MUSHROOM_ISLAND || b.getBiome() == Biome.MUSHROOM_SHORE ? Material.MYCEL : Material.GRASS);
            }
            return;
        }
        if (b.getType() == Material.GRASS && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                int t = CraftBookPlugin.inst().getRandom().nextInt(7);
                if (t == 0) {
                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 1, true);
                } else if (t == 1) {
                    b.getRelative(0, 1, 0).setType(Material.YELLOW_FLOWER);
                } else if (t == 2) {
                    b.getRelative(0, 1, 0).setType(Material.RED_ROSE);
                } else if (t == 3) {
                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 2, true);
                } else {
                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 1, true);
                }
            }
            return;
        }
        if (b.getTypeId() == BlockID.SAND && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 0, true);
            }
            return;
        }
        if (b.getType() == Material.VINE && b.getRelative(0, -1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, -1, 0).setTypeIdAndData(BlockID.VINE, b.getData(), true);
            }
            return;
        }
        if (b.getType() == Material.STATIONARY_WATER && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(30) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setType(Material.WATER_LILY);
            }
            return;
        }
        if (b.getType() == Material.MYCEL && b.getRelative(0, 1, 0).getType() == Material.AIR
                && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                int t = CraftBookPlugin.inst().getRandom().nextInt(2);
                if (t == 0) {
                    b.getRelative(0, 1, 0).setType(Material.RED_MUSHROOM);
                } else if (t == 1) {
                    b.getRelative(0, 1, 0).setType(Material.BROWN_MUSHROOM);
                }
            }
            return;
        }
    }

    public boolean consumeBonemeal() {

        Block chest = getBackBlock().getRelative(0, 1, 0);
        if (chest.getType() == Material.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(Material.INK_SACK, 1,(short) 15));
            if (over.isEmpty()) return true;
        }

        return false;
    }

    public boolean refundBonemeal() {

        Block chest = getBackBlock().getRelative(0, 1, 0);
        if (chest.getType() == Material.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().addItem(new ItemStack(Material.INK_SACK, 1, (short) 15));
            if (over.isEmpty()) return true;
        }

        return false;
    }

    public boolean isSameSapling(Block sapling, Block other) {

        return sapling.getType() == other.getType() && (other.getData() & 3) == (sapling.getData() & 3);
    }

    public boolean growTree(Block sapling, Random random) {

        int data = sapling.getData() & 3;
        int i1 = 0;
        int j1 = 0;
        boolean flag = false;

        TreeType treeType = null;

        if (data == 1) {
            treeType = TreeType.REDWOOD;
        } else if (data == 2) {
            treeType = TreeType.BIRCH;
        } else if (data == 3) {
            for (i1 = 0; i1 >= -1; --i1) {
                for (j1 = 0; j1 >= -1; --j1) {
                    if (isSameSapling(sapling, sapling.getRelative(i1, 0, j1)) && isSameSapling(sapling,
                            sapling.getRelative(i1 + 1, 0, j1))
                            && isSameSapling(sapling, sapling.getRelative(i1, 0, j1 + 1))
                            && isSameSapling(sapling, sapling.getRelative(i1 + 1, 0, j1 + 1))) {
                        treeType = TreeType.JUNGLE;
                        flag = true;
                        break;
                    }
                }

                if (flag) {
                    break;
                }
            }

            if (!flag) {
                j1 = 0;
                i1 = 0;
                treeType = TreeType.SMALL_JUNGLE;
            }
        } else {
            treeType = TreeType.TREE;
            if (random.nextInt(10) == 0) {
                treeType = TreeType.BIG_TREE;
            }
        }

        if (flag) {
            sapling.getRelative(i1, 0, j1).setTypeId(0);
            sapling.getRelative(i1 + 1, 0, j1).setTypeId(0);
            sapling.getRelative(i1, 0, j1 + 1).setTypeId(0);
            sapling.getRelative(i1 + 1, 0, j1 + 1).setTypeId(0);
        } else {
            sapling.setTypeId(0);
        }

        boolean planted = sapling.getWorld().generateTree(sapling.getRelative(i1, 0, j1).getLocation(), treeType);

        if (!planted) {
            if (flag) {
                sapling.getRelative(i1, 0, j1).setTypeIdAndData(BlockID.SAPLING, (byte) data, true);
                sapling.getRelative(i1 + 1, 0, j1).setTypeIdAndData(BlockID.SAPLING, (byte) data, true);
                sapling.getRelative(i1, 0, j1 + 1).setTypeIdAndData(BlockID.SAPLING, (byte) data, true);
                sapling.getRelative(i1 + 1, 0, j1 + 1).setTypeIdAndData(BlockID.SAPLING, (byte) data, true);
            } else {
                sapling.setTypeIdAndData(BlockID.SAPLING, (byte) data, true);
            }
        }

        return planted;
    }

    public static class Factory extends AbstractICFactory {

        int maxradius;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BonemealTerraformer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Terraforms an area using bonemeal.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oradius=x:y:z", null};
        }
    }
}