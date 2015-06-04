package com.sk89q.craftbook.core.mechanics;

import java.util.Map;

/**
 * A base data class for any data that can be associated with a {@link com.sk89q.craftbook.core.Mechanic}.
 */
public interface MechanicData {

    void load(Map<String, Object> dataMap);

    Map<String, Object> save();
}
