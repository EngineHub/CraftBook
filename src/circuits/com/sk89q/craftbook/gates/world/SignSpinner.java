// $Id$
/*
 * Copyright (C) 2011 purpleposeidon@gmail.com
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

package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.data.BlockData;

/**
 * [MC1290] Sign Spinner 2nd line: How many steps to spin. This is a value from
 * -16 to +16. Every 4 is a full block rotation. Blocks will rotate only if the
 * rotation is a multiple of 4.
 * 
 * The following blocks rotate: Signs, pumpkins, diodes, furnaces, dispensers,
 * stairs, rails The following blocks transfer rotational power: fence posts,
 * soul sand
 * 
 * @author purpleposeidon
 */

public class SignSpinner extends AbstractIC {

    protected boolean risingEdge;

    static Material spinnable[] = new Material[] { Material.PUMPKIN,
        Material.JACK_O_LANTERN, Material.DIODE_BLOCK_ON,
        Material.DIODE_BLOCK_OFF, Material.FURNACE,
        Material.BURNING_FURNACE, Material.DISPENSER, Material.WOOD_STAIRS,
        Material.COBBLESTONE_STAIRS, Material.DETECTOR_RAIL,
        Material.RAILS, Material.POWERED_RAIL };

    public SignSpinner(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Rotor";
    }

    @Override
    public String getSignTitle() {
        return "ROTOR";
    }

    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            Location loc = SignUtil.getBackBlock(getSign().getBlock()).getLocation();
            int rotation;
            byte blockRotation = 0;

            try {
                rotation = Integer.parseInt(getSign().getLine(2));
            } catch (NumberFormatException e) {
                rotation = 1;
            }

            rotation %= 16;

            if ((rotation % 4) == 0) {
                blockRotation = (byte) (rotation / 4);
            }

            for (int i = 0; i < 16; i++) {
                loc.setY(loc.getY() + 1);
                Block here = loc.getBlock();
                Material m = here.getType();

                if (m == Material.FENCE || m == Material.SOUL_SAND) {
                    /* spins fence posts; souls spin in graves */
                }
                else if (m == Material.SIGN_POST) {
                    int data = here.getData();
                    data = data + rotation % 16;
                    here.setData((byte) data, true);
                } else {
                    boolean validMaterial = false;
                    for (Material s : spinnable) {
                        if (m == s) {
                            byte data = here.getData();
                            for (byte r = blockRotation; r != 0; r--) {
                                data = (byte) BlockData.rotate90(m.getId(), data);
                            }
                            here.setData(data, true);
                            validMaterial = true;
                            break;
                        }
                    }
                    if (!validMaterial) {
                        break;
                    }
                    // TODO: Update block (So that spinning a diode between a torch
                    // and redstone dust will cause proper behavior)
                }
                // TODO: Spin attached torches, levers, trap doors.
                // Spinning walls signs might cause too much excitement and
                // packets?
            }
        }
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new SignSpinner(getServer(), sign, risingEdge);
        }
    }
}
