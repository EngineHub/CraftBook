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

package org.enginehub.craftbook.internal.util;

import com.sk89q.util.yaml.YAMLProcessor;

import java.util.Map;

public class RstUtils {

    public static String configToString(YAMLProcessor config) {
        StringBuilder builder = new StringBuilder();

        builder.append(".. csv-table::\n");
        builder.append("  :header: Node, Comment, Default\n");
        builder.append("  :widths: 15, 30, 10\n\n");

        for (String key : config.getKeys(null)) {
            String comment = config.getComment(key);
            if (comment == null) {
                System.out.println("[WARNING] Key " + key + " is missing a comment!");
                comment = "";
            }
            if (!comment.trim().isEmpty()) {
                comment = comment.trim().substring(2);
            }

            String defaultValue = String.valueOf(config.getProperty(key));
            if (config.getProperty(key) instanceof Map) {
                defaultValue = "";
            }

            builder.append("  ``").append(key).append("``,\"").append(comment).append("\",\"").append(defaultValue).append("\"\n");
        }

        return builder.toString();
    }
}
