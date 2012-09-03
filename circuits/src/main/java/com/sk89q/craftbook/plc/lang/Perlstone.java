// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.plc.lang;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.plc.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Perlstone implements PlcLanguage<Perlstone.State, Perlstone.Code> {
    @Override
    public String getName() {
        return "Perlstone-1.1";
    }

    @Override
    public State initState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Code compile(String code) throws ICVerificationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute(ChipState chip, State state, Code code) throws PlcException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class State implements PlcState {
        @Override
        public void dumpTo(DataOutputStream out) throws IOException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void loadFrom(DataInputStream in) throws IOException {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
    public static class Code {

    }
}
