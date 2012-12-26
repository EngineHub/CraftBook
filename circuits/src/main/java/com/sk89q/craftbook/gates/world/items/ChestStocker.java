package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class ChestStocker extends AbstractIC {

    public ChestStocker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;
    Vector offset;

    @Override
    public void load() {

        offset = new Vector(0, 2, 0);

        item = ICUtil.getItem(getLine(2));

        try {
            String[] loc = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(3))[1]);
            offset = new Vector(Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]));
            if (offset.getX() > 16) offset.setX(16);
            if (offset.getY() > 16) offset.setY(16);
            if (offset.getZ() > 16) offset.setZ(16);

            if (offset.getX() < -16) offset.setX(-16);
            if (offset.getY() < -16) offset.setY(-16);
            if (offset.getZ() < -16) offset.setZ(-16);
        } catch (Exception e) {
            offset = new Vector(0, 2, 0);
        }
    }

    @Override
    public String getTitle() {

        return "Chest Stocker";
    }

    @Override
    public String getSignTitle() {

        return "STOCKER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, stock());
    }

    public boolean stock() {

        Block chest = BukkitUtil.toSign(getSign()).getBlock().getRelative(offset.getBlockX(), offset.getBlockY(),
                offset.getBlockZ());

        if (chest.getTypeId() == BlockID.CHEST) {

            Chest c = (Chest) chest.getState();
            if (c.getInventory().addItem(item.clone()).isEmpty()) return true;
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ChestStocker(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Adds item into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"item id:data", "x:y:z offset"};
            return lines;
        }
    }
}