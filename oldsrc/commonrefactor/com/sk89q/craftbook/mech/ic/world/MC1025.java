package com.sk89q.craftbook.mech.ic.world;

import com.sk89q.craftbook.mech.ic.BaseIC;
import com.sk89q.craftbook.mech.ic.ChipState;


/**
 * 1-bit number based on modulus of server time.
 *
 * @author Shaun (sturmeh)
 */
public class MC1025 extends BaseIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "REL TIME MOD 2";
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {

        if (chip.getIn(1).is())
            chip.getOut(1).set(isServerTimeOdd(chip));
    }

    /**
     * Returns true if the relative time is odd.
     *
     * @return
     */
    private boolean isServerTimeOdd(ChipState chip) {

        long time = chip.getWorld().getTime() % 2;
        if (time < 0) time += 2;
        return (time == 1);
    }
}
