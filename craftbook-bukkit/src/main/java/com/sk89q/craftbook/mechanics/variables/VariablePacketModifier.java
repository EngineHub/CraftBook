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

package com.sk89q.craftbook.mechanics.variables;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ParsingUtil;

class VariablePacketModifier {

    VariablePacketModifier() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new VariablePacketAdapter());
    }

    private static class VariablePacketAdapter extends PacketAdapter {
        VariablePacketAdapter() {
            super(CraftBookPlugin.inst(), PacketType.Play.Server.CHAT);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            WrappedChatComponent chat = event.getPacket().getChatComponents().read(0);
            chat.setJson(ParsingUtil.parseVariables(chat.getJson(), event.getPlayer()));

            event.getPacket().getChatComponents().write(0, chat);
        }
    }
}