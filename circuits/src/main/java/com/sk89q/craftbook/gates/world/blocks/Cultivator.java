package com.sk89q.craftbook.gates.world.blocks;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

public class Cultivator extends AbstractIC {

    public Cultivator (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Cultivator";
    }

    @Override
    public String getSignTitle () {
        return "CULTIVATOR";
    }

    int maxradius = -1, radius;

    @Override
    public void load() {

        if (maxradius == -1) {
            maxradius = ((Factory) getFactory()).maxradius;
        }
        try {
            radius = Integer.parseInt(getSign().getLine(2));
            if (radius > maxradius) {
                radius = maxradius;
                getSign().setLine(3, String.valueOf(maxradius));
                getSign().update(false);
            }
        } catch (Exception e) {
            radius = 10;
        }
    }

    @Override
    public void trigger (ChipState chip) {
        if(chip.getInput(0))
            chip.setOutput(0, cultivate());
    }

    public boolean cultivate() {

        BlockWorldVector position = getSign().getBlockVector();
        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++) {
                    BlockVector current = position.subtract(x, y, z).toBlockPoint();
                    BaseBlock b = getSign().getLocalWorld().getBlock(current);
                    if(b.getId() == BlockID.DIRT || b.getId() == BlockID.GRASS) {
                        if(damageHoe()) {
                            b.setId(BlockID.SOIL);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean damageHoe() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            for(int i = 290; i < 294; i++) {
                for(int slot = 0; slot < c.getInventory().getSize(); slot++) {
                    if(c.getInventory().getItem(slot) == null || c.getInventory().getItem(slot).getTypeId() != i)
                        continue;
                    if(ItemUtil.isStackValid(c.getInventory().getItem(slot))) {
                        ItemStack item = c.getInventory().getItem(slot);
                        item.setDurability((short) (item.getDurability() + 1));
                        c.getInventory().setItem(slot, item);
                        return true;
                    }
                }
            }
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

            return new Cultivator(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Cultivates an area using a hoe.";
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