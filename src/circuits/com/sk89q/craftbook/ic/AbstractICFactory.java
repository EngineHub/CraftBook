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

package com.sk89q.craftbook.ic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Abstract IC factory.
 * 
 * @author sk89q
 */
public abstract class AbstractICFactory implements ICFactory {
    
    private Server server;
    
    public AbstractICFactory(Server server) {
        this.server = server;
    }
    
    protected Server getServer() {
        return server;
    }

    public void verify(Sign sign) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
    }
}
