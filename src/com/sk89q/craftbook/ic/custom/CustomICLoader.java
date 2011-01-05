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

package com.sk89q.craftbook.ic.custom;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.sk89q.craftbook.ic.plc.PlcLang;


import static com.sk89q.craftbook.ic.custom.CustomICSym.*;

public class CustomICLoader {
    public static void load(String source, CustomICAccepter dest, HashMap<String,PlcLang> languages) throws CustomICException {
        try {
            BufferedReader r;
            try {
                r = new BufferedReader(new FileReader(source));
            } catch (IOException e) {
                throw new CustomICException("could not read "+source, e);
            }
            
            CustomICScanner s = new CustomICScanner(r);
            Type type = null;
            Name name = null;
            int state = 0;
            while(true) {
                Symbol y = s.next();
                if(y==null) {
                    if(state!=0) throw new CustomICException("unexpected of file");
                    else return;
                }
                switch(state) {
                    case 0:
                        if(y.symbol==SYM_TYPE) {
                            type = (Type)y.value;
                            PlcLang l = languages.get(type.language);
                            if(l==null) throw new CustomICException("invalid ic language: "+type.language+" ("+y.line+":"+y.col+")");
                            state = 1;
                        } else throw new CustomICException("unexpected token: "+y.value+" ("+y.line+":"+y.col+")");
                        break;
                    case 1:
                        if(y.symbol==SYM_NAME) {
                            name = (Name)y.value;
                            state = 2;
                        } else throw new CustomICException("unexpected token: "+y.value+" ("+y.line+":"+y.col+")");
                        break;
                    case 2:
                        switch(y.symbol) {
                            case SYM_PROG:
                                PlcLang l = languages.get(type.language);
                                if(l==null) throw new CustomICException("internal error: invalid ic language");
                                CustomICBase cic = new CustomICBase(l,name.title,(String)y.value);
                                dest.registerIC(name.icName,cic,type.type);
                                state = 0;
                                break;
                            case SYM_FILE:
                                try {
                                    l = languages.get(type.language);
                                    if(l==null) throw new CustomICException("internal error: invalid ic language");
                                    
                                    DataInputStream in = new DataInputStream(new FileInputStream((String)y.value));
                                    byte[] data = new byte[(int)new File((String)y.value).length()];
                                    in.readFully(data);
                                    
                                    cic = new CustomICBase(l,name.title,new String(data,"UTF-8"));
                                    dest.registerIC(name.icName,cic,type.type);
                                    state = 0;
                                } catch(IOException e) {
                                    throw new CustomICException("Failed to read referenced file.",e);
                                }
                                break;
                            default:  
                                throw new CustomICException("unexpected token: "+y.value+" ("+y.line+":"+y.col+")");
                        }
                        break;
                    default: throw new CustomICException("internal error: unknown state");
                }
            }
                
        } catch(UnknownTokenException e) {
            throw new CustomICException("unknown token at ("+e.line+":"+e.column+")",e);
        } catch(CustomICException e) {
            throw e;
        } catch (Throwable t) {
            throw new CustomICException("unknown error: "+t.getClass().getName()+" ("+t.getMessage()+")",t);
        }
    }
}
