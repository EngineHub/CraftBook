// $Id$r
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

package com.sk89q.craftbook;


import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * FileConfiguration handler for CraftBook.
 * All fields are final because it is never appropriate to modify them during
 * operation, except for when the FileConfiguration is reloaded entirely, at which
 * point it is appropriate to construct an entirely new FileConfiguration instance
 * and update the plugin accordingly.
 *
 * @author sk89q
 * @author hash
 */
public class VehiclesConfiguration extends BaseConfiguration {

    public VehiclesConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
        this.dataFolder = dataFolder;

        matBoostMax = Material.getMaterial(getInt(cfg, "max-boost-block", 41));
        matBoost25x = Material.getMaterial(getInt(cfg, "25x-boost-block", 14));
        matSlow50x = Material.getMaterial(getInt(cfg, "50x-slow-block", 88));
        matSlow20x = Material.getMaterial(getInt(cfg, "20x-slow-block", 13));
        matReverse = Material.getMaterial(getInt(cfg, "reverse-block", 35));
        matStation = Material.getMaterial(getInt(cfg, "station-block", 49));
        matSorter = Material.getMaterial(getInt(cfg, "sort-block", 87));
        matEjector = Material.getMaterial(getInt(cfg, "eject-block", 42));
        matDeposit = Material.getMaterial(getInt(cfg, "deposit-block", 15));
        matTeleport = Material.getMaterial(getInt(cfg, "teleport-block", 89));
        matDispenser = Material.getMaterial(54);     // this can't be configurable because we need it to be a chest!
        matMessager = Material.getMaterial(getInt(cfg, "messager-block", 121));

        minecartEnterOnImpact = getBoolean(cfg, "minecart-enter-on-impact", true);
        minecartSlowWhenEmpty = getBoolean(cfg, "minecart-slow-when-empty", true);
        minecartDecayWhenEmpty = getBoolean(cfg, "minecart-decay-when-empty", false);
        minecartRemoveOnExit = getBoolean(cfg, "minecart-remove-on-exit", false);
        minecartRemoveEntities = getBoolean(cfg, "minecart-remove-entities", false);
        minecartRemoveEntitiesOtherCarts = getBoolean(cfg, "minecart-remove-"
                + "entities-othercarts", false);
        minecartMaxSpeedModifier = getDouble(cfg, "minecart-max-speed-modifier", 1);

        boatRemoveEntities = getBoolean(cfg, "boat-remove-entities", false);
        boatRemoveEntitiesOtherBoats = getBoolean(cfg, "boat-remove-"
                + "entities-otherboats", false);
        boatBreakReturn = getBoolean(cfg, "boat-break-return-boat", false);
        minecartTrackMessages = getBoolean(cfg, "minecart-track-messages", true);
    }

    public final File dataFolder;

    public final Material matBoostMax;
    public final Material matBoost25x;
    public final Material matSlow50x;
    public final Material matSlow20x;
    public final Material matReverse;
    public final Material matStation;
    public final Material matSorter;
    public final Material matEjector;
    public final Material matDeposit;
    public final Material matTeleport;
    public final Material matDispenser;
    public final Material matMessager;

    public final boolean minecartSlowWhenEmpty;
    public final boolean minecartRemoveOnExit;
    public final boolean minecartRemoveEntities;
    public final boolean minecartRemoveEntitiesOtherCarts;
    public final double minecartMaxSpeedModifier;
    public final boolean minecartTrackMessages;
    public final boolean minecartDecayWhenEmpty;
    public final boolean minecartEnterOnImpact;

    public final boolean boatRemoveEntities;
    public final boolean boatRemoveEntitiesOtherBoats;
    public final boolean boatBreakReturn;
}
