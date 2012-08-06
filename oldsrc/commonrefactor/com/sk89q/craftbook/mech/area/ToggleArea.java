package com.sk89q.craftbook.mech.area;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.blockbag.BlockBag;
import com.sk89q.craftbook.blockbag.BlockBagException;
import com.sk89q.craftbook.mech.SignOrientedMechanism;
import com.sk89q.craftbook.util.CraftBookUtil;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

import java.io.IOException;

/**
 * Represents an instance of a toggle area. This is temporarily created
 * when needed and GC'ed afterwards.
 *
 * @author sk89q
 */
public class ToggleArea extends SignOrientedMechanism {

    /**
     * Copy manager.
     */
    private CopyManager copyManager;

    /**
     * Indicates whether this instance uses the new [Area] signs or the
     * old [Toggle] signs. The old [Toggle] signs have less features.
     */
    private boolean isNewArea;

    /**
     * Construct the instance.
     *
     * @param pt
     * @param signText
     * @param copyManager
     */
    public ToggleArea(ServerInterface s, WorldInterface w, Vector pt, CopyManager copyManager) {

        super(s, w, pt);

        this.copyManager = copyManager;

        isNewArea = getSignIdentifier().equalsIgnoreCase("[Area]");
    }

    /**
     * Get the namespace as specified by the sign. This has not been
     * validated yet -- the raw value is returned.
     *
     * @return
     */
    public String getSignNamespace() {

        return getSignText().getLine3();
    }

    /**
     * Get the ID of the on state as specified by the sign. This has not
     * been validated yet -- the raw value is returned.
     *
     * @return
     */
    public String getSignActiveStateID() {

        return getSignText().getLine1();
    }

    /**
     * Get the ID of the off state as specified by the sign. This has not
     * been validated yet -- the raw value is returned.
     *
     * @return
     */
    public String getSignInactiveStateID() {

        return getSignText().getLine4();
    }

    /**
     * Get the namespace used for loading. This may raise a
     * InvalidSignNamespace if the specified namespace for the sign is
     * not an acceptable value.
     *
     * @return
     *
     * @throws InvalidSignNamespace
     */
    public String getNamespace() throws InvalidSignNamespace {

        String namespace = getSignNamespace();

        if (namespace.equals("") || !isNewArea) {
            return "global";
        } else {
            if (CopyManager.isValidNamespace(namespace)) {
                return "~" + namespace;
            }

            throw new InvalidSignNamespace();
        }
    }

    /**
     * Get the on state ID. This may raise a InvalidSignStateID exception if
     * the specified ID is invalid.
     *
     * @return
     *
     * @throws InvalidSignStateID
     */
    public String getActiveStateID() throws InvalidSignStateID {

        String id = getSignActiveStateID();

        if (CopyManager.isValidName(id)) {
            return id;
        }

        throw new InvalidSignStateID();
    }

    /**
     * Get the off state ID. This may raise a InvalidSignStateID exception if
     * the specified ID is invalid. If the returned string is empty, that means
     * that the area should be cleared. If it is null, it means that the
     * area should not be changed.
     *
     * @return
     *
     * @throws InvalidSignStateID
     */
    public String getInactiveStateID() throws InvalidSignStateID {

        String id = getSignInactiveStateID();

        if (id.equals("")) {
            return "";
        } else if (id.equals("-")) {
            return null;
        } else if (CopyManager.isValidName(id)) {
            return id;
        }

        throw new InvalidSignStateID();
    }

    /**
     * Toggle the area.
     *
     * @param player
     * @param bag
     *
     * @throws BlockBagException
     */
    public void playerToggle(PlayerInterface player, BlockBag bag)
            throws BlockBagException {

        try {
            String namespace = getNamespace();
            String activeID = getActiveStateID();

            CuboidCopy copy = copyManager.load(world, namespace, activeID);

            if (!isNewArea && copy.distance(pt) > 4) {
                player.printError("This sign is too far away!");
                return;
            }

            if (!copy.shouldClear(world)) {
                copy.paste(world, bag);
            } else {
                String inactiveID = getInactiveStateID();

                if (inactiveID == null) {
                    // Do nothing
                } else if (inactiveID.length() == 0) {
                    copy.clear(world, bag);
                } else {
                    copy = copyManager.load(world, namespace, inactiveID);
                    copy.paste(world, bag);
                }
            }

            player.print("Toggled!");
        } catch (InvalidSignNamespace e) {
            player.printError("The namespace on the sign is invalid.");
        } catch (InvalidSignStateID e) {
            player.printError("A copy ID on the sign is invalid.");
        } catch (MissingCuboidCopyException e) {
            player.printError("The '" + e.getCopyName() + "' area does not exist.");
        } catch (CuboidCopyException e) {
            player.printError("Could not load area: " + e.getMessage());
        } catch (IOException e) {
            player.printError("Could not load area: " + e.getMessage());
        }
    }

