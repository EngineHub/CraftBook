package com.sk89q.craftbook.core.util.documentation;

import com.sk89q.craftbook.core.util.ConfigValue;

public interface DocumentationProvider {

    /**
     * Gets the relative path inside the mechanics directory that this
     * documentation file goes in.
     *
     * @return The path.
     */
    String getPath();

    /**
     * Gets the main documentation section.
     *
     * <p>
     *     Each string in the array refers to a different line.
     * </p>
     *
     * @return The main documentation section
     */
    String[] getMainDocumentation();

    /**
     * Gets an array of all configuration nodes this mechanic uses.
     *
     * @return An array of configuration nodes.
     */
    ConfigValue<?>[] getConfigurationNodes();
}
