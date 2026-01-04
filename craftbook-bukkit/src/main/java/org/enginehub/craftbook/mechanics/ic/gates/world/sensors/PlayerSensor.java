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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.PlayerType;
import org.enginehub.craftbook.util.SearchArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Me4502
 */
public class PlayerSensor extends AbstractSelfTriggeredIC {

    public PlayerSensor(Server server, BukkitChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Player Detection";
    }

    @Override
    public String getSignTitle() {

        return "P-DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, invertOutput != isDetected());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, invertOutput != isDetected());
    }

    private static final Pattern NAME_STRIPPER = Pattern.compile("[gpnta!^]*:");

    private SearchArea area;

    private PlayerType type;
    private String nameLine;
    private boolean invertOutput = false;
    private boolean invertDetection = false;

    @Override
    public void load() {
        if (getLine(3).contains(":")) {
            type = PlayerType.getFromChar(getLine(3).replace("!", "").replace("^", "").trim().toCharArray()[0]);
        } else {
            type = PlayerType.NAME;
        }

        invertOutput = getLine(3).contains("!");
        invertDetection = getLine(3).contains("^");

        nameLine = NAME_STRIPPER.matcher(getLine(3)).replaceAll(Matcher.quoteReplacement("")).trim();

        area = SearchArea.createArea(getSign().getBlock(), getLine(2));
    }

    private boolean isDetected() {
        if (!nameLine.isEmpty() && type == PlayerType.NAME && !invertDetection) {
            Player p = Bukkit.getPlayerExact(nameLine);
            return p != null && area.isWithinArea(p.getLocation());
        }

        for (Player p : area.getPlayersInArea()) {
            if (p == null || !p.isValid()) {
                continue;
            }

            if (nameLine.isEmpty()) {
                return true;
            } else if (invertDetection != type.doesPlayerPass(p, nameLine)) {
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
        public IC create(BukkitChangedSign sign) {

            return new PlayerSensor(getServer(), sign, this);
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {

            if (!SearchArea.createArea(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(2))).isValid())
                throw new ICVerificationException("Invalid SearchArea on line 3!");
        }

        @Override
        public String getShortDescription() {

            return "Detects players within a radius.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {
                "SearchArea",
                "PlayerType"
            };
        }
    }
}