// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICMechanicFactory extends AbstractMechanicFactory<ICMechanic> {

    /**
     * The pattern used to match an IC on a sign.
     */
    public static final Pattern codePattern =
            Pattern.compile("^\\[(MC[^\\]]+)\\][A-Z]?$", Pattern.CASE_INSENSITIVE);

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
        Sign sign = (Sign) block.getState();

        // detect the text on the sign to see if it's any kind of IC at all.
        Matcher matcher = codePattern.matcher(sign.getLine(1));
        if (!matcher.matches()) return null;
        String id = matcher.group(1);
        // after this point, we don't return null if we can't make an IC: we throw shit,
        //  because it SHOULD be an IC and can't possibly be any other kind of mechanic.

        // now actually try to pull up an IC of that id number.
        RegisteredICFactory registration = manager.get(id);
        if (registration == null) throw new InvalidMechanismException(
                "\"" + sign.getLine(1) + "\" should be an IC ID, but no IC registered under that ID could be found.");

        IC ic;
        // check if the ic is cached and get that single instance instead of creating a new one
        if (ICManager.isCachedIC(pt)) {
            ic = ICManager.getCachedIC(pt);
        } else {
            ic = registration.getFactory().create(sign);
            // add the created ic to the cache
            ICManager.addCachedIC(pt, ic);
        }

        // okay, everything checked out.  we can finally make it.
        if (ic instanceof SelfTriggeredIC) {
            return new SelfTriggeredICMechanic(
                    plugin,
                    id,
                    (SelfTriggeredIC) ic,
                    registration.getFamily(),
                    pt
            );
        } else {
            return new ICMechanic(
                    plugin,
                    id,
                    ic,
                    registration.getFamily(),
                    pt
            );
        }
    }


    /**
     * Detect the mechanic at a placed sign.
     */
    public ICMechanic detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
            throws InvalidMechanismException {
        return detect(pt, player, sign, false);
    }

    private ICMechanic detect(BlockWorldVector pt, LocalPlayer player, Sign sign, boolean shortHand)
            throws InvalidMechanismException {

        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        Matcher matcher = codePattern.matcher(sign.getLine(1));
        if (matcher.matches()) {
            String id = matcher.group(1);
            String suffix = "";
            String[] str = sign.getLine(1).split("]");
            if (str.length > 1)
                suffix = str[1];

            if (block.getTypeId() != BlockID.WALL_SIGN) {
                throw new InvalidMechanismException("Only wall signs are used for ICs.");
            }

            RegisteredICFactory registration = manager.get(id);
            if (registration == null)
                throw new InvalidMechanismException("Unknown IC detected: " + id);

            ICFactory factory = registration.getFactory();

            if (factory instanceof RestrictedIC) {
                if (!player.hasPermission("craftbook.ic.restricted." + id.toLowerCase())) {
                    throw new ICVerificationException("You don't have permission to use "
                            + registration.getId() + ".");
                }
            } else {
                if (!player.hasPermission("craftbook.ic.safe." + id.toLowerCase())) {
                    throw new ICVerificationException("You don't have permission to use "
                            + registration.getId() + ".");
                }
            }

            factory.verify(sign);

            factory.checkPlayer(sign, player);

            IC ic = registration.getFactory().create(sign);

            sign.setLine(1, "[" + registration.getId() + "]" + suffix);

            ICMechanic mechanic;

            if (ic instanceof SelfTriggeredIC) {
                mechanic = new SelfTriggeredICMechanic(
                        plugin,
                        id,
                        (SelfTriggeredIC) ic,
                        registration.getFamily(),
                        pt
                );
            } else {
                mechanic = new ICMechanic(
                        plugin,
                        id,
                        ic,
                        registration.getFamily(),
                        pt
                );
            }

            if(!shortHand)
                sign.setLine(0, ic.getSignTitle());

            player.print("You've created " + registration.getId() + ": " + ic.getTitle() + ".");

            return mechanic;
        } else if(plugin.getLocalConfiguration().enableShorthandIcs &&
                  sign.getLine(0).startsWith("=")) {
           String id = sign.getLine(0).substring(1);

            if (block.getTypeId() != BlockID.WALL_SIGN) {
                throw new InvalidMechanismException("Only wall signs are used for ICs.");
            }

            String shortId = manager.longRegistered.get(id.toLowerCase());
            if (shortId == null)
                throw new InvalidMechanismException("Unknown IC detected: " + id);

            sign.setLine(1, "["+shortId+"]");

            detect(pt, player, sign, true);
        } return null;
    }
}
