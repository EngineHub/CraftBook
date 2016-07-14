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
package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.DeleteCommand;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.ListCommand;
import com.sk89q.craftbook.sponge.mechanics.area.complex.command.SaveCommand;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeMechanicData;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.regex.Pattern;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

@Module(moduleName = "Area", onEnable="onInitialize", onDisable="onDisable")
public class ComplexArea extends SpongeSignMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.area.create", "Allows the user to create the ToggleArea mechanic.", PermissionDescription.ROLE_USER);
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

    private static final Pattern MARKER_PATTERN = Pattern.compile("^\\-[A-Za-z0-9_]*?\\-$");

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        maxAreaSize.load(config);
        maxPerUser.load(config);

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

        Sponge.getCommandManager().register(CraftBookPlugin.inst(), mainAreaCommand, "area", "togglearea");
    }

    @Listener
    public void onSignChange(ChangeSignEvent event, @Named(NamedCause.SOURCE) Player player) {
        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getText(), 1).equalsIgnoreCase(line)) {
                if(!createPermissions.hasPermission(player) && !(line.equals("[SaveArea]") && createSavePermissions.hasPermission(player))) {
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    event.getText().lines().set(1, Text.of(line));

                    String namespace = event.getText().lines().get(0).toPlain();
                    boolean personal = false;
                    if (namespace.toLowerCase().equals("global")) {
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

                    event.getText().lines().set(0, Text.of(personal ? player.getName() : namespace));

                    if(!isValidArea(namespace, event.getText().lines().get(2).toPlain(), event.getText().lines().get(3).toPlain())) {
                        player.sendMessage(Text.of(TextColors.RED, "This area is missing!"));
                        event.setCancelled(true);
                        return;
                    }

                    ComplexAreaData data = getData(ComplexAreaData.class, event.getTargetTile().getLocation());
                    data.namespace = namespace;
                    player.sendMessage(Text.of(TextColors.YELLOW, "Created ToggleArea!"));
                }

                break;
            }
        }
    }

    @Listener
    public void onRightClick(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player human) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (isValid(location)) {
                location.getTileEntity().ifPresent((sign -> {
                    if (human == null || usePermissions.hasPermission(human)) {
                        if (triggerMechanic(location, (Sign) sign)) {
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

    public boolean triggerMechanic(Location<World> location, Sign sign) {
        boolean save = SignUtil.getTextRaw(sign, 1).equals("[SaveArea]");

        ComplexAreaData data = getData(ComplexAreaData.class, location);

        String namespace = data.namespace;
        String id = StringUtils.replace(SignUtil.getTextRaw(sign, 2), "-", "").toLowerCase();
        String inactiveID = StringUtils.replace(SignUtil.getTextRaw(sign, 3), "-", "").toLowerCase();

        try {
            CuboidCopy copy;

            if (checkToggleState(sign)) {
                copy = CopyManager.load(location.getExtent(), namespace, id);

                if (save) {
                    copy.copy();
                    CopyManager.save(namespace, id, copy);
                }

                if (!inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.load(location.getExtent(), namespace, inactiveID);
                    copy.paste();
                } else {
                    copy.clear();
                }

                setToggleState(sign, false);
            } else {
                if (save && !inactiveID.isEmpty() && !inactiveID.equals("--")) {
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
            if (areaOff == null || areaOff.isEmpty() || areaOff.equals("--")) return true;
            if (CopyManager.isExistingArea(CraftBookPlugin.inst().getWorkingDirectory(), namespace, areaOff)) return true;
        }
        return false;
    }

    private static boolean checkToggleState(Sign sign) {
        String line3 = SignUtil.getTextRaw(sign, 2).toLowerCase();
        String line4 = SignUtil.getTextRaw(sign, 3).toLowerCase();
        return MARKER_PATTERN.matcher(line3).matches() || !(line4.equals("--") || MARKER_PATTERN.matcher(line4).matches());
    }

    private static void setToggleState(Sign sign, boolean state) {
        int toToggleOn = state ? 2 : 3;
        int toToggleOff = state ? 3 : 2;
        sign.lines().set(toToggleOff, Text.of(sign.lines().get(toToggleOff).toPlain().replace("-", "")));
        sign.lines().set(toToggleOn, Text.of("-", sign.lines().get(toToggleOn), "-"));
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

    public static class ComplexAreaData extends SpongeMechanicData {
        public String namespace;
    }
}
