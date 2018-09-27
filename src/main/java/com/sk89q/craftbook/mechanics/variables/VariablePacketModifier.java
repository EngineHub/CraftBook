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