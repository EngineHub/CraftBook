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

package org.enginehub.craftbook;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.event.Listener;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractCraftBookMechanic implements CraftBookMechanic, Listener {

    private MechanicType<? extends CraftBookMechanic> mechanicType;

    public MechanicType<? extends CraftBookMechanic> getMechanicType() {
        if (this.mechanicType == null) {
            this.mechanicType = CraftBook.getInstance().getPlatform().getMechanicManager().getMechanicType(this);
            if (this.mechanicType == null) {
                throw new RuntimeException("Invalid mechanic found");
            }
        }
        return this.mechanicType;
    }

    @Override
    public void loadConfiguration(File configFile) {
        YAMLProcessor mechanicConfig = new YAMLProcessor(configFile, true, YAMLFormat.EXTENDED);

        try {
            mechanicConfig.load();
        } catch (FileNotFoundException e) {
            // Ignore this one.
        } catch (IOException e) {
            e.printStackTrace();
        }

        mechanicConfig.setWriteDefaults(true);

        String mechName = configFile.getName().substring(0, configFile.getName().length() - 4);

        mechanicConfig.setHeader(
            "# CraftBook " + mechName + " Configuration",
            "# -- Generated for version: " + CraftBookPlugin.getVersion(),
            "# ",
            "# More information about these features are available at...",
            "# " + CraftBookPlugin.getDocsDomain(),
            "#",
            "# NOTE! Make sure to enable this in the config.yml file if you wish to use it.",
            "");

        loadFromConfiguration(mechanicConfig);

        mechanicConfig.save();
    }
}
