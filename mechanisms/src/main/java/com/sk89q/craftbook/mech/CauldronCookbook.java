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

package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.util.Tuple2;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

// import java.io.*;

/**
 * Store of recipes.
 *
 * @author sk89q
 * @deprecated Use {@link com.sk89q.craftbook.mech.cauldron.ImprovedCauldronCookbook} instead
 */
@Deprecated
public class CauldronCookbook {

    private static final Pattern AT_PATTERN = Pattern.compile("@", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private static final Pattern ANYTHING_MULTIPLIED_BY_NUMBER_PATTERN = Pattern.compile("^.*\\*([0-9]+)$");

    /**
     * Constructs a CauldronCookbook - reads recipes.
     */
    public CauldronCookbook() {

        try {
            CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");
            if (recipes.size() != 0) {
                log.info(recipes.size() + " cauldron recipe(s) loaded");
            } else {
                log.warning("cauldron-recipes.txt had no recipes");
            }
        } catch (FileNotFoundException e) {
            log.info("cauldron-recipes.txt not found: " + e.getMessage());
            try {
                log.info("Looked in: " + new File(".").getCanonicalPath() + "/plugins/CraftBookMechanisms");
            } catch (IOException ioe) {
                // Eat error
            }
        } catch (IOException e) {
            log.warning("cauldron-recipes.txt not loaded: " + e.getMessage());
        }
    }

    /**
     * For fast recipe lookup.
     */
    private final List<Recipe> recipes = new ArrayList<Recipe>();

    /**
     * For logging purposes.
     */
    static final Logger log = Logger.getLogger("Minecraft");

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
    public Recipe find(Map<Tuple2<Integer, Short>, Integer> ingredients) {

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

        File file = new File("plugins/CraftBookMechanisms", path);
        FileReader input = null;
        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);
            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();
                // Blank lines
                if (line.isEmpty()) {
                    continue;
                }
                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.isEmpty()) {
                    continue;
                }
                String[] parts = COLON_PATTERN.split(line);
                if (parts.length < 3) {
                    log.log(Level.WARNING, "Invalid cauldron recipe line in " + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<Tuple2<Integer, Short>> ingredients = parseCauldronItems(parts[1]);
                    List<Tuple2<Integer, Short>> results = parseCauldronItems(parts[2]);
                    String[] groups = null;
                    if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                        groups = COMMA_PATTERN.split(parts[3]);
                    }
                    Recipe recipe = new Recipe(name, ingredients, results, groups);
                    add(recipe);
                }
            }
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
    private List<Tuple2<Integer, Short>> parseCauldronItems(String list) {

        String[] parts = COMMA_PATTERN.split(list);

        List<Tuple2<Integer, Short>> out = new ArrayList<Tuple2<Integer, Short>>();

        for (String part : parts) {
            int multiplier = 1;

            try {
                // Multiplier
                if (ANYTHING_MULTIPLIED_BY_NUMBER_PATTERN.matcher(part).matches()) {
                    int at = part.lastIndexOf('*');
                    multiplier = Integer.parseInt(part.substring(at + 1, part.length()));
                    part = part.substring(0, at);
                }

                try {
                    Short s = 0;
                    String[] split = AT_PATTERN.split(part);
                    Integer id = Integer.valueOf(split[0]);
                    if (split.length > 1) {
                        s = Short.valueOf(split[1]);
                    }
                    for (int i = 0; i < multiplier; i++) {
                        out.add(new Tuple2<Integer, Short>(id, s));
                    }
                } catch (NumberFormatException e) {
                    /*
                     * int item = server.getConfiguration().getItemId(part);
                     * 
                     * if (item > 0) { for (int i = 0; i < multiplier; i++) { out.add(item); } } else {
                     */
                    log.log(Level.WARNING, "Cauldron: Unknown item " + part);
                    // }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                log.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
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
        private final List<Tuple2<Integer, Short>> ingredients;
        /**
         * Stores a list of ingredients.
         */
        private final Map<Tuple2<Integer, Short>, Integer> ingredientLookup = new HashMap<Tuple2<Integer, Short>,
                Integer>();
        /**
         * List of resulting items or blocks.
         */
        private final List<Tuple2<Integer, Short>> results;
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
        public Recipe(String name, List<Tuple2<Integer, Short>> ingredients, List<Tuple2<Integer, Short>> results,
                      String[] groups) {

            this.name = name;
            this.ingredients = Collections.unmodifiableList(ingredients);
            this.results = Collections.unmodifiableList(results);
            this.groups = groups;

            // Make a list of required ingredients by item ID
            for (Tuple2<Integer, Short> id : ingredients) {
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
        public List<Tuple2<Integer, Short>> getIngredients() {

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
        public boolean hasAllIngredients(Map<Tuple2<Integer, Short>, Integer> check) {

            for (Map.Entry<Tuple2<Integer, Short>, Integer> entry : ingredientLookup.entrySet()) {
                Tuple2<Integer, Short> id = entry.getKey();
                if (!check.containsKey(id)) return false;
                else if (check.get(id) < entry.getValue()) return false;
            }
            return true;
        }

        /**
         * @return the results
         */
        public List<Tuple2<Integer, Short>> getResults() {

            return results;
        }
    }
}
