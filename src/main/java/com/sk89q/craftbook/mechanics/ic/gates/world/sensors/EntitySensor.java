package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.entity.Entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Silthus
 */
public class EntitySensor extends AbstractSelfTriggeredIC {

    private Set<EntityType> types;

    private SearchArea area;

    private short minimum;

    private short minMode;

    public EntitySensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        // lets get the types to detect first
        types = EntityType.getDetected(getLine(3).split("<")[0].trim().split("<=")[0].trim().split(">=")[0].trim().split("==")[0].trim().split(">")[0].trim());

        if(getLine(3).contains(">="))
            minMode = 0;
        else if (getLine(3).contains("=="))
            minMode = 1;
        else if (getLine(3).contains(">"))
            minMode = 2;
        else if (getLine(3).contains("<="))
            minMode = 3;
        else if (getLine(3).contains("<"))
            minMode = 4;
        else
            minMode = 0;

        try {
            if(minMode == 0)
                minimum = Short.parseShort(getLine(3).split(">=")[1].trim());
            else if(minMode == 1)
                minimum = Short.parseShort(getLine(3).split("==")[1].trim());
            else if(minMode == 2)
                minimum = Short.parseShort(getLine(3).split(">")[1].trim());
            else if(minMode == 3)
                minimum = Short.parseShort(getLine(3).split("<=")[1].trim());
            else
                minimum = Short.parseShort(getLine(3).split("<")[1].trim());
        } catch (Exception e) {
            minimum = 1;
        }

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Entity Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    protected boolean isDetected() {

        short cur = 0;

        for (Entity entity : area.getEntitiesInArea(types))
            if (entity.isValid())
                for (EntityType type : types) // Check Type
                    if (type.is(entity)) // Check Radius
                        cur++;

        if(minMode == 0 && cur >= minimum)
            return true;
        else if (minMode == 1 && cur == minimum)
            return true;
        else if (minMode == 2 && cur > minimum)
            return true;
        else if (minMode == 3 && cur <= minimum)
            return true;
        else if (minMode == 4 && cur < minimum)
            return true;

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntitySensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if(!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }

        @Override
        public String getShortDescription() {

            return "Detects specific entity types in a given radius.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"SearchArea", "Entity Types{(>=|==|>)minimum}"};
        }
    }
}