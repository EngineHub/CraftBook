package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

public class SentryGun extends AbstractSelfTriggeredIC {

    private Set<EntityType> types;
    private SearchArea area;
    private float speed;
    private boolean manned;

    public SentryGun(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        speed = 0.8f;
        types = EntityType.getDetected(getSign().getLine(2).split(":")[0]);
        if(types == null || types.isEmpty()) {
            types = EnumSet.of(EntityType.MOB_HOSTILE);
        }
        if(getSign().getLine(2).split(":").length > 1)
            speed = Float.parseFloat(getSign().getLine(2).split(":")[1]);
        area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
        manned = getSign().getLine(2).split(":").length > 2 && getSign().getLine(2).split(":")[2].equalsIgnoreCase("MAN");
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
        if(shooter != null) {
            Arrow ar = area.getWorld().spawnArrow(BlockUtil.getBlockCentre(area.getCenter() == null ? area.getCenter().getBlock() : getBackBlock()).add(0, 1, 0), shooter.getLocation().getDirection().normalize(), speed, 0);
            ar.setShooter(shooter);
            ar.setTicksLived(2500);
        } else {
            for (Entity ent : area.getEntitiesInArea()) {
                if(!(ent instanceof LivingEntity)) continue;
                boolean hasFound = false;
                for(EntityType type : types) {
                    if(type.is(ent)) {
                        hasFound = true;
                        break;
                    }
                }

                if (hasFound) {
                    double yOff = ((LivingEntity) ent).getEyeHeight();
                    Location k = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(getBackBlock(), BlockFace.UP));
                    Arrow ar = area.getWorld().spawnArrow(k, ent.getLocation().add(0, yOff, 0).subtract(k.clone().add(0.5,0.5,0.5)).toVector().normalize(), speed, 0);
                    if(!((LivingEntity)ent).hasLineOfSight(ar)) {
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
        for(Entity ent : LocationUtil.getNearbyEntities(BlockUtil.getBlockCentre(b), Vector3.at(2, 2, 2))) {
            if(EntityUtil.isEntityInBlock(ent, b) && ent instanceof Player)
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
        public IC create(ChangedSign sign) {

            return new SentryGun(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby mobs with arrows.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Mob Type{:power:MAN}", "SearchArea"};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            inverted = config.getBoolean(path + "inverted", false);
        }
    }
}