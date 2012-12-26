package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.Server;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class BonemealTerraformer extends AbstractIC {

    int radius;

    public BonemealTerraformer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        try {
            radius = Integer.parseInt(getSign().getLine(2));
            if (radius > ((Factory) getFactory()).maxradius) {
                radius = ((Factory) getFactory()).maxradius;
                getSign().setLine(3, String.valueOf(((Factory) getFactory()).maxradius));
                getSign().update(false);
            }
        } catch (Exception e) {
            radius = 10;
        }
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
            terraform(true);
        }
    }

    public void terraform(boolean overrideChance) {

        BlockWorldVector position = getSign().getBlockVector();
        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++) {
                    if (overrideChance || BaseBukkitPlugin.random.nextInt(40) == 0) {
                        int rx = position.getBlockX() - x;
                        int ry = position.getBlockY() - y;
                        int rz = position.getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == BlockID.CROPS && b.getData() < 0x7) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x1));
                            }
                            return;
                        }
                        if ((b.getTypeId() == BlockID.CROPS || b.getTypeId() == BlockID.CARROTS || b.getTypeId() ==
                                BlockID.POTATOES
                                || b.getTypeId() == BlockID.MELON_STEM || b.getTypeId() == BlockID.PUMPKIN_STEM)
                                && b.getData() < 0x7) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x1));
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.COCOA_PLANT && ((b.getData() & 0x8) != 0x8 || (b.getData() &
                                0xC) != 0xC)) {
                            if (consumeBonemeal()) {
                                if (BaseBukkitPlugin.random.nextInt(30) == 0) b.setData((byte) (b.getData() | 0xC));
                                else b.setData((byte) (b.getData() | 0x8));
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.NETHER_WART && b.getData() < 0x3) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x1));
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.SAPLING) {
                            if (consumeBonemeal()) {
                                if (!growTree(b, BaseBukkitPlugin.random)) refundBonemeal();
                                else return;
                            }
                        }
                        if (b.getTypeId() == BlockID.BROWN_MUSHROOM || b.getTypeId() == BlockID.RED_MUSHROOM) {
                            if (consumeBonemeal()) {
                                if (b.getTypeId() == BlockID.BROWN_MUSHROOM) {
                                    b.setTypeId(0);
                                    if (!b.getWorld().generateTree(b.getLocation(), TreeType.BROWN_MUSHROOM)) {
                                        b.setTypeId(BlockID.BROWN_MUSHROOM);
                                        refundBonemeal();
                                    } else return;
                                }
                                if (b.getTypeId() == BlockID.RED_MUSHROOM) {
                                    b.setTypeId(0);
                                    if (!b.getWorld().generateTree(b.getLocation(), TreeType.RED_MUSHROOM)) {
                                        b.setTypeId(BlockID.RED_MUSHROOM);
                                        refundBonemeal();
                                    } else return;
                                }
                            }
                        }
                        if ((b.getTypeId() == BlockID.REED || b.getTypeId() == BlockID.CACTUS) && b.getData() < 0x15
                                && b.getRelative(0, 1, 0).getTypeId() == 0) {
                            if (consumeBonemeal()) {
                                b.getRelative(0, 1, 0).setTypeId(b.getTypeId());
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.DIRT && b.getRelative(0, 1, 0).getTypeId() == 0) {
                            if (consumeBonemeal()) {
                                b.setTypeId(b.getBiome() == Biome.MUSHROOM_ISLAND || b.getBiome() == Biome
                                        .MUSHROOM_SHORE ? BlockID.MYCELIUM
                                                        : BlockID.GRASS);
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.GRASS && b.getRelative(0, 1, 0).getTypeId() == BlockID.AIR
                                && BaseBukkitPlugin.random.nextInt(15) == 0) {
                            if (consumeBonemeal()) {
                                int t = BaseBukkitPlugin.random.nextInt(7);
                                if (t == 0) {
                                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 1, true);
                                } else if (t == 1) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.YELLOW_FLOWER);
                                } else if (t == 2) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.RED_FLOWER);
                                } else if (t == 3) {
                                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 2, true);
                                } else {
                                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 1, true);
                                }
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.SAND && b.getRelative(0, 1, 0).getTypeId() == BlockID.AIR
                                && BaseBukkitPlugin.random.nextInt(15) == 0) {
                            if (consumeBonemeal()) {
                                b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 0, true);
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.VINE && b.getRelative(0, -1, 0).getTypeId() == BlockID.AIR
                                && BaseBukkitPlugin.random.nextInt(15) == 0) {
                            if (consumeBonemeal()) {
                                b.getRelative(0, -1, 0).setTypeIdAndData(BlockID.VINE, b.getData(), true);
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.STATIONARY_WATER && b.getRelative(0, 1,
                                0).getTypeId() == BlockID.AIR
                                && BaseBukkitPlugin.random.nextInt(30) == 0) {
                            if (consumeBonemeal()) {
                                b.getRelative(0, 1, 0).setTypeId(BlockID.LILY_PAD);
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.MYCELIUM && b.getRelative(0, 1, 0).getTypeId() == BlockID.AIR
                                && BaseBukkitPlugin.random.nextInt(15) == 0) {
                            if (consumeBonemeal()) {
                                int t = BaseBukkitPlugin.random.nextInt(2);
                                if (t == 0) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.RED_MUSHROOM);
                                } else if (t == 1) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.BROWN_MUSHROOM);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean consumeBonemeal() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(ItemID.INK_SACK, 1,
                    (short) 15));
            if (over.isEmpty()) return true;
        }

        return false;
    }

    public boolean refundBonemeal() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().addItem(new ItemStack(ItemID.INK_SACK, 1, (short) 15));
            if (over.isEmpty()) return true;
        }

        return false;
    }

    public boolean isSameSapling(Block sapling, Block other) {

        return sapling.getTypeId() == other.getTypeId() && (other.getData() & 3) == (sapling.getData() & 3);
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
        public String getDescription() {

            return "Terraforms an area using bonemeal.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius", null};
            return lines;
        }

        @Override
        public void addConfiguration(BaseConfiguration.BaseConfigurationSection section) {

            maxradius = section.getInt("max-radius", 15);
        }

        @Override
        public boolean needsConfiguration() {

            return true;
        }
    }
}