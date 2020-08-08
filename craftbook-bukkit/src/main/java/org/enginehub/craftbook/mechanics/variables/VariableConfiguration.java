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

package org.enginehub.craftbook.mechanics.variables;

import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.io.IOException;
import java.util.Map;

public class VariableConfiguration {

    public final YAMLProcessor config;

    public VariableConfiguration(YAMLProcessor config) {
        this.config = config;
    }

    public void load() {
        try {
            config.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // If we don't have any variables, don't bother.
        if (config.getKeys("variables") == null) {
            return;
        }
        for (String namespace : config.getKeys("variables")) {
            for (String variable : config.getNode("variables").getKeys(namespace)) {
                String value = String.valueOf(config.getProperty("variables." + namespace + "." + variable));

                try {
                    VariableKey key = VariableKey.of(namespace, variable, null);

                    if (VariableManager.DIRECT_VARIABLE_PATTERN.matcher(key.toString()).matches()
                            && VariableManager.ALLOWED_VALUE_PATTERN.matcher(value).find()) {
                        VariableManager.instance.setVariable(key, value);
                    } else {
                        throw new VariableException(TextComponent.of("Invalid variable " + key.toString() + " with value " + value), key);
                    }
                } catch (VariableException e) {
                    CraftBook.logger.error("Invalid variable in variables file", e);
                }
            }
        }
    }

    public void save() {
        config.clear();

        for (Map.Entry<String, Map<String, String>> namespaceEntry : VariableManager.instance.getVariableStore().entrySet()) {
            config.setProperty("variables." + namespaceEntry.getKey(), namespaceEntry.getValue());
        }

        config.save();
    }
}
