package com.sk89q.craftbook.mechanics.pipe;

public enum EnumerationResult {
    COMPLETED(false),
    EXCEEDED_TUBE_COUNT_LIMIT(true),
    EXCEEDED_PISTON_COUNT_LIMIT(true),
    EXCEEDED_CACHE_LOAD_LIMIT(false),
    NEEDS_CHUNK_LOADING(false),
    ;

    public final boolean didExceedExtentLimits;

    EnumerationResult(boolean didExceedExtentLimits) {
        this.didExceedExtentLimits = didExceedExtentLimits;
    }
}
