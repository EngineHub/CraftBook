// $Id$r
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

package com.sk89q.craftbook;

import com.sk89q.craftbook.util.ItemInfo;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * FileConfiguration handler for CraftBook. All fields are final because it is never appropriate to modify them
 * during operation, except for when the
 * FileConfiguration is reloaded entirely, at which point it is appropriate to construct an entirely new
 * FileConfiguration instance and update the
 * plugin accordingly.
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

        matBoostMax = ItemInfo.parseFromString(getString("max-boost-block", "41:0"));
        matBoost25x = ItemInfo.parseFromString(getString("25x-boost-block", "14:0"));
        matSlow50x = ItemInfo.parseFromString(getString("50x-slow-block", "88:0"));
        matSlow20x = ItemInfo.parseFromString(getString("20x-slow-block", "13:0"));
        matReverse = ItemInfo.parseFromString(getString("reverse-block", "35:0"));
        matStation = ItemInfo.parseFromString(getString("station-block", "49:0"));
        matSorter = ItemInfo.parseFromString(getString("sort-block", "87:0"));
        matEjector = ItemInfo.parseFromString(getString("eject-block", "42:0"));
        matDeposit = ItemInfo.parseFromString(getString("deposit-block", "15:0"));
        matTeleport = ItemInfo.parseFromString(getString("teleport-block", "133:0"));
        matLift = ItemInfo.parseFromString(getString("lift-block", "112:0"));
        matDispenser = ItemInfo.parseFromString(getString("dispenser-block", "129:0"));
        matMessager = ItemInfo.parseFromString(getString("messager-block", "121:0"));

        minecartEnterOnImpact = getBoolean("minecart-enter-on-impact", true);
        minecartSlowWhenEmpty = getBoolean("minecart-slow-when-empty", true);
        minecartDecayWhenEmpty = getBoolean("minecart-decay-when-empty", false);
        minecartRemoveOnExit = getBoolean("minecart-remove-on-exit", false);
        minecartRemoveEntities = getBoolean("minecart-remove-entities", false);
        minecartRemoveEntitiesOtherCarts = getBoolean("minecart-remove-entities-othercarts", false);
        minecartMaxSpeedModifier = getDouble("minecart-max-speed-modifier", 1);
        minecartOffRailSpeedModifier = getDouble("minecart-off-rail-speed-modifier", 0);

        boatRemoveEntities = getBoolean("boat-remove-entities", false);
        boatNoCrash = getBoolean("boat-no-crash", false);
        boatRemoveEntitiesOtherBoats = getBoolean("boat-remove-entities-otherboats", false);
        boatBreakReturn = getBoolean("boat-break-return-boat", false);
        minecartTrackMessages = getBoolean("minecart-track-messages", true);

        minecartDecayTime = getInt("minecart-decay-time", 20);

        minecartConstantSpeed = getDouble("minecart-constant-speed", 0);
    }

    public final File dataFolder;

    public ItemInfo matBoostMax;
    public ItemInfo matBoost25x;
    public ItemInfo matSlow50x;
    public ItemInfo matSlow20x;
    public ItemInfo matReverse;
    public ItemInfo matStation;
    public ItemInfo matSorter;
    public ItemInfo matEjector;
    public ItemInfo matDeposit;
    public ItemInfo matTeleport;
    public ItemInfo matLift;
    public ItemInfo matDispenser;
    public ItemInfo matMessager;

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