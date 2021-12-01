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
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.SearchArea;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BonemealTerraformer extends AbstractSelfTriggeredIC {

    private SearchArea area;

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

        if(state.getInput(0)) return;

        for(int i = 0; i < 10; i++)
            terraform();
    }

    public void terraform() {

        Block b = area.getRandomBlockInArea();

        if(b == null) return;

        if ((b.getType() == Material.WHEAT
                || b.getType() == Material.CARROTS
                || b.getType() == Material.POTATOES
                || b.getType() == Material.MELON_STEM
                || b.getType() == Material.BEETROOTS
                || b.getType() == Material.NETHER_WART
                || b.getType() == Material.COCOA
                || b.getType() == Material.PUMPKIN_STEM)
                && ((Ageable) b.getBlockData()).getAge() < ((Ageable) b.getBlockData()).getMaximumAge()) {
            if (consumeBonemeal()) {
                Ageable ageable = (Ageable) b.getBlockData();
                int add = CraftBookPlugin.inst().getRandom().nextInt(3);
                if(ageable.getAge() + add > ageable.getMaximumAge())
                    ageable.setAge(ageable.getMaximumAge());
                else
                    ageable.setAge(ageable.getAge() + add);
                b.setBlockData(ageable);
            }
            return;
        }
        if (Tag.SAPLINGS.isTagged(b.getType())) {
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
        if ((b.getType() == Material.SUGAR_CANE || b.getType() == Material.CACTUS) && b.getData() < 0x15 && b.getRelative(0, 1, 0).getType() == Material.AIR) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setType(b.getType());
            }
            return;
        }
        if (b.getType() == Material.DIRT && b.getRelative(0, 1, 0).getType() == Material.AIR) {
            if (consumeBonemeal()) {
                b.setType(b.getBiome() == Biome.MUSHROOM_FIELDS ? Material.MYCELIUM : Material.GRASS_BLOCK);
            }
            return;
        }
        if (b.getType() == Material.GRASS_BLOCK && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                int t = CraftBookPlugin.inst().getRandom().nextInt(7);
                if (t == 0) {
                    b.getRelative(0, 1, 0).setType(Material.GRASS);
                } else if (t == 1) {
                    b.getRelative(0, 1, 0).setType(Material.DANDELION);
                } else if (t == 2) {
                    b.getRelative(0, 1, 0).setType(Material.POPPY);
                } else if (t == 3) {
                    b.getRelative(0, 1, 0).setType(Material.FERN);
                } else {
                    b.getRelative(0, 1, 0).setType(Material.GRASS);
                }
            }
            return;
        }
        if (b.getType() == Material.SAND && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setType(Material.DEAD_BUSH);
            }
            return;
        }
        if (b.getType() == Material.VINE && b.getRelative(0, -1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(15) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, -1, 0).setBlockData(b.getBlockData(), true);
            }
            return;
        }
        if (b.getType() == Material.WATER && b.getRelative(0, 1, 0).getType() == Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(30) == 0) {
            if (consumeBonemeal()) {
                b.getRelative(0, 1, 0).setType(Material.LILY_PAD);
            }
            return;
        }
        if (b.getType() == Material.MYCELIUM && b.getRelative(0, 1, 0).getType() == Material.AIR
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
        return InventoryUtil.doesBlockHaveInventory(chest) && InventoryUtil.removeItemsFromInventory((InventoryHolder) chest.getState(),
                new ItemStack(Material.BONE_MEAL, 1));

    }

    public boolean refundBonemeal() {

        Block chest = getBackBlock().getRelative(0, 1, 0);
        return InventoryUtil.doesBlockHaveInventory(chest) && InventoryUtil.addItemsToInventory((InventoryHolder) chest.getState(), new ItemStack(Material.BONE_MEAL, 1)).isEmpty();

    }

    public static boolean isSameSapling(Block sapling, Block other) {

        return sapling.getType() == other.getType() && (other.getData() & 3) == (sapling.getData() & 3);
    }

    public boolean growTree(Block sapling, Random random) {

        Material data = sapling.getType();
        int i1 = 0;
        int j1 = 0;
        boolean flag = false;

        TreeType treeType = null;

        if (data == Material.SPRUCE_SAPLING) {
            treeType = TreeType.REDWOOD;
        } else if (data == Material.BIRCH_SAPLING) {
            treeType = TreeType.BIRCH;
        } else if (data == Material.JUNGLE_SAPLING) {
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
        } else if (data == Material.ACACIA_SAPLING) {
            treeType = TreeType.ACACIA;
        } else if (data == Material.DARK_OAK_SAPLING) {
            treeType = TreeType.DARK_OAK;
        } else {
            treeType = TreeType.TREE;
            if (random.nextInt(10) == 0) {
                treeType = TreeType.BIG_TREE;
            }
        }

        if (flag) {
            sapling.getRelative(i1, 0, j1).setType(Material.AIR);
            sapling.getRelative(i1 + 1, 0, j1).setType(Material.AIR);
            sapling.getRelative(i1, 0, j1 + 1).setType(Material.AIR);
            sapling.getRelative(i1 + 1, 0, j1 + 1).setType(Material.AIR);
        } else {
            sapling.setType(Material.AIR);
        }

        boolean planted = sapling.getWorld().generateTree(sapling.getRelative(i1, 0, j1).getLocation(), treeType);

        if (!planted) {
            if (flag) {
                sapling.getRelative(i1, 0, j1).setType(data);
                sapling.getRelative(i1 + 1, 0, j1).setType(data);
                sapling.getRelative(i1, 0, j1 + 1).setType(data);
                sapling.getRelative(i1 + 1, 0, j1 + 1).setType(data);
            } else {
                sapling.setType(data);
            }
        }

        return planted;
    }

    public static class Factory extends AbstractICFactory {

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

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}