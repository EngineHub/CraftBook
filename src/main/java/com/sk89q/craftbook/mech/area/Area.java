package com.sk89q.craftbook.mech.area;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.data.DataException;

/**
 * Area.
 *
 * @author Me4502, Sk89q, Silthus
 */
public class Area extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[Area]") && !event.getLine(1).equalsIgnoreCase("[SaveArea]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        String cbid = player.getCraftBookId();

        if (event.getLine(0).trim().isEmpty()) {
            if (CraftBookPlugin.inst().getConfiguration().areaShortenNames && cbid.length() > 14)
                event.setLine(0, ("~" + cbid).substring(0, 14));
            else
                event.setLine(0, "~" + cbid);
        }

        if (event.getLine(1).equalsIgnoreCase("[Area]")) {
            if(!player.hasPermission("craftbook.mech.area.sign.area")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.print("mech.create-permission");
                SignUtil.cancelSign(event);
                return;
            }
            event.setLine(1, "[Area]");
        } else if (event.getLine(1).equalsIgnoreCase("[SaveArea]")) {
            if(!player.hasPermission("craftbook.mech.area.sign.savearea")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.print("mech.create-permission");
                SignUtil.cancelSign(event);
                return;
            }
            event.setLine(1, "[SaveArea]");
        }
        // check if the namespace and area exists
        if(!isValidArea(event.getLine(0), event.getLine(2), event.getLine(3))) {
            player.printError("mech.area.missing");
            SignUtil.cancelSign(event);
            return;
        }

        player.print("mech.area.create");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        boolean save = false;

        ChangedSign sign = event.getSign();

        if (!sign.getLine(1).equals("[Area]") && !sign.getLine(1).equals("[SaveArea]")) return;

        if (!player.hasPermission("craftbook.mech.area.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.print("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        // check if the namespace and area exists
        if(!isValidArea(sign)) {
            player.printError("mech.area.missing");
            return;
        }
        save = sign.getLine(1).equals("[SaveArea]");

        // toggle the area on or off
        toggle(event.getClickedBlock(), sign, save);

        event.setCancelled(true);
    }

    private boolean isValidArea(String namespace, String areaOn, String areaOff) {

        if (CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), namespace, areaOn)) {
            if (areaOff == null || areaOff.isEmpty() || areaOff.equals("--")) return true;
            if (CopyManager.isExistingArea(CraftBookPlugin.inst().getDataFolder(), namespace, areaOff)) return true;
        }
        return false;
    }

    private boolean isValidArea(ChangedSign sign) {

        String namespace = sign.getLine(0).trim();

        OfflinePlayer player = Bukkit.getOfflinePlayer(namespace.replace("~", ""));
        if(player.hasPlayedBefore()) {
            String originalNamespace = namespace;
            namespace = "~" + CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId());
            CopyManager.renameNamespace(CraftBookPlugin.inst().getDataFolder(), originalNamespace, namespace);
            sign.setLine(0, namespace);
        }

        return isValidArea(namespace, sign.getLine(2).trim().toLowerCase(Locale.ENGLISH), sign.getLine(3).trim().toLowerCase(Locale.ENGLISH));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSelfTriggerPing(SelfTriggerPingEvent event) {

        if(SignUtil.isSign(event.getBlock())) {
            ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());
            if(sign.getLine(1).equals("[Area]")) {
                isValidArea(sign); //Perform a conversion,
                sign.update(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!CraftBookPlugin.inst().getConfiguration().areaAllowRedstone) return;
        if (!SignUtil.isSign(event.getBlock())) return;

        boolean save = false;

        ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Area]") && !sign.getLine(1).equals("[SaveArea]")) return;

        // check if the namespace and area exists
        if(!isValidArea(sign)) return;

        save = sign.getLine(1).equals("[SaveArea]");

        // toggle the area
        toggle(event.getBlock(), sign, save);
    }

    private boolean toggle(Block signBlock, ChangedSign sign, boolean save) {

        if (!checkSign(sign)) return false;

        try {
            World world = BukkitUtil.toSign(sign).getWorld();
            String namespace = sign.getLine(0);
            String id = StringUtils.replace(sign.getLine(2), "-", "").toLowerCase(Locale.ENGLISH);
            String inactiveID = StringUtils.replace(sign.getLine(3), "-", "").toLowerCase(Locale.ENGLISH);

            CuboidCopy copy;

            if (checkToggleState(sign)) {

                copy = CopyManager.getInstance().load(world, namespace, id);

                // if this is a save area save it before toggling off
                if (save) {
                    copy.copy();
                    CopyManager.getInstance().save(world, namespace, id, copy);
                }
                // if we are toggling to the second area we dont clear the old area
                if (!inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(world, namespace, inactiveID);
                    copy.paste();
                } else {
                    copy.clear();
                }
                setToggledState(sign, false);
            } else {

                // toggle the area on
                // if this is a save area save it before toggling off
                if (save && !inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(world, namespace, inactiveID);
                    copy.copy();
                    CopyManager.getInstance().save(world, namespace, inactiveID, copy);
                }

                copy = CopyManager.getInstance().load(world, namespace, id);
                copy.paste();
                setToggledState(sign, true);
            }
            return true;
        } catch (CuboidCopyException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to toggle Area: " + e.getMessage());
        } catch (DataException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to toggle Area: " + e.getMessage());
        } catch (IOException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to toggle Area: " + e.getMessage());
        }
        return false;
    }

    public static boolean toggleCold(ChangedSign sign) {

        if (!checkSign(sign)) return false;

        boolean toggleOn = coldCheckToggleState(sign);
        boolean save = sign.getLine(1).equalsIgnoreCase("[SaveArea]");

        try {
            World world = BukkitUtil.toSign(sign).getWorld();
            String namespace = sign.getLine(0);
            String id = StringUtils.replace(sign.getLine(2), "-", "").toLowerCase(Locale.ENGLISH);
            String inactiveID = StringUtils.replace(sign.getLine(3), "-", "").toLowerCase(Locale.ENGLISH);

            CuboidCopy copy;

            if (toggleOn) {

                copy = CopyManager.getInstance().load(world, namespace, id);

                // if this is a save area save it before toggling off
                if (save) {
                    copy.copy();
                    CopyManager.getInstance().save(world, namespace, id, copy);
                }
                // if we are toggling to the second area we dont clear the old area
                if (!inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(world, namespace, inactiveID);
                    copy.paste();
                } else {
                    copy.clear();
                }
                setToggledState(sign, false);
            } else {

                // toggle the area on
                // if this is a save area save it before toggling off
                if (save && !inactiveID.isEmpty() && !inactiveID.equals("--")) {
                    copy = CopyManager.getInstance().load(world, namespace, inactiveID);
                    copy.copy();
                    CopyManager.getInstance().save(world, namespace, inactiveID, copy);
                } else
                    copy = CopyManager.getInstance().load(world, namespace, id);
                copy.paste();
                setToggledState(sign, true);
            }
            return true;
        } catch (CuboidCopyException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to cold toggle Area: " + e.getMessage());
        } catch (DataException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to cold toggle Area: " + e.getMessage());
        } catch (IOException e) {
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to cold toggle Area: " + e.getMessage());
        }
        return false;
    }

    private static boolean checkSign(ChangedSign sign) {

        String namespace = sign.getLine(0);
        String id = sign.getLine(2).toLowerCase(Locale.ENGLISH);

        if (id == null || id.isEmpty() || id.length() < 1) return false;
        if (namespace == null || namespace.isEmpty() || namespace.length() < 1) return false;

        return true;
    }

    // pattern to check where the markers for on and off state are
    private static final Pattern pattern = Pattern.compile("^\\-[A-Za-z0-9_]*?\\-$");

    private boolean checkToggleState(ChangedSign sign) {

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
        sign.setLine(toToggleOn, "-" + sign.getLine(toToggleOn) + "-");
        sign.update(false);
    }
}