/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Ammeter", onEnable="onInitialize", onDisable="onDisable")
public class Ammeter extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode permissionNode = new SpongePermissionNode("craftbook.ammeter.use", "Allows usage of the Ammeter mechanic", PermissionDescription.ROLE_USER);

    private ConfigValue<ItemStack> ammeterItem = new ConfigValue<>("ammeter-item", "The item that triggers the ammeter mechanic.", ItemStack.builder().itemType(ItemTypes.COAL).add(Keys.COAL_TYPE, CoalTypes.CHARCOAL).build(), TypeToken.of(ItemStack.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        permissionNode.register();

        ammeterItem.load(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            int powerLevel = BlockUtil.getBlockPowerLevel(location).orElse(-1);

            HandType hand = event instanceof InteractBlockEvent.Secondary.MainHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;

            if (powerLevel >= 0 && permissionNode.hasPermission(player) && player.getItemInHand(hand).isPresent() && player.getItemInHand(hand).get().getItem() == ammeterItem.getValue().getItem()) {
                player.sendMessage(getCurrentLine(powerLevel));
                event.setCancelled(true);
            }
        });
    }

    private static Text getCurrentLine(int powerLevel) {
        Text.Builder builder = Text.builder();
        builder.append(Text.of(TextColors.YELLOW, "Ammeter: ["));
        TextColor color;
        if (powerLevel > 10)
            color = TextColors.DARK_GREEN;
        else if (powerLevel > 5)
            color = TextColors.GOLD;
        else if (powerLevel > 0)
            color = TextColors.DARK_RED;
        else
            color = TextColors.BLACK;

        for (int i = 0; i < powerLevel; i++)
            builder.append(Text.of(color, "|"));

        for (int i = powerLevel; i < 15; i++)
            builder.append(Text.of(TextColors.BLACK, "|"));
        builder.append(Text.of(TextColors.YELLOW, ']'));
        builder.append(Text.of(TextColors.WHITE, " " + powerLevel + " Amps"));
        return builder.build();
    }

    @Override
    public boolean isValid(Location location) {
        return location.get(Keys.POWER).isPresent();
    }

    @Override
    public String getPath() {
        return "mechanics/ammeter";
    }

    @Override
    public String[] getMainDocumentation() {
        return new String[] {
                "The ammeter allows you to get the current level in wires and redstone devices. ",
                "",
                "Right click any redstone device while holding coal to see the meter's output. " +
                        "The current in wires decrease by one every block and source blocks emit a level of 15, giving us the wire length limit of 15 blocks."
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                ammeterItem
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                permissionNode
        };
    }
}
