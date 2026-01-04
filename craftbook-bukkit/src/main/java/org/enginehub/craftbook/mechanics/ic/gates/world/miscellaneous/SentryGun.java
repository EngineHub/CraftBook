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

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.Set;

public class SentryGun extends AbstractSelfTriggeredIC {

    private Set<EntityType> types;
    private SearchArea area;
    private float speed;
    private boolean manned;

    public SentryGun(Server server, BukkitChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        speed = 0.8f;
        types = EntityType.getDetected(getLine(2).split(":")[0]);
        if (types == null || types.isEmpty()) {
            types = Set.of(EntityType.MOB_HOSTILE);
        }
        if (getLine(2).split(":").length > 1)
            speed = Float.parseFloat(getLine(2).split(":")[1]);
        area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
        manned = getLine(2).split(":").length > 2 && getLine(2).split(":")[2].equalsIgnoreCase("MAN");
    }

    @Override
    public String getTitle() {

        return "Sentry Gun";
    }

    @Override
    public String getSignTitle() {

        return "SENTRY GUN";
    }

    @Override
    public void trigger(ChipState chip) {

        shoot();
    }

    @Override
    public void think(ChipState chip) {

        if (((Factory) getFactory()).inverted == chip.getInput(0)) {
            trigger(chip);
        }
    }

    public void shoot() {

        Player shooter = manned ? getShootingPlayer() : null;
        if (shooter != null) {
            Arrow ar = area.getWorld().spawnArrow(BlockUtil.getBlockCentre(area.getCenter() == null ? area.getCenter().getBlock() : getBackBlock()).add(0, 1, 0), shooter.getLocation().getDirection().normalize(), speed, 0);
            ar.setShooter(shooter);
            ar.setTicksLived(2500);
        } else {
            for (Entity ent : area.getEntitiesInArea()) {
                if (!(ent instanceof LivingEntity)) continue;
                boolean hasFound = false;
                for (EntityType type : types) {
                    if (type.is(ent)) {
                        hasFound = true;
                        break;
                    }
                }

                if (hasFound) {
                    double yOff = ((LivingEntity) ent).getEyeHeight();
                    Location k = LocationUtil.getBlockCentreTop(LocationUtil.getNextFreeSpace(getBackBlock(), BlockFace.UP));
                    Arrow ar = area.getWorld().spawnArrow(k, ent.getLocation().add(0, yOff, 0).subtract(k.clone().add(0.5, 0.5, 0.5)).toVector().normalize(), speed, 0);
                    if (!((LivingEntity) ent).hasLineOfSight(ar)) {
                        ar.remove();
                        continue;
                    }
                    break;
                }
            }
        }
    }

    public Player getShootingPlayer() {

        Block b = getBackBlock().getRelative(0, 1, 0);
        for (Entity ent : b.getWorld().getNearbyEntities(BoundingBox.of(b))) {
            if (ent instanceof Player)
                return (Player) ent;
        }

        return null;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        public boolean inverted = false;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new SentryGun(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby mobs with arrows.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "Mob Type{:power:MAN}", "SearchArea" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            inverted = config.getBoolean("inverted", false);
        }
    }
}