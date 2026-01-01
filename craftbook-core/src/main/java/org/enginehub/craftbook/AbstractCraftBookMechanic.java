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

package org.enginehub.craftbook;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractCraftBookMechanic implements CraftBookMechanic {

    private final MechanicType<? extends CraftBookMechanic> mechanicType;

    public AbstractCraftBookMechanic(MechanicType<? extends CraftBookMechanic> mechanicType) {
        this.mechanicType = mechanicType;
    }

    public MechanicType<? extends CraftBookMechanic> getMechanicType() {
        return this.mechanicType;
    }

    @Override
    public void loadConfiguration(Path configFile) {
        YAMLProcessor mechanicConfig = new YAMLProcessor(configFile, true, YAMLFormat.EXTENDED);

        try {
            mechanicConfig.load();
        } catch (FileNotFoundException e) {
            // Ignore this one.
        } catch (IOException e) {
            CraftBook.LOGGER.error("Failed to load configuration for " + getMechanicType().getName(), e);
        }

        mechanicConfig.setWriteDefaults(true);

        MechanicType<? extends CraftBookMechanic> mechType = getMechanicType();

        mechanicConfig.setHeader(
            "# CraftBook " + mechType.getName() + " Configuration",
            "# -- Generated for version: " + CraftBook.getInstance().getPlatform().getPlatformVersion(),
            "# ",
            "# More information about this mechanic is available at...",
            "# " + getDocsUrl(mechType),
            "#",
            "# NOTE! Make sure to enable this in the config.yml file if you wish to use it.",
            "");

        loadFromConfiguration(mechanicConfig);

        mechanicConfig.save();
    }
}
