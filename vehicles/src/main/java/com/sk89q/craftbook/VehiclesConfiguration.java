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

        matBoostMax = getInt("max-boost-block", 41);
        matBoost25x = getInt("25x-boost-block", 14);
        matSlow50x = getInt("50x-slow-block", 88);
        matSlow20x = getInt("20x-slow-block", 13);
        matReverse = getInt("reverse-block", 35);
        matStation = getInt("station-block", 49);
        matSorter = getInt("sort-block", 87);
        matEjector = getInt("eject-block", 42);
        matDeposit = getInt("deposit-block", 15);
        matTeleport = getInt("teleport-block", 133);
        matLift = getInt("lift-block", 112);
        matDispenser = getInt("dispenser-block", 129);
        matMessager = getInt("messager-block", 121);

        minecartEnterOnImpact = getBoolean("minecart-enter-on-impact", true);
        minecartSlowWhenEmpty = getBoolean("minecart-slow-when-empty", true);
        minecartDecayWhenEmpty = getBoolean("minecart-decay-when-empty", false);
        minecartRemoveOnExit = getBoolean("minecart-remove-on-exit", false);
        minecartRemoveEntities = getBoolean("minecart-remove-entities", false);
        minecartRemoveEntitiesOtherCarts = getBoolean("minecart-remove-entities-othercarts", false);
        minecartMaxSpeedModifier = getDouble("minecart-max-speed-modifier", 1);
        minecartOffRailSpeedModifier = getDouble("minecart-off-rail-speed-modifier", 1);

        boatRemoveEntities = getBoolean("boat-remove-entities", false);
        boatNoCrash = getBoolean("boat-no-crash", false);
        boatRemoveEntitiesOtherBoats = getBoolean("boat-remove-entities-otherboats", false);
        boatBreakReturn = getBoolean("boat-break-return-boat", false);
        minecartTrackMessages = getBoolean("minecart-track-messages", true);

        minecartDecayTime = getInt("minecart-decay-time", 20);

        minecartConstantSpeed = getDouble("minecart-constant-speed", 0);
    }

    public final File dataFolder;

    public Integer matBoostMax;
    public Integer matBoost25x;
    public Integer matSlow50x;
    public Integer matSlow20x;
    public Integer matReverse;
    public Integer matStation;
    public Integer matSorter;
    public Integer matEjector;
    public Integer matDeposit;
    public Integer matTeleport;
    public Integer matLift;
    public Integer matDispenser;
    public Integer matMessager;

    public boolean minecartSlowWhenEmpty;
    public boolean minecartRemoveOnExit;
    public boolean minecartRemoveEntities;
    public boolean minecartRemoveEntitiesOtherCarts;
    public double minecartMaxSpeedModifier;
    public double minecartOffRailSpeedModifier;
    public boolean minecartTrackMessages;
    public boolean minecartDecayWhenEmpty;
    public boolean minecartEnterOnImpact;

    public boolean boatNoCrash;
    public boolean boatRemoveEntities;
    public boolean boatRemoveEntitiesOtherBoats;
    public boolean boatBreakReturn;

    public int minecartDecayTime;

    public double minecartConstantSpeed;

}