    /**
     * Set the area active.
     *
     * @param player
     * @param bag
     */
    public void setActive(BlockBag bag) {

        try {
            String namespace = getNamespace();
            String activeID = getActiveStateID();

            CuboidCopy copy = copyManager.load(world, namespace, activeID);

            if (!isNewArea && copy.distance(pt) > 4) {
                return;
            }

            copy.paste(world, bag);
        } catch (BlockBagException e) {
            ;
        } catch (InvalidSignNamespace e) {
        } catch (InvalidSignStateID e) {
        } catch (MissingCuboidCopyException e) {
        } catch (CuboidCopyException e) {
        } catch (IOException e) {
        }
    }

    /**
     * Set the area inactive.
     *
     * @param player
     * @param bag
     */
    public void setInactive(BlockBag bag) {

        try {
            String namespace = getNamespace();
            String inactiveID = getInactiveStateID();

            CuboidCopy copy = copyManager.load(world, namespace, inactiveID);

            if (!isNewArea && copy.distance(pt) > 4) {
                return;
            }

            if (inactiveID == null) {
                // Do nothing
            } else if (inactiveID.length() == 0) {
                copy.clear(world, bag);
            } else {
                copy = copyManager.load(world, namespace, inactiveID);
                copy.paste(world, bag);
            }
        } catch (BlockBagException e) {
        } catch (InvalidSignNamespace e) {
        } catch (InvalidSignStateID e) {
        } catch (MissingCuboidCopyException e) {
        } catch (CuboidCopyException e) {
        } catch (IOException e) {
        }
    }

    /**
     * Validates the sign's environment.
     *
     * @param signText
     *
     * @return false to deny
     */
    public static boolean validateEnvironment(ServerInterface server, WorldInterface world,
                                              PlayerInterface player, Vector pt) {

        ToggleArea area = new ToggleArea(server, world, pt, null);
        SignText signText = area.signText;

        String activeID = area.getSignActiveStateID();
        String inactiveID = area.getSignInactiveStateID();
        String signNS = area.getSignNamespace();
        String playerNS = CraftBookUtil.trimLength(player.getName(), 15);

        boolean noInactiveCopy = inactiveID.equals("-");

        if (!CopyManager.isValidName(activeID)) {
            player.printError("An invalid area name was indicated.");
            return false;
        }

        if (inactiveID.length() > 0 && !noInactiveCopy
                && !CopyManager.isValidName(inactiveID)) {
            player.printError("An invalid off state area name was indicated.");
            return false;
        }

        // Coerce the namespace to the player's name
        if (signNS.equals("") || signNS.equalsIgnoreCase(playerNS)) {
            signText.setLine3(playerNS);
        } else if (signNS.equals("@")) {
            if (!player.canCreateObject("savensarea")) {
                player.printError("You are unable to make toggles for global areas.");
                return false;
            }
        } else {
            if (!player.canCreateObject("savensarea")) {
                player.printError("You are unable to make toggles for global areas.");
                return false;
            }
        }

        if (area.isNewArea) {
            signText.setLine2("[Area]");
        } else {
            signText.setLine2("[Toggle]");
        }

        player.print("Area toggle created!");

        return true;
    }

    /**
     * Thrown when the sign specifies an invalid namespace.
     */
    private static class InvalidSignNamespace extends Exception {

        private static final long serialVersionUID = 2133463685142409991L;
    }

    /**
     * Thrown when the sign specifies an invalid ID.
     */
    private static class InvalidSignStateID extends Exception {

        private static final long serialVersionUID = -2120929355267917312L;
    }
}
