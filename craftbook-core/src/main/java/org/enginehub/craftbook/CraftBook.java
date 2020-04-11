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
package org.enginehub.craftbook;

import static com.google.common.base.Preconditions.checkNotNull;

import org.enginehub.craftbook.internal.platform.CraftBookPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point and container for a working implementation of CraftBook.
 */
public final class CraftBook {

    public static final Logger logger = LoggerFactory.getLogger(CraftBook.class);

    private static final CraftBook instance = new CraftBook();
    private static String version;

    private CraftBookPlatform platform;

    static {
        getVersion();
    }

    private CraftBook() {
    }

    /**
     * Gets the current instance of this class.
     *
     * @return an instance of CraftBook.
     */
    public static CraftBook getInstance() {
        return instance;
    }

    /**
     * The WorldGuard Platform.
     *
     * @return The platform
     */
    public CraftBookPlatform getPlatform() {
        checkNotNull(platform);
        return platform;
    }

    /**
     * For internal use only.
     *
     * @param platform The new platform.
     */
    public void setPlatform(CraftBookPlatform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    /**
     * Get the version.
     *
     * @return the version of CraftBook
     */
    public static String getVersion() {
        if (version != null) {
            return version;
        }

        CraftBookManifest manifest = CraftBookManifest.load();

        return version = manifest.getCraftBookVersion();
    }

}
