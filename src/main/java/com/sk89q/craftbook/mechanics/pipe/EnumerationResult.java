package com.sk89q.craftbook.mechanics.pipe;

public enum EnumerationResult {
    COMPLETED(false),
    EXCEEDED_TUBE_COUNT_LIMIT(true),
    EXCEEDED_PISTON_COUNT_LIMIT(true),
    STILL_WARMING_UP(false),
    ;

    public final boolean didExceedLimits;

    EnumerationResult(boolean didExceedLimits) {
        this.didExceedLimits = didExceedLimits;
    }
}
