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

package com.sk89q.craftbook.mech.ic.world;

import java.util.Random;

import com.sk89q.craftbook.access.ArrowInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Fireworks launcher.
 * 
 * @author yofreke
 * @author sk89q
 */
public class MC1250 extends BaseIC {
    /**
     * Random number generator.
     */
    private static Random r = new Random();
    
    /**
     * Get the title of the IC.
     * 
     * @return
     */
    public String getTitle() {
        return "FIREWORKS";
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
        if (sign.getLine3().length() != 0) {
            return "The third line must be blank.";
        }
        
        if (sign.getLine4().length() != 0) {
            return "The fourth line must be blank.";
        }

        return null;
    }

    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
        if (chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
            int x = chip.getBlockPosition().getBlockX();
            int y = chip.getBlockPosition().getBlockY();
            int z = chip.getBlockPosition().getBlockZ();
            
            if (y >= 125) {
                return;
            }
            
            ArrowInterface arrow = shoot(chip.getWorld(), x + 0.5, y + 1.5, z + 0.5);

            Firework instance = new Firework(arrow, chip.getWorld());
            (new Thread(instance)).start();
        }
    }

    /**
     * Single firework.
     */
    public class Firework implements Runnable {
        /**
         * Last arrow Y position for comparison.
         */
        private double lastY;
        /**
         * Arrow.
         */
        private ArrowInterface arrow;
        /**
         * Expiration time.
         */
        private long expireTime;
        
        private WorldInterface world;

        /**
         * Construct the object.
         * 
         * @param arrow
         */
        public Firework(ArrowInterface arrow, WorldInterface world) {
            expireTime = System.currentTimeMillis() + 5000;
            this.arrow = arrow;
            lastY = arrow.getY();
            this.world = world;
        }

        /**
         * Runs the firework.
         */
        public void run() {
            try {
                while (true) {
                    final double arrowX = arrow.getX();
                    final double arrowY = arrow.getY();
                    final double arrowZ = arrow.getZ();
                    
                    if (arrowY < lastY) {
                        world.enqueAction(new Runnable() {
                            public void run() {
                                
                                
                                // Make TNT explode
                                world.kyuu(arrowX, arrowY, arrowZ);
                                world.kyuu(
                                        arrowX + r.nextDouble() * 2 - 1,
                                        arrowY + r.nextDouble() * 1,
                                        arrowZ + r.nextDouble() * 2 - 1);
                            }
                        });
                        
                        break;
                    } else {
                        lastY = arrowY;
                    }
                    
                    if (System.currentTimeMillis() > expireTime) {
                        break;
                    }
                    
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
            }
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
    protected ArrowInterface shoot(WorldInterface w, double x, double y, double z) {
        return w.shootArrow(x, y, z, 
                            0, 50, 0, 
                            1.05F, 20);
    }
}