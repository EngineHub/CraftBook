// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic.plc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;

public interface PlcLanguage<StateT, CodeT> {

    String getName();

    StateT initState();

    CodeT compile(String code) throws ICVerificationException;

    boolean supports(String lang);

    void writeState(StateT t, DataOutputStream out) throws IOException;

    void loadState(StateT t, DataInputStream in) throws IOException;

    String dumpState(StateT t);

    void execute(ChipState chip, StateT state, CodeT code) throws PlcException;
}
