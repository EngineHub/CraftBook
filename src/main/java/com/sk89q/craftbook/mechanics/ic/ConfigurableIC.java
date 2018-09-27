package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.util.yaml.YAMLProcessor;

public interface ConfigurableIC {

    /**
     * Called when {@link ICConfiguration} is being read for this {@link ICFactory}.
     *
     * @param config The configuration processor.
     * @param path The path to this configuration section.
     */
    void addConfiguration(YAMLProcessor config, String path);
}