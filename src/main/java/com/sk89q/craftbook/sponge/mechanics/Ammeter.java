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

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Ammeter", onEnable="onInitialize", onDisable="onDisable")
public class Ammeter extends SpongeBlockMechanic {

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getState().get(Keys.POWER).ifPresent((data) -> player.sendMessage(getCurrentLine(data)));
    }

    private static Text getCurrentLine(int data) {
        Text.Builder builder = Text.builder();
        builder.append(Text.of(TextColors.YELLOW, "Ammeter: ["));
        TextColor color;
        if (data > 10)
            color = TextColors.DARK_GREEN;
        else if (data > 5)
            color = TextColors.GOLD;
        else if (data > 0)
            color = TextColors.DARK_RED;
        else
            color = TextColors.BLACK;

        for (int i = 0; i < data; i++)
            builder.append(Text.of(color, "|"));

        for (int i = data; i < 15; i++)
            builder.append(Text.of(TextColors.BLACK, "|"));
        builder.append(Text.of(TextColors.YELLOW, ']'));
        builder.append(Text.of(TextColors.WHITE, " " + data + "A"));
        return builder.build();
    }

    @Override
    public boolean isValid(Location location) {
        return location.get(Keys.POWER).isPresent();
    }
}
