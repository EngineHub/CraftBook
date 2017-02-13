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
package com.sk89q.craftbook.sponge.mechanics.signcopier;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.signcopier.command.EditSignCommand;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

@Module(id = "signcopier", name = "SignCopier", onEnable="onInitialize", onDisable="onDisable")
public class SignCopier extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<ItemStack> signCopierItem = new ConfigValue<>("signcopier-item", "The item that triggers the Sign Copier mechanic.", ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.BLACK).build(), TypeToken.of(ItemStack.class));

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.signcopier.use", "Allows the user to copy and paste signs.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode editPermissions = new SpongePermissionNode("craftbook.signcopier.edit", "Allows the user to use the sign edit command.", PermissionDescription.ROLE_USER);

    private CommandMapping signCommandManager;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        signCopierItem.load(config);

        usePermissions.register();

        signs = new HashMap<>();

        CommandSpec signEditCommand = CommandSpec.builder()
                .permission(editPermissions.getNode())
                .arguments(GenericArguments.integer(Text.of("line")), GenericArguments.remainingJoinedStrings(Text.of("text")))
                .executor(new EditSignCommand(this))
                .description(Text.of("Allows editing the currently copied sign!"))
                .build();

        signCommandManager = Sponge.getCommandManager().register(CraftBookPlugin.spongeInst().getContainer(), CommandSpec.builder()
                .child(signEditCommand, "edit")
                .description(Text.of("Base command of the SignCopier mechanic"))
                .build(), "sign", "signcopier").orElse(null);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        signs.clear();

        if (signCommandManager != null) {
            Sponge.getCommandManager().removeMapping(signCommandManager);
        }
    }

    private Map<UUID, List<Text>> signs;

    public Map<UUID, List<Text>> getSigns() {
        return signs;
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent event, @First Player player) {
        HandType hand = event instanceof InteractBlockEvent.Primary
                ? (event instanceof InteractBlockEvent.Primary.MainHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND)
                : (event instanceof InteractBlockEvent.Secondary.MainHand ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND);

        player.getItemInHand(hand).ifPresent(itemStack -> {
            if (itemStack.getItem() == signCopierItem.getValue().getItem()) {
                event.getTargetBlock().getLocation().ifPresent(location -> {
                    if (SignUtil.isSign(location)) {
                        if (usePermissions.hasPermission(player)) {
                            Sign sign = (Sign) location.getTileEntity().get();
                            if (event instanceof InteractBlockEvent.Primary) {
                                List<Text> lines = signs.get(player.getUniqueId());
                                if (lines != null) {
                                    SignData signData = sign.getSignData().copy();
                                    signData = signData.set(Keys.SIGN_LINES, lines);

                                    ChangeSignEvent changeSignEvent = SpongeEventFactory.createChangeSignEvent(
                                            Cause.source(CraftBookPlugin.spongeInst().getContainer()).notifier(player).build(),
                                            sign.getSignData().asImmutable(),
                                            signData,
                                            sign
                                    );

                                    Sponge.getEventManager().post(changeSignEvent);

                                    if (!changeSignEvent.isCancelled()) {
                                        sign.offer(Keys.SIGN_LINES, changeSignEvent.getText().lines().get());
                                        player.sendMessage(Text.of(TextColors.YELLOW, "Pasted sign!"));
                                    } else {
                                        player.sendMessage(Text.of(TextColors.RED, "Not allowed to paste sign!"));
                                    }

                                } else {
                                    player.sendMessage(Text.of(TextColors.RED, "No sign data to paste!"));
                                }

                                event.setCancelled(true);
                            } else if (event instanceof InteractBlockEvent.Secondary) {
                                signs.put(player.getUniqueId(), sign.lines().get());
                                player.sendMessage(Text.of(TextColors.YELLOW, "Copied sign!"));
                                event.setCancelled(true);
                            }
                        } else {
                            player.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this mechanic!"));
                        }
                    }
                });
            }
        });
    }

    @Override
    public String getPath() {
        return "mechanics/sign_copier";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                signCopierItem
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                usePermissions,
                editPermissions
        };
    }
}
