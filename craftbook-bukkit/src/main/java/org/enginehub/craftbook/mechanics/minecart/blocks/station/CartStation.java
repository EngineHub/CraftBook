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

package org.enginehub.craftbook.mechanics.minecart.blocks.station;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartBlockMechanism;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockEnterEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartStation extends CartBlockMechanism {

    private final static List<String> SIGNS = List.of("Station");

    private final Map<UUID, String> stationSelection = new HashMap<>();

    @Override
    public void enable() throws MechanicInitializationException {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelCommands(
            (commandManager, registration) -> StationCommands.register(commandManager, registration, this)
        );

        stationSelection.clear();

        super.enable();
    }

    @Override
    public void disable() {
        stationSelection.clear();

        super.disable();
    }

    public String getStation(UUID player) {
        return this.stationSelection.get(player);
    }

    public void setStation(UUID player, String stationName) {
        this.stationSelection.put(player, stationName);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        stationInteraction(event.getMinecart(), event.getBlocks(), false);
    }

    @EventHandler
    public void onBlockPower(CartBlockRedstoneEvent event) {
        stationInteraction(event.getMinecart(), event.getBlocks(), true);
    }

    @EventHandler
    public void onVehicleEnter(CartBlockEnterEvent event) {
        if (!event.getBlocks().hasSign()) {
            return;
        }

        /*
        // TODO Temporarily disable tickets for now, until we have proper item comparisons.
        if (!sign.getLine(3).isEmpty() && event.getEntered() instanceof Player player) {
            ItemStack testItem = ItemSyntax.getItem(sign.getLine(3));
            if (!ItemUtil.areItemsIdentical(testItem, player.getItemInHand()))
                return;
        }
        */

        stationInteraction(event.getMinecart(), event.getBlocks(), false);
    }

    private void stationInteraction(Minecart cart, CartMechanismBlocks blocks, boolean powerChange) {
        if (cart == null || !blocks.matches(getBlock())) {
            return;
        }

        Side side = blocks.matches("station");
        if (side == null) {
            return;
        }

        ChangedSign sign = blocks.getChangedSign(side);
        boolean autoStart = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).equalsIgnoreCase("AUTOSTART");

        if (!powerChange && !autoStart) {
            return;
        }

        RedstoneUtil.Power pow = isActive(blocks);

        switch (pow) {
            case ON -> {
                if (autoStart && powerChange) {
                    return;
                }
                cart.setVelocity(SignUtil.getFacing(blocks.sign()).getDirection().multiply(0.2));
            }
            case OFF, NA -> {
                // park it.
                cart.setVelocity(new Vector(0, 0, 0));

                // recenter it
                Location l = blocks.rail().getLocation().add(0.5, 0.5, 0.5);
                cart.teleport(l, TeleportFlag.EntityState.RETAIN_VEHICLE, TeleportFlag.EntityState.RETAIN_PASSENGERS);
            }
        }
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the station mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.OBSIDIAN.id()), true));
    }
}
