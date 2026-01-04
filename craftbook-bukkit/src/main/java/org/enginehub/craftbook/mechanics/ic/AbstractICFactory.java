/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.ic;

import org.bukkit.Server;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    public void verify(BukkitChangedSign sign) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
        // TODO make some IC's use this to check if its valid.
    }

    @Override
    public void checkPlayer(BukkitChangedSign sign, CraftBookPlayer player) throws ICVerificationException {
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

        return new String[] { "Missing Description" };
    }

    @Override
    public String[] getLineHelp() {

        return new String[] { null, null };
    }

    @Override
    public String[] getPinDescription(ChipState state) {

        return new String[state.getInputCount() + state.getOutputCount()];
    }

    @Override
    public void load() {
        if (this instanceof PersistentDataIC && ICMechanic.instance.savePersistentData) {
            try {
                if (((PersistentDataIC) this).getStorageFile().exists())
                    ((PersistentDataIC) this).loadPersistentData(new DataInputStream(new FileInputStream(((PersistentDataIC) this).getStorageFile())));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                CraftBook.LOGGER.error("An invalid ic save file was found!", e);
            }
        }
    }

    @Override
    public void unload() {

    }
}