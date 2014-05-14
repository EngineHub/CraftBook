package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.ItemInfo;

public abstract class SetBlock extends AbstractSelfTriggeredIC {

    public SetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemInfo item;
    String force;

    @Override
    public void load() {

        force = getSign().getLine(3).toUpperCase(Locale.ENGLISH).trim();

        item = new ItemInfo(getLine(2));
    }

    public void onTrigger() {

        Block body = getBackBlock();

        doSet(body, item, force.equals("FORCE"));
    }

    @Override
    public void trigger(ChipState chip) {

        chip.setOutput(0, chip.getInput(0));

        onTrigger();
    }

    @Override
    public void think(ChipState chip) {

        onTrigger();
    }

    protected abstract void doSet(Block block, ItemInfo item, boolean force);

    public boolean takeFromChest(Block bl, ItemInfo item) {

        if (bl.getType() != Material.CHEST) {
            return false;
        }
        BlockState state = bl.getState();
        if (!(state instanceof Chest)) {
            return false;
        }
        Chest c = (Chest) state;
        ItemStack[] is = c.getInventory().getContents();
        for (int i = 0; i < is.length; i++) {
            final ItemStack stack = is[i];
            if (stack == null) {
                continue;
            }
            if (stack.getAmount() > 0 && stack.getType() == item.getType()) {
                if (item.getData() != -1 && stack.getData().getData() != item.getData()) {
                    continue;
                }
                if (stack.getAmount() == 1) {
                    is[i] = new ItemStack(Material.AIR, 0);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                c.getInventory().setContents(is);
                c.update();
                return true;
            }
        }
        return false;
    }
}
