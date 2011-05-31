// $Id$
/*
 * Copyright (C) 2011 purpleposeidon@gmail.com
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
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Hashtable;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.ic.ICVerificationException;

public class SimpleSong extends AbstractIC {
    protected boolean risingEdge;
    protected int maxIndex;
    
    
    public SimpleSong(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
		maxIndex = 0;
    }

    @Override
    public String getTitle() {
        return "Simple Song Player";
    }

    @Override
    public String getSignTitle() {
        return "SIMPLE SONG";
    }

    public int getIndex() {
        Integer ret = 0;
        try {
            ret = Integer.parseInt(getSign().getLine(3));
        }
        catch (NumberFormatException e) {
            ret = 0;
        }
        if (ret > maxIndex) {
            ret = 0;
        }
        return ret;
    }
    
    public void setIndex(int index) {
        getSign().setLine(3, ""+index);
    }
    
    @Override
    public void trigger(ChipState chip) {
    	if (!risingEdge) return;
    	String name = getSign().getLine(2);
    	byte[] source;
    	try {
			source = Song.get(name);
			maxIndex = source.length;
		} catch (FileNotFoundException e) {
			return;
		}
		
        if (chip.isValid(1) ? chip.get(1) : chip.get(2)) {
        	if (source[getIndex()] == -1) {
        		Song.refresh(name);
        		setIndex(0);
        		return;
        	}
        }
        if (chip.get(0)) {
            Block above = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, 1, 0);
            if (above.getType() != Material.NOTE_BLOCK) return;
            int index = getIndex();
            if (source[index] == -1) {
                //end of song
                chip.setOutput(0, true);
                return;
            }
            else {
                chip.setOutput(0, false);
            }
            org.bukkit.block.NoteBlock noteState = (org.bukkit.block.NoteBlock)above.getState();
            byte sound = source[index];
            if (sound != 0) {
            	sound--;
            	noteState.setNote(sound);
            	noteState.play();
            }
            setIndex(index+1);
        }
    }

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;
    	
        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new SimpleSong(getServer(), sign, risingEdge);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {
            Block above = SignUtil.getBackBlock(sign.getBlock()).getRelative(0, 1, 0);
            if (above.getType() != Material.NOTE_BLOCK) {
                throw new ICVerificationException("There must be a noteblock above the sign's block");
            }
            if (sign.getLine(2).length() == 0) throw new ICVerificationException("No song given");
            try {
            	Song.get(sign.getLine(2));
            }
            catch (FileNotFoundException e) {
            	throw new ICVerificationException("Unknown song name"); 
            }
        }

    }

    
    static private class Song {
    	protected static final int maxLength = 1024;
    	protected static Hashtable<String, byte[]> songsTable = new Hashtable<String, byte[]>();
    	protected static Hashtable<String, Long> refreshTable = new Hashtable<String, Long>();
    	
    	public static byte[] get(String name) throws FileNotFoundException {
    		byte[] ret = songsTable.get(name);
    		if (ret != null) return ret;
    		ret = loadSong(name);
    		if (ret != null) return ret;
    		throw new FileNotFoundException();
    	}
    	
    	public static void refresh(String name) {
    		Long lastRefresh = refreshTable.get(name);
    		if (lastRefresh == null || lastRefresh + 2*1000 > System.currentTimeMillis()) return;
    		try {
				songsTable.put(name, loadSong(name));
				refreshTable.put(name, System.currentTimeMillis());
			} catch (FileNotFoundException e) {
				songsTable.remove(name);
			}
    	}
    	
        static private String getSongFileName(String filename) {
            if (filename.contains("/") | filename.contains(".")) return "";
            filename = "songs/"+filename+".txt";
            File f = new File(filename);
            if (f.exists()) {
                return filename;
            }
            return "";
        }
        
        static private byte[] loadSong(String name) throws FileNotFoundException {
        	Scanner scanner = new Scanner(new File(getSongFileName(name)));
        	byte[] songData = new byte[maxLength];
        	int i = 0;
            while(scanner.hasNextByte() && i < maxLength){
               songData[i++] = scanner.nextByte();
            }
            songData[i] = -1;
            return songData;
        }
    }

}
