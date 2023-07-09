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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;

/**
 * @author Me4502
 */
public class CombinationLock extends AbstractIC {

    public CombinationLock(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Combination Lock";
    }

    @Override
    public String getSignTitle() {

        return "COMBINATION LOCK";
    }

    @Override
    public void trigger(ChipState state) {

        try {
            char[] data = getLine(2).toCharArray();
            checkCombo:
            {
                if (state.getInput(0) != (data[1] == 'X'))
                    break checkCombo;
                if (state.getInput(1) != (data[2] == 'X'))
                    break checkCombo;
                if (state.getInput(2) != (data[0] == 'X'))
                    break checkCombo;

                state.setOutput(0, true);
                return;
            }
            state.setOutput(0, false);
        } catch (Exception e) {
            state.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new CombinationLock(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (sign.getLine(2) == null && PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).isEmpty())
                throw new ICVerificationException("Line three needs to be a combination");
        }

        @Override
        public String getShortDescription() {

            return "Checks combination on sign against inputs.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Combination Bit 1",//Inputs
                "Combination Bit 2",
                "Combination Bit 3",
                "High on Correct Combination"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Combination. X = On, O = Off (XOX)", null };
        }
    }
}
