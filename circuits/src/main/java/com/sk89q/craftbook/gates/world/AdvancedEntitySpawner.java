package com.sk89q.craftbook.gates.world;

import net.minecraft.server.Enchantment;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class AdvancedEntitySpawner extends CreatureSpawner {

    public AdvancedEntitySpawner(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
        load();
    }

    Location location;
    EntityType type = EntityType.PIG;
    int amount = 1;

    @Override
    public String getTitle() {
        return "Advanced Entity Spawner";
    }

    @Override
    public String getSignTitle() {
        return "ADV ENT SPAWNER";
    }

    public void load() {

        type = EntityType.fromName(getSign().getLine(3).trim().split("\\*")[0]);

        try {
            amount = Integer.parseInt(getSign().getLine(3).trim().split("\\*")[1]);
        }
        catch(Exception e) {
            amount = 1;
        }

        try {
            double x, y, z;
            x = Double.parseDouble(getSign().getLine(2).split(":")[0]);
            y = Double.parseDouble(getSign().getLine(2).split(":")[1]);
            z = Double.parseDouble(getSign().getLine(2).split(":")[2]);
            x += getSign().getX();
            y += getSign().getY();
            z += getSign().getZ();
            location = new Location(BukkitUtil.getLocalWorld(BukkitUtil.toSign(getSign()).getWorld()), new Vector(x,y,z));
        }
        catch(Exception e){
            location = new Location(BukkitUtil.getLocalWorld(BukkitUtil.toSign(getSign()).getWorld()),
                    new Vector(getSign().getX(),getSign().getY(),getSign().getZ()));
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if(!chip.getInput(0))
            return;
        Block left = SignUtil.getLeftBlock(BukkitUtil.toSign(getSign()).getBlock());
        ChangedSign effectSign = null;
        if(left.getTypeId() == BlockID.WALL_SIGN) {
            effectSign = BukkitUtil.toChangedSign((Sign) left.getState());
        }

        Block right = SignUtil.getRightBlock(BukkitUtil.toSign(getSign()).getBlock());
        ChangedSign armourSign = null;
        if(right.getTypeId() == BlockID.WALL_SIGN) {
            armourSign = BukkitUtil.toChangedSign((Sign) right.getState());
        }

        for(int i = 0; i < amount; i++) {
            Entity ent = BukkitUtil.toSign(getSign()).getWorld().spawnEntity(BukkitUtil.toLocation(location), type);

            if(armourSign != null) { //Apply armor
                if(ent instanceof LivingEntity) {
                    CraftLivingEntity cle = (CraftLivingEntity) ent;
                    EntityLiving eliv = cle.getHandle();

                    for(int s = 0; s < 4; s++) {
                        try {
                            String bit = armourSign.getLine(s);
                            if(bit == null || bit.trim().length() == 0)
                                continue;

                            byte data = 0;
                            try {
                                data = Byte.parseByte(bit.split(";")[0].split(":")[1]);
                            }
                            catch(Exception e){}

                            ItemStack slot = new ItemStack(Item.byId[Integer.parseInt(bit.split(";")[0].split(":")[0])], 1, data);
                            try {
                                for(int e = 1; e < bit.split(";").length; e++) {
                                    slot.addEnchantment(Enchantment.byId[Integer.parseInt(bit.split(";")[e].split(":")[0])],
                                            Integer.parseInt(bit.split(";")[e].split(":")[1]));
                                }
                            }
                            catch(Exception e){}
                            eliv.setEquipment(s + 1, slot);
                        }
                        catch(Exception e){}
                    }
                }
            }

            if(effectSign != null) { //Apply effects
                for(int s = 0; s < 4; s++) {
                    try {
                        String bit = effectSign.getLine(s);
                        if(bit == null || bit.trim().length() == 0)
                            continue;

                        String[] data = bit.split(":");

                        if(data[0].equalsIgnoreCase("e"))
                            setEntityData(ent, bit.replace(data[0] + ":", ""));
                        else if(data[0].equalsIgnoreCase("p") && ent instanceof LivingEntity) {
                            for(int a = 1; a < data.length; a++) {
                                try {
                                    String[] potionBits = data[a].split(";");
                                    PotionEffect effect = new PotionEffect(PotionEffectType.getById(Integer.parseInt(potionBits[0])),
                                            Integer.parseInt(potionBits[1]),Integer.parseInt(potionBits[2]));
                                    ((LivingEntity)ent).addPotionEffect(effect, true);
                                }
                                catch(Exception e){}
                            }
                        }
                        else if(data[0].equalsIgnoreCase("v")) {
                            try {
                                double x, y, z;
                                x = Double.parseDouble(data[1].split(",")[0]);
                                y = Double.parseDouble(data[1].split(",")[1]);
                                z = Double.parseDouble(data[1].split(",")[2]);
                                ent.setVelocity(new org.bukkit.util.Vector(x,y,z));
                            }
                            catch(Exception e){
                            }
                        }
                        else if(data[0].equalsIgnoreCase("s")) {
                            if(!(ent instanceof LivingEntity))
                                continue;
                            CraftLivingEntity cle = (CraftLivingEntity) ent;
                            EntityLiving eliv = cle.getHandle();

                            byte d = 0;
                            try {
                                d = Byte.parseByte(bit.split(";")[0].split(":")[2]);
                            }
                            catch(Exception e){}

                            ItemStack slot = new ItemStack(Item.byId[Integer.parseInt(bit.split(";")[0].split(":")[1])], 1, d);
                            try {
                                for(int e = 1; e < bit.split(";").length; e++) {
                                    slot.addEnchantment(Enchantment.byId[Integer.parseInt(bit.split(";")[e].split(":")[0])],
                                            Integer.parseInt(bit.split(";")[e].split(":")[1]));
                                }
                            }
                            catch(Exception e){}
                            eliv.setEquipment(0, slot);
                        }
                    }
                    catch(Exception e){}
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AdvancedEntitySpawner(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Spawns a mob with many customizations.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "x:y:z",
                    "entitytype*amount"
            };
            return lines;
        }
    }
}