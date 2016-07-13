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
package com.sk89q.craftbook.sponge.mechanics.area;

import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import com.sk89q.craftbook.sponge.util.type.BlockFilterListTypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

public abstract class SimpleArea extends SpongeSignMechanic {

    SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook." + getName().toLowerCase() + ".create", "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);
    SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook." + getName().toLowerCase() + ".use", "Allows the user to use the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks(), new BlockFilterListTypeToken());
    ConfigValue<Boolean> allowRedstone = new ConfigValue<>("allow-redstone", "Whether to allow redstone to be used to trigger this mechanic or not", true);

    TranslatableText missingOtherEnd = TranslatableText.of(new ResourceBundleTranslation("area.missing-other-end", TranslationsManager.getResourceBundleFunction()));

    void loadCommonConfig(ConfigurationNode config) {
        allowedBlocks.load(config);
        allowRedstone.load(config);
    }

    void registerCommonPermissions() {
        createPermissions.register();
        usePermissions.register();
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Humanoid human) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (isValid(location)) {
                location.getTileEntity().ifPresent((sign -> {
                    if ((!(human instanceof Subject) || usePermissions.hasPermission((Subject) human))) {
                        if (triggerMechanic(location, (Sign) sign, human, null)) {
                            event.setCancelled(true);
                        }
                    } else if (human instanceof CommandSource) {
                        ((CommandSource) human).sendMessage(USE_PERMISSIONS);
                    }
                }));
            }
        });
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First BlockSnapshot source) {
        if(!allowRedstone.getValue())
            return;

        if(!SignUtil.isSign(source.getState())) return;
        Sign sign = (Sign) source.getLocation().get().getTileEntity().get();

        if (isMechanicSign(sign)) {
            Humanoid human = event.getCause().get(NamedCause.SOURCE, Humanoid.class).orElse(null);
            if(human != null && human instanceof Subject) {
                if(!usePermissions.hasPermission((Subject) human)) {
                    if(human instanceof CommandSource)
                        ((CommandSource) human).sendMessage(USE_PERMISSIONS);
                    return;
                }
            }

            event.getNeighbors().entrySet().stream().map(
                    (Function<Entry<Direction, BlockState>, Location>) entry -> source.getLocation().get().getRelative(entry.getKey()))
                    .collect(Collectors.toList()).stream()
                    .filter((block) -> BlockUtil.getBlockPowerLevel(source.getLocation().get(), block).isPresent())
                    .filter((block) -> source.getLocation().get().get(CraftBookKeys.LAST_POWER).orElse(0) != BlockUtil.getBlockPowerLevel(source.getLocation().get(), block).get())
                    .findFirst().ifPresent(block -> {
                Optional<Integer> powerOptional = BlockUtil.getBlockPowerLevel(source.getLocation().get(), block);

                triggerMechanic(source.getLocation().get(), sign, human, powerOptional.get() > 0);
                source.getLocation().get().offer(CraftBookKeys.LAST_POWER, powerOptional.get());
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

    public abstract List<BlockFilter> getDefaultBlocks();
}
