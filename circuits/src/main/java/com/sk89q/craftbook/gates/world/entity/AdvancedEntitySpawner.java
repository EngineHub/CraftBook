package com.sk89q.craftbook.gates.world.entity;

import java.util.regex.Pattern;

import net.minecraft.server.v1_4_5.Enchantment;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.Item;
import net.minecraft.server.v1_4_5.ItemStack;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
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

    private static final Pattern ASTERISK_PATTERN = Pattern.compile("*", Pattern.LITERAL);
    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    public AdvancedEntitySpawner(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
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

    @Override
    public void load() {

        String[] splitLine3 = ASTERISK_PATTERN.split(getSign().getLine(3).trim());
        type = EntityType.fromName(splitLine3[0]);

        try {
            amount = Integer.parseInt(splitLine3[1]);
        }
        catch(Exception e) {
            amount = 1;
        }

        try {
            double x, y, z;
            String[] splitLine2 = COLON_PATTERN.split(getSign().getLine(2));
            x = Double.parseDouble(splitLine2[0]);
            y = Double.parseDouble(splitLine2[1]);
            z = Double.parseDouble(splitLine2[2]);
            x += getSign().getX();
            y += getSign().getY();
            z += getSign().getZ();
            location = new Location(getSign().getLocalWorld(), new Vector(x,y,z));
        }
        catch(Exception e){
            location = new Location(getSign().getLocalWorld(),
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
            effectSign = BukkitUtil.toChangedSign(left);
        }

        Block right = SignUtil.getRightBlock(BukkitUtil.toSign(getSign()).getBlock());
        ChangedSign armourSign = null;
        if(right.getTypeId() == BlockID.WALL_SIGN) {
            armourSign = BukkitUtil.toChangedSign(right);
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
                            if(bit == null || bit.trim().isEmpty())
                                continue;

                            byte data = 0;
                            String[] bitSplit = SEMICOLON_PATTERN.split(bit);
                            try {
                                data = Byte.parseByte(COLON_PATTERN.split(bitSplit[0])[1]);
                            }
                            catch(Exception e){}

                            ItemStack slot = new ItemStack(Item.byId[Integer.parseInt(COLON_PATTERN.split(bitSplit[0])[0])], 1, data);
                            try {
                                for(int e = 1; e < bitSplit.length; e++) {
                                    String[] enchantInfo = COLON_PATTERN.split(bitSplit[e]);
                                    slot.addEnchantment(Enchantment.byId[Integer.parseInt(enchantInfo[0])],
                                            Integer.parseInt(enchantInfo[1]));
                                }
                            }
                            catch(Exception e){}
                            eliv.setEquipment(s + 1, slot);
                        }
                        catch(Exception e){}
                    }
                }
            }

            Boolean upwards = null;

            while(effectSign != null) { //Apply effects
                for(int s = 0; s < 4; s++) {
                    String bit = effectSign.getLine(s);
                    if(bit == null || bit.trim().isEmpty())
                        continue;

                    String[] data = COLON_PATTERN.split(bit);

                    if(data[0].equalsIgnoreCase("e"))
                        setEntityData(ent, bit.substring(2));
                    else if(data[0].equalsIgnoreCase("r")) {
                        EntityType rider = EntityType.fromName(data[1].trim());
                        Entity rid = BukkitUtil.toSign(getSign()).getWorld().spawnEntity(BukkitUtil.toLocation(location), rider);
                        ent.setPassenger(rid);
                    }
                    else if(data[0].equalsIgnoreCase("p") && ent instanceof LivingEntity) {
                        for(int a = 1; a < data.length; a++) {
                            try {
                                String[] potionBits = SEMICOLON_PATTERN.split(data[a]);
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
                            String[] coords = COMMA_PATTERN.split(data[1]);
                            x = Double.parseDouble(coords[0]);
                            y = Double.parseDouble(coords[1]);
                            z = Double.parseDouble(coords[2]);
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
                        String[] splitBit = SEMICOLON_PATTERN.split(bit);
                        String[] splitEvenMore = COLON_PATTERN.split(splitBit[0]);
                        try {
                            d = Byte.parseByte(splitEvenMore[2]);
                        }
                        catch(Exception e){}

                        ItemStack slot = new ItemStack(Item.byId[Integer.parseInt(splitEvenMore[1])], 1, d);
                        try {
                            for(int e = 1; e < splitBit.length; e++) {
                                String[] enchantInfo = COLON_PATTERN.split(splitBit[e]);
                                slot.addEnchantment(Enchantment.byId[Integer.parseInt(enchantInfo[0])],
                                        Integer.parseInt(enchantInfo[1]));
                            }
                        }
                        catch(Exception e){}
                        eliv.setEquipment(0, slot);
                    }
                }
                if(upwards == null) {
                    if(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, 1, 0).getTypeId() == BlockID.WALL_SIGN) {
                        effectSign = BukkitUtil.toChangedSign(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, 1, 0));
                        upwards = true;
                    }
                    else if(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, -1, 0).getTypeId() == BlockID.WALL_SIGN) {
                        effectSign = BukkitUtil.toChangedSign(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, -1, 0));
                        upwards = false;
                    }
                    else
                        break;
                }
                else {
                    if(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, upwards ? 1 : -1, 0).getTypeId() == BlockID.WALL_SIGN)
                        effectSign = BukkitUtil.toChangedSign(BukkitUtil.toSign(effectSign).getBlock().getRelative(0, upwards ? 1 : -1, 0));
                    else
                        break;
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