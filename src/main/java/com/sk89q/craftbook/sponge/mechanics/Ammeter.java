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
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.CoalTypes;
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
public class Ammeter extends SpongeBlockMechanic {

    private SpongePermissionNode permissionNode = new SpongePermissionNode("craftbook.ammeter.use", "Allows usage of the Ammeter mechanic", PermissionDescription.ROLE_USER);

    private ConfigValue<ItemStack> ammeterItem = new ConfigValue<>("ammeter-item", "The item that triggers the ammeter mechanic.", ItemStack.builder().itemType(ItemTypes.COAL).add(Keys.COAL_TYPE, CoalTypes.CHARCOAL).build(), TypeToken.of(ItemStack.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        permissionNode.register();
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Player player) {
        int powerLevel = getPowerLevel(event.getTargetBlock().getExtendedState());
        if (player.getItemInHand().isPresent() && powerLevel >= 0 && permissionNode.hasPermission(player) && player.getItemInHand().get().getItem() == ammeterItem.getValue().getItem()) {
            player.sendMessage(getCurrentLine(powerLevel));
            event.setCancelled(true);
        }
    }

    private static int getPowerLevel(BlockState state) {
        if(state.get(Keys.POWER).isPresent()) {
            return state.get(Keys.POWER).get();
        } else if(state.get(Keys.POWERED).isPresent()){
            return state.get(Keys.POWERED).get() ? 15 : 0;
        }

        //No method of retrieving power from this block is implemented, or it has no power.
        return -1;
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
}
