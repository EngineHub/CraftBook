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

package org.enginehub.craftbook;

import java.util.HashMap;

@Deprecated
public class LanguageManager {

    public static final HashMap<String, String> defaultMessages = new HashMap<String, String>(32, 1.0f) {{
        put("area.permissions", "You don't have permissions to do that in this area!");
        put("area.use-permissions", "You don't have permissions to use that in this area!");
        put("area.break-permissions", "You don't have permissions to break that in this area!");


        put("mech.restock-permission", "You don't have permission to restock this mechanic.");
        put("mech.not-enough-blocks", "Not enough blocks to trigger mechanic!");
        put("mech.group", "You are not in the required group!");
        put("mech.restock", "Mechanism Restocked!");

        put("mech.area.create", "Toggle Area Created!");
        put("mech.area.missing", "The area or namespace does not exist.");

        put("mech.cauldron.create", "Cauldron Created!");
        put("mech.cauldron.too-small", "Cauldron is too small!");
        put("mech.cauldron.leaky", "Cauldron has a leak!");
        put("mech.cauldron.no-lava", "Cauldron lacks lava!");
        put("mech.cauldron.legacy-not-a-recipe", "Hmm, this doesn't make anything...");
        put("mech.cauldron.legacy-not-in-group", "Doesn't seem as if you have the ability...");
        put("mech.cauldron.legacy-create", "In a poof of smoke, you've made");
        put("mech.cauldron.stir", "You stir the cauldron but nothing happens.");
        put("mech.cauldron.permissions", "You dont have permission to cook this recipe.");
        put("mech.cauldron.cook", "You have cooked the recipe:");

        put("mech.command.create", "Command Sign Created!");

        put("mech.command-items.out-of-sync", "Inventory became out of sync during usage of command-items!");
        put("mech.command-items.wait", "You have to wait %time% seconds to use this again!");
        put("mech.command-items.need", "You need %item% to use this command!");

        put("mech.custom-crafting.recipe-permission", "You do not have permission to craft this recipe.");

        put("mech.hiddenswitch.key", "The key did not fit!");
        put("mech.hiddenswitch.toggle", "You hear the muffled click of a switch!");

        put("mech.pay.create", "Pay Created!");
        put("mech.pay.success", "Payment Successful! You paid: ");
        put("mech.pay.not-enough-money", "Payment Failed! You don't have enough money.");
        put("mech.pay.failed-to-pay", "Payment Failed! The money failed to be exchanged.");

        put("circuits.pipes.create", "Pipe created!");
        put("circuits.pipes.pipe-not-found", "Failed to find pipe!");

        put("worldedit.ic.unsupported", "WorldEdit selection type currently unsupported for IC's!");
        put("worldedit.ic.notfound", "WorldEdit not found!");
        put("worldedit.ic.noselection", "No selection was found!");
    }};
}