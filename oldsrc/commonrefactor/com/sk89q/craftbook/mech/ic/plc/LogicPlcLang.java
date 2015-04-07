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
import com.sk89q.craftbook.mech.ic.LogicChipState;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

public abstract class LogicPlcLang implements PlcLang {

    public final boolean[] tick(ChipState state, String program) throws PlcException {

        LogicChipState s = new LogicChipState(state.getInputs(), state.getOutputs(), state.getText(),
                state.getBlockPosition());
        return tick(state.getWorld().getUniqueIdString(), s, program);
    }

    public abstract boolean[] tick(String id, LogicChipState chip, String program) throws PlcException;

    public final String validateEnvironment(WorldInterface w, Vector v, SignText t, String code) {

        return validateEnvironment(w.getUniqueIdString(), v, t, code);
    }

    public abstract String validateEnvironment(String id, Vector v, SignText t, String code);
}
