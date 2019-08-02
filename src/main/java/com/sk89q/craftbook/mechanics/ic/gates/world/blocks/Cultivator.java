package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SearchArea;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

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

    private SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, cultivate());
    }

    @Override
    public void think(ChipState state) {

        if(state.getInput(0)) return;

        for(int i = 0; i < 10; i++)
            state.setOutput(0, cultivate());
    }

    public boolean cultivate() {

        Block b = area.getRandomBlockInArea();

        if(b == null) return false;

        if (b.getType() == Material.DIRT || b.getType() == Material.GRASS_BLOCK) {
            if (b.getRelative(BlockFace.UP).getType() == Material.AIR && damageHoe()) {
                b.setType(Material.FARMLAND);
                return true;
            }
        }

        return false;
    }

    private static final Set<Material> hoes = EnumSet.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
            Material.DIAMOND_HOE);

    public boolean damageHoe() {

        if (getBackBlock().getRelative(0, 1, 0).getType() == Material.CHEST) {
            Chest c = (Chest) getBackBlock().getRelative(0, 1, 0).getState();
            for (int slot = 0; slot < c.getInventory().getSize(); slot++) {
                if (c.getInventory().getItem(slot) == null || !hoes.contains(c.getInventory().getItem(slot).getType()))
                    continue;
                if (ItemUtil.isStackValid(c.getInventory().getItem(slot))) {
                    ItemStack item = c.getInventory().getItem(slot);
                    item.setDurability((short) (item.getDurability() + 1));
                    if(item.getDurability() > ItemUtil.getMaxDurability(item.getType()))
                        item = null;
                    c.getInventory().setItem(slot, item);
                    return true;
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
        public String[] getLongDescription() {

            return new String[]{
                    "The '''MC1235''' tills farmland in the alloted radius using a hoe placed inside the above chest.",
                    "This IC is part of the Farming IC family, and can be used to make a fully automated farm.",
                    "",
                    "== Video example ==",
                    "",
                    "<div style=\"text-align: center\">{{#ev:youtube|GnMfQtTAZZc|480}}</div>"
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oSearchArea", null};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}