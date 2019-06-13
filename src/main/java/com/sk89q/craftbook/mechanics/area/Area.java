package com.sk89q.craftbook.mechanics.area;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
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
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[Area]") && !event.getLine(1).equalsIgnoreCase("[SaveArea]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        String cbid = player.getCraftBookId();

        if (event.getLine(0).trim().isEmpty()) {
            if (shortenNames && cbid.length() > 14)
                event.setLine(0, ('~' + cbid).substring(0, 14));
            else
                event.setLine(0, '~' + cbid);
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
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        boolean save;

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

        if(CraftBookPlugin.inst().getConfiguration().convertNamesToCBID
                && namespace.startsWith("~") && CraftBookPlugin.inst().getUUIDMappings().getUUID(namespace.replace("~", "")) == null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(namespace.replace("~", ""));
            if(player.hasPlayedBefore()) {
                String originalNamespace = namespace;

                try {
                    ProfileService resolver = HttpRepositoryService.forMinecraft();
                    Profile profile = resolver.findByName("player.getName()"); // May be null

                    namespace = '~' + CraftBookPlugin.inst().getUUIDMappings().getCBID(profile.getUniqueId());
                    CopyManager.renameNamespace(CraftBookPlugin.inst().getDataFolder(), originalNamespace, namespace);
                    sign.setLine(0, namespace);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return isValidArea(namespace, sign.getLine(2).trim().toLowerCase(Locale.ENGLISH), sign.getLine(3).trim().toLowerCase(Locale.ENGLISH));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSelfTriggerPing(SelfTriggerPingEvent event) {

        if(SignUtil.isSign(event.getBlock())) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());
            if(sign.getLine(1).equals("[Area]")) {
                isValidArea(sign); //Perform a conversion,
                sign.update(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!allowRedstone) return;
        if (!SignUtil.isSign(event.getBlock())) return;

        boolean save;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[Area]") && !sign.getLine(1).equals("[SaveArea]")) return;

        // check if the namespace and area exists
        if(!isValidArea(sign)) return;

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
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to toggle Area: " + e.getMessage());
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
            CraftBookPlugin.logger().log(Level.SEVERE, "Failed to cold toggle Area: " + e.getMessage());
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
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-redstone", "Allow ToggleAreas to be toggled via redstone.");
        allowRedstone = config.getBoolean(path + "allow-redstone", true);

        config.setComment(path + "use-schematics", "Use Schematics for saving areas. This allows support of all blocks and chest/sign data.");
        useSchematics = config.getBoolean(path + "use-schematics", true);

        config.setComment(path + "shorten-long-names", "If this is enabled, namespaces too long to fit on signs will be shortened.");
        shortenNames = config.getBoolean(path + "shorten-long-names", true);

        config.setComment(path + "max-size", "Sets the max amount of blocks that a ToggleArea can hold.");
        maxAreaSize = config.getInt(path + "max-size", 5000);

        config.setComment(path + "max-per-user", "Sets the max amount of ToggleAreas that can be within one namespace.");
        maxAreasPerUser = config.getInt(path + "max-per-user", 30);
    }
}