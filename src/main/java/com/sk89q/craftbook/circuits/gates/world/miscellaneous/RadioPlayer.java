package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.SearchArea;

public class RadioPlayer extends AbstractSelfTriggeredIC {

    String band;
    SearchArea area;

    Map<Player, SearchArea> listening;

    public RadioPlayer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getLine(2);
        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), getLine(3));

        listening = new WeakHashMap<Player, SearchArea>();
    }

    @Override
    public String getTitle () {
        return "Radio Player";
    }

    @Override
    public String getSignTitle () {
        return "RADIO PLAYER";
    }

    @Override
    public void trigger (ChipState chip) {

        Playlist playlist = RadioStation.getPlaylist(band);

        if(playlist == null)
            return;

        if(chip.getInput(0)) {
            for(Player player : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {

                if(area != null && !area.isWithinArea(player.getLocation())) continue;
                listening.put(player, area);
            }

            playlist.addPlayers(listening);
        } else {

            playlist.removePlayers(listening);
            listening.clear();
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RadioPlayer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Plays a radio station.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Radio Band", "Radius"};
        }
    }
}