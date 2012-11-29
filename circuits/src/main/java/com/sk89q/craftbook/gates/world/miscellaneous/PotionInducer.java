package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

/**
 * @author Me4502
 */
public class PotionInducer extends AbstractIC implements SelfTriggeredIC {

    public PotionInducer(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public String getTitle() {

        return "Potion Inducer";
    }

    @Override
    public String getSignTitle() {

        return "POTION INDUCER";
    }

    int radius = 10, effectID = 1, effectAmount = 1, effectTime = 10;

    @Override
    public void load() {

        String[] effectInfo = ICUtil.COLON_PATTERN.split(getSign().getLine(2), 3);
        effectID = Integer.parseInt(effectInfo[0]);
        effectAmount = Integer.parseInt(effectInfo[1]);
        effectTime = Integer.parseInt(effectInfo[2]);
        radius = Integer.parseInt(getSign().getLine(3));
    }

    @Override
    public void trigger(ChipState chip) {

    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            for (Player p : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(BukkitUtil.toSign(getSign()).getLocation()) > radius * radius) {
                    continue;
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.getById(effectID), effectTime * 20,
                        effectAmount - 1), true);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PotionInducer(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                int effectId = Integer.parseInt(ICUtil.COLON_PATTERN.split(sign.getLine(2), 2)[0]);

                if (PotionEffectType.getById(effectId) == null)
                    throw new ICVerificationException("The third line must be a valid potion effect id.");
            } catch (NumberFormatException e) {
                throw new ICVerificationException("The third line must be a valid potion effect id.");
            }
        }

        @Override
        public String getDescription() {

            return "Gives nearby players a potion effect.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:level:time",
                    "range"
            };
            return lines;
        }
    }
}
