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

import com.sk89q.craftbook.access.MinecartInterface;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldInterface;

public class HmodMinecartImpl extends HmodBaseEntityImpl
        implements MinecartInterface {

    private Minecart cart;
    private Type type;

    public HmodMinecartImpl(Minecart cart, WorldInterface w) {

        super(cart, w);
        this.cart = cart;
        switch (cart.getType()) {
            case Minecart:
                type = Type.REGULAR;
                break;
            case PoweredMinecart:
                type = Type.POWERED;
                break;
            case StorageCart:
                type = Type.STORAGE;
                break;
            default:
                assert false : "bad enum value";
        }
    }

    public void remove() {

        cart.destroy();
    }

    public Type getType() {

        return type;
    }

    public boolean hasPassenger() {

        return cart.getEntity().j != null;
    }

    public boolean isMobType(String mobType) {

        if (cart.getEntity().j instanceof mj) {
            Mob mob = new Mob((mj) cart.getEntity().j);
            if (mobType.equalsIgnoreCase(mob.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPlayer() {

        return cart.getPassenger() != null;
    }

    public boolean hasAnimal() {

        return cart.getEntity().j instanceof bl;
    }

    public boolean hasMob() {

        return (cart.getEntity().j instanceof hq) || (cart.getEntity().j instanceof hl);
    }

    public PlayerInterface getPlayer() {

        Player p = cart.getPassenger();
        if (p == null) return null;
        return new HmodPlayerImpl(p, world);
    }
}

