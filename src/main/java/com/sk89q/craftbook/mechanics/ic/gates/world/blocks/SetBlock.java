package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public abstract class SetBlock extends AbstractSelfTriggeredIC {

    public SetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    BlockStateHolder item;
    String force;

    @Override
    public void load() {

        force = getSign().getLine(3).toUpperCase(Locale.ENGLISH).trim();

        item = BlockSyntax.getBlock(getLine(2));
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

    protected abstract void doSet(Block block, BlockStateHolder blockData, boolean force);

    public boolean takeFromChest(Block bl, ItemType item) {

        if (bl.getType() != Material.CHEST) {
            return false;
        }
        Material material = BukkitAdapter.adapt(item);
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
            if (stack.getAmount() > 0 && stack.getType() == material) {
                if (stack.getAmount() == 1) {
                    is[i] = new ItemStack(Material.AIR, 0);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                c.getInventory().setContents(is);
                //c.update();
                return true;
            }
        }
        return false;
    }
}
