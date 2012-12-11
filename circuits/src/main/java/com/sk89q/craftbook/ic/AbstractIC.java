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

import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;

/**
 * A base abstract IC that all ICs can inherit from.
 *
 * @author sk89q
 */
public abstract class AbstractIC implements IC {

    private static final Pattern LEFT_BRACKET_PATTERN = Pattern.compile("[", Pattern.LITERAL);
    private static final Pattern RIGHT_BRACKET_PATTERN = Pattern.compile("]", Pattern.LITERAL);
    private final Server server;
    private final ChangedSign sign;
    private final ICFactory factory;

    public AbstractIC(Server server, ChangedSign sign, ICFactory factory) {

        this.factory = factory;
        this.server = server;
        this.sign = sign;
        if(sign != null)
            load();
    }

    protected Server getServer() {

        return server;
    }

    protected ChangedSign getSign() {

        return sign;
    }

    protected String getLine(int line) {

        return sign.getLine(line);
    }

    protected ICFactory getFactory() {

        return factory;
    }

    protected CircuitsPlugin getPlugin() {

        return CircuitsPlugin.getInst();
    }

    @Override
    public void onRightClick(Player p) {
        if(p.isSneaking()) {
            CircuitsPlugin.getInst().generateICDocs(p, RIGHT_BRACKET_PATTERN.split(LEFT_BRACKET_PATTERN.split(getSign().getLine(1))[1])[0]);
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public void load() {

    }
}