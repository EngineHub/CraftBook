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
package com.sk89q.craftbook.sponge.mechanics.area.complex;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.DeleteCommand;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.ListCommand;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.SaveCommand;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import com.sk89q.craftbook.sponge.util.data.mutable.NamespaceData;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.regex.Pattern;

@Module(id = "area", name = "Area", onEnable="onInitialize", onDisable="onDisable")
public class ComplexArea extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.area", "Allows the user to create the ToggleArea mechanic.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode createSavePermissions = new SpongePermissionNode("craftbook.area.create.save", "Allows the user to create the ToggleArea Save-Only mechanic.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode createGlobalPermissions = new SpongePermissionNode("craftbook.area.create.global", "Allows the user to create global ToggleArea mechanics.", PermissionDescription.ROLE_STAFF);
    private SpongePermissionNode createOtherPermissions = new SpongePermissionNode("craftbook.area.create.other", "Allows the user to create ToggleArea mechanics with other namespaces.", PermissionDescription.ROLE_STAFF);
    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.area.use", "Allows the user to use the ToggleArea mechanic.", PermissionDescription.ROLE_USER);

    private SpongePermissionNode commandSavePermissions = new SpongePermissionNode("craftbook.area.save", "Allows the user to save ToggleArea regions.", PermissionDescription.ROLE_USER);
    public SpongePermissionNode commandSaveOtherPermissions = new SpongePermissionNode("craftbook.area.save.other", "Allows the user to save ToggleArea regions.", PermissionDescription.ROLE_STAFF);
    public SpongePermissionNode commandSaveBypassLimitPermissions = new SpongePermissionNode("craftbook.area.save.bypass-limit", "Allows the user to bypass area limits.", PermissionDescription.ROLE_ADMIN);
    private SpongePermissionNode commandDeletePermissions = new SpongePermissionNode("craftbook.area.delete", "Allows the user to delete ToggleArea regions.", PermissionDescription.ROLE_USER);
    public SpongePermissionNode commandDeleteOtherPermissions = new SpongePermissionNode("craftbook.area.delete.other", "Allows the user to delete ToggleArea regions.", PermissionDescription.ROLE_STAFF);
    private SpongePermissionNode commandListPermissions = new SpongePermissionNode("craftbook.area.list", "Allows the user to list ToggleArea regions.", PermissionDescription.ROLE_USER);
    public SpongePermissionNode commandListOtherPermissions = new SpongePermissionNode("craftbook.area.list.other", "Allows the user to list ToggleArea regions.", PermissionDescription.ROLE_STAFF);

    public ConfigValue<Integer> maxAreaSize = new ConfigValue<>("maximum-size", "The maximum amount of blocks that the saved areas can contain. -1 to disable limit.", 5000);
    public ConfigValue<Integer> maxPerUser = new ConfigValue<>("maximum-per-user", "The maximum amount of areas that a namespace can own. 0 to disable limit.", 30);
    public ConfigValue<Boolean> allowRedstone = new ConfigValue<>("allow-redstone", "Whether to allow redstone to be used to trigger this mechanic or not", true);

    private CommandMapping areaCommandMapping;

    private static final Pattern MARKER_PATTERN = Pattern.compile("^-[A-Za-z0-9_]*?-$");

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        maxAreaSize.load(config);
        maxPerUser.load(config);
        allowRedstone.load(config);

        CommandSpec saveCommand = CommandSpec.builder()
                .description(Text.of("Command to save selected WorldEdit regions."))
                .permission(commandSavePermissions.getNode())
                .executor(new SaveCommand(this))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("id"))), GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("namespace")))))
                .build();

        CommandSpec deleteCommand = CommandSpec.builder()
                .description(Text.of("Command to delete saved ToggleArea regions."))
                .permission(commandDeletePermissions.getNode())
                .executor(new DeleteCommand(this))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("id"))), GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("namespace")))))
                .build();

        CommandSpec listCommand = CommandSpec.builder()
                .description(Text.of("Command to list saved ToggleArea regions."))
                .permission(commandListPermissions.getNode())
                .executor(new ListCommand(this))
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("namespace")))))
                .build();

        CommandSpec mainAreaCommand = CommandSpec.builder()
                .description(Text.of("Base command for the ToggleArea system."))
                .child(saveCommand, "save")
                .child(deleteCommand, "delete", "del")
                .child(listCommand, "list")
                .build();

        areaCommandMapping = Sponge.getCommandManager().register(CraftBookPlugin.inst(), mainAreaCommand, "area", "togglearea").orElse(null);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (areaCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(areaCommandMapping);
        }
    }

    @Listener
    public void onSignChange(ChangeSignEvent event, @Named(NamedCause.SOURCE) Player player) {
        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getText(), 1).equalsIgnoreCase(line)) {
                if(!createPermissions.hasPermission(player) && !("[SaveArea]".equals(line) && createSavePermissions.hasPermission(player))) {
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    List<Text> lines = event.getText().lines().get();
                    lines.set(1, Text.of(line));

                    String namespace = lines.get(0).toPlain();
                    boolean personal = false;
                    if ("global".equals(namespace.toLowerCase())) {
                        if (!createGlobalPermissions.hasPermission(player)) {
                            player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create global ToggleAreas!"));
                            event.setCancelled(true);
                            return;
                        } else {
                            namespace = "GLOBAL";
                        }
                    } else if (namespace.trim().isEmpty() || !createOtherPermissions.hasPermission(player)) {
                        namespace = player.getUniqueId().toString();
                        personal = true;
                    }

                    lines.set(0, Text.of(personal ? player.getName() : namespace));

                    if(!isValidArea(namespace, lines.get(2).toPlain(), lines.get(3).toPlain())) {
                        player.sendMessage(Text.of(TextColors.RED, "This area is missing!"));
                        event.setCancelled(true);
                        return;
                    }

                    event.getTargetTile().offer(new NamespaceData(namespace));

                    event.getText().set(Keys.SIGN_LINES, lines);

                    player.sendMessage(Text.of(TextColors.YELLOW, "Created ToggleArea!"));
                }

                break;
            }
        }
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
                triggerMechanic(block, sign, isPowered);
                block.offer(new LastPowerData(isPowered ? 15 : 0));
            }
        }
    }

    @Listener
    public void onRightClick(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player human) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (isValid(location)) {
                location.getTileEntity().ifPresent((sign -> {
                    if (human == null || usePermissions.hasPermission(human)) {
                        if (triggerMechanic(location, (Sign) sign, null)) {
                            event.setCancelled(true);
                            if (human != null)
                                human.sendMessage(Text.of(TextColors.YELLOW, "Toggled!"));
                        }
                    } else {
                        human.sendMessage(USE_PERMISSIONS);
                    }
                }));
            }
        });
    }

    public boolean triggerMechanic(Location<World> location, Sign sign, Boolean forceState) {
        boolean save = "[SaveArea]".equals(SignUtil.getTextRaw(sign, 1));

        String namespace = sign.get(CraftBookKeys.NAMESPACE).orElse(null);
        String id = StringUtils.replace(SignUtil.getTextRaw(sign, 2), "-", "").toLowerCase();
        String inactiveID = StringUtils.replace(SignUtil.getTextRaw(sign, 3), "-", "").toLowerCase();

        if (namespace == null) {
            return false;
        }

        try {
            CuboidCopy copy;

            boolean state = checkToggleState(sign);
            if (forceState != null) {
                state = forceState;
            }

            if (state) {
                copy = CopyManager.load(location.getExtent(), namespace, id);

                if (save) {
                    copy.copy();
                    CopyManager.save(namespace, id, copy);
                }

                if (!inactiveID.isEmpty() && !"--".equals(inactiveID)) {
                    copy = CopyManager.load(location.getExtent(), namespace, inactiveID);
                    copy.paste();
                } else {
                    copy.clear();
                }

                setToggleState(sign, false);
            } else {
                if (save && !inactiveID.isEmpty() && !"--".equals(inactiveID)) {
                    copy = CopyManager.load(location.getExtent(), namespace, inactiveID);
                    copy.copy();
                    CopyManager.save(namespace, inactiveID, copy);
                }

                copy = CopyManager.load(location.getExtent(), namespace, id);
                copy.paste();
                setToggleState(sign, true);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean isValidArea(String namespace, String areaOn, String areaOff) {
        if (CopyManager.isExistingArea(CraftBookPlugin.inst().getWorkingDirectory(), namespace, areaOn)) {
            if (areaOff == null || areaOff.isEmpty() || "--".equals(areaOff)) return true;
            if (CopyManager.isExistingArea(CraftBookPlugin.inst().getWorkingDirectory(), namespace, areaOff)) return true;
        }
        return false;
    }

    private static boolean checkToggleState(Sign sign) {
        String line3 = SignUtil.getTextRaw(sign, 2).toLowerCase();
        String line4 = SignUtil.getTextRaw(sign, 3).toLowerCase();
        return MARKER_PATTERN.matcher(line3).matches() || !("--".equals(line4) || MARKER_PATTERN.matcher(line4).matches());
    }

    private static void setToggleState(Sign sign, boolean state) {
        int toToggleOn = state ? 2 : 3;
        int toToggleOff = state ? 3 : 2;
        List<Text> lines = sign.lines().get();
        lines.set(toToggleOff, Text.of(sign.lines().get(toToggleOff).toPlain().replace("-", "")));
        lines.set(toToggleOn, Text.of("-", sign.lines().get(toToggleOn), "-"));
        sign.offer(Keys.SIGN_LINES, lines);
    }

    public String[] getValidSigns() {
        return new String[]{
                "[SaveArea]",
                "[Area]"
        };
    }

    @Override
    public String getName() {
        return "ToggleArea";
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public String getPath() {
        return "mechanics/togglearea";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                maxAreaSize,
                maxPerUser,
                allowRedstone
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                createPermissions,
                usePermissions,
                createSavePermissions,
                createGlobalPermissions,
                createOtherPermissions,
                commandSavePermissions,
                commandSaveOtherPermissions,
                commandSaveBypassLimitPermissions,
                commandListPermissions,
                commandListOtherPermissions,
                commandDeletePermissions,
                commandDeleteOtherPermissions
        };
    }
}
