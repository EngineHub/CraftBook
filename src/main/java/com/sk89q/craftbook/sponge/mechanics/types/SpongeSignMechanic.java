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
