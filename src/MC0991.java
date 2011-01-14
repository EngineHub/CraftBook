// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Command-triggered switch
 *
 * @author robhol
 */
public class MC0991 extends BaseIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "CMD SWITCH EXT";
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign)
    {
        String id = sign.getLine3();

        if (id.isEmpty())
            return "Needs an ID.";

        if (id.contains(" ") || sign.getLine4().contains(" "))
            return "No spaces in ID/code!";

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {

        if (!chip.getIn(1).is())
        {
            //Falling flank. Clear any existing outputs.
            chip.getOut(1).set(false);
            chip.getOut(2).set(false);
            chip.getOut(3).set(false);

            return;
        }

        //Check "central" for pending activations
        String code = CommandIC.check(chip.getText().getLine3());

        if (code == null) //Nothing for us
            return;

        String correctcode = chip.getText().getLine4();

        if (correctcode == "" || code.equals(correctcode)) //Correct code OR none was given on the IC
        {
            chip.getOut(1).set(true); //pulse main output
            return;
        }

        //Only remaining state is wrong code, pulse secondary outputs
        chip.getOut(2).set(true);
        chip.getOut(3).set(true);

    }

}
