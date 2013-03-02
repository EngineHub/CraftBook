package com.sk89q.craftbook;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;

public interface ChangedSign {

    // Location
    public int getX();

    public int getY();

    public int getZ();

    public LocalWorld getLocalWorld();

    public BlockWorldVector getBlockVector();

    // Lines
    public String[] getLines();

    public void setLines(String[] lines);

    public String getLine(int line);

    public void setLine(int line, String data);

    public boolean hasChanged();

    public void flushLines();

    // Other
    public boolean update(boolean force);

    public boolean setTypeId(int type);

    public int getTypeId();

    public byte getRawData();

    public void setRawData(byte data);

    public byte getLightLevel();
}