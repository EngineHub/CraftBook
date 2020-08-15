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

package org.enginehub.craftbook.mechanics.area;

import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Area.
 *
 * @author Me4502, Sk89q, Silthus
 */
public class Area extends AbstractCraftBookMechanic {

    protected static Area instance;

    @Override
    public boolean enable() {

        instance = this;

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "area",
            Lists.newArrayList("togglearea"),
            "CraftBook Area Commands",
            AreaCommands::register
        );

        return true;
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

        if (!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[Area]") && !event.getLine(1).equalsIgnoreCase("[SaveArea]"))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        String cbid = player.getCraftBookId();

        if (event.getLine(0).trim().isEmpty()) {
            if (shortenNames && cbid.length() > 14)
                event.setLine(0, ('~' + cbid).substring(0, 14));
            else
                event.setLine(0, '~' + cbid);
        }

        if (event.getLine(1).equalsIgnoreCase("[Area]")) {
            if (!player.hasPermission("craftbook.mech.area.sign.area")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                    player.print("mech.create-permission");
                SignUtil.cancelSignChange(event);
                return;
            }
            event.setLine(1, "[Area]");
        } else if (event.getLine(1).equalsIgnoreCase("[SaveArea]")) {
            if (!player.hasPermission("craftbook.mech.area.sign.savearea")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                    player.print("mech.create-permission");
                SignUtil.cancelSignChange(event);
                return;
            }
            event.setLine(1, "[SaveArea]");
        }
        // check if the namespace and area exists
        if (!isValidArea(event.getLine(0), event.getLine(2), event.getLine(3))) {
            player.printError("mech.area.missing");
            SignUtil.cancelSignChange(event);
            return;
        }

        player.print("mech.area.create");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        boolean save;

        ChangedSign sign = event.getSign();

        if (!sign.getLine(1).equals("[Area]") && !sign.getLine(1).equals("[SaveArea]")) return;

        if (!player.hasPermission("craftbook.mech.area.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.print("mech.use-permission");
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        // check if the namespace and area exists
        if (!isValidArea(sign)) {
            player.printError("mech.area.missing");
            return;
        }
        save = sign.getLine(1).equals("[SaveArea]");

        // toggle the area on or off
        toggle(sign, save);

        event.setCancelled(true);
    }

    private static boolean isValidArea(String namespace, String areaOn, String areaOff) {

        if (CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), namespace, areaOn)) {
            if (areaOff == null || areaOff.isEmpty() || areaOff.equals("--")) return true;
            return CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), namespace, areaOff);
        }
        return false;
    }

    private static boolean isValidArea(ChangedSign sign) {

        String namespace = sign.getLine(0).trim();

        return isValidArea(namespace, sign.getLine(2).trim().toLowerCase(Locale.ENGLISH), sign.getLine(3).trim().toLowerCase(Locale.ENGLISH));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSelfTriggerPing(SelfTriggerPingEvent event) {

        if (SignUtil.isSign(event.getBlock())) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());
            if (sign.getLine(1).equals("[Area]")) {
                isValidArea(sign); //Perform a conversion,
                sign.update(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!allowRedstone) return;
        if (!SignUtil.isSign(event.getBlock())) return;

        boolean save;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Area]") && !sign.getLine(1).equals("[SaveArea]")) return;

        // check if the namespace and area exists
        if (!isValidArea(sign)) return;

        save = sign.getLine(1).equals("[SaveArea]");

        // toggle the area
        toggle(sign, save);
    }

    private static boolean toggle(ChangedSign sign, boolean save) {

        if (!checkSign(sign)) return false;

        try {
            String namespace = sign.getLine(0);
            String id = StringUtils.replace(sign.getLine(2), "-", "").toLowerCase(Locale.ENGLISH);
            String inactiveID = StringUtils.replace(sign.getLine(3), "-", "").toLowerCase(Locale.ENGLISH);

            BlockArrayClipboard copy;

            if (checkToggleState(sign)) {
                copy = CopyManager.getInstance().load(namespace, id);
                copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));

                // if this is a save area save it before toggling off
                if (save) {
                    copy = CopyManager.getInstance().copy(copy.getRegion());
                    CopyManager.getInstance().save(namespace, id, copy);
                }
                // if we are toggling to the second area we dont clear the old area
                if (!inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(namespace, inactiveID);
                    copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                    CopyManager.getInstance().paste(copy);
                } else {
                    CopyManager.getInstance().clear(copy);
                }
                setToggledState(sign, false);
            } else {

                // toggle the area on
                // if this is a save area save it before toggling off
                if (save && !inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(namespace, inactiveID);
                    copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                    copy = CopyManager.getInstance().copy(copy.getRegion());
                    CopyManager.getInstance().save(namespace, inactiveID, copy);
                }

                copy = CopyManager.getInstance().load(namespace, id);
                copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                CopyManager.getInstance().paste(copy);
                setToggledState(sign, true);
            }
            return true;
        } catch (IOException | WorldEditException e) {
            CraftBook.logger.error("Failed to toggle Area: " + e.getMessage());
        }
        return false;
    }

    public static boolean toggleCold(ChangedSign sign) {

        if (!checkSign(sign)) return false;

        boolean toggleOn = coldCheckToggleState(sign);
        boolean save = sign.getLine(1).equalsIgnoreCase("[SaveArea]");

        try {
            String namespace = sign.getLine(0);
            String id = StringUtils.replace(sign.getLine(2), "-", "").toLowerCase(Locale.ENGLISH);
            String inactiveID = StringUtils.replace(sign.getLine(3), "-", "").toLowerCase(Locale.ENGLISH);

            BlockArrayClipboard copy;

            if (toggleOn) {
                copy = CopyManager.getInstance().load(namespace, id);
                copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));

                // if this is a save area save it before toggling off
                if (save) {
                    copy = CopyManager.getInstance().copy(copy.getRegion());
                    CopyManager.getInstance().save(namespace, id, copy);
                }
                // if we are toggling to the second area we dont clear the old area
                if (!inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(namespace, inactiveID);
                    copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                    CopyManager.getInstance().paste(copy);
                } else {
                    CopyManager.getInstance().clear(copy);
                }
                setToggledState(sign, false);
            } else {
                // toggle the area on
                // if this is a save area save it before toggling off
                if (save && !inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(namespace, inactiveID);
                    copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                    copy = CopyManager.getInstance().copy(copy.getRegion());
                    CopyManager.getInstance().save(namespace, inactiveID, copy);
                } else {
                    copy = CopyManager.getInstance().load(namespace, id);
                    copy.getRegion().setWorld(BukkitAdapter.adapt(sign.getBlock().getWorld()));
                }
                CopyManager.getInstance().paste(copy);
                setToggledState(sign, true);
            }
            return true;
        } catch (IOException | WorldEditException e) {
            CraftBook.logger.error("Failed to cold toggle Area: " + e.getMessage());
        }
        return false;
    }

    private static boolean checkSign(ChangedSign sign) {

        String namespace = sign.getLine(0);
        String id = sign.getLine(2).toLowerCase(Locale.ENGLISH);

        return !id.isEmpty() && namespace != null && !namespace.isEmpty();
    }

    // pattern to check where the markers for on and off state are
    private static final Pattern pattern = Pattern.compile("^\\-[A-Za-z0-9_]*?\\-$");

    private static boolean checkToggleState(ChangedSign sign) {

        return coldCheckToggleState(sign);
    }

    private static boolean coldCheckToggleState(ChangedSign sign) {

        String line3 = sign.getLine(2).toLowerCase(Locale.ENGLISH);
        String line4 = sign.getLine(3).toLowerCase(Locale.ENGLISH);
        return pattern.matcher(line3).matches() || !(line4.equals("--") || pattern.matcher(line4).matches());
    }

    private static void setToggledState(ChangedSign sign, boolean state) {

        int toToggleOn = state ? 2 : 3;
        int toToggleOff = state ? 3 : 2;
        sign.setLine(toToggleOff, StringUtils.replace(sign.getLine(toToggleOff), "-", ""));
        sign.setLine(toToggleOn, '-' + sign.getLine(toToggleOn) + '-');
        sign.update(false);
    }

    private boolean allowRedstone;
    boolean useSchematics;
    boolean shortenNames;
    int maxAreaSize;
    int maxAreasPerUser;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("allow-redstone", "Allow ToggleAreas to be toggled via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);

        config.setComment("use-schematics", "Use Schematics for saving areas. This allows support of all blocks and chest/sign data.");
        useSchematics = config.getBoolean("use-schematics", true);

        config.setComment("shorten-long-names", "If this is enabled, namespaces too long to fit on signs will be shortened.");
        shortenNames = config.getBoolean("shorten-long-names", true);

        config.setComment("max-size", "Sets the max amount of blocks that a ToggleArea can hold.");
        maxAreaSize = config.getInt("max-size", 5000);

        config.setComment("max-per-user", "Sets the max amount of ToggleAreas that can be within one namespace.");
        maxAreasPerUser = config.getInt("max-per-user", 30);
    }
}