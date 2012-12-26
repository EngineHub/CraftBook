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

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.block.Block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICMechanicFactory extends AbstractMechanicFactory<ICMechanic> {

    /**
     * The pattern used to match an IC on a sign.
     */
    public static final Pattern IC_PATTERN = Pattern.compile("^\\[(([A-Z]{1,3})[0-9]{1,4})\\][A-Z]?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RIGHT_BRACKET_PATTERN = Pattern.compile("]", Pattern.LITERAL);

    /**
     * Manager of ICs.
     */
    protected final ICManager manager;

    /**
     * Holds the reference to the plugin.
     */
    protected final CircuitsPlugin plugin;

    /**
     * Construct the object.
     *
     * @param plugin
     * @param manager
     */
    public ICMechanicFactory(CircuitsPlugin plugin, ICManager manager) {

        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public ICMechanic detect(BlockWorldVector pt) throws InvalidMechanismException {

        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        // if we're not looking at a wall sign, it can't be an IC.
        if (block.getTypeId() != BlockID.WALL_SIGN) return null;
        ChangedSign sign = BukkitUtil.toChangedSign(block);

        // detect the text on the sign to see if it's any kind of IC at all.
        Matcher matcher = IC_PATTERN.matcher(sign.getLine(1));
        if (!matcher.matches()) return null;

        String prefix = matcher.group(2);
        // TODO: remove after some time to stop converting existing MCA ICs
        // convert existing MCA ICs to the new [MCXXXX]A syntax
        if (prefix.equalsIgnoreCase("MCA")) {
            sign.setLine(1, sign.getLine(1).replace("A", "") + "A");
            sign.update(false);
        }

        if (!manager.hasCustomPrefix(prefix)) return null;

        String id = matcher.group(1);
        // after this point, we don't return null if we can't make an IC: we throw shit,
        // because it SHOULD be an IC and can't possibly be any other kind of mechanic.

        // now actually try to pull up an IC of that id number.
        RegisteredICFactory registration = manager.get(id);
        if (registration == null)
            throw new InvalidMechanismException("\"" + sign.getLine(1) + "\" should be an IC ID, " +
                    "but no IC registered under that ID could be found.");

        IC ic;
        // check if the ic is cached and get that single instance instead of creating a new one
        if (ICManager.isCachedIC(pt)) {
            ic = ICManager.getCachedIC(pt);
        } else {
            ic = registration.getFactory().create(sign);
            // add the created ic to the cache
            ICManager.addCachedIC(pt, ic);
        }
        // extract the suffix
        String suffix = "";
        String[] str = RIGHT_BRACKET_PATTERN.split(sign.getLine(1));
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
        if (ic instanceof SelfTriggeredIC)
            return new SelfTriggeredICMechanic(plugin, id, (SelfTriggeredIC) ic, family, pt);
        else return new ICMechanic(plugin, id, ic, family, pt);
    }

    /**
     * Detect the mechanic at a placed sign.
     */
    @Override
    public ICMechanic detect(BlockWorldVector pt, LocalPlayer player,
                             ChangedSign sign) throws InvalidMechanismException {

        return detect(pt, player, sign, false);
    }

    private ICMechanic detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign,
                              boolean shortHand) throws InvalidMechanismException {

        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        boolean matches = true;
        Matcher matcher = IC_PATTERN.matcher(sign.getLine(1));
        // lets check for custom ics
        if (!matcher.matches()) {
            matches = false;
        }
        try {
            if (!manager.hasCustomPrefix(matcher.group(2))) {
                matches = false;
            }
        } catch (Exception e) {
            // we need to catch here if the sign changes when beeing parsed
            matches = false;
        }

        if (matches) {

            String id = matcher.group(1);
            String suffix = "";
            String[] str = RIGHT_BRACKET_PATTERN.split(sign.getLine(1));
            if (str.length > 1) {
                suffix = str[1];
            }

            if (block.getTypeId() != BlockID.WALL_SIGN)
                throw new InvalidMechanismException("Only wall signs are used for ICs.");

            if (ICManager.isCachedIC(pt)) {
                ICManager.removeCachedIC(pt);
            }

            RegisteredICFactory registration = manager.get(id);
            if (registration == null) throw new InvalidMechanismException("Unknown IC detected: " + id);

            ICFactory factory = registration.getFactory();

            checkPermissions(player, factory, id, registration);

            factory.verify(sign);

            factory.checkPlayer(sign, player);

            IC ic = registration.getFactory().create(sign);

            sign.setLine(1, "[" + registration.getId() + "]" + suffix);

            ICFamily family = registration.getFamilies()[0];
            if (suffix != null && !suffix.isEmpty()) {
                for (ICFamily f : registration.getFamilies()) {
                    if (f.getSuffix().equalsIgnoreCase(suffix)) {
                        family = f;
                        break;
                    }
                }
            }

            ICMechanic mechanic;

            if (ic instanceof SelfTriggeredIC) {
                mechanic = new SelfTriggeredICMechanic(plugin, id, (SelfTriggeredIC) ic, family, pt);
            } else {
                mechanic = new ICMechanic(plugin, id, ic, family, pt);
            }

            if (!shortHand) {
                sign.setLine(0, ic.getSignTitle());
            }

            player.print("You've created " + registration.getId() + ": " + ic.getTitle() + ".");

            return mechanic;
        } else if (plugin.getLocalConfiguration().icSettings.shorthand && sign.getLine(0).startsWith("=")) {
            String id = sign.getLine(0).substring(1);

            if (block.getTypeId() != BlockID.WALL_SIGN)
                throw new InvalidMechanismException("Only wall signs are used for ICs.");

            String shortId = manager.longRegistered.get(id.toLowerCase());
            if (shortId == null) {
                player.printError("Warning: Unknown IC");
                return null;
            }

            sign.setLine(1, "[" + shortId + "]");

            detect(pt, player, sign, true);
        }
        return null;
    }

    public boolean checkPermissions(LocalPlayer player, ICFactory factory, String id, RegisteredICFactory registration)
            throws ICVerificationException {

        if (player.hasPermission("craftbook.ic." + factory.getClass().getPackage().getName())) return true;

        if (player.hasPermission("craftbook.ic." + id.toLowerCase())) // Simpler overriding permission.
            return true;

        if (factory instanceof RestrictedIC) {
            if (!player.hasPermission("craftbook.ic.restricted." + id.toLowerCase()))
                throw new ICVerificationException("You don't have permission to use " + registration.getId() + ".");
        } else if (!player.hasPermission("craftbook.ic.safe." + id.toLowerCase()))
            throw new ICVerificationException("You don't have permission to use " + registration.getId() + ".");

        return true;
    }
}
