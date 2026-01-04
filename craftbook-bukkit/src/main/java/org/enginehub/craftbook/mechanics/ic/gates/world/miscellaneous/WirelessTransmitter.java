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

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.util.yaml.YAMLProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.CommandIC;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICMechanic;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.PersistentDataIC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class WirelessTransmitter extends AbstractIC {

    protected static final Set<String> memory = new LinkedHashSet<>();

    protected String band;

    public WirelessTransmitter(Server server, BukkitChangedSign sign, ICFactory factory) {

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

        if (!val) //List preening
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
        public IC create(BukkitChangedSign sign) {

            return new WirelessTransmitter(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1110''' transmits the input value to a particular named ''band'' or ''network''.",
                "To receive the value of the transmitter, use [[../MC1111/]].",
                "",
                "If there are multiple transmitters for the same band, the last one to transmit to a particular band will have its state apply until the next transmission."
            };
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

            return new String[] { "Wireless Band", "Player's CBID (Automatic)" };
        }

        @Override
        public void checkPlayer(BukkitChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
            String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
            if (requirename && (line3.isEmpty() || !ICMechanic.hasRestrictedPermissions(player, this, "MC1110")))
                sign.setLine(3, Component.text(player.getCraftBookId()));
            else if (!line3.isEmpty() && !ICMechanic.hasRestrictedPermissions(player, this, "MC1110"))
                sign.setLine(3, Component.text(player.getCraftBookId()));
            sign.update(false);
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment(path + "per-player", "Require a name to be entered on the sign. This allows for 'per-player' wireless bands. This is done automatically.");
            requirename = config.getBoolean(path + "per-player", false);
        }

        @Override
        public void loadPersistentData(DataInputStream stream) throws IOException {

            int length = stream.readInt();
            for (int i = 0; i < length; i++)
                memory.add(stream.readUTF());
            stream.close();
            getStorageFile().delete();
        }

        @Override
        public void savePersistentData(DataOutputStream stream) throws IOException {
        }

        @Override
        public File getStorageFile() {
            return new File(CraftBookPlugin.inst().getDataFolder(), "wireless-bands.dat");
        }

        @SuppressWarnings("unchecked")
        @Override
        public void load() {
            super.load();

            /* FIXME if (ICMechanic.instance.savePersistentData && CraftBookPlugin.inst().hasPersistentStorage() && CraftBookPlugin.inst().getPersistentStorage().has("wireless-ic-states"))
                WirelessTransmitter.memory.addAll((Set<String>) CraftBookPlugin.inst().getPersistentStorage().get("wireless-ic-states"));*/
        }

        @Override
        public void unload() {
            /* FIXME if (ICMechanic.instance.savePersistentData && CraftBookPlugin.inst().hasPersistentStorage())
                CraftBookPlugin.inst().getPersistentStorage().set("wireless-ic-states", memory);*/
        }

        @Override
        public void onICCommand(CommandContext args, CommandSender sender) {

            if (args.getString(1).equalsIgnoreCase("get")) {

                if (memory.contains(args.getString(2)))
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

                if (memory.contains(args.getString(2)))
                    memory.remove(args.getString(2));
                else
                    memory.add(args.getString(2));
            } else
                sender.sendMessage(ChatColor.RED + "Usage: /ic ic mc1110 <get/set/toggle> <band> <state>");
        }

        @Override
        public int getMinCommandArgs() {
            return 2;
        }

        @Override
        public String[][] getCommandInformation() {
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