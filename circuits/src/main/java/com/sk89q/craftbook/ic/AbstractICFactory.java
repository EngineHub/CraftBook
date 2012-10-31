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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import com.sk89q.craftbook.LocalPlayer;

/**
 * Abstract IC factory.
 *
 * @author sk89q
 */
public abstract class AbstractICFactory implements ICFactory {

    private final Server server;

    public AbstractICFactory(Server server) {

        this.server = server;
    }

    protected Server getServer() {

        return server;
    }

    @Override
    public void verify(Sign sign) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
        //TODO make some IC's use this to check if its valid.
    }

    @Override
    public void checkPlayer(Sign sign, LocalPlayer player) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
        //TODO Use this to make some restricted IC's allowed to normal users, but limited.
    }

    @Override
    public String getDescription() {

        return "No Description.";
    }

    @Override
    public String[] getLineHelp() {

        String[] lines = new String[] {
                null,
                null
        };
        return lines;
    }

    @Override
    public void addConfiguration(ConfigurationSection section) {

    }

    public int getInt(ConfigurationSection cfg, String name, int def) {

        int it = cfg.getInt(name, def);
        cfg.set(name, it);
        return it;
    }

    public double getDouble(ConfigurationSection cfg, String name, double def) {

        double it = cfg.getDouble(name, def);
        cfg.set(name, it);
        return it;
    }

    public boolean getBoolean(ConfigurationSection cfg, String name, boolean def) {

        boolean it = cfg.getBoolean(name, def);
        cfg.set(name, it);
        return it;
    }

    public String getString(ConfigurationSection cfg, String name, String def) {

        String it = cfg.getString(name, def);
        cfg.set(name, it);
        return it;
    }

    public List<String> getStringList(ConfigurationSection cfg, String name, List<String> def) {

        List<String> it = cfg.getStringList(name);
        if (it == null || it.size() == 0) {
            it = def;
        }
        cfg.set(name, it);
        return it;
    }

    public Set<Integer> getIntegerSet(ConfigurationSection cfg, String name, List<Integer> def) {

        List<Integer> tids = cfg.getIntegerList(name);
        if (tids == null || tids.isEmpty() || tids.size() < 1) {
            tids = def;
        }
        Set<Integer> allowedBlocks = new HashSet<Integer>();
        for (Integer tid : tids) {
            allowedBlocks.add(tid);
        }
        cfg.set(name, tids);
        return allowedBlocks;
    }

    public Set<Material> getMaterialSet(ConfigurationSection cfg, String name, List<Integer> def) {

        List<Integer> tids = cfg.getIntegerList(name);
        if (tids == null || tids.isEmpty() || tids.size() < 1) {
            tids = def;
        }
        Set<Material> allowedBlocks = new HashSet<Material>();
        for (Integer tid : tids) {
            allowedBlocks.add(Material.getMaterial(tid));
        }
        cfg.set(name, tids);
        return Collections.unmodifiableSet(allowedBlocks);
    }
}
