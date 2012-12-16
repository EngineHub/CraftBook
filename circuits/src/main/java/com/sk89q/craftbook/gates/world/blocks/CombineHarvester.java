package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CombineHarvester extends AbstractIC {

    public CombineHarvester (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    Vector offset = new Vector(0,2,0);
    int radius = 10;

    Block target;
    Block onBlock;

    @Override
    public void load() {

        onBlock = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        try {
            radius = Integer.parseInt(ICUtil.EQUALS_PATTERN.split(getSign().getLine(3))[0]);
            try {
                String[] loc = ICUtil.COLON_PATTERN.split(ICUtil.EQUALS_PATTERN.split(getSign().getLine(3))[1]);
                offset = new Vector(Integer.parseInt(loc[0]),Integer.parseInt(loc[1]),Integer.parseInt(loc[2]));
                if(offset.getX() > 16)
                    offset.setX(16);
                if(offset.getY() > 16)
                    offset.setY(16);
                if(offset.getZ() > 16)
                    offset.setZ(16);

                if(offset.getX() < -16)
                    offset.setX(-16);
                if(offset.getY() < -16)
                    offset.setY(-16);
                if(offset.getZ() < -16)
                    offset.setZ(-16);
            }
            catch(Exception e){
                offset = new Vector(0,2,0);
            }

        } catch (Exception e) {
            radius = 10;
            offset = new Vector(0,2,0);
        }

        target = onBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
    }

    @Override
    public String getTitle () {
        return "Combine Harvester";
    }

    @Override
    public String getSignTitle () {
        return "HARVEST";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, harvest());
    }

    public boolean harvest() {

        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++) {
                    int rx = target.getX() - x;
                    int ry = target.getY() - y;
                    int rz = target.getZ() - z;
                    Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);

                    if (harvestable(b)) {

                        collectDrops(b.getDrops().toArray(new ItemStack[b.getDrops().size()]));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void collectDrops(ItemStack[] drops) {

        if(onBlock.getRelative(0, 1, 0).getTypeId() == BlockID.CHEST) {

            Chest c = (Chest) onBlock.getState();
            HashMap<Integer, ItemStack> leftovers = c.getInventory().addItem(drops);
            for(ItemStack item : leftovers.values()) {

                onBlock.getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation().add(0.5, 0,  0.5), item);
            }
        }
        else {
            for(ItemStack item : drops) {

                onBlock.getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation().add(0.5, 0,  0.5), item);
            }
        }
    }

    public boolean harvestable(Block block) {

        //TODO add a list of things that can be harvestable, and in what circumstance.
        if((block.getTypeId() == BlockID.CROPS || block.getTypeId() == BlockID.CARROTS || block.getTypeId() == BlockID.POTATOES) && block.getData() >= 0x7)
            return true;
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new CombineHarvester(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Harvests nearby crops.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius=x:y:z offset",
                    null
            };
            return lines;
        }
    }
}