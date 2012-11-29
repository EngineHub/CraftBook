package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
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
import com.sk89q.worldedit.blocks.BlockID;

public class Sorter extends AbstractIC {

    public Sorter (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    Block chestBlock;
    boolean inverted = false;

    @Override
    public void load() {

        chestBlock = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0,1,0);
        inverted = getSign().getLine(2).equalsIgnoreCase("invert");
    }

    @Override
    public String getTitle () {
        return "Sorter";
    }

    @Override
    public String getSignTitle () {
        return "SORTER";
    }

    @Override
    public void trigger (ChipState chip) {
        if(chip.getInput(0))
            chip.setOutput(0, sort());
    }

    public boolean sort() {

        boolean returnValue = false;

        for (Entity en : BukkitUtil.toSign(getSign()).getChunk().getEntities()) {
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
            if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {

                BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
                Block b;

                if(isInAboveChest(stack) || inverted) {
                    b = SignUtil.getRightBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
                }
                else {
                    b = SignUtil.getLeftBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
                }

                item.teleport(b.getLocation().add(0.5, 0.5, 0.5));

                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean isInAboveChest(ItemStack item) {

        if(chestBlock.getTypeId() == BlockID.CHEST) {
            Chest chest = (Chest) chestBlock.getState();
            return chest.getInventory().contains(new ItemStack(item.getTypeId(), 1, item.getDurability(), item.getData().getData()));
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Sorter(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Sorts items and spits out left/right depending on above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "invert - to invert output sides",
                    null
            };
            return lines;
        }
    }
}