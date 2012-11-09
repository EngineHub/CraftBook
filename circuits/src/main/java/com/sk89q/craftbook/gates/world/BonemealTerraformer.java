package com.sk89q.craftbook.gates.world;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class BonemealTerraformer extends AbstractIC {

    int radius;
    Integer maxradius;

    public BonemealTerraformer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    private void load() {

        if (maxradius == null) {
            maxradius = ((Factory) getFactory()).maxradius;
        }
        try {
            radius = Integer.parseInt(getSign().getLine(3));
            if (radius > maxradius) {
                radius = maxradius;
                getSign().setLine(3, maxradius + "");
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

        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++)
                    if (overrideChance || BaseBukkitPlugin.random.nextInt(40) == 0) {
                        int rx = getSign().getSignLocation().getPosition().getBlockX() - x;
                        int ry = getSign().getSignLocation().getPosition().getBlockY() - y;
                        int rz = getSign().getSignLocation().getPosition().getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == BlockID.CROPS && b.getData() < 0x7) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x1));
                            }
                            return;
                        }
                        if ((b.getTypeId() == BlockID.CROPS || b.getTypeId() == BlockID.CARROTS || b.getTypeId() == BlockID.POTATOES || b.getTypeId() == BlockID.MELON_STEM || b.getTypeId() ==
                                BlockID.PUMPKIN_STEM) && b.getData() < 0x7) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x1));
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
                                b.setData((byte) (b.getData() | 0x8));
                            }
                            return;
                        }
                        if ((b.getTypeId() == BlockID.REED || b.getTypeId() == BlockID.CACTUS) && b.getData
                                () < 0x14 && b.getRelative(0, 1, 0).getTypeId() == 0 && b.getRelative(0, -2,
                                        0).getTypeId() != b.getTypeId() && b.getRelative(0, -1,
                                                0).getTypeId() != b.getTypeId()) {
                            if (consumeBonemeal()) {
                                b.setData((byte) (b.getData() + 0x2));
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.DIRT && b.getRelative(0, 1, 0).getTypeId() == 0) {
                            if (consumeBonemeal()) {
                                b.setTypeId(b.getBiome() == Biome.MUSHROOM_ISLAND || b.getBiome() == Biome
                                        .MUSHROOM_SHORE ? BlockID.MYCELIUM : BlockID.GRASS);
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.GRASS && b.getRelative(0, 1,
                                0).getTypeId() == BlockID.AIR && BaseBukkitPlugin.random.nextInt(15) == 0) {
                            if (consumeBonemeal()) {
                                int t = BaseBukkitPlugin.random.nextInt(7);
                                if (t == 0) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.LONG_GRASS);
                                    b.getRelative(0, 1, 0).setData((byte) 1);
                                } else if (t == 1) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.YELLOW_FLOWER);
                                } else if (t == 2) {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.RED_FLOWER);
                                } else if (t == 3) {
                                    b.getRelative(0, 1, 0).setTypeIdAndData(BlockID.LONG_GRASS, (byte) 2, true);
                                } else {
                                    b.getRelative(0, 1, 0).setTypeId(BlockID.LONG_GRASS);
                                    b.getRelative(0, 1, 0).setData((byte) 1);
                                }
                            }
                            return;
                        }
                        if (b.getTypeId() == BlockID.MYCELIUM && b.getRelative(0, 1,
                                0).getTypeId() == BlockID.AIR && BaseBukkitPlugin.random.nextInt(15) == 0) {
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

    public boolean consumeBonemeal() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(ItemID.INK_SACK, 1,
                    (short) 15));
            if (over.size() == 0)
                return true;
        }

        return false;
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

            String[] lines = new String[] {
                    "radius",
                    null
            };
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