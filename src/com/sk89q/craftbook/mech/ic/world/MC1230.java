package com.sk89q.craftbook.mech.ic.world;

import com.sk89q.craftbook.mech.ic.BaseIC;
import com.sk89q.craftbook.mech.ic.ChipState;


/**
 * Takes in a clock input, and outputs whether the time is day or night.
 *
 * @author Shaun (sturmeh)
 */
public class MC1230 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "IS IT DAY";
    }

    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
        long time = (chip.getTime() % 24000);
        if (time < 0) time += 24000;
        
        if (time < 13000l)
            chip.getOut(1).set(true);
        else 
            chip.getOut(1).set(false);
    }
}
