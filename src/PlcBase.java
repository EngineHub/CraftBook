// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import lymia.plc.PlcException;
import lymia.plc.PlcLang;
import lymia.plc.State;
import lymia.util.Base64;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.VIVOFamilyIC;
import com.sk89q.craftbook.ic._3I3OFamilyIC;

public class PlcBase extends BaseIC implements VIVOFamilyIC, _3I3OFamilyIC {
    private PlcLang language;
    private boolean signStorage;
    public PlcBase(PlcLang language, boolean signStorage) {
        this.language = language;
        this.signStorage = signStorage;
    }
    
    public String getTitle() {
        return "PLC ("+language.getName()+")";
    }

    public void think(ChipState chip) {
        SignText t = chip.getText();
        
        String code;
        try {
            code = getCode(chip.getPosition());
        } catch (PlcException e) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code not found");
            return;
        }
        
        if(!t.getLine3().equals("HASH:"+Integer.toHexString(code.hashCode()))) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code modified");
            return;
        }
        
        byte[] presistantStorage;
        if(signStorage) try {
            presistantStorage = Base64.decode(t.getLine4().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("no utf-8");
            return;
        } catch (IOException e) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("unknown error");
            return;
        } else presistantStorage = getSwitchBank(chip.getPosition());
        
        State s = new State();
        s.input = new boolean[] {
                chip.getIn(1).is(),
                chip.getIn(2).is(),
                chip.getIn(3).is(),
        };
        s.presistantStorage = presistantStorage;
        
        boolean[] output;
        try {
            output = language.tick(s, code);
        } catch (PlcException e) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(e.getMessage());
            return;
        } catch (Throwable r) {
            t.setLine2(Colors.Red+t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(r.getClass().getSimpleName());
            return;
        }
        
        if(signStorage) t.setLine4(Base64.encodeBytes(s.presistantStorage));
        else storeSwitchBank(chip.getPosition(),s.presistantStorage);
        
        chip.getOut(1).set(output[0]);
        chip.getOut(2).set(output[1]);
        chip.getOut(3).set(output[2]);
        
        t.supressChange();
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
        
        Sign s = (Sign) etc.getServer().getComplexBlock(v.getBlockX(),v.getBlockY(),v.getBlockZ());
        
        s.setText(2, "HASH:"+Integer.toHexString(code.hashCode()));
        if(signStorage) s.setText(3,"AAAAAAAAAAAA");
        
        return null;
    }

    private static String getCode(Vector v) throws PlcException { 
        Server s = etc.getServer();
        StringBuilder b = new StringBuilder();
        int x = v.getBlockX();
        int z = v.getBlockZ();
        int x0 = x;
        int y0 = v.getBlockY();
        int z0 = z;
        for(int y=0;y<128;y++) if(CraftBook.getBlockID(x,y,z)==BlockType.WALL_SIGN) { 
            if(((Sign)s.getComplexBlock(x, y, z)).getText(1).equalsIgnoreCase("[CODE BLOCK]"))
                for(y--;y>=0;y--) if(!(x==x0&&y==y0&&z==z0)&&CraftBook.getBlockID(x,y,z)==BlockType.WALL_SIGN) {
                    Sign n = (Sign) s.getComplexBlock(x, y, z);
                    b.append(n.getText(0)+"\n");
                    b.append(n.getText(1)+"\n");
                    b.append(n.getText(2)+"\n");
                    b.append(n.getText(3)+"\n");
                } else return b.toString();
        }
        throw new PlcException("code not found");
    }
    
    private static byte[] getSwitchBank(Vector v) {
        //TODO Implement switch based memory
        return null;
    }
    private static void storeSwitchBank(Vector v, byte[] d) {
        //TODO Implement switch based memory
    }
}
