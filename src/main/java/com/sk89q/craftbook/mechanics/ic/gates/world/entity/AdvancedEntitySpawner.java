package com.sk89q.craftbook.mechanics.ic.gates.world.entity;

import java.util.Locale;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class AdvancedEntitySpawner extends AbstractIC {

    public AdvancedEntitySpawner(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    private Location location;
    private EntityType type;
    private int amount;

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

        String[] splitLine3 = RegexUtil.ASTERISK_PATTERN.split(getSign().getLine(3).trim());
        try {
            type = EntityType.valueOf(splitLine3[0].trim().toLowerCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            try {
                type = EntityType.valueOf(splitLine3[0].trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ee) {
                type = EntityType.PIG;
            }
        }

        try {
            amount = Integer.parseInt(splitLine3[1]);
        } catch (Exception e) {
            amount = 1;
        }

        location = ICUtil.parseBlockLocation(getSign(), 2).getLocation();
    }

    @Override
    public void trigger(ChipState chip) {

        if(!location.getChunk().isLoaded())
            return;

        if (!chip.getInput(0)) return;
        Block left = SignUtil.getLeftBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        ChangedSign effectSign = null;
        if (SignUtil.isWallSign(left))
            effectSign = CraftBookBukkitUtil.toChangedSign(left);

        Block right = SignUtil.getRightBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        ChangedSign armourSign = null;
        if (SignUtil.isWallSign(right))
            armourSign = CraftBookBukkitUtil.toChangedSign(right);

        for (int i = 0; i < amount; i++) {
            Entity ent = CraftBookBukkitUtil.toSign(getSign()).getWorld().spawn(location, type.getEntityClass());

            if (armourSign != null) { // Apply armor
                if (ent instanceof LivingEntity) {

                    for (int s = 0; s < 4; s++) {
                        String bit = armourSign.getLine(s);

                        ItemStack slot = ItemUtil.makeItemValid(ItemSyntax.getItem(bit));

                        switch (s) {
                            case 0:
                                ((LivingEntity) ent).getEquipment().setHelmet(slot);
                                break;
                            case 1:
                                ((LivingEntity) ent).getEquipment().setChestplate(slot);
                                break;
                            case 2:
                                ((LivingEntity) ent).getEquipment().setLeggings(slot);
                                break;
                            case 3:
                                ((LivingEntity) ent).getEquipment().setBoots(slot);
                                break;
                        }
                    }
                }
            }

            Boolean upwards = null;

            while (effectSign != null) { // Apply effects
                for (int s = 0; s < 4; s++) {
                    String bit = effectSign.getLine(s);
                    if (bit == null || bit.trim().isEmpty()) continue;

                    String[] data = RegexUtil.COLON_PATTERN.split(bit);

                    if (data[0].equalsIgnoreCase("e")) EntityUtil.setEntityData(ent, bit.substring(2));
                    else if (data[0].equalsIgnoreCase("r")) {
                        EntityType rider = EntityType.fromName(data[1].trim());
                        Entity rid = CraftBookBukkitUtil.toSign(getSign()).getWorld().spawnEntity(location, rider);
                        ent.setPassenger(rid);
                    } else if (data[0].equalsIgnoreCase("p") && ent instanceof LivingEntity) {
                        for (int a = 1; a < data.length; a++) {
                            try {
                                String[] potionBits = RegexUtil.SEMICOLON_PATTERN.split(data[a]);
                                PotionEffect effect = new PotionEffect(PotionEffectType.getById(Integer.parseInt(potionBits[0])), Integer.parseInt(potionBits[1]), Integer.parseInt(potionBits[2]));
                                ((LivingEntity) ent).addPotionEffect(effect, true);
                            } catch (Exception ignored) {
                            }
                        }
                    } else if (data[0].equalsIgnoreCase("v")) {
                        try {
                            double x, y, z;
                            String[] coords = RegexUtil.COMMA_PATTERN.split(data[1]);
                            x = Double.parseDouble(coords[0]);
                            y = Double.parseDouble(coords[1]);
                            z = Double.parseDouble(coords[2]);
                            ent.setVelocity(new org.bukkit.util.Vector(x, y, z));
                        } catch (Exception ignored) {
                        }
                    } else if (data[0].equalsIgnoreCase("s")) {
                        if (!(ent instanceof LivingEntity)) continue;

                        ItemStack slot = ItemUtil.makeItemValid(ItemSyntax.getItem(bit.replace("s:", "")));
                        ((LivingEntity) ent).getEquipment().setItemInHand(slot);
                    }
                }
                if (upwards == null) {
                    if (SignUtil.isWallSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, 1, 0))) {
                        effectSign = CraftBookBukkitUtil.toChangedSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, 1, 0));
                        upwards = true;
                    } else if (SignUtil.isWallSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, -1, 0))) {
                        effectSign = CraftBookBukkitUtil.toChangedSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, -1, 0));
                        upwards = false;
                    } else break;
                } else {
                    if (SignUtil.isWallSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, upwards ? 1 : -1, 0)))
                        effectSign = CraftBookBukkitUtil
                                .toChangedSign(CraftBookBukkitUtil.toSign(effectSign).getBlock().getRelative(0, upwards ? 1 : -1, 0));
                    else break;
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
        public String getShortDescription() {

            return "Spawns a mob with many customizations.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+ox:y:z", "entitytype{*amount}"};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            String[] splitLine3 = RegexUtil.ASTERISK_PATTERN.split(sign.getLine(3).trim());
            EntityType type = EntityType.fromName(splitLine3[0].trim().toLowerCase(Locale.ENGLISH));
            if(type == null)
                try {
                    EntityType.valueOf(splitLine3[0].trim().toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    throw new ICVerificationException("Invalid Entity! See bukkit EntityType list!");
                }
            else if (!type.isSpawnable())
                throw new ICVerificationException("Entity is not spawnable!");
        }
    }
}
