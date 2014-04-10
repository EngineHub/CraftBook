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

package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.CommandIC;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.PersistentDataIC;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.util.yaml.YAMLProcessor;

public class WirelessTransmitter extends AbstractIC {

    protected static final Set<String> memory = new LinkedHashSet<String>();

    protected String band;

    public WirelessTransmitter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getSign().getLine(2);
        if (!getLine(3).trim().isEmpty()) {
            if(CraftBookPlugin.inst().getConfiguration().convertNamesToCBID) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(band);
                if(player.hasPlayedBefore()) {
                    band = CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId());
                    getSign().setLine(3, band);
                    getSign().update(false);
                }
            }
            band = band + getSign().getLine(3);
        }
    }

    @Override
    public String getTitle() {

        return "Wireless Transmitter";
    }

    @Override
    public String getSignTitle() {

        return "TRANSMITTER";
    }

    @Override
    public void trigger(ChipState chip) {

        setValue(band, chip.getInput(0));
        chip.setOutput(0, chip.getInput(0));
    }

    public static Boolean getValue(String band) {

        return memory.contains(band);
    }

    public static void setValue(String band, boolean val) {

        if(!val) //List preening
            memory.remove(band);
        else
            memory.add(band);
    }

    public static class Factory extends AbstractICFactory implements PersistentDataIC, ConfigurableIC, CommandIC {

        public boolean requirename;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WirelessTransmitter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Transmits wireless signal to wireless recievers.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC",//Inputs
                    "Same as Input",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"wireless band", "user"};
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            if (requirename && (sign.getLine(3).isEmpty() || !ICMechanic.hasRestrictedPermissions(player, this, "MC1110"))) sign.setLine(3, player.getCraftBookId());
            else if (!sign.getLine(3).isEmpty() && !ICMechanic.hasRestrictedPermissions(player, this, "MC1110")) sign.setLine(3, player.getCraftBookId());
            sign.update(false);
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            requirename = config.getBoolean(path + "per-player", false);
        }

        @Override
        public void loadPersistentData (DataInputStream stream) throws IOException {

            memory.clear();
            int length = stream.readInt();
            for(int i = 0; i < length; i++)
                memory.add(stream.readUTF());
            stream.close();
        }

        @Override
        public void savePersistentData (DataOutputStream stream) throws IOException {
            stream.writeInt(memory.size());
            for(String band : memory) {
                stream.writeUTF(band);
            }
            stream.close();
        }

        @Override
        public File getStorageFile () {
            return new File(CraftBookPlugin.inst().getDataFolder(), "wireless-bands.dat");
        }

        @Override
        public void onICCommand (CommandContext args, CommandSender sender) {

            if (args.getString(1).equalsIgnoreCase("get")) {

                if(memory.contains(args.getString(2)))
                    sender.sendMessage("Wireless-Band-State: TRUE");
                else
                    sender.sendMessage("Wireless-Band-State: FALSE");
            } else if (args.getString(1).equalsIgnoreCase("set") && args.argsLength() > 3) {

                if (args.getString(3).equalsIgnoreCase("true"))
                    memory.add(args.getString(2));
                else if (args.getString(3).equalsIgnoreCase("false"))
                    memory.remove(args.getString(2));
                else
                    sender.sendMessage(ChatColor.RED + "Invalid Boolean Argument!");
            } else if (args.getString(1).equalsIgnoreCase("toggle") && args.argsLength() > 2) {

                if(memory.contains(args.getString(2)))
                    memory.remove(args.getString(2));
                else
                    memory.add(args.getString(2));
            } else
                sender.sendMessage(ChatColor.RED + "Usage: /ic ic mc1110 <get/set/toggle> <band> <state>");
        }

        @Override
        public int getMinCommandArgs () {
            return 2;
        }

        @Override
        public String[][] getCommandInformation () {
            return new String[][] {
                    new String[] {
                            "get <band>",
                            "none",
                            "Gets the value of the wireless band."
                    },
                    new String[] {
                            "set <band> <value>",
                            "none",
                            "Sets the value of the wireless band."
                    },
                    new String[] {
                            "toggle <band>",
                            "none",
                            "Toggles the value of the wireless band."
                    }
            };
        }
    }
}