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
package com.sk89q.craftbook.sponge.mechanics.types;

import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

public abstract class SpongeSignMechanic extends SpongeBlockMechanic {

    @Listener
    public void onSignChange(ChangeSignEvent event, @Named(NamedCause.SOURCE) Player player) {

        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getText(), 1).equalsIgnoreCase(line)) {
                if(!getCreatePermission().hasPermission(player)) {
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    event.getText().lines().set(1, Text.of(line));
                }
            }
        }
    }

    @Override
    public boolean isValid(Location location) {
        if (SignUtil.isSign(location)) {

            Sign sign = (Sign) location.getTileEntity().get();

            for(String signLine : getValidSigns()) {
                if(SignUtil.getTextRaw(sign, 1).equals(signLine))
                    return true;
            }
        }

        return false;
    }

    public boolean isMechanicSign(Sign sign) {
        for(String text : getValidSigns())
            if(SignUtil.getTextRaw(sign, 1).equals(text))
                return true;
        return false;
    }

    public abstract String[] getValidSigns();

    public abstract SpongePermissionNode getCreatePermission();
}
