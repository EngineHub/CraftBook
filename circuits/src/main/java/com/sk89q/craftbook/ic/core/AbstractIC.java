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

package com.sk89q.craftbook.ic.core;

import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * A base abstract IC that all ICs can inherit from.
 * 
 * @author sk89q
 */
public abstract class AbstractIC implements IC {
    
    private Server server;
    private Sign sign;
    
    public AbstractIC(Server server, Sign block) {
        this.server = server;
        this.sign = block;
    }
    
    protected Server getServer() {
        return server;
    }
    
    protected Sign getSign() {
        return sign;
    }

    @Override
    public void unload() {
    }

}
