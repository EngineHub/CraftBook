package com.sk89q.craftbook.sponge.mechanics.area;

import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeRedstoneMechanicData;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public abstract class SimpleArea extends SpongeBlockMechanic {

    private ConfigValue<Set<BlockState>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks());

    public void loadCommonConfig(ConfigurationNode config) {
        allowedBlocks.load(config);
    }

    public void saveCommonConfig(ConfigurationNode config) {
        allowedBlocks.save(config);
    }

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
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    event.getText().lines().set(1, Text.of(line));
                }
            }
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event) {

        Humanoid human;
        if(event.getCause().first(Humanoid.class).isPresent())
            human = event.getCause().first(Humanoid.class).get();
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
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {

        BlockSnapshot source;
        if(event.getCause().first(BlockSnapshot.class).isPresent())
            source = event.getCause().first(BlockSnapshot.class).get();
        else
            return;

        if(!SignUtil.isSign(source.getState())) return;
        Sign sign = (Sign) source.getLocation().get().getTileEntity().get();

        if (isMechanicSign(sign)) {
            event.getNeighbors().entrySet().stream().map(
                    (Function<Entry<Direction, BlockState>, Location>) entry -> source.getLocation().get().getRelative(entry.getKey())).
                    collect(Collectors.toList()).stream().forEach(block -> {

                SpongeRedstoneMechanicData data = getData(SpongeRedstoneMechanicData.class, source.getLocation().get());
                if (block.getBlock().get(Keys.POWER).isPresent()) {
                    if (data.lastCurrent != block.getBlock().get(Keys.POWER).get()) {
                        triggerMechanic(source.getLocation().get(), sign, event.getCause().get(NamedCause.NOTIFIER, Player.class).orElse(null), block.getBlock().get(Keys.POWER).get() > 0);
                        data.lastCurrent = block.getBlock().get(Keys.POWER).get();
                    }
                }
            });
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
    public abstract boolean triggerMechanic(Location block, Sign sign, @Nullable Humanoid human, @Nullable Boolean forceState);

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

    public abstract Set<BlockState> getDefaultBlocks();

    public static class SimpleAreaData extends SpongeRedstoneMechanicData {
        public long blockBagId;
    }
}
