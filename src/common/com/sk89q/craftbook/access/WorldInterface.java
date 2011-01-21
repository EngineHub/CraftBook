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

import java.io.File;
import java.util.List;

import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.Vector;

public interface WorldInterface {
    String getName();
    
    boolean setId(int x, int y, int z, int t);
    boolean setId(Vector v, int t);
    int getId(int x, int y, int z);
    int getId(Vector v);
    
    boolean setData(int x, int y, int z, int t);
    boolean setData(Vector v, int t);
    boolean setDataAndUpdate(int x, int y, int z, int t);
    boolean setDataAndUpdate(Vector v, int t);
    int getData(int x, int y, int z);
    int getData(Vector v);
    
    void dropItem(float x, float y, float z, int type, int count);
    
    BlockEntity getBlockEntity(int x, int y, int z);
    BlockEntity getBlockEntity(Vector v);
    
    boolean isPlayerInWorld(String player);
    PlayerInterface getPlayer(String player);
    PlayerInterface matchPlayer(String player);
    List<PlayerInterface> getPlayerList();
    
    Tuple2<Integer,Integer>[] getLoadedChunks();
    
    void delayAction(Action a);
    
    long getTime();
    void setTime(long time);
    
    void sendMessage(String message);
    
    int getLightLevel(int x, int y, int z);
    int getLightLevel(Vector v);
    
    String getUniqueIdString();
    File getPath();
    File getToggleAreaPath();
}
