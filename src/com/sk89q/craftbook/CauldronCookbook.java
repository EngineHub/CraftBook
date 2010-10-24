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

package com.sk89q.craftbook;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Store of recipes.
 *
 * @author sk89q
 */
public class CauldronCookbook {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * For fast recipe lookup.
     * 
     */
    private List<CauldronRecipe> recipes =
            new ArrayList<CauldronRecipe>();

    /**
     * Adds a recipe.
     *
     * @param recipe
     */
    public void add(CauldronRecipe recipe) {
        recipes.add(recipe);
    }

    /**
     * Gets a recipe by its ingredients. The list will be sorted.
     *
     * @param ingredients
     */
    public CauldronRecipe find(Map<Integer,Integer> ingredients) {
        for (CauldronRecipe recipe : recipes) {
            if (recipe.hasAllIngredients(ingredients)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Get the number of recipes.
     * 
     * @return
     */
    public int size() {
        return recipes.size();
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static CauldronCookbook readCauldronRecipes(String path)
            throws IOException {
        File file = new File(path);
        FileReader input = null;
        CauldronCookbook cookbook = new CauldronCookbook();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank lines
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.equals("")) {
                    continue;
                }

                String[] parts = line.split(":");
                if (parts.length < 3) {
                    logger.log(Level.WARNING, "Invalid cauldron recipe line in "
                            + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    String[] ingredientStrs = parts[1].split(",");
                    String[] resultStrs = parts[2].split(",");
                    List<Integer> ingredients = new ArrayList<Integer>();
                    List<Integer> results = new ArrayList<Integer>();

                    try {
                        for (String ingredientStr : ingredientStrs) {
                            ingredients.add(Integer.valueOf(ingredientStr));
                        }
                        for (String resultStr : resultStrs) {
                            results.add(Integer.valueOf(resultStr));
                        }
                        CauldronRecipe recipe =
                                new CauldronRecipe(name, ingredients, results);
                        cookbook.add(recipe);
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, "Bad cauldron recipe (non-number encountered) in "
                                + file.getName() + " for '" + line + "'");
                    }
                }
            }

            return cookbook;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e2) {
            }
        }
    }
}
