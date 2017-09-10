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
package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.st.SpongeSelfTriggerManager;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

@Module(id = "xpstorer", name = "XPStorer", onEnable="onInitialize", onDisable="onDisable")
public class XPStorer extends SpongeSignMechanic implements DocumentationProvider, SelfTriggeringMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.xp-storer.use", "Allows the user to use the " +
            getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.xp-storer", "Allows the user to create the " +
            getName() + " mechanic.", PermissionDescription.ROLE_STAFF);

    private SpongePermissionNode rangedPermissions = new SpongePermissionNode("craftbook.xp-storer.ranged", "Allows the user to create the ranged " +
            getName() + " mechanic.", PermissionDescription.ROLE_STAFF);

    private ConfigValue<Boolean> requireBottle = new ConfigValue<>("require-bottle", "Requires the player to be holding a glass bottle to use.", false);
    private ConfigValue<BlockState> block = new ConfigValue<>("block", "The block that is an XP Storer.", BlockTypes.MOB_SPAWNER.getDefaultState(), TypeToken.of(BlockState.class));
    private ConfigValue<TernaryState> sneakState = new ConfigValue<>("sneak-state", "Sets how the player must be sneaking in order to use the XP Storer.", TernaryState.FALSE, TypeToken.of(TernaryState.class));
    private ConfigValue<Integer> xpPerBottle = new ConfigValue<>("xp-per-bottle", "Sets the amount of XP points required per each bottle.", 16);
    private ConfigValue<Boolean> requireSign = new ConfigValue<>("require-sign", "Require sign always, not just for automatic mode.", false);

    // Range mode configuration
    private ConfigValue<Boolean> allowAutomaticMode = new ConfigValue<>("allow-automatic-mode", "Allows the mechanic to be built with a sign and "
            + "collect XP within a range.", false);
    private ConfigValue<Integer> maximumRange = new ConfigValue<>("maximum-range", "Maximum allowed range for ranged mode.", 15);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        block.load(config);
        requireBottle.load(config);
        sneakState.load(config);
        xpPerBottle.load(config);
        requireSign.load(config);
        allowAutomaticMode.load(config);
        maximumRange.load(config);

        usePermissions.register();
        createPermissions.register();
        rangedPermissions.register();
    }

    @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable Player player) {
        String line2 = SignUtil.getTextRaw(lines.get(2)).trim();
        if (!line2.isEmpty()) {
            // Ranged Mode.
            if (allowAutomaticMode.getValue() && (player == null || rangedPermissions.hasPermission(player))) {
                try {
                    int range = Integer.parseInt(line2);
                    range = Math.max(0, Math.min(range, maximumRange.getValue()));
                    lines.set(2, Text.of(String.valueOf(range)));
                } catch (NumberFormatException e) {
                    lines.set(2, Text.of(Math.min(10, maximumRange.getValue())));
                }
                ((SpongeSelfTriggerManager) CraftBookPlugin.inst().getSelfTriggerManager().get()).register(this, location);
            } else {
                lines.set(2, Text.EMPTY);
                if (player != null) {
                    if (!rangedPermissions.hasPermission(player)) {
                        player.sendMessage(Text.of(TextColors.RED, "You don't have permission to create ranged XPStorers."));
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "Automatic mode for XPStorers is not allowed."));
                    }
                }
            }
        }

        return super.verifyLines(location, lines, player);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().filter(this::isValid).ifPresent(location -> {
            if (!sneakState.getValue().doesPass(player.get(Keys.IS_SNEAKING).orElse(false))
                    || player.get(Keys.EXPERIENCE_LEVEL).orElse(0) < 1
                    || !usePermissions.hasPermission(player)) {
                return;
            }

            AtomicInteger bottleCount = new AtomicInteger(Integer.MAX_VALUE);

            if(requireBottle.getValue()) {
                bottleCount.set(0);
                for (HandType handType : Sponge.getRegistry().getAllOf(HandType.class)) {
                    player.getItemInHand(handType).filter(itemStack -> itemStack.getType() == ItemTypes.GLASS_BOTTLE)
                            .ifPresent((itemStack -> bottleCount.addAndGet(itemStack.getQuantity())));
                }

                if (bottleCount.get() == 0) {
                    player.sendMessage(Text.of(TextColors.RED, "You need a bottle to use this mechanic!"));
                    return;
                }
            }

            int xp = player.get(Keys.TOTAL_EXPERIENCE).orElse(0);

            if (xp < xpPerBottle.getValue()) {
                player.sendMessage(Text.of(TextColors.RED, "Not enough XP!"));
            }

            int outputBottles = (int) Math.min(bottleCount.get(), Math.floor(((float) xp) / xpPerBottle.getValue()));

            if(requireBottle.getValue()) {
                outputBottles = player.getInventory().query(ItemTypes.GLASS_BOTTLE).poll(outputBottles).orElse(ItemStack.of(ItemTypes.GLASS_BOTTLE, 0)).getQuantity();
            }

            // Reset their xp
            player.offer(Keys.EXPERIENCE_LEVEL, 0);
            player.offer(Keys.TOTAL_EXPERIENCE, 0);

            player.offer(Keys.TOTAL_EXPERIENCE, convertXp(location, xp, outputBottles));
        });
    }

    @Override
    public void onThink(Location<World> signLocation) {
        for (Direction directFace : BlockUtil.getDirectFaces()) {
            Location<World> location = signLocation.getRelative(directFace);
            if (isValid(location)) {
                Location<World> chestBlock = location.getRelative(Direction.UP);
                if (chestBlock.getBlockType() == BlockTypes.CHEST || chestBlock.getBlockType() == BlockTypes.TRAPPED_CHEST) {
                    Sign sign = signLocation.getTileEntity().map(tile -> (Sign) tile).get();
                    if (isMechanicSign(sign)) {
                        int distance;
                        try {
                            distance = Math.min(maximumRange.getValue(), Integer.parseInt(SignUtil.getTextRaw(sign, 2)));
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        Chest chest = chestBlock.getTileEntity().map(tile -> (Chest) tile).get();
                        Inventory inventory = chest.getDoubleChestInventory().orElse(chest.getInventory());
                        int bottleCount = Integer.MAX_VALUE;

                        if (requireBottle.getValue()) {
                            bottleCount = inventory.query(ItemTypes.GLASS_BOTTLE).totalItems();
                            if (bottleCount == 0) {
                                continue;
                            }
                        }

                        Set<ExperienceOrb> experienceOrbs = new HashSet<>();
                        int xp = 0;

                        for (Entity entity : LocationUtil.getNearbyEntities(location, new Vector3d(distance, distance, distance))) {
                            if (entity instanceof ExperienceOrb) {
                                experienceOrbs.add((ExperienceOrb) entity);
                                xp += ((ExperienceOrb) entity).experience().get();
                            }
                        }

                        bottleCount = (int) Math.min(bottleCount, Math.floor(((float) xp) / xpPerBottle.getValue()));

                        if(requireBottle.getValue()) {
                            bottleCount = inventory.query(ItemTypes.GLASS_BOTTLE).poll(bottleCount).orElse(ItemStack.of(ItemTypes.GLASS_BOTTLE, 0)).getQuantity();
                        }

                        int remainingXp = convertXp(location, xp, bottleCount);

                        for (ExperienceOrb orb : experienceOrbs) {
                            if (remainingXp > 0) {
                                orb.experience().set(Math.min(5, remainingXp));
                                remainingXp -= 5;
                            } else {
                                orb.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private int convertXp(Location<World> location, int inputXp, int outputBottles) {
        int tempBottles = outputBottles;

        Location<World> chestBlock = location.getRelative(Direction.UP);
        Inventory resultInventory = null;
        if (chestBlock.getBlockType() == BlockTypes.CHEST || chestBlock.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            Chest chest = chestBlock.getTileEntity().map(tile -> (Chest) tile).get();
            resultInventory = chest.getDoubleChestInventory().orElse(chest.getInventory());
        }

        while(tempBottles > 0) {
            ItemStack bottles = ItemStack.of(ItemTypes.EXPERIENCE_BOTTLE, Math.min(tempBottles, 64));

            if (resultInventory == null || !resultInventory.offer(bottles).getRejectedItems().isEmpty()) {
                Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
                item.offer(Keys.REPRESENTED_ITEM, bottles.createSnapshot());

                location.getExtent().spawnEntity(item);
            }

            tempBottles -= 64;
        }

        return inputXp - outputBottles * xpPerBottle.getValue();
    }

    @Override
    public String getPath() {
        return "mechanics/xp_storer";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                usePermissions,
                createPermissions,
                rangedPermissions
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                requireBottle,
                block,
                sneakState,
                xpPerBottle,
                requireSign,
                allowAutomaticMode,
                maximumRange
        };
    }

    @Override
    public boolean isValid(Location<World> location) {
        if (location.getBlock().equals(block.getValue())) {
            if (requireSign.getValue()) {
                for (Location<World> attachedSign : SignUtil.getAttachedSigns(location)) {
                    if (isMechanicSign(attachedSign.getTileEntity().map(tile -> (Sign) tile).get())) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            return SignUtil.isSign(location) && isMechanicSign(location.getTileEntity().map(tile -> (Sign) tile).get());
        }

        return false;
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[XP]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }
}
