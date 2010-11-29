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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Mob spawner.
 *
 * @author sk89q
 */
public class MC1200 extends SISOFamilyIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MOB SPAWNER";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {
        return true;
    }
    
    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a mob type on the third line.";
        } else if (!Mob.isValid(id)) {
            return "Not a valid mob type: " + sign.getLine3() + ".";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (chip.getIn(1).is()) {
            String id = chip.getText().getLine3();
            if (Mob.isValid(id)) {
                Vector pos = chip.getPosition();
                Location loc =
                        new Location(pos.getBlockX(),
                        pos.getBlockY() + 2,
                        pos.getBlockZ());
                Mob mob = new Mob(id, loc);
                mob.spawn();
            }
        }
    }
}
