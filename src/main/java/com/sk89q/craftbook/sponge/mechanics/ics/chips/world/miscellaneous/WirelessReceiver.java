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

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SelfTriggeringIC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
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

public class WirelessReceiver extends IC implements SelfTriggeringIC {

    private WirelessTransmitter.Factory.WirelessData wirelessData;

    private transient Tuple<String, String> cachedTuple;

    public WirelessReceiver(ICFactory<WirelessReceiver> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        if (SignUtil.getTextRaw(lines.get(2)).length() == 0) {
            throw new InvalidICException("A band must be supplied on the 3rd line!");
        }

        wirelessData = new WirelessTransmitter.Factory.WirelessData();

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
    public void think() {
        trigger();
    }

    @Override
    public void trigger() {
        getPinSet().setOutput(0, WirelessTransmitter.wirelessStates.contains(cachedTuple), this);
    }

    public static class Factory extends SerializedICFactory<WirelessReceiver, WirelessTransmitter.Factory.WirelessData> {

        public Factory() {
            super(WirelessTransmitter.Factory.WirelessData.class, 1);
        }

        @Override
        public WirelessReceiver createInstance(Location<World> location) {
            return new WirelessReceiver(this, location);
        }

        @Override
        protected Optional<WirelessTransmitter.Factory.WirelessData> buildContent(DataView container) throws InvalidDataException {
            WirelessTransmitter.Factory.WirelessData wirelessData = new WirelessTransmitter.Factory.WirelessData();

            wirelessData.shortband = container.getString(DataQuery.of("ShortBand")).orElse(null);
            wirelessData.wideband = container.getString(DataQuery.of("WideBand")).orElse(null);

            if (wirelessData.shortband == null || wirelessData.wideband == null) {
                return Optional.empty();
            }

            return Optional.of(wirelessData);
        }

        @Override
        public void setData(WirelessReceiver ic, WirelessTransmitter.Factory.WirelessData data) {
            ic.wirelessData = data;
        }

        @Override
        public WirelessTransmitter.Factory.WirelessData getData(WirelessReceiver ic) {
            return ic.wirelessData;
        }
    }
}
