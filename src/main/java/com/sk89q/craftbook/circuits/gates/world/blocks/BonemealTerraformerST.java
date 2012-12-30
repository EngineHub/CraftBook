package com.sk89q.craftbook.circuits.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class BonemealTerraformerST extends BonemealTerraformer implements SelfTriggeredIC {

    public BonemealTerraformerST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        terraform(false);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Bonemeal Terraformer";
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
        public IC create(ChangedSign sign) {

            return new BonemealTerraformerST(getServer(), sign, this);
        }
    }
}