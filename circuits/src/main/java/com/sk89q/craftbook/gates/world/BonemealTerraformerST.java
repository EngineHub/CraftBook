package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class BonemealTerraformerST extends BonemealTerraformer implements SelfTriggeredIC {

    public BonemealTerraformerST(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        terraform();
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Bonemeal Cultivator";
    }

    @Override
    public String getSignTitle() {

        return "TERRAFORMER ST";
    }

    public static class Factory extends BonemealTerraformer.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new BonemealTerraformerST(getServer(), sign, this);
        }
    }
}