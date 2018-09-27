// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mechanics.cauldron.legacy;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

// import java.io.*;

/**
 * Store of recipes.
 *
 * @author sk89q
 * @deprecated Use {@link com.sk89q.craftbook.mechanics.cauldron.ImprovedCauldronCookbook} instead
 */
@Deprecated
public class CauldronCookbook {

    private static final Pattern AT_PATTERN = Pattern.compile("@", Pattern.LITERAL);
    private static final Pattern ANYTHING_MULTIPLIED_BY_NUMBER_PATTERN = Pattern.compile("^.*\\*([0-9]+)$");

    /**
     * Constructs a CauldronCookbook - reads recipes.
     */
    public CauldronCookbook() {

        try {
            CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");
            if (recipes.size() != 0) {
                CraftBookPlugin.logger().info(recipes.size() + " cauldron recipe(s) loaded");
            } else {
                CraftBookPlugin.logger().warning("cauldron-recipes.txt had no recipes");
            }
        } catch (FileNotFoundException e) {
            CraftBookPlugin.logger().info("cauldron-recipes.txt not found: " + e.getMessage());
            try {
                CraftBookPlugin.logger().info("Looked in: " + CraftBookPlugin.inst().getDataFolder().getCanonicalPath());
            } catch (IOException ioe) {
                // Eat error
            }
        } catch (IOException e) {
            CraftBookPlugin.logger().warning("cauldron-recipes.txt not loaded: " + e.getMessage());
        }
    }

    /**
     * For fast recipe lookup.
     */
    private final List<Recipe> recipes = new ArrayList<>();

    /**
     * Adds a recipe.
     *
     * @param recipe
     */
    public void add(Recipe recipe) {

        recipes.add(recipe);
    }

    /**
     * Gets a recipe by its ingredients. If multiple recipies have the all of the specified ingredients,
     * the first one that matches will be selected
     * (the list is checked in the same order as recipes are entered in the config file).
     *
     * @param ingredients
     *
     * @return a recipe matching the given ingredients
     */
    public Recipe find(Map<BlockStateHolder, Integer> ingredients) {

        for (Recipe recipe : recipes) { if (recipe.hasAllIngredients(ingredients)) return recipe; }
        return null;
    }

    /**
     * Get the number of recipes.
     *
     * @return the number of recipes.
     */
    public int size() {

        return recipes.size();
    }

    private CauldronCookbook readCauldronRecipes(String path) throws IOException {

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), path), path);

        File file = new File(CraftBookPlugin.inst().getDataFolder(), path);
        InputStreamReader input = null;
        try {
            input = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader buff = new BufferedReader(input);
            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();
                // Blank lines
                if (line.isEmpty()) {
                    continue;
                }
                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }
                String[] parts = RegexUtil.COLON_PATTERN.split(line);
                if (parts.length < 3) {
                    CraftBookPlugin.logger().log(Level.WARNING, "Invalid cauldron recipe line in " + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<BlockStateHolder> ingredients = parseCauldronItems(parts[1]);
                    List<BlockStateHolder> results = parseCauldronItems(parts[2]);
                    String[] groups = null;
                    if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                        groups = RegexUtil.COMMA_PATTERN.split(parts[3]);
                    }
                    Recipe recipe = new Recipe(name, ingredients, results, groups);
                    add(recipe);
                }
            }
            buff.close();
            return this;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Parse a list of cauldron items.
     */
    private List<BlockStateHolder> parseCauldronItems(String list) {

        String[] parts = RegexUtil.COMMA_PATTERN.split(list);

        List<BlockStateHolder> out = new ArrayList<>();

        for (String part : parts) {
            int multiplier = 1;

            try {
                // Multiplier
                if (ANYTHING_MULTIPLIED_BY_NUMBER_PATTERN.matcher(part).matches()) {
                    int at = part.lastIndexOf('*');
                    multiplier = Integer.parseInt(part.substring(at + 1));
                    part = part.substring(0, at);
                }

                try {
                    String[] split = AT_PATTERN.split(part);
                    BlockStateHolder state = BlockSyntax.getBlock(StringUtil.joinString(split, ":"));
                    if (state != null) {
                        for (int i = 0; i < multiplier; i++) {
                            out.add(state);
                        }
                    }
                } catch (NumberFormatException e) {
                    /*
                     * int item = server.getConfiguration().getItemId(part);
                     * 
                     * if (item > 0) { for (int i = 0; i < multiplier; i++) { out.add(item); } } else {
                     */
                    CraftBookPlugin.logger().log(Level.WARNING, "Cauldron: Unknown item " + part);
                    // }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                CraftBookPlugin.logger().log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
            }
        }
        return out;
    }

    /**
     * @author sk89q
     */
    public static final class Recipe {

        /**
         * Recipe name.
         */
        private final String name;
        /**
         * Stores a list of ingredients.
         */
        private final List<BlockStateHolder> ingredients;
        /**
         * Stores a list of ingredients.
         */
        private final Map<BlockStateHolder, Integer> ingredientLookup = new HashMap<>();
        /**
         * List of resulting items or blocks.
         */
        private final List<BlockStateHolder> results;
        /**
         * List of groups that can use this recipe. This may be null.
         */
        private final String[] groups;

        /**
         * Construct the instance. The list will be sorted.
         *
         * @param name
         * @param ingredients
         * @param results
         * @param groups
         */
        public Recipe(String name, List<BlockStateHolder> ingredients, List<BlockStateHolder> results, String[] groups) {

            this.name = name;
            this.ingredients = Collections.unmodifiableList(ingredients);
            this.results = Collections.unmodifiableList(results);
            this.groups = groups;

            // Make a list of required ingredients by item ID
            for (BlockStateHolder id : ingredients) {
                if (ingredientLookup.containsKey(id)) {
                    ingredientLookup.put(id, ingredientLookup.get(id) + 1);
                } else {
                    ingredientLookup.put(id, 1);
                }
            }
        }

        /**
         * @return the name
         */
        public String getName() {

            return name;
        }

        /**
         * @return the ingredients
         */
        public List<BlockStateHolder> getIngredients() {

            return ingredients;
        }

        /**
         * @return the groups
         */
        public String[] getGroups() {

            return groups;
        }

        /**
         * Checks to see if all the ingredients are met.
         *
         * @param check
         */
        public boolean hasAllIngredients(Map<BlockStateHolder, Integer> check) {

            for (Map.Entry<BlockStateHolder, Integer> entry : ingredientLookup.entrySet()) {
                BlockStateHolder id = entry.getKey();
                if (!check.containsKey(id)) return false;
                else if (check.get(id) < entry.getValue()) return false;
            }
            return true;
        }

        /**
         * @return the results
         */
        public List<BlockStateHolder> getResults() {

            return results;
        }
    }
}
