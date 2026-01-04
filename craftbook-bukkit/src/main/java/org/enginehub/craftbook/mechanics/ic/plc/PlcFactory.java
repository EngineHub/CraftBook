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

package org.enginehub.craftbook.mechanics.ic.plc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.RegexUtil;

import java.util.concurrent.ThreadLocalRandom;

public class PlcFactory<StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements ICFactory {

    private Lang lang;
    private boolean selfTriggered;
    private Server s;
    private String id;

    public PlcFactory(Server s, Lang lang, boolean selfTriggered, String id) {

        this.s = s;
        this.lang = lang;
        this.selfTriggered = selfTriggered;
        this.id = id;
    }

    @Override
    public IC create(BukkitChangedSign sign) {

        PlcIC<StateT, CodeT, Lang> i = new PlcIC<>(s, sign, lang);
        return selfTriggered ? i.selfTriggered() : i;
    }

    @Override
    public void verify(BukkitChangedSign sign) throws ICVerificationException {

        new PlcIC<>(sign, lang); // Huge ugly hack!!
        sign.setLine(2, Component.text("id:" + ThreadLocalRandom.current().nextInt()));
        String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
        if (!line3.isEmpty()) {
            if (!RegexUtil.PLC_NAME_PATTERN.matcher(line3).matches())
                throw new ICVerificationException("illegal storage name");
        }
        sign.update(false);
    }

    @Override
    public void checkPlayer(BukkitChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
        // Do nothing
    }

    public static <StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> PlcFactory<StateT, CodeT, Lang> fromLang(Server s, Lang lang, boolean selfTriggered, String id) {

        return new PlcFactory<>(s, lang, selfTriggered, id);
    }

    @Override
    public String getShortDescription() {

        return "Programmable Logic Chip";
    }

    @Override
    public String[] getLineHelp() {

        return new String[] { "PLC ID", "Shared Access ID" };
    }

    @Override
    public String[] getLongDescription() {

        //TODO MC5001
        if (id.equalsIgnoreCase("MC5000")) {
            return new String[] {
                "The '''MC5000''' is a [[../Perlstone/]]-powered programmable logic chip. Because it is of the VIVO family, it has a variable number of inputs and outputs, giving you the choice of either 3-1 or 1-3 for the number of inputs and outputs, respectively.",
                "",
                "== Construction ==",
                "",
                "=== Sign Method ===",
                "Code for the MC5000 is to be put in signs anywhere above or below the IC sign. The top most sign must contain '''[Code Block]''' on the second line of the sign and code then starts on signs below, and ends on the first non-sign block, or the IC sign. No code goes on the actual IC sign. If multiple '''[Code Block]''' signs exist, the topmost one will be used. Code is to be written in [[../Perlstone/]].",
                "",
                "[[File:MC5000.png|center]]",
                "",
                "=== Book Method ===",
                "Alternatively, as of version 3.3, code for the MC5000 can be placed in a chest directly above or below the IC sign. There must only be one book in the chest. If there is both a chest above the IC, and one below it, only the one above it will be searched.",
                "",
                "[[File:Altmc5000.png|center]]",
                "",
                "== Execution ==",
                "",
                "The first three functions of the PerlStone code is called, and their return values are put into the respective outputs, if they exist. Note that they will be called even if the outputs do not exist."
            };
        }
        return new String[] { "Missing Description" };
    }

    @Override
    public String[] getPinDescription(ChipState state) {

        String[] pins = new String[state.getInputCount() + state.getOutputCount()];

        for (int i = 0; i < pins.length; i++)
            pins[i] = "Programmable Pin";

        return pins;
    }

    @Override
    public void unload() {
    }

    @Override
    public void load() {

    }
}
