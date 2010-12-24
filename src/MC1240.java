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
 * Shoots arrows.
 *
 * @author sk89q
 */
public class MC1240 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "ARROW SHOOTER";
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
        String speedSpreadLine = sign.getLine3();
        String vertVelLine = sign.getLine4();

        try {
            if (speedSpreadLine.length() > 0) {
                String[] parts = speedSpreadLine.split(":");
                
                float speed = Float.parseFloat(parts[0]);
                if (speed < 0.3 || speed > 2) {
                    return "Speed must be >= 0.3 and <= 2.";
                }
                
                if (parts.length > 1) {
                    float spread = Float.parseFloat(parts[1]);
                    if (spread < 0 || spread > 20) {
                        return "Spread must be >= 0 and <= 20.";
                    }
                }
            }

            if (vertVelLine.length() > 0) {
                float speed = Float.parseFloat(vertVelLine);
                if (speed < -1 || speed > 1) {
                    return "Vertical elocity must be between or equal to -1 and 1.";
                }
            }
        } catch (NumberFormatException e) {
            return "Speed is the third line and spread is the fourth line.";
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
            String speedSpreadLine = chip.getText().getLine3();
            String vertVelLine = chip.getText().getLine4();
            float speed = 0.5F;
            float spread = 12F;
            float vertVel = 0F;

            try {
                if (speedSpreadLine.length() > 0) {
                    String[] parts = speedSpreadLine.split(":");
                    
                    speed = Float.parseFloat(parts[0]);
                    
                    if (parts.length > 1) {
                        spread = Float.parseFloat(parts[1]);
                    }
                }

                if (vertVelLine.length() > 0) {
                    vertVel = Float.parseFloat(vertVelLine);
                }
            } catch (NumberFormatException e) {
            }
            
            shoot(chip, speed, spread, vertVel);
        }
    }
    
    /**
     * Shoot the arrow.
     * 
     * @param chip
     * @param speed
     * @param spread
     * @param vertVel
     */
    protected void shoot(ChipState chip, float speed, float spread, float vertVel) {
        Vector backDir = chip.getBlockPosition().subtract(
                chip.getPosition());
        Vector firePos = chip.getBlockPosition().add(backDir);
        en arrow = new en(etc.getMCServer().e);
        arrow.c(firePos.getBlockX() + 0.5, firePos.getBlockY() + 0.5,
                firePos.getBlockZ() + 0.5, 0, 0);
        etc.getMCServer().e.a(arrow);
        arrow.a(backDir.getBlockX(), vertVel, backDir.getBlockZ(),
                speed, spread);
    }
}
