/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
package org.enginehub.craftbook.sponge.mechanics;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.util.ConfigValue;
import org.enginehub.craftbook.util.CraftBookException;
import org.enginehub.craftbook.util.PermissionNode;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.enginehub.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "ammeter", name = "Ammeter", onEnable = "onInitialize", onDisable = "onDisable")
public class Ammeter extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode permissionNode = new SpongePermissionNode("craftbook.ammeter.use", "Allows usage of the Ammeter mechanic", PermissionDescription.ROLE_USER);

    private ConfigValue<ItemStack>
            ammeterItem = new ConfigValue<>("ammeter-item", "The item that triggers the ammeter mechanic.", ItemStack.builder().itemType(ItemTypes.COAL).add(Keys.COAL_TYPE, CoalTypes.CHARCOAL).build(), TypeToken.of(ItemStack.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        permissionNode.register();

        ammeterItem.load(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            int powerLevel = BlockUtil.getDirectBlockPowerLevel(location).orElse(-1);

            HandType hand = event instanceof InteractBlockEvent.Secondary.MainHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;

            if (powerLevel >= 0 && permissionNode.hasPermission(player) && player.getItemInHand(hand).isPresent()
                    && player.getItemInHand(hand).get().getType() == ammeterItem.getValue().getType()) {
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
    public boolean isValid(Location<World> location) {
        return location.get(Keys.POWER).isPresent();
    }

    @Override
    public String getPath() {
        return "mechanics/ammeter";
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
