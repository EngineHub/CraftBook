/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.util.yaml.YAMLProcessor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.SignUtil;

public class FlameThrower extends AbstractIC {

    private int distance;
    private int delay;

    public FlameThrower(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            distance = Math.min(((Factory) getFactory()).maxRange, Integer.parseInt(getLine(2)));
        } catch (Exception ignored) {
            distance = 10;
        }

        try {
            delay = Integer.parseInt(getLine(3));
        } catch (Exception ignored) {
            delay = 0;
        }
    }

    @Override
    public String getTitle() {

        return "Flame Thrower";
    }

    @Override
    public String getSignTitle() {

        return "FLAME THROWER";
    }

    @Override
    public void trigger(ChipState chip) {

        sendFlames(chip.getInput(0));
    }

    public void sendFlames(final boolean make) {

        final Block block = getSign().getBlock();
        final BlockFace direction = SignUtil.getBack(block);

        if (delay <= 0) {

            Block fire = block.getRelative(direction, 2);
            for (int i = 0; i < distance; i++) {
                if (make) {
                    if (fire.getType() == Material.AIR || fire.getType() == Material.GRASS) {
                        fire.setType(Material.FIRE);
                    }
                } else if (fire.getType() == Material.FIRE) {
                    fire.setType(Material.AIR);
                }
                fire = fire.getRelative(direction);
            }
        } else {

            for (int i = 0; i < distance; i++) {

                final int fi = i;
                CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {

                    Block fire = block.getRelative(direction, 2 + fi);
                    if (make) {
                        if (fire.getType() == Material.AIR || fire.getType() == Material.GRASS) {
                            fire.setType(Material.FIRE);
                        }
                    } else if (fire.getType() == Material.FIRE) {
                        fire.setType(Material.AIR);
                    }
                }, delay * fi);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        public int maxRange;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FlameThrower(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Makes a line of fire.";
        }

        @Override
        public String[] getLongDescription() {
            return new String[] {
                "The '''MC1252''' sets a certain length of blocks in fron of the IC block on fire (putting fire Block on top of them)."
            };
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                int distance = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)));
                if (distance > maxRange) throw new ICVerificationException("Distance too great!");

            } catch (Exception ignored) {
                throw new ICVerificationException("Invalid distance!");
            }
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "distance", "delay" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment("max-fire-range", "The maximum range the Flamethrower IC can be set to.");
            maxRange = config.getInt("max-fire-range", 20);
        }
    }
}