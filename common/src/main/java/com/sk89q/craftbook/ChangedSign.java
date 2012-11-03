package com.sk89q.craftbook;

import org.bukkit.block.Sign;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;

public interface ChangedSign extends Sign {


    //Location
    @Override
    public int getX();

    @Override
    public int getY();

    @Override
    public int getZ();

    public LocalWorld getLocalWorld();

    public BlockWorldVector getBlockVector();

    public Location getSignLocation();

    //TODO public Chunk getChunk();

    //Lines
    @Override
    public String[] getLines();

    public void setLines(String[] lines);

    @Override
    public String getLine(int line);

    @Override
    public void setLine(int line, String data);

    //Other
    @Override
    public boolean update(boolean force);

    @Override
    public boolean setTypeId(int type);

    @Override
    public int getTypeId();

    @Override
    public byte getRawData();

    @Override
    public void setRawData(byte data);

    @Override
    public byte getLightLevel();
}