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

import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.PlayerType;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;

public class PlayerTrap extends AbstractSelfTriggeredIC {

    public PlayerTrap(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {
        return "Player Trap";
    }

    @Override
    public String getSignTitle() {
        return "PLAYER TRAP";
    }

    SearchArea area;

    int damage;
    PlayerType type;
    String nameLine;

    @Override
    public void load() {

        if (getSign().getLine(2).contains("&")) {
            getSign().setLine(2, StringUtils.replace(getSign().getLine(2), "&", "="));
            getSign().update(false);
        }

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[0]);

        try {
            damage = Integer.parseInt(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1]);
        } catch (Exception ignored) {
            damage = 2;
        }

        if (getLine(3).contains(":"))
            type = PlayerType.getFromChar(getLine(3).trim().toCharArray()[0]);
        else
            type = PlayerType.ALL;

        nameLine = getLine(3).replace("g:", "").replace("p:", "").replace("n:", "").replace("t:", "").replace("a:", "").trim();
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0))
            chip.setOutput(0, hurt());
    }

    public boolean hurt() {

        boolean hasHurt = false;

        for (Player p : area.getPlayersInArea()) {
            if (!type.doesPlayerPass(p, nameLine)) continue;
            p.damage(damage);
            hasHurt = true;
        }

        return hasHurt;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerTrap(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Damages nearby players that fit criteria.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "SearchArea=damage", "PlayerType" };
        }
    }
}