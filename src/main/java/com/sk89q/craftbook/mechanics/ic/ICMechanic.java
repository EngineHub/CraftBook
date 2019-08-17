// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ICUtil.LocationCheckType;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Mechanic wrapper for ICs. The mechanic manager dispatches events to this mechanic,
 * and then it is processed and passed onto the associated IC.
 *
 * @author sk89q
 */
public class ICMechanic extends AbstractCraftBookMechanic {

    /**
     * Manager of ICs.
     */
    protected final ICManager manager;
    public static ICMechanic instance;

    //protected final String id;
    //protected final ICFamily family;
    //protected final IC ic;
    //protected final BlockWorldVector pos;

    public ICMechanic() {

        manager = new ICManager();
        instance = this;
    }

    @Override
    public boolean enable() {

        ICManager.inst().enable();
        return true;
    }

    @Override
    public void disable() {

        manager.disable();
    }

    public Object[] setupIC(Block block, boolean create) {

        // if we're not looking at a wall sign, it can't be an IC.
        if (!SignUtil.isWallSign(block)) return null;
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);

        // detect the text on the sign to see if it's any kind of IC at all.
        Matcher matcher = RegexUtil.IC_PATTERN.matcher(sign.getLine(1));
        if (!matcher.matches()) return null;

        String prefix = matcher.group(2);
        // TODO: remove after some time to stop converting existing MCA ICs
        // convert existing MCA ICs to the new [MCXXXX]A syntax
        if (prefix.equalsIgnoreCase("MCA")) {
            sign.setLine(1, (StringUtils.replace(sign.getLine(1).toLowerCase(Locale.ENGLISH), "mca", "mc") + "a").toUpperCase(Locale.ENGLISH));
            sign.update(false);

            return setupIC(block, create);
        }
        if (sign.getLine(1).toLowerCase(Locale.ENGLISH).startsWith("[mc0")) {
            if(sign.getLine(1).equalsIgnoreCase("[mc0420]"))
                sign.setLine(1, "[MC1421]S");
            else if(sign.getLine(1).equalsIgnoreCase("[mc0421]"))
                sign.setLine(1, "[MC1422]S");
            else
                sign.setLine(1, (StringUtils.replace(sign.getLine(1).toLowerCase(Locale.ENGLISH), "mc0", "mc1") + "s").toUpperCase(Locale.ENGLISH));
            sign.update(false);

            return setupIC(block, create);
        }

        if (sign.getLine(1).toLowerCase(Locale.ENGLISH).startsWith("[mcz")) {
            sign.setLine(1, (StringUtils.replace(sign.getLine(1).toLowerCase(Locale.ENGLISH), "mcz", "mcx") + "s").toUpperCase(Locale.ENGLISH));
            sign.update(false);

            return setupIC(block, create);
        }

        if (!ICManager.hasCustomPrefix(prefix)) return null;

        String id = matcher.group(1);

        if(disabledICs.contains(id.toLowerCase()) || disabledICs.contains(id)) return null; //This IC is disabled.
        // after this point, we don't return null if we can't make an IC: we throw shit,
        // because it SHOULD be an IC and can't possibly be any other kind of mechanic.

        // now actually try to pull up an IC of that id number.
        RegisteredICFactory registration = manager.get(id);
        if (registration == null) {
            CraftBookPlugin.logger().warning("\"" + sign.getLine(1) + "\" should be an IC ID, but no IC registered under that ID could be found.");
            block.breakNaturally();
            return null;
        }

        IC ic;
        // check if the ic is cached and get that single instance instead of creating a new one
        if (ICManager.isCachedIC(block.getLocation())) {
            ic = ICManager.getCachedIC(block.getLocation());
            if(ic.getSign().updateSign(sign)) {

                ICManager.removeCachedIC(block.getLocation());
                ic = registration.getFactory().create(sign);
                if(!sign.getLine(0).equals(ic.getSignTitle()) && !sign.getLine(0).startsWith("=")) {
                    sign.setLine(0, ic.getSignTitle());
                    sign.update(false);
                }
                ic.load();
                // add the created ic to the cache
                ICManager.addCachedIC(block.getLocation(), ic);
            }
        } else if (create) {
            ic = registration.getFactory().create(sign);
            if(!sign.getLine(0).equals(ic.getSignTitle()) && !sign.getLine(0).startsWith("=")) {
                sign.setLine(0, ic.getSignTitle());
                sign.update(false);
            }
            ic.load();
            // add the created ic to the cache
            ICManager.addCachedIC(block.getLocation(), ic);
        } else
            return null;
        // extract the suffix
        String suffix = "";
        String[] str = RegexUtil.RIGHT_BRACKET_PATTERN.split(sign.getLine(1));
        if (str.length > 1) {
            suffix = str[1];
        }

