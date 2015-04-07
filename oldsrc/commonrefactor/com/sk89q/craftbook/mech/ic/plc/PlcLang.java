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

package com.sk89q.craftbook.mech.ic.plc;

import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.ChipState;
import com.sk89q.craftbook.state.StateHolder;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

public interface PlcLang extends StateHolder {

    String getName();

    boolean[] tick(ChipState chip, String program) throws PlcException;

    void checkSyntax(String program) throws PlcException;

    String validateEnvironment(WorldInterface w, Vector v, SignText t, String code);
}
