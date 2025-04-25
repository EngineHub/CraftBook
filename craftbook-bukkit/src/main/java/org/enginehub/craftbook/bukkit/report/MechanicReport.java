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

package org.enginehub.craftbook.bukkit.report;

import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldedit.util.report.HierarchyObjectReport;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlatform;
import org.enginehub.craftbook.bukkit.st.BukkitSelfTriggerManager;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;

public class MechanicReport extends DataReport {

    public MechanicReport() {
        super("Mechanics");

        CraftBookPlatform platform = CraftBook.getInstance().getPlatform();

        append("Mechanics Loaded", CraftBook.getInstance().getPlatform().getMechanicManager().getLoadedMechanics().size());
        append("ST Mechanics Loaded", platform.getSelfTriggerManager() == null
            ? 0
            : ((BukkitSelfTriggerManager) platform.getSelfTriggerManager()).getSelfTriggeringMechanics().size());

        for (CraftBookMechanic mechanic : CraftBook.getInstance().getPlatform().getMechanicManager().getLoadedMechanics()) {
            DataReport report = new DataReport("Mechanic: " + mechanic.getClass().getSimpleName());
            report.append("Configuration", new HierarchyObjectReport("Configuration", mechanic));

            append(report.getTitle(), report);
        }
    }
}
