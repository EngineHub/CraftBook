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

package com.sk89q.craftbook.mech.ic.custom;

import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.plc.PlcBase;
import com.sk89q.craftbook.mech.ic.plc.PlcException;
import com.sk89q.craftbook.mech.ic.plc.PlcLang;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

class CustomICBase extends PlcBase {

    private final String name, code;

    CustomICBase(PlcLang language, String name, String code) {

        super(language);
        this.name = name;
        this.code = code;
    }

    public String getTitle() {

        return name;
    }

    protected String getCode(WorldInterface w, Vector v) throws PlcException {

        return code;
    }

    protected String validateEnviromentEx(ServerInterface i, WorldInterface w, Vector v, SignText t) {

        return null;
    }
}
