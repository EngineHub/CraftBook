/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.translation.TranslationManager;
import org.slf4j.LoggerFactory;

public class CraftBook {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(CraftBook.class);

    private static final CraftBook instance = new CraftBook();

    private CraftBookPlatform platform;
    private final ResourceLoader resourceLoader = new CraftBookResourceLoader();
    private final TranslationManager translationManager = new TranslationManager(resourceLoader);

    private final Supervisor supervisor = new SimpleSupervisor();

    public static CraftBook getInstance() {
        return instance;
    }

    public void setup() {
        getPlatform().load();
    }

    /**
     * The WorldGuard Platform.
     *
     * @return The platform
     */
    public CraftBookPlatform getPlatform() {
        checkNotNull(this.platform);
        return this.platform;
    }

    public void setPlatform(CraftBookPlatform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    /**
     * Gets the CraftBook {@link Supervisor}.
     *
     * @return The supervisor
     */
    public Supervisor getSupervisor() {
        return this.supervisor;
    }

    /**
     * Gets the Translation Manager.
     *
     * @return The translation manager
     */
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

}
