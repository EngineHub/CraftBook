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

package org.enginehub.craftbook.mechanics.cauldron;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanics.cauldron.ImprovedCauldron.UnknownRecipeException;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Silthus
 */
public class ImprovedCauldronCookbook {

    private Collection<Recipe> recipes;
    protected final YAMLProcessor config;

    public ImprovedCauldronCookbook(YAMLProcessor config) {

        this.config = config;
        load();
    }

    public void load() {

        recipes = new ArrayList<>();

        if (config == null) return; // If the config is null, it can't continue.

        try {
            config.load();
        } catch (IOException e) {
            CraftBook.LOGGER.error("Corrupt Cauldron cauldron-recipes.yml File! Make sure that the correct syntax has been used, and that there are no tabs!");
            e.printStackTrace();
        }

        List<String> keys = config.getKeys("cauldron-recipes");
        if (keys != null)
            for (String key : keys)
                recipes.add(new Recipe(key, config));
    }

    public boolean hasRecipes() {
        return recipes.size() > 0;
    }

    public Recipe getRecipe(Collection<CauldronItemStack> items) throws UnknownRecipeException {

        for (Recipe recipe : recipes) {
            if (recipe.checkIngredients(items)) {
                return recipe;
            }
        }
        throw new UnknownRecipeException("Are you sure you have the right ingredients?");
    }

    public static final class Recipe {

        private final String id;
        private final YAMLProcessor config;

        private String name;
        private String description;
        private Collection<CauldronItemStack> ingredients;
        private Collection<CauldronItemStack> results;
        private double chance;

        private Recipe(String id, YAMLProcessor config) {

            this.id = id;
            this.config = config;
            ingredients = new ArrayList<>();
            results = new ArrayList<>();
            chance = 60;
            load();
        }

        private void load() {

            name = config.getString("cauldron-recipes." + id + ".name");
            description = config.getString("cauldron-recipes." + id + ".description");
            ingredients = getItems("cauldron-recipes." + id + ".ingredients");
            results = getItems("cauldron-recipes." + id + ".results");
            chance = config.getDouble("cauldron-recipes." + id + ".chance", 60);
        }

        private Collection<CauldronItemStack> getItems(String path) {

            Collection<CauldronItemStack> items = new ArrayList<>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String okey = String.valueOf(oitem);
                    String item = okey.trim();

                    ItemStack stack = ItemUtil.makeItemValid(ItemSyntax.getItem(item));

                    if (stack != null) {

                        stack.setAmount(config.getInt(path + "." + okey, 1));
                        CauldronItemStack itemStack = new CauldronItemStack(stack);
                        items.add(itemStack);
                    }
                }
            } catch (Exception e) {
                CraftBook.LOGGER.error("An error occured generating ingredients for cauldron recipe: " + id);
                e.printStackTrace();
            }
            return items;
        }

        public String getId() {

            return id;
        }

        public String getName() {

            return name;
        }

        public String getDescription() {

            return description;
        }

        public double getChance() {

            return chance;
        }

        /**
         * Checks if the recipe
         *
         * @param items
         * @return
         */
        public boolean checkIngredients(Collection<CauldronItemStack> items) {

            if (items.size() <= 0) return false;
            int count = 0;
            for (CauldronItemStack item : items) {
                if (!ingredients.contains(item)) return false;
                count++;
            }
            return count == ingredients.size();
        }

        public Collection<CauldronItemStack> getResults() {

            return results;
        }
    }
}