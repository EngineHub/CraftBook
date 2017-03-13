/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.blockbags.AdminBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.IdentifiableBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.data.BlockBagData;
import com.sk89q.craftbook.sponge.mechanics.blockbags.data.EmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public abstract class SimpleArea extends SpongeSignMechanic {

    SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook." + getName().toLowerCase(), "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);
    SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook." + getName().toLowerCase() + ".use", "Allows the user to use the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks(), new TypeTokens.BlockFilterListTypeToken());
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
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Humanoid human) {
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
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        if(!allowRedstone.getValue())
            return;

        if(!SignUtil.isSign(source.getLocation())) return;
        Location<World> block = source.getLocation();
        Sign sign = (Sign) block.getTileEntity().get();

        if (isMechanicSign(sign)) {
            Player player = event.getCause().get(NamedCause.SOURCE, Player.class).orElse(null);
            if(player != null) {
                if(!usePermissions.hasPermission(player)) {
                    player.sendMessage(USE_PERMISSIONS);
                    return;
                }
            }

            boolean isPowered = BlockUtil.getBlockPowerLevel(block).orElse(0) > 0;
            boolean wasPowered = block.get(CraftBookKeys.LAST_POWER).orElse(0) > 0;

            if (isPowered != wasPowered) {
                triggerMechanic(block, sign, player, isPowered);
                block.offer(new LastPowerData(isPowered ? 15 : 0));
            }
        }
    }

    @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable Player player) {
        String line0 = SignUtil.getTextRaw(lines.get(0));
        if (!line0.isEmpty()) {
            Optional<ModuleWrapper> moduleWrapper = CraftBookPlugin.spongeInst().moduleController.getModule("blockbag");
            if (moduleWrapper.isPresent() && moduleWrapper.get().isEnabled()) {
                if ("ADMIN".equals(line0)) {
                    if ((player == null || ((BlockBagManager) moduleWrapper.get().getModule().get()).adminPermissions.hasPermission(player))) {
                        location.offer(new BlockBagData(-1));
                    } else {
                        lines.set(0, Text.of());
                    }
                }
            }
        }

        return super.verifyLines(location, lines, player);
    }

    public BlockBag getBlockBag(Location<World> location) {
        Optional<ModuleWrapper> moduleWrapper = CraftBookPlugin.spongeInst().moduleController.getModule("blockbag");
        if (moduleWrapper.isPresent() && moduleWrapper.get().isEnabled()) {
            BlockBagManager manager = ((BlockBagManager) moduleWrapper.get().getModule().get());
            Optional<Long> blockBag = location.get(BlockBagManager.BLOCK_BAG);
            if (blockBag.isPresent()) {
                Optional<IdentifiableBlockBag> foundBlockBag = blockBag.map(manager::getBlockBag);
                if (foundBlockBag.isPresent()) {
                    return foundBlockBag.get();
                }
            }

            Optional<EmbeddedBlockBag> embeddedBlockBag = location.get(BlockBagManager.EMBEDDED_BLOCK_BAG);
            if (embeddedBlockBag.isPresent()) {
                return embeddedBlockBag.get();
            }

            EmbeddedBlockBag actualBlockBag = new EmbeddedBlockBag();
            manager.addBlockBag(actualBlockBag);
            location.offer(new EmbeddedBlockBagData(actualBlockBag));
            return actualBlockBag;
        }

        return AdminBlockBag.INSTANCE;
    }

    /**
     * Triggers the mechanic.
     * 
     * @param block The block the mechanic is being triggered at
     * @param sign The sign of the mechanic
     * @param human The triggering human, if applicable
     * @param forceState If the mechanic should forcibly enter a specific state
     */
    public abstract boolean triggerMechanic(Location<World> block, Sign sign, @Nullable Humanoid human, @Nullable Boolean forceState);

    public abstract List<BlockFilter> getDefaultBlocks();
}
