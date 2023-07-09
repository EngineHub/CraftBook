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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RegexUtil;

import java.util.List;

public class CartTeleporter extends CartBlockMechanism {

    private final static List<String> SIGNS = ImmutableList.of("TeleCart");

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor() || !event.getBlocks().matches(getBlock()) || !event.getBlocks().hasSign()) {
            return;
        }

        Side side = event.getBlocks().matches("telecart");
        if (side == null) {
            return;
        }
        ChangedSign sign = event.getBlocks().getChangedSign(side);

        String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).trim();
        String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3)).trim();
        World world = event.getMinecart().getWorld();
        String[] pts = RegexUtil.COMMA_PATTERN.split(line2, 3);
        if (!line3.isEmpty()) {
            world = event.getMinecart().getServer().getWorld(line3);
        }

        double x;
        double y;
        double z;
        try {
            x = Double.parseDouble(pts[0].trim());
            y = Double.parseDouble(pts[1].trim());
            z = Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException e) {
            // incorrect format, just set them still and let them figure it out
            x = event.getFrom().getX();
            y = event.getFrom().getY();
            z = event.getFrom().getZ();

            event.getMinecart().setVelocity(new Vector(0, 0, 0));
        }

        Location loc = new Location(world, x, y, z, event.getMinecart().getLocation().getYaw(), event.getMinecart().getLocation().getPitch()).toCenterLocation();
        loc.getChunk().load(true);
        event.getMinecart().teleport(loc, TeleportFlag.EntityState.RETAIN_VEHICLE, TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {
        String[] pts = RegexUtil.COMMA_PATTERN.split(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).trim(), 3);
        try {
            Double.parseDouble(pts[0].trim());
            Double.parseDouble(pts[1].trim());
            Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            player.printError(TranslatableComponent.of("craftbook.minecartteleporter.invalid-location-syntax"));
            return false;
        }
        return true;
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "The block the TeleCart mechanic uses.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.EMERALD_BLOCK.getId()), true));
    }
}
