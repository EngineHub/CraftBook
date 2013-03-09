package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Effect;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * @author Me4502
 */
public class ParticleEffect extends AbstractIC implements SelfTriggeredIC {

    public ParticleEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            doEffect();
        }
    }

    int effectID;
    int effectData;
    int times;
    Vector offset;

    @Override
    public void load() {

        String[] eff = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[0], 2);
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
        } catch (Exception ignored) {
            times = 1;
        }

        try {
            String[] off = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1], 2);
            offset = new Vector(Double.parseDouble(off[0]), Double.parseDouble(off[1]), Double.parseDouble(off[2]));
        } catch (Exception e) {
            offset = new Vector(0, 1, 0);
        }
    }

    public void doEffect() {

        try {
            if (effectID == 0) return;
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

            return new ParticleEffect(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Creates particle effects.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"effectID:effectData=xOff:yOff:zOff", "amount of particles"};
            return lines;
        }
    }
}
