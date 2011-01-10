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

import com.sk89q.craftbook.util.SignText;

public abstract class SignInterface extends SignText implements BlockEntity {
    public SignInterface(String l1, String l2, String l3, String l4) {
        super(l1, l2, l3, l4);
    }
    
    public abstract int getX();
    public abstract int getY();
    public abstract int getZ();
}
