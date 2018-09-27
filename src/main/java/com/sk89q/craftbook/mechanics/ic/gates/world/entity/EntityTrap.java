package com.sk89q.craftbook.mechanics.ic.gates.world.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Me4502
 */
public class EntityTrap extends AbstractSelfTriggeredIC {

    public EntityTrap(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY TRAP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, hurt());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hurt());
    }

    SearchArea area;

    int damage;
    List<EntityType> types;
    boolean erase;

    @Override
    public void load() {

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));

        try {
            damage = Integer.parseInt(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[2]);
        } catch (Exception ignored) {
            damage = 2;
        }

        if (!getLine(3).isEmpty()) {
            types = new ArrayList<>(EntityType.getDetected(getLine(3)));
        } else
            types = Collections.singletonList(EntityType.MOB_HOSTILE);

        if(types.isEmpty())
            types.add(EntityType.ANY);
        erase = getLine(3).startsWith("!");
    }

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean hurt() {

        boolean hasHurt = false;

        for (Entity e : area.getEntitiesInArea(types)) {

            if(erase)
                e.remove();
            else
                EntityUtil.damageEntity(e, damage);
            hasHurt = true;
        }

        return hasHurt;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityTrap(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Damage nearby entities of type.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"radius=x:y:z=damage", "mob type"};
        }
    }
}