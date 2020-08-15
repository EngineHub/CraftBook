/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.entity;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.Tuple2;

public class TeleportReciever extends AbstractSelfTriggeredIC {

    public TeleportReciever(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Teleport Reciever";
    }

    @Override
    public String getSignTitle() {

        return "TELEPORT IN";
    }

    String band;
    String welcome;

    @Override
    public void load() {

        band = getLine(2);
        welcome = getLine(3);
        if (welcome == null || welcome.isEmpty()) welcome = "The Teleporter moves you here...";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, check());
        }
    }

    @Override
    public void think(ChipState chip) {

        if (!chip.getInput(0))
            chip.setOutput(0, check());
    }

    public boolean check() {

        Tuple2<Long, String> val = TeleportTransmitter.getValue(band);
        if (val == null) return false;

        Player p = Bukkit.getServer().getPlayerExact(val.b);

        if (p == null || !p.isOnline()) {
            return false;
        }

        Block block = getBackBlock();
        while (block.getType().isSolid())
            block = block.getRelative(0, 1, 0);

        p.teleport(block.getLocation().add(0.5, 0.5, 0.5));
        CraftBookPlugin.inst().wrapPlayer(p).print(welcome);
        TeleportTransmitter.lastKnownLocations.put(band, block.getLocation());
        return true;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TeleportReciever(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1113''' will teleport a player from a corresponding [[../MC1112/]] IC on a redstone signal."
            };
        }

        @Override
        public String getShortDescription() {

            return "Reciever for the teleportation network.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC (When ST, disables IC when high)",//Inputs
                "High on successful teleport",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Frequency", "+oWelcome Text" };
        }
    }
}