/*    
Craftbook 
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

package com.sk89q.craftbook.access;

import java.util.List;
import java.util.logging.Logger;

import com.sk89q.craftbook.CraftBookDelegateListener;
import com.sk89q.craftbook.state.StateManager;

public interface ServerInterface {
    boolean isCraftBookLoaded();
    boolean isCraftBookEnabled();
    
    boolean isPlayerOnline(String player);
    PlayerInterface getPlayer(String player);
    PlayerInterface matchPlayer(String player);
    List<PlayerInterface> getPlayerList();
    
    void registerListener(Event e, CraftBookDelegateListener l);
    
    WorldInterface matchWorldName(String world);
    WorldInterface getWorld(String world);
    List<WorldInterface> getWorlds();
    
    Configuration getConfiguration();
    
    void sendMessage(String message);
    
    String getCraftBookVersion();
    
    StateManager getStateManager();
    
    WorldEditInterface getWorldEditBridge();
    
    Logger getLogger();
}
