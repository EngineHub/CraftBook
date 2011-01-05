// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 tmhrtly <http://www.tmhrtly.com>
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

/**
 * Broadcasts a message to nearby players.
 *
 * @author Tom (tmhrtly)
 */

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MC1520 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PROXIMITY SENSOR";
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
        String name = sign.getLine3();
        if (name.equals("")||(name.contains(" ") && !name.contains("g:"))) {
            return "Please put a player's name or g: groupName on the third line.";
        }
        try {
            Double.parseDouble(sign.getLine4());
        } catch (NumberFormatException e) {
            return "Distance is not a number.";
        }
        return null;
    }
    
    
    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
        String p = chip.getText().getLine3();
        Double distance = Double.parseDouble(chip.getText().getLine4());
        Vector pos = chip.getBlockPosition();
        if (p.contains("g: ")) {
            //We're dealing with a group
            String grp = p.substring(3);
            for(Player aPlayer: playersInGroup(grp)) {
                if (playerVector(aPlayer).distance(pos)<=distance) {
                    chip.getOut(1).set(true);
                    return;
                };
				chip.getOut(1).set(false);
            }
            
        } else {
            //We're dealing with a player
            Player pl = etc.getServer().matchPlayer(p);
            if (pl== null) {
                chip.getOut(1).set(false);
            } else {
                chip.getOut(1).set(playerVector(pl).distance(pos)<=distance);   
            }
            return;
        }

    }

    public List<Player> playersInGroup(String grp){
        List<Player> players = new ArrayList<Player>();
        for(Player p: etc.getServer().getPlayerList())
            if (Arrays.asList(p.getGroups()).contains(grp)) players.add(p);
        return players;
    }

    public Vector playerVector(Player n){
        return new Vector(n.getX(),n.getY(),n.getZ());
    }
    
}
