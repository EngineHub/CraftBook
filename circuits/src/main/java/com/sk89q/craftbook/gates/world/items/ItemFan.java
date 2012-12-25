package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

public class ItemFan extends AbstractIC {

    public ItemFan (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    double force;

    @Override
    public void load () {
        try {
            force = Double.parseDouble(getSign().getLine(2));
        } catch (Exception ignored) {
            force = 1;
        }
    }

    @Override
    public String getTitle () {
        return "Item Fan";
    }

    @Override
    public String getSignTitle () {
        return "ITEM FAN";
    }

    @Override
    public void trigger (ChipState chip) {
        if (chip.getInput(0)) chip.setOutput(0, push());
    }

    public boolean push () {

        boolean returnValue = false;

        Block aboveBlock = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);

        for (Entity en : aboveBlock.getChunk().getEntities()) {
            if (!(en instanceof Item)) {
                continue;
            }
            Item item = (Item) en;
            ItemStack stack = item.getItemStack();
            if (!ItemUtil.isStackValid(stack) || item.isDead() || !item.isValid()) {
                continue;
            }
            Location location = item.getLocation();
            int ix = location.getBlockX();
            int iy = location.getBlockY();
            int iz = location.getBlockZ();
            if (ix == aboveBlock.getX() && iy == aboveBlock.getY() && iz == aboveBlock.getZ()) {

                item.teleport(item.getLocation().add(0, force, 0));

                returnValue = true;
            }
        }

        return returnValue;
    }

    public static class Factory extends AbstractICFactory {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new ItemFan(getServer(), sign, this);
        }

        @Override
        public String getDescription () {

            return "Gently pushes items upwards.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "force (default 1)", null };
            return lines;
        }
    }
}