/*
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

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.util.yaml.YAMLProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICMechanic;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;

public class WirelessReceiver extends AbstractSelfTriggeredIC {

    private String band;

    public WirelessReceiver(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getLine(2);
        if (!getLine(3).trim().isEmpty()) {
            band = band + getLine(3);
        }
    }

    @Override
    public String getTitle() {

        return "Wireless Receiver";
    }

    @Override
    public String getSignTitle() {

        return "RECEIVER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {

            chip.setOutput(0, getOutput());
        }
    }

    public boolean getOutput() {

        Boolean val = WirelessTransmitter.getValue(band);

        if (val == null) {
            return false;
        }

        return val;
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, getOutput());
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        public boolean requirename;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WirelessReceiver(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1111''' receives the state in a particular ''band'' or ''network'' when the clock input goes from low to high.",
                "The corresponding transmitter is the [[../MC1110/]] IC.",
                "",
                "If there are multiple transmitters for the same band, the last one to transmit to a particular band will have its state apply until the next transmission."
            };
        }

        @Override
        public String getShortDescription() {

            return "Recieves signal from wireless transmitter.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "State of Wireless Band",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Wireless Band", "Player's CBID (Automatic)" };
        }

        @Override
        public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
            String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
            if (requirename && (line3.isEmpty() || !ICMechanic.hasRestrictedPermissions(player, this, "MC1111")))
                sign.setLine(3, Component.text(player.getCraftBookId()));
            else if (!line3.isEmpty() && !ICMechanic.hasRestrictedPermissions(player, this, "MC1111"))
                sign.setLine(3, Component.text(player.getCraftBookId()));
            sign.update(false);
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment("per-player", "Require a name to be entered on the sign. This allows for 'per-player' wireless bands. This is done automatically.");
            requirename = config.getBoolean("per-player", false);
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }
}