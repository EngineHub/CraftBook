package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * @author Silthus
 */
public class ItemNotSensor extends ItemSensor {

    public ItemNotSensor(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Item Not Sensor";
    }

    @Override
    public String getSignTitle() {
        return "ITEM NOT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            chip.setOutput(0, !isDetected());
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
	        return new ItemNotSensor(getServer(), sign);
        }
    }
}
