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

import java.util.List;

import com.sk89q.craftbook.access.Action;
import com.sk89q.craftbook.access.BlockEntity;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldInterface$;
import com.sk89q.craftbook.util.TickDelayer;
import com.sk89q.craftbook.util.Tuple2;

public class HmodWorldImpl extends WorldInterface$ {
    private Server server = etc.getServer();
    private CraftBook main;
    private TickDelayer delay = new TickDelayer(this);
    
    public HmodWorldImpl(CraftBook main) {
        this.main = main;
    }
    
    public boolean setId(int x, int y, int z, int t) {
        return server.setBlockAt(t, x, y, z);
    }
    public int getId(int x, int y, int z) {
        return server.getBlockIdAt(x, y, z);
    }

    public boolean setData(int x, int y, int z, int t) {
        return server.setBlockData(x, y, z, t);
    }
    public int getData(int x, int y, int z) {
        return server.getBlockData(x, y, z);
    }

    public void dropItem(float x, float y, float z, int type, int count) {
        server.dropItem(x, y, z, type, count);
    }
    
    public String getName() {
        return CraftBook.DEFAULT_WORLD_NAME;
    }

    public boolean setDataAndUpdate(int x, int y, int z, int t) {
        if(server.setBlockData(x, y, z, t)) {
            server.updateBlockPhysics(x,y,z,t);
            return true;
        }
        return false;
    }

    public boolean isPlayerInWorld(String player) {
        return main.isPlayerOnline(player);
    }
    public PlayerInterface getPlayer(String player) {
        return main.getPlayer(player);
    }
    public PlayerInterface matchPlayer(String player) {
        return main.matchPlayer(player);
    }
    public List<PlayerInterface> getPlayerList() {
        return main.getPlayerList();
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        ComplexBlock b = server.getComplexBlock(x,y,z);
        if(b==null) return null;
        else if(b instanceof Sign) return new HmodSignInterfaceImpl((Sign)b);
        else return null;
    }

    public Tuple2<Integer, Integer>[] getLoadedChunks() {
        return ChunkFinder.getLoadedChunks(server.getMCServer().e);
    }
    
    public long getTime() {
        return server.getTime();
    }
    
    public void delayAction(Action a) {
        delay.delayAction(a);
    }
    
    public boolean equals(Object o) {
        return this==o;
    }
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
