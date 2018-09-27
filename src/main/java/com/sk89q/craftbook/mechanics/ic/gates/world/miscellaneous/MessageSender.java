// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICMechanic;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.PlayerType;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

public class MessageSender extends AbstractIC {

    public MessageSender(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Message Sender";
    }

    @Override
    public String getSignTitle() {

        return "MESSAGE SENDER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, sendMessage());
        }
    }

    @Override
    public void load() {

        String[] bits = RegexUtil.AMPERSAND_PATTERN.split(getLine(2));

        for(String bit : bits) {
            if (bit.contains(":"))
                type = PlayerType.getFromChar(bit.trim().toCharArray()[0]);
            else if (type == null)
                type = PlayerType.ALL;

            bit = bit.replace("g:", "").replace("p:", "").replace("n:", "").replace("t:", "").replace("a:", "").trim();

            if(SearchArea.isValidArea(getLocation().getBlock(), bit))
                area = SearchArea.createArea(getLocation().getBlock(), bit);
            else
                name = bit;
        }
        message = getLine(3);
    }

    PlayerType type;
    String name;
    String message;
    SearchArea area;

    /**
     * Returns true if a message was sent.
     *
     * @return
     */
    private boolean sendMessage() {

        boolean sent = false;

        if(area != null) {
            for(Player p : area.getPlayersInArea()) {
                if(!type.doesPlayerPass(p, name)) continue;
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                sent = true;
            }
        } else {

            if(type == PlayerType.NAME) {
                Player player = Bukkit.getPlayer(name);
                if(player != null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    sent = true;
                }
            }

            for(Player player : Bukkit.getOnlinePlayers()) {
                if (type.doesPlayerPass(player, name)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    sent = true;
                } else if (name.equalsIgnoreCase("BROADCAST") || name.isEmpty()) {
                    getServer().broadcastMessage(message);
                    sent = true;
                }
            }
        }
        return sent;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MessageSender(getServer(), sign, this);
        }

        @Override
        public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {

            if (!sign.getLine(2).equalsIgnoreCase(player.getName()))
                if (!ICMechanic.hasRestrictedPermissions(player, this, "mc1510"))
                    throw new ICVerificationException("You don't have permission to use other players!");
        }

        @Override
        public String getShortDescription() {

            return "Sends a pre-written message on high.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"PlayerType, SearchArea, or BROADCAST for whole server", "Message to send."};
        }
    }
}