        ICFamily family = registration.getFamilies()[0];
        if (suffix != null && !suffix.isEmpty()) {
            for (ICFamily f : registration.getFamilies()) {
                if (f.getSuffix().equalsIgnoreCase(suffix)) {
                    family = f;
                    break;
                }
            }
        }

        // okay, everything checked out. we can finally make it.
        if (ic instanceof SelfTriggeredIC && (sign.getLine(1).trim().toUpperCase(Locale.ENGLISH).endsWith("S") || ((SelfTriggeredIC) ic).isAlwaysST())) {
            if (disableSelfTriggered)
                return null;
            CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(block.getLocation());
        }

        Object[] rets = new Object[3];
        rets[0] = id;
        rets[1] = family;
        rets[2] = ic;

        return rets;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        final Object[] icData = setupIC(event.getBlock(), true);

        if(icData == null) return;

        final Block block = event.getBlock();
        // abort if the current did not change
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        if (SignUtil.isWallSign(block)) {
            final Block source = event.getSource();
            // abort if the sign is the source or the block the sign is attached to
            if (SignUtil.getBackBlock(block).equals(source) || block.equals(source)) return;


            Runnable runnable = () -> {

                if (!SignUtil.isWallSign(block)) return;
                try {
                    ChipState chipState = ((ICFamily) icData[1]).detect(BukkitAdapter.adapt(source.getLocation()), CraftBookBukkitUtil.toChangedSign(block));
                    int cnt = 0;
                    for (int i = 0; i < chipState.getInputCount(); i++) {
                        if (chipState.isTriggered(i)) {
                            cnt++;
                        }
                    }
                    if (cnt > 0) {
                        ((IC) icData[2]).trigger(chipState);
                    }
                } catch (IllegalArgumentException ex) {
                    // Exclude these exceptions so that we don't spam consoles because of Bukkit
                    if (!ex.getMessage().contains("Null ChangedSign found")) throw ex;
                }
            };
            // FIXME: these should be registered with a global scheduler so we can end up with one runnable actually
            // running per set of inputs in a given time window.
            CraftBookPlugin.server().getScheduler().runTaskLater(CraftBookPlugin.inst(), runnable, 2);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(!EventUtil.passesFilter(event)) return;

        if(ICManager.isCachedIC(event.getClickedBlock().getLocation()) && event.getPlayer().isSneaking()) {
            ICManager.getCachedIC(event.getClickedBlock().getLocation()).unload();
            ICManager.removeCachedIC(event.getClickedBlock().getLocation());
        }

        final Object[] icData = setupIC(event.getClickedBlock(), true);

        if(icData == null) return;

        ((IC) icData[2]).onRightClick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThinkPing(SelfTriggerPingEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        setupIC(event.getBlock(), true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThinkUnregister(SelfTriggerUnregisterEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        final Object[] icData = setupIC(event.getBlock(), false);

        if(icData != null) {
            if(event.getReason() == UnregisterReason.ERROR) {
                if(breakOnError) {
                    ((IC) icData[2]).unload();
                    event.getBlock().breakNaturally();
                    return;
                }
            }
            if(keepLoaded) {
                event.setCancelled(true);
                return;
            }
            ((IC) icData[2]).unload();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        final Object[] icData = setupIC(event.getBlock(), true);

        if(icData != null && icData[2] instanceof SelfTriggeredIC) {
            event.setHandled(true);
            ChipState chipState = ((ICFamily) icData[1]).detectSelfTriggered(BukkitAdapter.adapt(event.getBlock().getLocation()), ((IC) icData[2]).getSign());
            ((SelfTriggeredIC) icData[2]).think(chipState);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        final Object[] icData = setupIC(event.getBlock(), false);

        if(icData == null) return;

        // remove the ic from cache
        CraftBookPlugin.inst().getSelfTriggerManager().unregisterSelfTrigger(event.getBlock().getLocation(), UnregisterReason.BREAK);
        ICManager.removeCachedIC(event.getBlock().getLocation());
        ((IC) icData[2]).onICBreak(event);
        if(!event.isCancelled())
            ((IC) icData[2]).unload();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipePut(PipePutEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        final Object[] icData = setupIC(event.getPuttingBlock(), true);

        if(icData == null) return;

        if(icData[2] instanceof PipeInputIC)
            ((PipeInputIC) icData[2]).onPipeTransfer(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        initializeIC(event.getBlock(), CraftBookPlugin.inst().wrapPlayer(event.getPlayer()), event, false);
    }

    public void initializeIC(final Block block, final CraftBookPlayer player, final SignChangeEvent event, final boolean shortHand) {

        boolean matches = true;
        Matcher matcher = RegexUtil.IC_PATTERN.matcher(event.getLine(1));
        // lets check for custom ics
        if (!matcher.matches()) {
            matches = false;
        }

        try {
            if (!ICManager.hasCustomPrefix(matcher.group(2))) {
                matches = false;
            }
        } catch (Exception e) {
            // we need to catch here if the sign changes when beeing parsed
            matches = false;
        }

        if (matches) {

            try {
                String prefix = matcher.group(2);
                // TODO: remove after some time to stop converting existing MCA ICs
                // convert existing MCA ICs to the new [MCXXXX]A syntax
                if (prefix.equalsIgnoreCase("MCA")) {
                    event.setLine(1, (event.getLine(1).toLowerCase(Locale.ENGLISH).replace("mca", "mc") + "a").toUpperCase(Locale.ENGLISH));

                    initializeIC(block, player, event, shortHand);
                    return;
                }
                if (event.getLine(1).toLowerCase(Locale.ENGLISH).startsWith("[mc0")) {
                    if(event.getLine(1).equalsIgnoreCase("[mc0420]"))
                        event.setLine(1, "[MC1421]S");
                    else if(event.getLine(1).equalsIgnoreCase("[mc0421]"))
                        event.setLine(1, "[MC1422]S");
                    else
                        event.setLine(1, (event.getLine(1).toLowerCase(Locale.ENGLISH).replace("mc0", "mc1") + "s").toUpperCase(Locale.ENGLISH));

                    initializeIC(block, player, event, shortHand);
                    return;
                }

                if (event.getLine(1).toLowerCase(Locale.ENGLISH).startsWith("[mcz")) {
                    event.setLine(1, (event.getLine(1).toLowerCase(Locale.ENGLISH).replace("mcz", "mcx") + "s").toUpperCase(Locale.ENGLISH));

                    initializeIC(block, player, event, shortHand);
                    return;
                }
            }
            catch(Exception ignored){}

            String id = matcher.group(1);
            final String suffix;
            String[] str = RegexUtil.RIGHT_BRACKET_PATTERN.split(event.getLine(1));
            if (str.length > 1) {
                suffix = str[1];
            } else
                suffix = "";

            if (!SignUtil.isWallSign(block)) {
                player.printError("Only wall signs are used for ICs.");
                SignUtil.cancelSign(event);
                return;
            }

            if (ICManager.isCachedIC(block.getLocation())) {

                CraftBookPlugin.logDebugMessage("Existing IC found at selected location!", "ic-create");
                ICManager.getCachedIC(block.getLocation()).unload();
                ICManager.removeCachedIC(block.getLocation());
            }

            final RegisteredICFactory registration = manager.get(id);
            if (registration == null) {
                player.printError("Unknown IC detected: " + id);
                SignUtil.cancelSign(event);
                return;
            }

            final ICFactory factory = registration.getFactory();

            try {
                checkPermissions(player, factory, registration.getId().toLowerCase(Locale.ENGLISH));
            } catch (ICVerificationException e) {
                player.printError(e.getMessage());
                SignUtil.cancelSign(event);
                return;
            }

            Bukkit.getServer().getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                ChangedSign sign = CraftBookPlugin.inst().getNmsAdapter().getChangedSign(event.getBlock(), event.getLines(), null);

                //WorldEdit offset/radius tools.
                ICUtil.parseSignFlags(player, sign);

                try {
                    factory.verify(sign);
                    factory.checkPlayer(sign, player);
                } catch (ICVerificationException e) {
                    player.printError(e.getMessage());
                    event.getBlock().breakNaturally();
                    return;
                }

                IC ic = registration.getFactory().create(sign);
                ic.load();

                sign.setLine(1, "[" + registration.getId() + "]" + suffix);
                if (!shortHand)
                    sign.setLine(0, ic.getSignTitle());

                sign.update(false);

                if (ic instanceof SelfTriggeredIC && (event.getLine(1).trim().toUpperCase(Locale.ENGLISH).endsWith("S") || ((SelfTriggeredIC) ic).isAlwaysST())) {
                    if (disableSelfTriggered) {
                        player.printError("Self-triggered ICs are disabled!");
                        return;
                    }
                    CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(block.getLocation());
                }

                player.print("You've created " + registration.getId() + ": " + ic.getTitle() + ".");
            });

            return;
        } else if (shortHand && event.getLine(0).startsWith("=")) {
            String id = event.getLine(0).substring(1);

            boolean st = id.toLowerCase(Locale.ENGLISH).endsWith(" st");
            id = StringUtils.replace(id.toLowerCase(Locale.ENGLISH), " st", "");

            String shortId = manager.longRegistered.get(id.toLowerCase(Locale.ENGLISH));
            if (shortId == null) {
                player.printError("Warning: Unknown IC");
                return;
            }

            if (!SignUtil.isWallSign(block)) {
                player.printError("Only wall signs are used for ICs.");
                SignUtil.cancelSign(event);
                return;
            }

            event.setLine(1, "[" + shortId + "]" + (st ? "S" : ""));

            initializeIC(block, player, event, true);
            return;
        }
    }

    public static boolean checkPermissionsBoolean(CraftBookPlayer player, ICFactory factory, String id) {

        try {
            checkPermissions(player, factory, id);
        } catch (ICVerificationException e) {
            return false;
        }
        return true;
    }

    public static void checkPermissions(CraftBookPlayer player, ICFactory factory, String id) throws ICVerificationException {

        if (player.hasPermission("craftbook.ic." + id.toLowerCase(Locale.ENGLISH))) {
            return;
        }

        if (player.hasPermission("craftbook.ic." + factory.getClass().getPackage().getName() + '.' + id.toLowerCase(Locale.ENGLISH))) {
            return;
        }

        if (factory instanceof RestrictedIC) {
            if (hasRestrictedPermissions(player, factory, id)) return;
        } else if (hasSafePermissions(player, factory, id)) {
            return;
        }

        throw new ICVerificationException("You don't have permission to use " + id.toLowerCase(Locale.ENGLISH) + ".");
    }

    public static boolean hasRestrictedPermissions(CraftBookPlayer player, ICFactory factory, String id) {
        return player.hasPermission("craftbook.ic.restricted." + id.toLowerCase(Locale.ENGLISH));
    }

    public static boolean hasSafePermissions(CraftBookPlayer player, ICFactory factory, String id) {
        return player.hasPermission("craftbook.ic.safe." + id.toLowerCase(Locale.ENGLISH));
    }

    public boolean cache;
    public boolean shortHand;
    public double maxRange;
    public List<String> disabledICs;
    public boolean keepLoaded;
    public LocationCheckType defaultCoordinates;
    public boolean savePersistentData;
    public boolean usePercussionMidi;
    public boolean breakOnError;
    public boolean disableSelfTriggered;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "cache", "Saves many CPU cycles with a VERY small cost to memory (Highly Recommended)");
        cache = config.getBoolean(path + "cache", true);

        config.setComment(path + "max-radius", "The max radius IC's with a radius setting can use. (WILL cause lag at higher values)");
        maxRange = config.getDouble(path + "max-radius", 10);

        config.setComment(path + "allow-short-hand", "Allows the usage of IC Shorthand, which is an easier way to create ICs.");
        shortHand = config.getBoolean(path + "allow-short-hand", true);

        config.setComment(path + "keep-loaded", "Keep any chunk with an ST IC in it loaded.");
        keepLoaded = config.getBoolean(path + "keep-loaded", false);

        config.setComment(path + "disallowed-ics", "A list of IC's which are never loaded. They will not work or show up in /ic list.");
        disabledICs = config.getStringList(path + "disallowed-ics", new ArrayList<>());

        config.setComment(path + "default-coordinate-system", "The default coordinate system for ICs. This changes the way IC offsets work. From RELATIVE, OFFSET and ABSOLUTE.");
        defaultCoordinates = LocationCheckType.getTypeFromName(config.getString(path + "default-coordinate-system", "RELATIVE"));

        config.setComment(path + "save-persistent-data", "Saves extra data to the CraftBook folder that allows some ICs to work better on server restart.");
        savePersistentData = config.getBoolean(path + "save-persistent-data", true);

        config.setComment(path + "midi-use-percussion", "Plays the MIDI percussion channel when using a MIDI playing IC. Note: This may sound horrible on some songs.");
        usePercussionMidi = config.getBoolean(path + "midi-use-percussion", false);

        config.setComment(path + "break-on-error", "Break the IC sign when an error occurs from that specific IC.");
        breakOnError = config.getBoolean(path + "break-on-error", false);
        
        config.setComment(path + "disable-self-triggered", "Disable creation and checking of self-triggered ICs.");
        disableSelfTriggered = config.getBoolean(path + "disable-self-triggered", false);
    }
}