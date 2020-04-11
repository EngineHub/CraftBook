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

@Module(id = "lightstone", name = "LightStone", onEnable="onInitialize", onDisable="onDisable")
public class LightStone extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode permissionNode = new SpongePermissionNode("craftbook.lightstone.use", "Allows usage of the LightStone mechanic", PermissionDescription.ROLE_USER);

    private ConfigValue<ItemStack>
            lightstoneItem = new ConfigValue<>("lightstone-item", "The item that triggers the LightStone mechanic.", ItemStack.builder().itemType(ItemTypes.GLOWSTONE_DUST).build(), TypeToken.of(ItemStack.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        permissionNode.register();

        lightstoneItem.load(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            int lightLevel = BlockUtil.getLightLevel(location.getRelative(event.getTargetSide()));

            HandType hand = event instanceof InteractBlockEvent.Secondary.MainHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;

            if (lightLevel >= 0 && permissionNode.hasPermission(player) && player.getItemInHand(hand).isPresent() && player.getItemInHand(hand).get().getType() == lightstoneItem.getValue().getType()) {
                player.sendMessage(getCurrentLine(lightLevel));
                event.setCancelled(true);
            }
        });
    }

    private static Text getCurrentLine(int lightLevel) {
        Text.Builder builder = Text.builder();
        builder.append(Text.of(TextColors.YELLOW, "LightStone: ["));
        TextColor color;
        if (lightLevel >= 9)
            color = TextColors.GREEN;
        else
            color = TextColors.DARK_RED;

        for (int i = 0; i < lightLevel; i++)
            builder.append(Text.of(color, "|"));

        for (int i = lightLevel; i < 15; i++)
            builder.append(Text.of(TextColors.BLACK, "|"));
        builder.append(Text.of(TextColors.YELLOW, ']'));
        builder.append(Text.of(TextColors.WHITE, " " + lightLevel + " L"));
        return builder.build();
    }

    @Override
    public boolean isValid(Location<World> location) {
        return BlockUtil.getLightLevel(location) >= 0;
    }

    @Override
    public String getPath() {
        return "mechanics/lightstone";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                lightstoneItem
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                permissionNode
        };
    }
}
