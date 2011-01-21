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

import com.sk89q.craftbook.util.Vector;

public abstract class WorldInterface$ implements WorldInterface {
    public boolean setId(Vector v, int t) {
        return setId(v.getBlockX(),v.getBlockY(),v.getBlockZ(),t);
    }
    public int getId(Vector v) {
        return getId(v.getBlockX(),v.getBlockY(),v.getBlockZ());
    }
    
    public boolean setData(Vector v, int t) {
        return setData(v.getBlockX(),v.getBlockY(),v.getBlockZ(),t);
    }
    public boolean setDataAndUpdate(Vector v, int t) {
        return setDataAndUpdate(v.getBlockX(),v.getBlockY(),v.getBlockZ(),t);
    }
    
    public int getData(Vector v) {
        return getData(v.getBlockX(),v.getBlockY(),v.getBlockZ());
    }

    public BlockEntity getBlockEntity(Vector v) {
        return getBlockEntity(v.getBlockX(),v.getBlockY(),v.getBlockZ());
    }
    
    public int getLightLevel(Vector v) {
        return getLightLevel(v.getBlockX(),v.getBlockY(),v.getBlockZ());
    }
}
