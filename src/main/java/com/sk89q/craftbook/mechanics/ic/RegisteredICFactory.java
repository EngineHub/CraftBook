// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic;

/**
 * Stores a mapping for a registered IC factory with its native family. This is used in {@link ICManager}.
 *
 * @author sk89q
 */
public class RegisteredICFactory {

    protected final String id, longId;
    protected final ICFactory factory;
    protected final ICFamily[] family;

    /**
     * Construct the object.
     *
     * @param id
     * @param factory
     * @param family
     */
    public RegisteredICFactory(String id, String longId, ICFactory factory, ICFamily... family) {

        this.id = id;
        this.longId = longId;
        this.factory = factory;
        this.family = family;
    }

    public String getId() {

        return id;
    }

    public ICFactory getFactory() {

        return factory;
    }

    public ICFamily[] getFamilies() {

        return family;
    }

    public String getShorthand() {

        return longId;
    }
}
