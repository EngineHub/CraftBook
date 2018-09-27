package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class ContainerStocker extends AbstractSelfTriggeredIC {

    public ContainerStocker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;
    Location offset;

    @Override
    public void load() {

        if(getLine(3).isEmpty())
            offset = getBackBlock().getRelative(0, 1, 0).getLocation();
        else
            offset = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        item = ItemSyntax.getItem(getLine(2));
    }

    @Override
    public String getTitle() {

        return "Container Stocker";
    }

    @Override
    public String getSignTitle() {

        return "STOCKER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, stock());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, stock());
    }

    public boolean stock() {

        if (InventoryUtil.doesBlockHaveInventory(offset.getBlock())) {

            BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Collections.singletonList(item.clone())), getBackBlock());
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isValid())
                return false;

            InventoryHolder c = (InventoryHolder) offset.getBlock().getState();
            for(ItemStack stack : event.getItems())
                if (c.getInventory().addItem(stack).isEmpty()) {
                    //((BlockState) c).update();
                    return true;
                }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerStocker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Adds item into container at specified offset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"item id:data", "x:y:z offset"};
        }
    }
}