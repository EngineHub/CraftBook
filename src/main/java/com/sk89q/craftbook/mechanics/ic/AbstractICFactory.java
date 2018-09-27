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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

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
    public void verify(ChangedSign sign) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
        // TODO make some IC's use this to check if its valid.
    }

    @Override
    public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
        // TODO Use this to make some restricted IC's allowed to normal users, but limited.
    }

    @Override
    public String getShortDescription() {

        return "No Description.";
    }

    @Override
    public String[] getLongDescription() {

        return new String[]{"Missing Description"};
    }

    @Override
    public String[] getLineHelp() {

        return new String[] {null, null};
    }

    @Override
    public String[] getPinDescription(ChipState state) {

        return new String[state.getInputCount() + state.getOutputCount()];
    }

    @Override
    public void load() {
        if(this instanceof PersistentDataIC && ICMechanic.instance.savePersistentData) {
            try {
                if(((PersistentDataIC) this).getStorageFile().exists())
                    ((PersistentDataIC) this).loadPersistentData(new DataInputStream(new FileInputStream(((PersistentDataIC) this).getStorageFile())));
            } catch (FileNotFoundException e) {
                CraftBookBukkitUtil.printStacktrace(e);
            } catch (IOException e) {
                CraftBookPlugin.logger().severe("An invalid ic save file was found!");
                CraftBookBukkitUtil.printStacktrace(e);
            }
        }
    }

    @Override
    public void unload() {

    }
}