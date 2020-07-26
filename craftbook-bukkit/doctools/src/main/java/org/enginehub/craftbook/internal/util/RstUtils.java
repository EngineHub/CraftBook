package org.enginehub.craftbook.internal.util;

import com.sk89q.util.yaml.YAMLProcessor;

import java.util.Map;

public class RstUtils {

    public static String configToString(YAMLProcessor config) {
        StringBuilder builder = new StringBuilder();

        builder.append(".. csv-table::\n");
        builder.append("  :header: Node, Comment, Default\n");
        builder.append("  :widths: 15, 30, 10\n\n");

        for(String key : config.getKeys(null)) {
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
