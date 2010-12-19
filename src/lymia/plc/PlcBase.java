// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

package lymia.plc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import lymia.util.Base64;

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.Signal;

public abstract class PlcBase extends BaseIC {
    private PlcLang language;
    public PlcBase(PlcLang language) {
        this.language = language;
    }

    public void think(ChipState chip) {
        SignText t = chip.getText();
        
        String code;
        try {
            code = getCode(chip.getPosition());
        } catch (PlcException e) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code not found");
            return;
        }
        
        if(!t.getLine3().equals("HASH:"+Integer.toHexString(code.hashCode()))) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code modified");
            return;
        }
        
        byte[] presistantStorage;
        try {
            presistantStorage = Base64.decode(t.getLine4().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("no utf-8");
            return;
        } catch (IOException e) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("unknown error");
            return;
        }
        
        State s = new State();
        Signal[] in = chip.getInputs();
        s.input = new boolean[in.length];
        for(int i=0;i<in.length;i++) s.input[i] = in[i].is();
        
        s.presistantStorage = presistantStorage;
        
        boolean[] output;
        try {
            output = language.tick(s, code);
        } catch (PlcException e) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(e.getMessage());
            return;
        } catch (Throwable r) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(r.getClass().getSimpleName());
            return;
        }
        
        t.setLine4(Base64.encodeBytes(s.presistantStorage));
        
        try {
            for(int i=0;i<output.length;i++) {
                Signal out = chip.getOut(i+1);
                if(out==null) break;
                out.set(output[i]);
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            t.setLine2("§c"+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("too many outputs");
            return;
        }
        
        t.supressUpdate();
    }
    
    public String validateEnvironment(Vector v, SignText t) {
        if(!t.getLine3().isEmpty()) return "line 3 is not empty";
        if(!t.getLine4().isEmpty()) return "line 4 is not empty";
        
        String code;
        try {
            code = getCode(v);
        } catch (PlcException e) {
            return "Code block not found.";
        }
        
        t.setLine3("HASH:"+Integer.toHexString(code.hashCode()));
        t.setLine4("AAAAAAAAAAAA");
        
        return validateEnviromentEx(v,t);
    }
    
    protected abstract String validateEnviromentEx(Vector v, SignText t);
    protected abstract String getCode(Vector v) throws PlcException;
    protected PlcLang getLanguage() {return language;}
}
