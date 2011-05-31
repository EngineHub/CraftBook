// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.gates.world;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;

public class SchematicPaster extends AbstractIC {

    protected boolean risingEdge;

    public SchematicPaster(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
        
    }

    @Override
    public String getTitle() {
        return "Schematic Paster";
    }

    @Override
    public String getSignTitle() {
        return "SCHEMA PASTE";
    }

    public static File getFile(Server server, String filename) throws ICVerificationException {
        WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
        WorldEdit we = wep.getWorldEdit();
        if (we == null) {
            throw new ICVerificationException("This IC requires the WorldEdit plugin");
        }

        if (filename.length() == 0) {
            throw new ICVerificationException("Filename not provided");
        }
        
        
        File dir = we.getWorkingDirectoryFile(we.getConfiguration().saveDir);
        File f;
        try {
        	//null argument is lame
            f = we.getSafeOpenFile((LocalPlayer)null, dir, filename, "schematic", new String[] {"schematic"});
        } catch (FilenameException e) {
            throw new ICVerificationException("Unable to open schematic: " + e.toString());
        }
        

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                throw new ICVerificationException("Schematic could not read or it does not exist.");
            } else {
                return f;
            }
        } catch (IOException e) {
            throw new ICVerificationException("Schematic could not read or it does not exist: " + e.getMessage());
        }
    }
    
    
    private CuboidClipboard getCuboid() throws ICVerificationException, IOException, DataException {
        //This could be cached.
        File f = getFile(getServer(), getSign().getLine(2));
        CuboidClipboard ret = CuboidClipboard.loadSchematic(f);
        return ret;
    }

    @Override
    public void trigger(ChipState chip) {
        if (!(risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0)))) {
            return;
        }
        Location L = SignUtil.getBackBlock(getSign().getBlock()).getLocation();
        Vector loc = new Vector(L.getBlockX(), L.getBlockY()+1, L.getBlockZ());
        
        String line3 = getSign().getLine(3).toUpperCase();
        boolean pasteAtOrigin = false;
        if (line3.length() > 0 && line3.charAt(0) == '0') {
        	pasteAtOrigin = true;
        	line3 = line3.substring(1).trim();
        }
        
        
    	CuboidClipboard cb;
        try {
            cb = getCuboid();
        } catch (Throwable t) {
            return;
        }
        EditSession editSession = new EditSession(new BukkitWorld(getSign().getWorld()), 500000);
        
        try {
	        boolean noAir = line3 == "NOAIR";
	        if (noAir || line3.length() == 0) {
	        	//Use old world edit method
            	if (pasteAtOrigin) {
            		cb.place(editSession, cb.getOrigin(), noAir);
            	}
            	else {
            		cb.paste(editSession, loc, noAir);
            	}
	        }
	        else {
	        	//Use new world edit paste with boolean ops
	        	//NOTE: Comment this out if your WorldEdit isn't awesome enough
	        	boolean reverse = false;
	        	if (line3.length() > 1 && line3.charAt(0) == '~') {
	        		reverse = true;
	        		line3 = line3.substring(1).trim();
	        	}
	        	EditSession.BooleanOperation op = EditSession.BooleanOperation.valueOf(line3);
	        	if (pasteAtOrigin) {
	        		cb.place(editSession, cb.getOrigin(), op, reverse);
	        	}
	        	else {
	        		cb.paste(editSession, cb.getOrigin(), op, reverse);
	        	}
	        }
        }
        catch (MaxChangedBlocksException e) {
        	//Does it undo?
            return;
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new SchematicPaster(getServer(), sign, risingEdge);
        }
        
        @Override
        public void verify(Sign sign) throws ICVerificationException {
            SchematicPaster.getFile(getServer(), sign.getLine(2));
        }
    }
}


