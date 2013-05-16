package com.sk89q.craftbook.circuits.ic;

import com.sk89q.util.yaml.YAMLProcessor;

public interface ConfigurableIC {

    /**
     * Adds config to the IC.
     *
     * @param section
     */
    public void addConfiguration(YAMLProcessor config, String path);
}