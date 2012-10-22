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
    }

    @Override
    public void load() {

        matBoostMax = Material.getMaterial(getInt("max-boost-block", 41));
        matBoost25x = Material.getMaterial(getInt("25x-boost-block", 14));
        matSlow50x = Material.getMaterial(getInt("50x-slow-block", 88));
        matSlow20x = Material.getMaterial(getInt("20x-slow-block", 13));
        matReverse = Material.getMaterial(getInt("reverse-block", 35));
        matStation = Material.getMaterial(getInt("station-block", 49));
        matSorter = Material.getMaterial(getInt("sort-block", 87));
        matEjector = Material.getMaterial(getInt("eject-block", 42));
        matDeposit = Material.getMaterial(getInt("deposit-block", 15));
        matTeleport = Material.getMaterial(getInt("teleport-block", 133));
        matDispenser = Material.getMaterial(getInt("dispenser-block", 129));
        matMessager = Material.getMaterial(getInt("messager-block", 121));

        minecartEnterOnImpact = getBoolean("minecart-enter-on-impact", true);
        minecartSlowWhenEmpty = getBoolean("minecart-slow-when-empty", true);
        minecartDecayWhenEmpty = getBoolean("minecart-decay-when-empty", false);
        minecartRemoveOnExit = getBoolean("minecart-remove-on-exit", false);
        minecartRemoveEntities = getBoolean("minecart-remove-entities", false);
        minecartRemoveEntitiesOtherCarts = getBoolean("minecart-remove-entities-othercarts", false);
        minecartMaxSpeedModifier = getDouble("minecart-max-speed-modifier", 1);

        boatRemoveEntities = getBoolean("boat-remove-entities", false);
        boatNoCrash = getBoolean("boat-no-crash", false);
        boatRemoveEntitiesOtherBoats = getBoolean("boat-remove-entities-otherboats", false);
        boatBreakReturn = getBoolean("boat-break-return-boat", false);
        minecartTrackMessages = getBoolean("minecart-track-messages", true);

        minecartDecayTime = getInt("minecart-decay-time", 20);
    }

    public final File dataFolder;

    public Material matBoostMax;
    public Material matBoost25x;
    public Material matSlow50x;
    public Material matSlow20x;
    public Material matReverse;
    public Material matStation;
    public Material matSorter;
    public Material matEjector;
    public Material matDeposit;
    public Material matTeleport;
    public Material matDispenser;
    public Material matMessager;

    public boolean minecartSlowWhenEmpty;
    public boolean minecartRemoveOnExit;
    public boolean minecartRemoveEntities;
    public boolean minecartRemoveEntitiesOtherCarts;
    public double minecartMaxSpeedModifier;
    public boolean minecartTrackMessages;
    public boolean minecartDecayWhenEmpty;
    public boolean minecartEnterOnImpact;

    public boolean boatNoCrash;
    public boolean boatRemoveEntities;
    public boolean boatRemoveEntitiesOtherBoats;
    public boolean boatBreakReturn;

    public int minecartDecayTime;

}