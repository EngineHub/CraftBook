/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous;

import com.google.common.collect.Sets;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WirelessTransmitter extends IC {

    public static final Set<Tuple<String, String>> wirelessStates = Sets.newHashSet();

    private Factory.WirelessData wirelessData;

    private Tuple<String, String> cachedTuple;

    public WirelessTransmitter(ICFactory<WirelessTransmitter> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        if (SignUtil.getTextRaw(lines.get(2)).length() == 0) {
            throw new InvalidICException("A band must be supplied on the 3rd line!");
        }

        wirelessData = new Factory.WirelessData();

        wirelessData.shortband = SignUtil.getTextRaw(lines.get(2));
        if (SignUtil.getTextRaw(lines.get(3)).length() == 0) {
            wirelessData.wideband = player.getUniqueId().toString();
        } else {
            wirelessData.wideband = SignUtil.getTextRaw(lines.get(3));
        }
    }

    public void load() {
        cachedTuple = new Tuple<>(wirelessData.shortband, wirelessData.wideband);
    }

    @Override
    public void trigger() {
        boolean enable = getPinSet().getInput(0, this);
        if (enable) {
            wirelessStates.add(cachedTuple);
        } else {
            wirelessStates.remove(cachedTuple);
        }
    }

    public static class Factory extends SerializedICFactory<WirelessTransmitter, Factory.WirelessData> {

        public Factory() {
            super(WirelessData.class, 1);
        }

        @Override
        public WirelessTransmitter createInstance(Location<World> location) {
            return new WirelessTransmitter(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "The channel name (narrowband)",
                    "Optional wideband. Defaults to UUID if blank"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Wireless Input"
                    },
                    new String[] {
                            "None"
                    }
            };
        }

        @Override
        protected Optional<WirelessData> buildContent(DataView container) throws InvalidDataException {
            WirelessData wirelessData = new WirelessData();

            wirelessData.shortband = container.getString(DataQuery.of("ShortBand")).orElse(null);
            wirelessData.wideband = container.getString(DataQuery.of("WideBand")).orElse(null);

            if (wirelessData.shortband == null || wirelessData.wideband == null) {
                return Optional.empty();
            }

            return Optional.of(wirelessData);
        }

        @Override
        public void setData(WirelessTransmitter transmitter, WirelessData data) {
            transmitter.wirelessData = data;
        }

        @Override
        public WirelessData getData(WirelessTransmitter ic) {
            return ic.wirelessData;
        }

        public static class WirelessData extends SerializedICData {

            public String wideband;
            public String shortband;

            @Override
            public int getContentVersion() {
                return 1;
            }

            @Override
            public DataContainer toContainer() {
                return super.toContainer()
                        .set(DataQuery.of("ShortBand"), this.shortband)
                        .set(DataQuery.of("WideBand"), this.wideband);
            }
        }
    }
}
