package com.sk89q.craftbook.gates.world;

import net.minecraft.server.Packet61WorldEvent;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class ParticleEffect extends AbstractIC {

    public ParticleEffect(Server server, Sign sign) {
        super(server, sign);
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
            doEffect(chip);
        }
    }

    public void doEffect(ChipState chip)
    {
        try
        {
            int effectID = Integer.parseInt(getSign().getLine(2).split(":")[0]);
            int effectData;
            try {
                effectData = Integer.parseInt(getSign().getLine(2).split(":")[1]);
            }
            catch(Exception e){
                effectData = 0;
            }
            if(effectID == 2001 && Material.getMaterial(effectData) == null) return;
            int times = Integer.parseInt(getSign().getLine(3));
            Block b = SignUtil.getBackBlock(getSign().getBlock());
            for(int i = 0; i < times; i++)
                ((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), 50, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet61WorldEvent(effectID, b.getX(), b.getY()+1,b.getZ(),effectData));
        }
        catch(Exception e){}
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new ParticleEffect(getServer(), sign);
        }
    }
}
