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
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.TernaryState;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import java.util.concurrent.atomic.AtomicInteger;

@Module(moduleName = "XPStorer", onEnable="onInitialize", onDisable="onDisable")
public class XPStorer extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.xp-storer.use", "Allows the user to use the " + getName() +
            " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<Boolean> requireBottle = new ConfigValue<>("require-bottle", "Requires the player to be holding a glass bottle to use.", false);
    private ConfigValue<BlockState> block = new ConfigValue<>("block", "The block that is an XP Storer.", BlockTypes.MOB_SPAWNER.getDefaultState(), TypeToken.of(BlockState.class));
    private ConfigValue<TernaryState> sneakState = new ConfigValue<>("sneak-state", "Sets how the player must be sneaking in order to use the XP Storer.", TernaryState.FALSE, TypeToken.of(TernaryState.class));
    private ConfigValue<Integer> xpPerBottle = new ConfigValue<>("xp-per-bottle", "Sets the amount of XP points required per each bottle.", 16);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        block.load(config);
        requireBottle.load(config);
        sneakState.load(config);
        xpPerBottle.load(config);

        usePermissions.register();
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getLocation().filter((this::isValid)).ifPresent(location -> {
            if (!sneakState.getValue().doesPass(player.get(Keys.IS_SNEAKING).orElse(false))
                    || player.get(Keys.EXPERIENCE_LEVEL).orElse(0) < 1
                    || !usePermissions.hasPermission(player)) {
                return;
            }

            AtomicInteger max = new AtomicInteger(Integer.MAX_VALUE);

            if(requireBottle.getValue()) {
                max.set(0);
                for (HandType handType : Sponge.getRegistry().getAllOf(HandType.class)) {
                    player.getItemInHand(handType).filter(itemStack -> itemStack.getItem() == ItemTypes.GLASS_BOTTLE)
                            .ifPresent((itemStack -> max.addAndGet(itemStack.getQuantity())));
                }

                if (max.get() == 0) {
                    player.sendMessage(Text.of(TextColors.RED, "You need a bottle to use this mechanic!"));
                    return;
                }
            }

            int xp = player.get(Keys.TOTAL_EXPERIENCE).orElse(0);

            // Reset their xp
            player.offer(Keys.EXPERIENCE_LEVEL, 0);
            player.offer(Keys.TOTAL_EXPERIENCE, 0);

            if (xp < xpPerBottle.getValue()) {
                player.sendMessage(Text.of(TextColors.RED, "Not enough XP!"));
                return;
            }

            int bottleCount = (int) Math.min(max.get(), Math.floor(((float) xp) / xpPerBottle.getValue()));

            if(requireBottle.getValue()) {
                bottleCount = player.getInventory().query(ItemTypes.GLASS_BOTTLE).poll(bottleCount).orElse(ItemStack.of(ItemTypes.GLASS_BOTTLE, 0)).getQuantity();
            }

            int tempBottles = bottleCount;

            while(tempBottles > 0) {
                ItemStack bottles = ItemStack.of(ItemTypes.EXPERIENCE_BOTTLE, Math.min(tempBottles, 64));

                Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
                item.offer(Keys.REPRESENTED_ITEM, bottles.createSnapshot());

                SpawnCause cause = Sponge.getRegistry().createBuilder(SpawnCause.Builder.class).type(SpawnTypes.DROPPED_ITEM).build();

                location.getExtent().spawnEntity(item, Cause.source(cause).build());

                tempBottles -= 64;
            }

            int remainingXP = xp - bottleCount * xpPerBottle.getValue();

            player.offer(Keys.TOTAL_EXPERIENCE, remainingXP);
        });
    }

    @Override
    public String getPath() {
        return "mechanics/xp_storer";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                usePermissions
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                requireBottle,
                block,
                sneakState,
                xpPerBottle
        };
    }

    @Override
    public boolean isValid(Location<?> location) {
        return location.getBlock().equals(block.getValue());
    }
}
