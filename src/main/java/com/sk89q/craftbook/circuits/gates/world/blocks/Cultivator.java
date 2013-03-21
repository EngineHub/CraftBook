package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class Cultivator extends AbstractSelfTriggeredIC {

    public Cultivator(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Cultivator";
    }

    @Override
    public String getSignTitle() {

        return "CULTIVATOR";
    }

    Vector radius;
    Location offset;

    @Override
    public void load() {

        radius = ICUtil.parseRadius(getSign());
        offset = ICUtil.parseBlockLocation(getSign()).getLocation();

    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, cultivate());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, cultivate());
    }

    public boolean cultivate() {

        for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
            for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                    Block b = offset.add(x, y, z).getBlock();
                    if (b.getTypeId() == BlockID.DIRT || b.getTypeId() == BlockID.GRASS) {
                        if (b.getRelative(BlockFace.UP).getTypeId() == 0 && damageHoe()) {
                            b.setTypeId(BlockID.SOIL);
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
            for (int i = 290; i < 294; i++) {
                for (int slot = 0; slot < c.getInventory().getSize(); slot++) {
                    if (c.getInventory().getItem(slot) == null || c.getInventory().getItem(slot).getTypeId() != i)
                        continue;
                    if (ItemUtil.isStackValid(c.getInventory().getItem(slot))) {
                        ItemStack item = c.getInventory().getItem(slot);
                        item.setDurability((short) (item.getDurability() + 1));
                        if(item.getDurability() < 0)
                            item = null;
                        c.getInventory().setItem(slot, item);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Cultivator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Cultivates an area using a hoe.";
        }

        @Override
        public String getLongDescription() {

            return "The Cultivator IC tills dirt and grass around the IC within the area designated on line 3, using the hoes in the above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"+oradius", null};
            return lines;
        }
    }
}