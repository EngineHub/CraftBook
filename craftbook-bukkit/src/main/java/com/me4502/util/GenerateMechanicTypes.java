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

package com.me4502.util;

import com.sk89q.craftbook.core.mechanic.MechanicType;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateMechanicTypes extends ExternalUtilityBase {

    public GenerateMechanicTypes (String[] args) {
        super(args);
    }

    @Override
    public void generate(String[] args) {

        File language = new File(getGenerationFolder(), "mechanicTypes.java");

        try(FileWriter writer = new FileWriter(language)) {
            MechanicType.REGISTRY.keySet().stream().sorted().forEach(mech -> {
                MechanicType<?> type = MechanicType.REGISTRY.get(mech);
                try {
                    writer.write("@Nullable public static final MechanicType<" + type.getMechanicClass().getSimpleName() + "> " + mech.toUpperCase() + " = get(\"" + mech + "\");\n");
                } catch (IOException | ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
