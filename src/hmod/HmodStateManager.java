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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.state.StateHolder;
import com.sk89q.craftbook.state.StateManager;

public class HmodStateManager extends StateManager {
    private CraftBook main;
    
    public HmodStateManager(CraftBook craftBook) {
        main = craftBook;
    }
    
    synchronized void loadAll() throws IOException {
        WorldInterface world = main.getWorld();
        for(String name:stateHolders.keySet()) {
            StateHolder h = stateHolders.get(name);
            DataInputStream in = null;
            try {
                File f = new File(main.pathToGlobalState,name);
                if(!f.exists()) h.resetCommonData();
                else {
                    in = new DataInputStream(new FileInputStream(f));
                    h.readCommonData(in);
                }
            } finally {
                if(in!=null) in.close();
            }
            try {
                File f = new File(main.pathToGlobalState,name);
                if(!f.exists()) h.resetWorldData(world);
                else {
                    in = new DataInputStream(new FileInputStream(f));
                    h.readWorldData(world, in);
                }
            } finally {
                if(in!=null) in.close();
            }
        }
    }
    
    synchronized void saveAll() throws IOException {
        WorldInterface world = main.getWorld();
        for(String name:stateHolders.keySet()) {
            StateHolder h = stateHolders.get(name);
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(new FileOutputStream(
                        new File(main.pathToGlobalState,name)));
                h.writeCommonData(out);
            } finally {
                if(out!=null) out.close();
            }
            try {
                out = new DataOutputStream(new FileOutputStream(
                        new File(main.pathToWorldState,name)));
                h.writeWorldData(world, out);
            } finally {
                if(out!=null) out.close();
            }
        }
    }
}
