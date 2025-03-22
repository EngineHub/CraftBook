/*
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

package org.enginehub.craftbook.mechanics.area.clipboard;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.util.persistence.OwnedSignHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class ToggleArea extends AbstractCraftBookMechanic {

    private static final TextReplacementConfig DASH_REMOVER = TextReplacementConfig.builder().matchLiteral("-").replacement("").build();

    public ToggleArea(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "area",
            List.of("togglearea"),
            "CraftBook ToggleArea Commands",
            (commandManager, registration) -> AreaCommands.register(commandManager, registration, this)
        );
    }

    @Override
    public void disable() {
        super.disable();

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("area");
        registrar.unregisterTopLevel("togglearea");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[togglearea]") && !signLine1.equalsIgnoreCase("[toggleareasave]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        String namespace;

        String signLine0 = PlainTextComponentSerializer.plainText().serialize(event.line(0));
        if (signLine0.trim().isEmpty()) {
            event.line(0, Component.text("~" + player.getName(), null, TextDecoration.ITALIC));
            namespace = player.getUniqueId().toString();

            // Set the owner of the sign once the sign has been created.
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                if (!SignUtil.isSign(event.getBlock())) {
                    return;
                }

                // Ensure this is still a ToggleArea sign and wasn't cancelled.
                Sign sign = (Sign) event.getBlock().getState(false);
                String innerLine1 = PlainTextComponentSerializer.plainText().serialize(sign.getSide(event.getSide()).line(1));
                if (!innerLine1.equals("[ToggleArea]") && !innerLine1.equals("[ToggleAreaSave]")) {
                    return;
                }

                OwnedSignHelper.setOwner(sign, player.getUniqueId());
            });
        } else {
            namespace = signLine0;
        }

        boolean saving = false;

        if (signLine1.equalsIgnoreCase("[togglearea]")) {
            if (!player.hasPermission("craftbook.togglearea.create")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError(TranslatableComponent.of(
                        "craftbook.mechanisms.create-permission",
                        TextComponent.of(getMechanicType().getName())
                    ));
                }
                SignUtil.cancelSignChange(event);
                return;
            }
            event.line(1, Component.text("[ToggleArea]"));
        } else if (signLine1.equalsIgnoreCase("[toggleareasave]")) {
            if (!player.hasPermission("craftbook.togglearea.create.save")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError(TranslatableComponent.of(
                        "craftbook.mechanisms.create-permission",
                        TextComponent.of(getMechanicType().getName())
                    ));
                }
                SignUtil.cancelSignChange(event);
                return;
            }
            event.line(1, Component.text("[ToggleAreaSave]"));
            saving = true;
        }

        ToggleAreaData toggleAreaData = new ToggleAreaData(
            namespace,
            PlainTextComponentSerializer.plainText().serialize(event.line(2)),
            PlainTextComponentSerializer.plainText().serialize(event.line(3))
        );

        // check if the namespace and area exists
        if (!isValidArea(toggleAreaData)) {
            player.printError(TranslatableComponent.of("craftbook.togglearea.missing-area"));
            SignUtil.cancelSignChange(event);
            return;
        }

        player.printInfo(TranslatableComponent.of(saving ? "craftbook.togglearea.create.save" : "craftbook.togglearea.create"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        ChangedSign sign = event.getSign();

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[ToggleArea]") && !line1.equals("[ToggleAreaSave]")) {
            return;
        }

        if (!player.hasPermission("craftbook.togglearea.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        ToggleAreaData toggleAreaData = getToggleAreaData(sign);

        // check if the namespace and area exists
        if (!isValidArea(toggleAreaData)) {
            player.printError(TranslatableComponent.of("craftbook.togglearea.missing-area"));
            return;
        }

        boolean save = line1.equals("[ToggleAreaSave]");

        // toggle the area on or off
        if (toggle(sign, toggleAreaData, save)) {
            player.printInfo(TranslatableComponent.of("craftbook.togglearea.toggled"));
        } else {
            player.printError(TranslatableComponent.of("craftbook.togglearea.toggle-failed"));
        }

        event.setCancelled(true);
    }

    private boolean isValidArea(ToggleAreaData toggleAreaData) {
        if (CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), toggleAreaData.namespace, toggleAreaData.areaOn)) {
            if (toggleAreaData.areaOff == null || toggleAreaData.areaOff.isEmpty() || toggleAreaData.areaOff.equals("--")) {
                return true;
            }
            return CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), toggleAreaData.namespace, toggleAreaData.areaOff);
        }
        return false;
    }

    private ToggleAreaData getToggleAreaData(ChangedSign sign) {
        String namespace;
        if (OwnedSignHelper.hasOwner(sign.getSign())) {
            namespace = OwnedSignHelper.getOwner(sign.getSign()).toString();
        } else {
            namespace = PlainTextComponentSerializer.plainText().serialize(sign.getLine(0));
        }

        String areaOn = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2).replaceText(DASH_REMOVER)).toLowerCase(Locale.ENGLISH);
        String areaOff = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3).replaceText(DASH_REMOVER)).toLowerCase(Locale.ENGLISH);

        return new ToggleAreaData(namespace, areaOn, areaOff);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!allowRedstone || !EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        boolean save;

        Sign bukkitSign = (Sign) event.getBlock().getState(false);
        Side side = bukkitSign.getInteractableSideFor(event.getSource().getLocation());
        ChangedSign sign = ChangedSign.create(bukkitSign, side);

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[ToggleArea]") && !line1.equals("[ToggleAreaSave]")) {
            return;
        }

        ToggleAreaData toggleAreaData = getToggleAreaData(sign);
        // check if the namespace and area exists
        if (!isValidArea(toggleAreaData)) {
            return;
        }

        save = line1.equals("[ToggleAreaSave]");

        // toggle the area
        toggle(sign, toggleAreaData, save);
    }

    private boolean toggle(ChangedSign sign, ToggleAreaData toggleAreaData, boolean save) {
        try {
            BlockArrayClipboard copy;
            World weWorld = BukkitAdapter.adapt(sign.getBlock().getWorld());

            if (checkToggleState(sign)) {
                copy = CopyManager.getInstance().load(toggleAreaData.namespace, toggleAreaData.areaOn);

                // if this is a save area save it before toggling off
                if (save) {
                    copy = CopyManager.getInstance().copy(copy.getRegion(), weWorld);
                    CopyManager.getInstance().save(toggleAreaData.namespace, toggleAreaData.areaOn, copy);
                }
                // if we are toggling to the second area we dont clear the old area
                if (!toggleAreaData.areaOff.isEmpty() && !toggleAreaData.areaOff.equals("--")) {
                    copy = CopyManager.getInstance().load(toggleAreaData.namespace, toggleAreaData.areaOff);
                    CopyManager.getInstance().paste(copy, weWorld);
                } else {
                    CopyManager.getInstance().clear(copy, weWorld);
                }
                setToggledState(sign, false);
            } else {

                // toggle the area on
                // if this is a save area save it before toggling off
                if (save && !toggleAreaData.areaOff.isEmpty() && !toggleAreaData.areaOff.equals("--")) {
                    copy = CopyManager.getInstance().load(toggleAreaData.namespace, toggleAreaData.areaOff);
                    copy = CopyManager.getInstance().copy(copy.getRegion(), weWorld);
                    CopyManager.getInstance().save(toggleAreaData.namespace, toggleAreaData.areaOff, copy);
                }

                copy = CopyManager.getInstance().load(toggleAreaData.namespace, toggleAreaData.areaOn);
                CopyManager.getInstance().paste(copy, weWorld);
                setToggledState(sign, true);
            }
            return true;
        } catch (IOException | WorldEditException e) {
            CraftBook.LOGGER.error("Failed to toggle ToggleArea: " + e.getMessage());
        }
        return false;
    }

    public boolean toggleCold(Block block) {
        Sign bukkitSign = (Sign) block.getState(false);
        ChangedSign sign = null;
        for (Side side : Side.values()) {
            ChangedSign testSign = ChangedSign.create(bukkitSign, side);
            String signLine1 = PlainTextComponentSerializer.plainText().serialize(testSign.getLine(1));
            if (signLine1.equals("[ToggleArea]") || signLine1.equals("[ToggleAreaSave]")) {
                sign = testSign;
                break;
            }
        }
        if (sign == null) {
            return false;
        }

        boolean save = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1)).equalsIgnoreCase("[ToggleAreaSave]");
        ToggleAreaData toggleAreaData = getToggleAreaData(sign);

        return toggle(sign, toggleAreaData, save);
    }

    // pattern to check where the markers for on and off state are
    private static final Pattern TOGGLED_ON_PATTERN = Pattern.compile("^-[A-Za-z0-9_]*?-$");

    private boolean checkToggleState(ChangedSign sign) {
        String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).toLowerCase(Locale.ENGLISH);
        String line4 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3)).toLowerCase(Locale.ENGLISH);
        return TOGGLED_ON_PATTERN.matcher(line3).matches() || !(line4.equals("--") || TOGGLED_ON_PATTERN.matcher(line4).matches());
    }

    private void setToggledState(ChangedSign sign, boolean state) {
        int toToggleOn = state ? 2 : 3;
        int toToggleOff = state ? 3 : 2;
        sign.setLine(toToggleOff, sign.getLine(toToggleOff).replaceText(DASH_REMOVER));
        sign.setLine(toToggleOn, Component.text('-').append(sign.getLine(toToggleOn)).append(Component.text('-')));
        updateOwnerName(sign);
        sign.update(false);
    }

    private void updateOwnerName(ChangedSign sign) {
        if (!OwnedSignHelper.hasOwner(sign.getSign())) {
            return;
        }

        UUID owner = OwnedSignHelper.getOwner(sign.getSign());

        String existingName = PlainTextComponentSerializer.plainText().serialize(sign.getLine(0));
        if (existingName.startsWith("~")) {
            existingName = existingName.substring(1);
        }

        String name = Bukkit.getOfflinePlayer(owner).getName();
        if (name != null && !name.equals(existingName)) {
            sign.setLine(0, Component.text("~" + name, null, TextDecoration.ITALIC));
        }
    }

    private boolean allowRedstone;
    boolean removeEntitiesOnToggle;
    int maxAreaSize;
    int maxAreasPerUser;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allow ToggleAreas to be toggled via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);

        config.setComment("remove-entities-on-toggle", "Whether the area toggling will remove entities within it.");
        removeEntitiesOnToggle = config.getBoolean("remove-entities-on-toggle", false);

        config.setComment("max-size", "Sets the max amount of blocks that a ToggleArea can hold.");
        maxAreaSize = config.getInt("max-size", 5000);

        config.setComment("max-per-user", "Sets the max amount of ToggleAreas that can be within one personal namespace.");
        maxAreasPerUser = config.getInt("max-per-user", 30);
    }

    public record ToggleAreaData(String namespace, String areaOn, String areaOff) {
    }
}
