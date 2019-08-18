/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.ics.plc;

import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlcFactory<Lang extends PlcLanguage> extends SerializedICFactory<PlcIC<Lang>, PlcFactory.PlcStateData> {
    private Lang lang;

    public PlcFactory(Lang lang) {
        super(PlcStateData.class, 1);
        this.lang = lang;
    }

    @Override
    public PlcIC<Lang> createInstance(Location<World> location) {
        return new PlcIC<>(this, location, lang);
    }

    @Override
    protected Optional<PlcStateData> buildContent(DataView container) throws InvalidDataException {
        PlcStateData state = new PlcStateData();

        state.state = container.getBooleanList(DataQuery.of("State")).orElse(new ArrayList<>());
        state.languageName = container.getString(DataQuery.of("LanguageName")).orElse("Perlstone");
        state.codeString = container.getString(DataQuery.of("Code")).orElse("");
        state.error = container.getBoolean(DataQuery.of("Error")).orElse(false);
        state.errorCode = container.getString(DataQuery.of("ErrorCode")).orElse("");

        return Optional.of(state);
    }

    @Override
    public PlcStateData getData(PlcIC<Lang> ic) {
        return ic.state;
    }

    @Override
    public void setData(PlcIC<Lang> ic, PlcStateData data) {
        ic.state = data;
    }

    @Override
    public String[] getLineHelp() {
        return new String[] {"", ""};
    }

    @Override
    public String[][] getPinHelp() {
        return new String[][] {
                new String[] {
                        "Programmable Input",
                        "Programmable Input",
                        "Programmable Input"
                },
                new String[] {
                        "Program Output"
                }
        };
    }

    public static class PlcStateData extends SerializedICData {
        public List<Boolean> state;
        public String languageName;
        public String codeString = "";
        public boolean error;
        public String errorCode = "";

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer()
                    .set(DataQuery.of("State"), this.state)
                    .set(DataQuery.of("LanguageName"), this.languageName)
                    .set(DataQuery.of("Code"), this.codeString)
                    .set(DataQuery.of("Error"), this.error)
                    .set(DataQuery.of("ErrorCode"), this.errorCode);
        }
    }
}