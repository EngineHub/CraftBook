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

package org.enginehub.craftbook.mechanics.crafting;

import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitCraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.crafting.RecipeManager.RecipeType;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class RecipeCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
            commandManager,
            RecipeCommandsRegistration.builder(),
            new RecipeCommands()
        );
    }

    public RecipeCommands() {
    }

    private final CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(name = "remove", aliases = { "delete" }, desc = "Delete a recipe")
    @CommandPermissions(value = "craftbook.mech.recipes.remove")
    public void remove(Actor actor, @Arg(desc = "The recipe to remove") String recipe) {
        if (RecipeManager.INSTANCE.removeRecipe(recipe)) {
            actor.print("Recipe removed successfully! This will be in effect after a restart!");
            RecipeManager.INSTANCE.save();
        } else
            actor.printError("Recipe doesn't exist!");
    }

    @Command(name = "save", aliases = { "add" }, desc = "Saves the current recipe")
    @CommandPermissions(value = "craftbook.mech.recipes.add")
    public void saveRecipe(CraftBookPlayer player,
                           @Arg(desc = "The recipe to remove") String name,
                           @Arg(desc = "The recipe type") String recipeType,
                           @ArgFlag(name = 'p', desc = "The permission node to assign") String permissionNode
    ) {
        Player bukkitPlayer = ((BukkitCraftBookPlayer) player).getPlayer();

        RecipeType type = RecipeType.getTypeFromName(recipeType);
        HashMap<String, Object> advancedData = new HashMap<>();

        if (permissionNode != null) {
            advancedData.put("permission-node", permissionNode);
        }

        ItemStack[] slots = new ItemStack[] { bukkitPlayer.getInventory().getItem(9), bukkitPlayer.getInventory().getItem(10),
            bukkitPlayer.getInventory().getItem(11), bukkitPlayer.getInventory().getItem(18), bukkitPlayer.getInventory().getItem(19),
            bukkitPlayer.getInventory().getItem(20), bukkitPlayer.getInventory().getItem(27), bukkitPlayer.getInventory().getItem(28),
            bukkitPlayer.getInventory().getItem(29) };

        if (type == RecipeType.SHAPED) {

            LinkedHashMap<CraftingItemStack, Character> items = new LinkedHashMap<>();

            int furtherestX = -1;
            int furtherestY = -1;

            for (int slot = 0; slot < 3; slot++) {
                ItemStack stack = slots[slot];
                if (ItemUtil.isStackValid(stack)) {
                    furtherestY = 0;
                    if (furtherestX < slot)
                        furtherestX = slot;
                }
            }
            for (int slot = 3; slot < 6; slot++) {
                ItemStack stack = slots[slot];
                if (ItemUtil.isStackValid(stack)) {
                    furtherestY = 1;
                    if (furtherestX < slot - 3)
                        furtherestX = slot - 3;
                }
            }
            for (int slot = 6; slot < 9; slot++) {
                ItemStack stack = slots[slot];
                if (ItemUtil.isStackValid(stack)) {
                    furtherestY = 2;
                    if (furtherestX < slot - 6)
                        furtherestX = slot - 6;
                }
            }

            if (furtherestX > 2)
                furtherestX = 2;

            String[] shape = new String[furtherestY + 1];
            Character[] characters = new Character[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' };
            int curChar = 0;

            for (int y = 0; y < furtherestY + 1; y++) {
                for (int x = 0; x < furtherestX + 1; x++) {

                    String c = " ";
                    CraftingItemStack stack = new CraftingItemStack(slots[x + y * 3]);
                    if (ItemUtil.isStackValid(stack.getItemStack())) {

                        boolean found = false;
                        for (Entry<CraftingItemStack, Character> st : items.entrySet()) {
                            if (st.getKey().isSameType(stack)) {
                                c = st.getValue().toString();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            items.put(stack, characters[curChar]);
                            c = characters[curChar].toString();
                            curChar++;
                        }
                    }

                    if (x == 0)
                        shape[y] = c;
                    else
                        shape[y] = shape[y] + c;
                }
            }

            List<CraftingItemStack> results = getResults(bukkitPlayer.getInventory());
            if (results.size() > 1)
                advancedData.put("extra-results", results.subList(1, results.size()));
            else if (results.isEmpty()) {
                player.printError("Results are required to create a recipe!");
                return;
            }

            try {
                RecipeManager.Recipe recipe = RecipeManager.INSTANCE.new Recipe(name, type, items, Arrays.asList(shape), results.get(0), advancedData);
                RecipeManager.INSTANCE.addRecipe(recipe);
                if (CustomCrafting.INSTANCE == null) {
                    player.printError("You do not have CustomCrafting enabled, or Java has bugged and unloaded it (Did you use /reload?)!");
                    return;
                }
                CustomCrafting.INSTANCE.addRecipe(recipe);
                player.print("Successfully added a new " + type.name() + " recipe!");
            } catch (Exception e) {
                player.printError("Error adding recipe! See console for more details!");
                e.printStackTrace();
            }

        } else if (type == RecipeType.SHAPELESS || type == RecipeType.FURNACE) {

            ArrayList<CraftingItemStack> ingredients = new ArrayList<>();

            for (ItemStack slot : slots) {

                if (!ItemUtil.isStackValid(slot))
                    continue;

                CraftingItemStack stack = new CraftingItemStack(slot.clone());

                boolean used = false;
                for (CraftingItemStack compare : ingredients) {

                    if (compare.isSameType(stack)) {
                        ingredients.set(ingredients.indexOf(compare), compare.add(stack));
                        used = true;
                        break;
                    }
                }

                if (!used)
                    ingredients.add(stack);
            }

            List<CraftingItemStack> results = getResults(bukkitPlayer.getInventory());
            if (results.size() > 1)
                advancedData.put("extra-results", results.subList(1, results.size()));
            else if (results.isEmpty()) {
                player.printError("Results are required to create a recipe!");
                return;
            }

            try {
                RecipeManager.Recipe recipe = RecipeManager.INSTANCE.new Recipe(name, type, ingredients, results.get(0), advancedData);
                RecipeManager.INSTANCE.addRecipe(recipe);
                CustomCrafting.INSTANCE.addRecipe(recipe);
                player.print("Successfully added a new " + type.name() + " recipe!");
            } catch (Exception e) {
                player.printError("Error adding recipe! See console for more details!");
                e.printStackTrace();
            }
        }
    }

    public List<CraftingItemStack> getResults(Inventory inv) {

        List<CraftingItemStack> results = new ArrayList<>();

        for (int i = 21; i < 27; i++) {

            ItemStack slot = inv.getItem(i);
            if (!ItemUtil.isStackValid(slot))
                break;

            results.add(new CraftingItemStack(slot));
        }

        return results;
    }
}