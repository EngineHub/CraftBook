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

package com.sk89q.craftbook.ic.plc;

import java.util.logging.Logger;

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.state.StateHolder;

public interface PlcLang extends StateHolder {
    /**
     * Logger instance.
     */
    static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    String getName();
    boolean[] tick(ChipState chip, String program) throws PlcException;
    void checkSyntax(String program) throws PlcException;
    String validateEnvironment(Vector v, SignText t, String code);
}
