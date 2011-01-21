/*    
Craftbook 
Copyright (C) 2010 sk89q <http://www.sk89q.com>
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.MinecraftUtil;
import com.sk89q.craftbook.util.Vector;

public class MiscListener extends CraftBookDelegateListener {    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public MiscListener(CraftBookCore craftBook, ServerInterface server) {
        super(craftBook, server);
    }

    public void loadConfiguration() {}

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(PlayerInterface i, WorldInterface world, Vector v, SignInterface s) {
        String line2 = s.getLine2();
        
        // Black Hole
        if (line2.equalsIgnoreCase("[Black Hole]")
                && !i.canCreateObject("blackhole")) {
            i.sendMessage(Colors.RED
                    + "You don't have permission to make black holes.");
            MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
            return true;
        }
        
        // Block Source
        if (line2.equalsIgnoreCase("[Block Source]")
                && !i.canCreateObject("blocksource")) {
            i.sendMessage(Colors.RED
                    + "You don't have permission to make block sources.");
            MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
            return true;
        }
        
        return false;
    }
    
    public boolean onCommand(PlayerInterface player, String[] split) {
        if (split[0].equalsIgnoreCase("/craftbookversion")) {
            player.sendMessage(Colors.GRAY + "CraftBook version: " +
                    server.getCraftBookVersion());
            player.sendMessage(Colors.GRAY
                    + "Website: http://wiki.sk89q.com/wiki/CraftBook");

            return true;
        }
        return false;
    }
}
