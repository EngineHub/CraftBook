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

package org.enginehub.craftbook.mechanics.ic.gates.world.entity;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.HistoryHashMap;
import org.enginehub.craftbook.util.PlayerType;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;
import org.enginehub.craftbook.util.Tuple2;

public class TeleportTransmitter extends AbstractSelfTriggeredIC {

    public TeleportTransmitter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    protected static final HistoryHashMap<String, Tuple2<Long, String>> memory = new HistoryHashMap<>(50);
    protected static HistoryHashMap<String, Location> lastKnownLocations = new HistoryHashMap<>(50);

    protected String band;

    @Override
    public String getTitle() {

        return "Teleport Transmitter";
    }

    @Override
    public String getSignTitle() {

        return "TELEPORT OUT";
    }

    SearchArea area;
    PlayerType type;
    String typeData;

    @Override
    public void load() {

        band = RegexUtil.PIPE_PATTERN.split(getLine(2))[0];
        if (getLine(2).contains("|")) {
            type = PlayerType.getFromChar(RegexUtil.PIPE_PATTERN.split(getLine(2))[1].charAt(0));
            typeData = RegexUtil.COLON_PATTERN.split(RegexUtil.PIPE_PATTERN.split(getLine(2))[1])[1];
        }
        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(3));
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, sendPlayer());
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, sendPlayer());
    }

    public boolean sendPlayer() {

        Player closest = null;

        for (Player e : area.getPlayersInArea()) {
            if (e == null || !e.isValid() || e.isDead())
                continue;

            if (type != null && !type.doesPlayerPass(e, typeData))
                continue;

            if (closest == null) closest = e;
            if (area.getCenter() == null) break;
            else if (closest.getWorld() == area.getWorld() && closest.getLocation().distanceSquared(area.getCenter()) >= e.getLocation().distanceSquared(area.getCenter()))
                closest = e;
        }
        if (closest != null && lastKnownLocations.containsKey(band))
            lastKnownLocations.get(band).getChunk().load();
        if (closest != null && !setValue(band, new Tuple2<>(System.currentTimeMillis(), closest.getName())))
            closest.sendMessage(ChatColor.RED + "This Teleporter Frequency is currently busy! Try again soon (3s)!");
        else
            return true;
        return false;
    }

    public static Tuple2<Long, String> getValue(String band) {

        if (memory.containsKey(band)) {
            long time = System.currentTimeMillis() - memory.get(band).a;
            int seconds = (int) (time / 1000) % 60;
            if (seconds > 5) { // Expired.
                memory.remove(band);
                return null;
            }
        }
        Tuple2<Long, String> val = memory.get(band);
        memory.remove(band); // Remove on teleport.
        return val;
    }

    public static boolean setValue(String band, Tuple2<Long, String> val) {

        if (memory.containsKey(band)) {
            long time = System.currentTimeMillis() - memory.get(band).a;
            int seconds = (int) (time / 1000) % 60;
            if (seconds > 3) { // Expired.
                memory.remove(band);
            } else return false;
        }
        memory.put(band, val);
        return true;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TeleportTransmitter(getServer(), sign, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1112''' teleports a player located within IC's radius to a receiver ([[../MC1113/]]) tuned to the same ''frequency''.",
                "This IC requires the recieving chunk to be loaded for the initial teleport, future teleports should not require the chunk to be loaded."
            };
        }

        @Override
        public String getShortDescription() {

            return "Transmitter for the teleportation network.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "High on successful teleport queue",//Outputs
            };
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (!SearchArea.isValidArea(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(3))))
                throw new ICVerificationException("Invalid SearchArea on 4th line!");
        }

        @SuppressWarnings("unchecked")
        @Override
        public void load() {

            /* FIXME if (!(ICMechanic.instance.savePersistentData && CraftBookPlugin.inst().hasPersistentStorage()))
                return;

            if (CraftBookPlugin.inst().getPersistentStorage().has("teleport-ic-locations.list")) {

                Set<String> list = new HashSet<>((Set<String>) CraftBookPlugin.inst().getPersistentStorage().get("teleport-ic-locations.list"));

                for (String ent : list) {
                    String locString = (String) CraftBookPlugin.inst().getPersistentStorage().get("teleport-ic-locations." + ent);
                    String[] bits = RegexUtil.COLON_PATTERN.split(locString);
                    Location loc = new Location(Bukkit.getWorld(bits[0]), Double.parseDouble(bits[1]), Double.parseDouble(bits[2]), Double.parseDouble(bits[3]));
                    TeleportTransmitter.lastKnownLocations.put(ent, loc);
                }
            }*/
        }

        @Override
        public void unload() {

            /* FIXME if (!(ICMechanic.instance.savePersistentData && CraftBookPlugin.inst().hasPersistentStorage()))
                return;

            CraftBookPlugin.inst().getPersistentStorage().set("teleport-ic-locations.list",
                new HashSet<>(TeleportTransmitter.lastKnownLocations.keySet()));

            for (Entry<String, Location> locations : TeleportTransmitter.lastKnownLocations.entrySet()) {
                if (locations == null || locations.getValue() == null)
                    continue;

                String loc = locations.getValue().getWorld().getName() + ":" + locations.getValue().getBlockX() + ":" + locations.getValue().getBlockY() + ":" + locations.getValue().getBlockZ();

                CraftBookPlugin.inst().getPersistentStorage().set("teleport-ic-locations." + locations.getKey(), loc);
            }*/
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Frequency|PlayerType", "SearchArea" };
        }
    }
}