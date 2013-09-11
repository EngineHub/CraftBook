package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class CombineHarvester extends AbstractSelfTriggeredIC {

    public CombineHarvester(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Vector radius;
    Block target;
    Block onBlock;

    @Override
    public void load() {

        onBlock = getBackBlock();
        radius = ICUtil.parseRadius(getSign());
        if (getLine(2).contains("=")) {
            target = ICUtil.parseBlockLocation(getSign());
        } else {
            target = getBackBlock();
        }
    }

    @Override
    public String getTitle() {

        return "Combine Harvester";
    }

    @Override
    public String getSignTitle() {

        return "HARVEST";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, harvest());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, harvest());
    }

    public boolean harvest() {

        for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
            for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                    int rx = target.getX() - x;
                    int ry = target.getY() - y;
                    int rz = target.getZ() - z;
                    Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);

                    if (harvestable(b)) {

                        collectDrops(BlockUtil.getBlockDrops(b, null));
                        b.setTypeId(0);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void collectDrops(ItemStack[] drops) {

        BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
        Block pipe = getBackBlock().getRelative(back);
        if(Pipes.Factory.setupPipes(pipe, getBackBlock(), drops) != null)
            return;
        if (onBlock.getRelative(0, 1, 0).getTypeId() == BlockID.CHEST) {

            Chest c = (Chest) onBlock.getRelative(0, 1, 0).getState();
            HashMap<Integer, ItemStack> leftovers = c.getInventory().addItem(drops);
            for (ItemStack item : leftovers.values()) {

                onBlock.getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation().add(0.5, 0, 0.5), item);
            }
        } else {
            for (ItemStack item : drops) {

                onBlock.getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getLocation().add(0.5, 0, 0.5), item);
            }
        }
    }

    public boolean harvestable(Block block) {

        if((block.getTypeId() == BlockID.CROPS || block.getTypeId() == BlockID.CARROTS || block.getTypeId() == BlockID.POTATOES) && block.getData() >= 0x7)
            return true;

        if(block.getTypeId() == BlockID.CACTUS && block.getRelative(0, -1, 0).getTypeId() == BlockID.CACTUS && block.getRelative(0, 1, 0).getTypeId() != BlockID.CACTUS)
            return true;

        if(block.getTypeId() == BlockID.REED && block.getRelative(0, -1, 0).getTypeId() == BlockID.REED && block.getRelative(0, 1, 0).getTypeId() != BlockID.REED)
            return true;

        if(block.getTypeId() == BlockID.VINE && block.getRelative(0, 1, 0).getTypeId() == BlockID.VINE && block.getRelative(0, -1, 0).getTypeId() != BlockID.VINE)
            return true;

        if(block.getTypeId() == BlockID.COCOA_PLANT && ((block.getData() & 0x8) == 0x8 || (block.getData() & 0xC) == 0xC))
            return true;

        if(block.getTypeId() == BlockID.NETHER_WART && block.getData() >= 0x3)
            return true;

        if(block.getTypeId() == BlockID.MELON_BLOCK || block.getTypeId() == BlockID.PUMPKIN)
            return true;

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new CombineHarvester(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Harvests nearby crops.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oradius=x:y:z offset", null};
        }
    }
}