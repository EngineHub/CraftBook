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

package org.enginehub.craftbook.bukkit.report;

import com.sk89q.worldedit.util.report.DataReport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;

import java.util.Collection;

public class ServicesReport extends DataReport {

    public ServicesReport() {
        super("Services");

        ServicesManager manager = Bukkit.getServer().getServicesManager();
        Collection<Class<?>> services = manager.getKnownServices();

        for (Class<?> service : services) {
            Object provider = manager.load(service);
            if (provider != null) {
                append(service.getName(), provider);
            }
        }
    }

}
