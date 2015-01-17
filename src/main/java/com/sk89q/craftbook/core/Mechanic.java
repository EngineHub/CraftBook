package com.sk89q.craftbook.core;

import com.sk89q.craftbook.core.util.CachePolicy;

public interface Mechanic {

    public String getName();

    public void onLoad();

    public CachePolicy getCachePolicy();
}
