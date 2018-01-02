/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.factory;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;

public abstract class SerializedICFactory<T extends IC, D extends SerializedICData> extends AbstractDataBuilder<D> implements ICFactory<T> {

    private Class<D> requiredClass;

    public SerializedICFactory(Class<D> requiredClass, int supportedVersion) {
        super(requiredClass, supportedVersion);

        this.requiredClass = requiredClass;
    }

    public Class<D> getRequiredClass() {
        return this.requiredClass;
    }

    public abstract void setData(T ic, D data);

    public abstract D getData(T ic);
}
