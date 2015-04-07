package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.worldedit.Vector;

public class CombineHarvester extends AbstractSelfTriggeredIC {

    public CombineHarvester(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
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

        if(chip.getInput(0)) return;

        for(int i = 0; i < 10; i++)
            chip.setOutput(0, harvest());
    }

    public boolean harvest() {

        Block b = area.getRandomBlockInArea();

        if(b == null) return false;

        if (harvestable(b)) {
            ICUtil.collectItem(this, new Vector(0, 1, 0), BlockUtil.getBlockDrops(b, null));
            b.setType(Material.AIR);
            return true;
        }
        return false;
    }

    public boolean harvestable(Block block) {
        Material above = block.getRelative(0, 1, 0).getType();
        Material below = block.getRelative(0, -1, 0).getType();
        switch (block.getType()) {
            case CROPS:
            case CARROT:
            case POTATO:
                return block.getData() >= 0x7;
            case CACTUS:
                return below == Material.CACTUS && above != Material.CACTUS;
            case SUGAR_CANE_BLOCK:
                return below == Material.SUGAR_CANE_BLOCK && above != Material.SUGAR_CANE_BLOCK;
            case VINE:
                return above == Material.VINE && below != Material.VINE;
            case COCOA:
                return (block.getData() & 0x8) == 0x8 || (block.getData() & 0xC) == 0xC;
            case NETHER_WARTS:
                return block.getData() >= 0x3;
            case MELON_BLOCK:
            case PUMPKIN:
            case LOG:
            case LOG_2:
                return true;
            default:
                return false;
        }
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

            return new String[] {"SearchArea", null};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(BukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}