package com.sk89q.craftbook;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;

public interface ChangedSign {

    // CraftBlock
    public CraftBlock getCraftBlock();

    //Location
    public int getX();

    public int getY();

    public int getZ();

    public LocalWorld getLocalWorld();

    public BlockWorldVector getBlockVector();

    public Location getSignLocation();

    //TODO public Chunk getChunk();

    //Lines
    public String[] getLines();

    public void setLines(String[] lines);

    public String getLine(int line);

    public void setLine(int line, String data);

    //Other
    public boolean update(boolean force);

    public boolean setTypeId(int type);

    public int getTypeId();

    public byte getRawData();

    public void setRawData(byte data);

    public byte getLightLevel();
}