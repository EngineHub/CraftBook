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

import com.sk89q.craftbook.access.ChestInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Vector;

public class HmodChestInterfaceImpl extends HmodInventoryInterfaceImpl
                                    implements ChestInterface{
    private final WorldInterface w;
    private final BlockVector pos;
    
    public HmodChestInterfaceImpl(WorldInterface w, BlockVector pos, Chest chest) {
        super(chest);
        this.pos = pos;
        this.w = w;
    }
    public HmodChestInterfaceImpl(WorldInterface w, BlockVector pos, DoubleChest chest) {
        super(chest);
        this.pos = pos;
        this.w = w;
    }

    public boolean equals(Object other) {
        if (other instanceof HmodChestInterfaceImpl) {
            HmodChestInterfaceImpl chest = ((HmodChestInterfaceImpl)other);
            return chest.pos.equals(pos)&&chest.w.equals(w);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return pos.hashCode()*31+w.hashCode();
    }

    public Vector getPosition() {
        return pos;
    }
    
    public WorldInterface getWorld() {
        return w;
    }
}
