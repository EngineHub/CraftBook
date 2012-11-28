package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Effect;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.gates.world.blocks.SetDoor;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * @author Me4502
 */
public class ParticleEffect extends AbstractIC {

    public ParticleEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    @Override
    public String getTitle() {

        return "Particle Effect";
    }

    @Override
    public String getSignTitle() {

        return "PARTICLE EFFECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            doEffect();
        }
    }

    int effectID;
    int effectData;
    int times = 1;
    Vector offset;

    public void load() {
        try {
            String[] eff = ICUtil.COLON_PATTERN.split(ICUtil.EQUALS_PATTERN.split(getSign().getLine(2))[0], 2);
            try {
                effectID = Integer.parseInt(eff[0]);
            } catch (Exception e) {
                effectID = Effect.valueOf(eff[0]).getId();
            }
            if (Effect.getById(effectID) == null) return;
            try {
                effectData = Integer.parseInt(eff[1]);
            } catch (Exception e) {
                effectData = 0;
            }

            try {
                times = Integer.parseInt(getSign().getLine(3));
            }
            catch(Exception e){}

            String[] off = ICUtil.COLON_PATTERN.split(ICUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1], 2);
            offset = new Vector(Double.parseDouble(off[0]), Double.parseDouble(off[1]), Double.parseDouble(off[2]));
        }
        catch(Exception e){
            offset = new Vector(0,1,0);
        }
    }

    public void doEffect() {

        try {
            if (effectID == 0)
                return;
            if (effectID == 2001 && BlockType.fromID(effectData) == null) return;
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            for (int i = 0; i < times; i++) {
                b.getWorld().playEffect(b.getLocation().add(offset), Effect.getById(effectID), effectData, 50);
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            try {
                if (sign.getLine(0).equalsIgnoreCase("SET P-DOOR")) {
                    sign.setLine(1, "[MC1212]");
                    sign.update(false);
                    return new SetDoor(getServer(), sign, this);
                }
            } catch (Exception ignored) {
            }
            return new ParticleEffect(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Creates particle effects.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "effectID:effectData=xOff:yOff:zOff",
                    "amount of particles"
            };
            return lines;
        }
    }
}
