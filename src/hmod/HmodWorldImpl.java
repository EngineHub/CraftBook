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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.craftbook.access.Action;
import com.sk89q.craftbook.access.ArrowInterface;
import com.sk89q.craftbook.access.BlockEntity;
import com.sk89q.craftbook.access.MinecartInterface;
import com.sk89q.craftbook.access.MinecartInterface.Type;
import com.sk89q.craftbook.access.MobInterface;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldInterface$;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Tuple2;

public class HmodWorldImpl extends WorldInterface$ {
    /**
     * Logger.
     */
    static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    private final Server server = etc.getServer();
    private final CraftBook main;
    private final TickDelayer delay = new TickDelayer(this);
    
    private boolean fakeExisting = false;
    private int fakeX = 0;
    private int fakeY = 0;
    private int fakeZ = 0;
    private int fakeData = 0;
    
    public HmodWorldImpl(CraftBook main) {
        this.main = main;
    }
    
    public boolean setId(int x, int y, int z, int t) {
        if(server.setBlockAt(t, x, y, z)) {
            if(isFakedBlock(x,y,z)) fakeExisting = false;
            
            return true;
        }
        return false;
    }
    public int getId(int x, int y, int z) {
        return server.getBlockIdAt(x, y, z);
    }

    public boolean setData(int x, int y, int z, int t) {
        if(isFakedBlock(x,y,z)) fakeExisting = false;
        return server.setBlockData(x, y, z, t);
    }
    public int getData(int x, int y, int z) {
        if(isFakedBlock(x,y,z)) {
            return fakeData;
        }
        return server.getBlockData(x, y, z);
    }

    public void dropItem(double x, double y, double z, int type, int count) {
        server.dropItem(x, y, z, type, count);
    }
    
    public String getName() {
        return main.name;
    }

    public boolean setDataAndUpdate(int x, int y, int z, int t) {
        if(server.setBlockData(x, y, z, t)) {
            if(isFakedBlock(x,y,z)) fakeExisting = false;
            
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
        else if(b instanceof Sign) return new HmodSignImpl(this,new BlockVector(x,y,z),(Sign)b);
        else if(b instanceof Chest) return new HmodChestImpl(this,new BlockVector(x,y,z),(Chest)b);
        else if(b instanceof DoubleChest) return new HmodChestImpl(this,new BlockVector(x,y,z),(DoubleChest)b);
        else return null;
    }

    public Tuple2<Integer, Integer>[] getLoadedChunks() {
        return ChunkFinder.getLoadedChunks(server.getMCServer().e);
    }
    
    public long getTime() {
        return server.getTime();
    }
    public void setTime(long time) {
        server.setTime(time);
    }
    
    public void delayAction(Action a) {
        delay.delayAction(a);
    }
    
    protected void fakeData(int x, int y, int z, int d) {
        fakeExisting = true;
        fakeX = x;
        fakeY = y;
        fakeZ = z;
        fakeData = d;
    }
    protected void destroyFake() {
        fakeExisting = false;
    }
    
    private boolean isFakedBlock(int x, int y, int z) {
        return fakeExisting&&fakeX==x&&fakeY==y&&fakeZ==z;
    }

    public void sendMessage(String message) {
        server.messageAll(message);
    }

    public int getLightLevel(int x, int y, int z) {
        return server.getMCServer().e.j(x, y + 1, z);
    }
    
    public String getUniqueIdString() {
        return main.name;
    }
    public File getPath() {
        return main.path;
    }
    
    public File getToggleAreaPath() {
        return main.pathToToggleAreas;
    }

    public void explode(double x, double y, double z) {
        dh tnt = new dh(etc.getMCServer().e);
        tnt.a(x, y, z);
        tnt.b_();
    }
    
    public ArrowInterface shootArrow(double x, double y, double z, 
                                     double xComponent, double yComponent, double zComponent, 
                                     double speed, double spread) {
        fc arrow = new fc(etc.getMCServer().e);
        arrow.c(x, y, z, 0, 0);
        etc.getMCServer().e.a(arrow);
        arrow.a(xComponent, yComponent, zComponent, (float)speed, (float)spread);
        return new HmodArrowImpl(arrow,this);
    }

    public void enqueAction(Runnable r) {
        server.addToServerQueue(r);
    }

    public List<MobInterface> getMobList() {
        List<Mob> list = server.getMobList();
        List<MobInterface> list2 = new ArrayList<MobInterface>();
        
        for(Mob p:list) list2.add(new HmodMobImpl(p,this));
        
        return list2;
    }

    public MobInterface spawnMob(double x, double y, double z, String type) {
        Mob m = new Mob(type);
        m.setX(x);
        m.setY(y);
        m.setZ(z);
        m.spawn();
        return new HmodMobImpl(m,this);
    }

    @Override
    public MobInterface spawnMob(double x, double y, double z, String type, String rider) {
        Mob m = new Mob(type);
        m.setX(x);
        m.setY(y);
        m.setZ(z);
        Mob r = new Mob(rider);
        r.setX(x);
        r.setY(y);
        r.setZ(z);
        m.spawn(r);
        return new HmodMobImpl(m,this);
    }

    public List<MinecartInterface> getMinecartList() {
        List<MinecartInterface> list = new ArrayList<MinecartInterface>();
        for(BaseVehicle v: server.getVehicleEntityList()) if(v instanceof Minecart) {
            Minecart m = (Minecart)v;
            if(m.getStorage()==null) 
                list.add(new HmodMinecartImpl(m,this));
            else
                list.add(new HmodStorageMinecartImpl(m,this));
        }
        return list;
    }

    @Override
    public MinecartInterface spawnMinecart(double x, double y, double z, Type type) {
        Minecart minecart = null;
        switch(type) {
            case REGULAR:
                minecart = new Minecart(x,y,z,Minecart.Type.Minecart);
                break;
            case POWERED:
                minecart = new Minecart(x,y,z,Minecart.Type.PoweredMinecart);
                break;
            case STORAGE:
                minecart = new Minecart(x,y,z,Minecart.Type.StorageCart);
                break;
            default:
                assert false;
        }
        return new HmodMinecartImpl(minecart,this);
    }
}
