package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

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
            ICUtil.collectItem(this, BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(b, null));
            b.setType(Material.AIR);
            return true;
        }
        return false;
    }

    public boolean harvestable(Block block) {
        Material above = block.getRelative(0, 1, 0).getType();
        Material below = block.getRelative(0, -1, 0).getType();
        switch (block.getType()) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case NETHER_WART:
            case COCOA:
                Ageable ageable = (Ageable) block.getBlockData();
                return ageable.getAge() == ageable.getMaximumAge();
            case CACTUS:
                return below == Material.CACTUS && above != Material.CACTUS;
            case SUGAR_CANE:
                return below == Material.SUGAR_CANE && above != Material.SUGAR_CANE;
            case VINE:
                return above == Material.VINE && below != Material.VINE;
            case MELON:
            case PUMPKIN:
                return true;
            default:
                return Tag.LOGS.isTagged(block.getType());
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
            if(!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}