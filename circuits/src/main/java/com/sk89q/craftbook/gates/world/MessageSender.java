// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

public class MessageSender extends AbstractIC {

    public MessageSender(Server server, Sign sign) {
        super(server, sign);
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

    /**
     * Returns true if a message was sent.
     * 
     * @return
     */
    private boolean sendMessage() {
        boolean sent = false;
        String name = getSign().getLine(2);
        String message = getSign().getLine(3);
        Player player = getServer().getPlayer(name);
        // FIXME: There is a cached block problem somewhere if the sign is
        // destroyed and replaced.
        // Until the player relogs, the sign will continue sending to the name
        // on the first sign.
        if (player != null) {
            player.sendMessage(message.replace("&", "\u00A7"));
            sent = true;
        }
        else if(name.equalsIgnoreCase("BROADCAST"))
            getServer().broadcastMessage(message);
        return sent;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new MessageSender(getServer(), sign);
        }
    }

}
