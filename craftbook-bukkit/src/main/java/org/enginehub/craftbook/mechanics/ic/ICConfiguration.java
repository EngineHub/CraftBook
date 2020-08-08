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

package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.CraftBook;
import com.sk89q.util.yaml.YAMLProcessor;

import java.io.IOException;

public class ICConfiguration {

    public final YAMLProcessor config;

    public ICConfiguration(YAMLProcessor config) {

        this.config = config;
    }

    public void load () {

        try {
            config.load();
        } catch (IOException e) {
            CraftBook.logger.error("Error loading CraftBook IC configuration", e);
        }

        for (RegisteredICFactory factory : ICManager.inst().getICList())
            if (factory.getFactory() instanceof ConfigurableIC)
                ((ConfigurableIC) factory.getFactory()).addConfiguration(config, "ics." + factory.getId() + ".");

        config.save(); //Save all the added values.
    }
}