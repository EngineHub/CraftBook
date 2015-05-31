package com.sk89q.craftbook.sponge.mechanics.area;

import javax.annotation.Nullable;

import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.manipulator.tileentity.SignData;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.event.block.tileentity.SignChangeEvent;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeRedstoneMechanicData;

public abstract class SimpleArea extends SpongeBlockMechanic {

    @Subscribe
    public void onSignChange(SignChangeEvent event) {

        Player player = null;
        if(event.getCause().isPresent() && event.getCause().get().getCause() instanceof Player)
            player = (Player) event.getCause().get().getCause();

        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getCurrentData(), 1).equalsIgnoreCase(line)) {
                if(player != null && !player.hasPermission("craftbook." + getName().toLowerCase() + ".create")) {
                    player.sendMessage(Texts.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    SignData data = event.getCurrentData();
                    data.setLine(1, Texts.of(line));
                    event.setNewData(data);
                }
            }
        }
    }

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if (event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionTypes.USE) return;

        if (SignUtil.isSign(event.getBlock())) {

            Sign sign = (Sign) event.getBlock().getTileEntity().get();

            if (isMechanicSign(sign)) {

                if (triggerMechanic(event.getBlock(), sign, event.getEntity(), null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        for (Location block : event.getAffectedBlocks()) {
            if (SignUtil.isSign(block)) {

                Sign sign = (Sign) block.getTileEntity().get();

                if (isMechanicSign(sign)) {
                    SpongeRedstoneMechanicData data = getData(SpongeRedstoneMechanicData.class, block);
                    if (data.lastCurrent != (block.isPowered() ? 15 : 0)) {
                        triggerMechanic(block, sign, null, block.isPowered());
                        data.lastCurrent = block.isPowered() ? 15 : 0;
                    }
                }
            }
        }
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

        long blockBagId;

        @Override
        public DataContainer toContainer() {

            DataContainer container = super.toContainer();

            container.set(DataQuery.of("blockBagId"), blockBagId);

            return container;
        }
    }
}
