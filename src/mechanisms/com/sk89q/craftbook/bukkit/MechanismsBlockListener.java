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

package com.sk89q.craftbook.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.mech.Bookcase;

/**
 * Listener for block events.
 * 
 * @author sk89q
 */
public class MechanismsBlockListener extends BlockListener {
    
    protected MechanismsPlugin plugin;
    
    /**
     * Construct the object.
     * 
     * @param mechanismsPlugin
     */
    public MechanismsBlockListener(MechanismsPlugin mechanismsPlugin) {
        this.plugin = mechanismsPlugin;
    }
    
    /**
     * Called on block right click.
     */
    @Override
    public void onBlockRightClick(BlockRightClickEvent event) {
        Block block = event.getBlock();
        LocalPlayer player = plugin.wrap(event.getPlayer());
        
        if (block.getType() == Material.BOOKSHELF) {
            Bookcase bookcase = new Bookcase(BukkitUtil.toVector(block),
                    plugin.getLocalConfiguration());
            bookcase.read(player, plugin.getLocalConfiguration().bookcaseReadLine);
        }
    }
    
    /**
     * Called when a sign changes.
     */
    @Override
    public void onSignChange(SignChangeEvent event) {
    }
    
}
