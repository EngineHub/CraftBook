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

package com.sk89q.craftbook.bukkit.report;

import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.mechanic.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldedit.util.report.HierarchyObjectReport;

public class MechanicReport extends DataReport  {

    public MechanicReport() {
        super("Mechanics");

        CraftBookPlugin plugin = CraftBookPlugin.inst();

        append("Mechanics Loaded", CraftBook.getInstance().getPlatform().getMechanicManager().getLoadedMechanics().size());
        append("ST Mechanics Loaded", plugin.getSelfTriggerManager() == null ? 0 : plugin.getSelfTriggerManager().getSelfTriggeringMechanics().size());

        for (CraftBookMechanic mechanic : CraftBook.getInstance().getPlatform().getMechanicManager().getLoadedMechanics()) {
            DataReport report = new DataReport("Mechanic: " + mechanic.getClass().getSimpleName());
            report.append("Configuration", new HierarchyObjectReport("Configuration", mechanic));

            append(report.getTitle(), report);
        }
    }
}
