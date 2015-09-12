package com.sk89q.craftbook.sponge.mechanics.area;

import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeRedstoneMechanicData;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.PoweredData;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;

public abstract class SimpleArea extends SpongeBlockMechanic {

    @Listener
    public void onSignChange(ChangeSignEvent event) {

        Player player;
        if(event.getCause().first(Player.class).isPresent())
            player = event.getCause().first(Player.class).get();
        else
            return;

        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getText(), 1).equalsIgnoreCase(line)) {
                if(!player.hasPermission("craftbook." + getName().toLowerCase() + ".create")) {
                    player.sendMessage(Texts.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    event.getText().lines().set(1, Texts.of(line));
                }
            }
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event) {

        Human human;
        if(event.getCause().first(Human.class).isPresent())
            human = event.getCause().first(Human.class).get();
        else
            return;

        if (SignUtil.isSign(event.getTargetBlock().getLocation().get())) {
            Sign sign = (Sign) event.getTargetBlock().getLocation().get().getTileEntity().get();

            if (isMechanicSign(sign)) {

                if (triggerMechanic(event.getTargetBlock().getLocation().get(), sign, human, null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent.Power event) {

        event.getRelatives().values().stream().filter(SignUtil::isSign).forEach(block -> {

            Sign sign = (Sign) block.getTileEntity();

            if (isMechanicSign(sign)) {
                SpongeRedstoneMechanicData data = getData(SpongeRedstoneMechanicData.class, block);
                if (data.lastCurrent != (block.get(PoweredData.class).isPresent() ? 15 : 0)) {
                    triggerMechanic(block, sign, null, block.get(Keys.POWERED).isPresent());
                    data.lastCurrent = block.get(Keys.POWERED).isPresent() ? 15 : 0;
                }
            }
        });
    }

    /**
     * Triggers the mechanic.
     * 
     * @param block The block the mechanic is being triggered at
     * @param sign The sign of the mechanic
     * @param human The triggering human, if applicable
     * @param forceState If the mechanic should forcibly enter a specific state
     */
    public abstract boolean triggerMechanic(Location block, Sign sign, @Nullable Human human, @Nullable Boolean forceState);

    public Location getOtherEnd(Location block, Direction back, int maximumLength) {
        for (int i = 0; i < maximumLength; i++) {
            block = block.getRelative(back);
            if (SignUtil.isSign(block)) {
                Sign sign = (Sign) block.getTileEntity().get();

                if (isMechanicSign(sign)) {
                    return block;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValid(Location location) {
        if (SignUtil.isSign(location)) {
            Sign sign = (Sign) location.getTileEntity().get();
            return isMechanicSign(sign);
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

    public static class SimpleAreaData extends SpongeRedstoneMechanicData {
        public long blockBagId;
    }
}
