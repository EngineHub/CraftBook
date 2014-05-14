package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import java.util.Collections;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Silthus
 */
public class ItemSensor extends AbstractSelfTriggeredIC {

    private ItemStack item;

    private SearchArea area;

    public ItemSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        item = ItemSyntax.getItem(getLine(3));
        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Item Detection";
    }

    @Override
    public String getSignTitle() {

        return "ITEM DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, isDetected());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    protected boolean isDetected() {

        for (Entity entity : area.getEntitiesInArea(Collections.singleton(EntityType.ITEM))) {
            ItemStack itemStack = ((Item) entity).getItemStack();
            if (ItemUtil.areItemsIdentical(itemStack, item))
                return true;
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getShortDescription() {

            return "Detects items within a given radius";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"SearchArea", "id:data"};
        }
    }
}
