/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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
package org.enginehub.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.core.util.ConfigValue;
import org.enginehub.craftbook.core.util.CraftBookException;
import org.enginehub.craftbook.core.util.PermissionNode;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.enginehub.craftbook.sponge.util.ItemUtil;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.enginehub.craftbook.sponge.util.SpongePermissionNode;
import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.mutable.KeyLockData;
import org.enginehub.craftbook.sponge.util.prompt.ItemStackSnapshotDataPrompt;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Module(id = "hiddenswitch", name = "HiddenSwitch", onEnable="onInitialize", onDisable="onDisable")
public class HiddenSwitch extends SpongeSignMechanic implements DocumentationProvider {

    private static ItemStackSnapshotDataPrompt ITEMS_PROMPT = new ItemStackSnapshotDataPrompt(
            1, 1, "Enter Key"
    );

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> allowAnySide = new ConfigValue<>("allow-any-side", "Allows the user to click any side of the attached block.", false);

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.hidden-switch", "Allows the user to create Hidden Switches", PermissionDescription.ROLE_USER);
    private SpongePermissionNode usePermission = new SpongePermissionNode("craftbook.hidden-switch.use", "Allows the user to use Hidden Switches", PermissionDescription.ROLE_USER);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowAnySide.load(config);

        createPermissions.register();
        usePermission.register();
    }

    @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable Player player) {
        if (SignUtil.getTextRaw(lines.get(2)).equalsIgnoreCase("locked")) {
            ITEMS_PROMPT.getData(player, itemStacks -> {
                KeyLockData keyLockData = new KeyLockData(itemStacks.get(0));
                location.offer(keyLockData);
            });
        }

        return super.verifyLines(location, lines, player);
    }

    @Listener
    public void onClick(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            List<Location<World>> signLocations = new ArrayList<>();

            if (allowAnySide.getValue()) {
                signLocations.addAll(BlockUtil.getAdjacentExcept(location, event.getTargetSide()));
            } else {
                signLocations.add(location.getRelative(event.getTargetSide().getOpposite()));
            }

            for (Location<World> signLocation : signLocations) {
                if (SignUtil.isSign(signLocation)) {
                    Sign sign = (Sign) signLocation.getTileEntity().get();
                    if (isMechanicSign(sign)) {
                        if (!usePermission.hasPermission(player)) {
                            return;
                        }
                        Optional<ItemStackSnapshot> key = sign.get(CraftBookKeys.KEY_LOCK);
                        if (key.isPresent()) {
                            if (ItemUtil.ALL_ANY_SIZE.compare(key.get().createStack(), player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty())) != 0
                                    && ItemUtil.ALL_ANY_SIZE.compare(key.get().createStack(), player.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStack.empty())) != 0) {
                                player.sendMessage(Text.of(TextColors.RED, "The key doesn't fit."));
                                return;
                            }
                        }
                        if (toggleSwitches(sign, player)) {
                            player.sendMessage(Text.of(TextColors.YELLOW, "You hear the muffled click of a switch."));
                        }
                        break;
                    }
                }
            }
        });
    }

    private static boolean toggleSwitches(Sign sign, Player player) {
        Direction[] checkFaces = new Direction[4];
        checkFaces[0] = Direction.UP;
        checkFaces[1] = Direction.DOWN;

        switch (SignUtil.getFacing(sign.getLocation())) {
            case EAST:
            case WEST:
                checkFaces[2] = Direction.NORTH;
                checkFaces[3] = Direction.SOUTH;
                break;
            case NONE:
                break;
            default:
                checkFaces[2] = Direction.EAST;
                checkFaces[3] = Direction.WEST;
                break;
        }

        boolean found = false;

        for (Direction direction : checkFaces) {
            if (direction == null) continue;
            Location<World> checkBlock = sign.getLocation().getRelative(direction);

            if (checkBlock.getBlock().getType() == BlockTypes.LEVER) {
                Sponge.getCauseStackManager().pushCause(player);
                checkBlock.offer(Keys.POWERED, !checkBlock.get(Keys.POWERED).orElse(false));
                Sponge.getCauseStackManager().popCause();
                found = true;
            } else if (checkBlock.getBlock().getType() == BlockTypes.STONE_BUTTON || checkBlock.getBlock().getType() == BlockTypes.WOODEN_BUTTON) {
                Sponge.getCauseStackManager().pushCause(player);
                checkBlock.offer(Keys.POWERED, true);
                Sponge.getCauseStackManager().popCause();
                checkBlock.addScheduledUpdate(1, checkBlock.getBlock().getType() == BlockTypes.STONE_BUTTON ? 20 : 30);
                found = true;
            }
        }

        return found;
    }

    @Override
    public String getPath() {
        return "mechanics/hidden_switch";
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[X]"
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermissions,
                usePermission
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                allowAnySide
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }
}
