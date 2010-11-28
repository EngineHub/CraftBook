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

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.SISOFamilyIC;

/**
 * Sets the server time to day or night, repeats the signal.
 *
 * @author Shaun (sturmeh)
 */
public class MC1231 extends SISOFamilyIC {

    public void think(ChipState chip) {
        chip.title("TIME CONTROL");

        Long specific;

        if (chip.in(1).is())
            specific = 0l;
        else 
            specific = 13000l;

        etc.getServer().setRelativeTime(specific);

        chip.out(1).set(chip.in(1).is());
    }
}
