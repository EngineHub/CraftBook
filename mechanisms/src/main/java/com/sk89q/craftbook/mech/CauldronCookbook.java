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

package com.sk89q.craftbook.mech;

import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
//import java.io.*;

/**
 * Store of recipes.
 *
 * @author sk89q
 */
public class CauldronCookbook {
	/**
	 * Constructs a CauldronCookbook - reads recipes.
	 */
	public CauldronCookbook(){

		try {
			CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");
			if (recipes.size() != 0) {
				log.info(recipes.size()
						+ " cauldron recipe(s) loaded");
			} else {
				log.warning("cauldron-recipes.txt had no recipes");
			}
		} catch (FileNotFoundException e) {
			log.info("cauldron-recipes.txt not found: " + e.getMessage());
			try {
				log.info("Looked in: " + (new File(".")).getCanonicalPath() + "/plugins/CraftBookMechanisms");
			} catch (IOException ioe) {
				// Eat error
			}
		} catch (IOException e) {
			log.warning("cauldron-recipes.txt not loaded: " + e.getMessage());
		}
	}
	/**
	 * For fast recipe lookup.
	 * 
	 */
	private List<Recipe> recipes =
		new ArrayList<Recipe>();

	/** 
	 * For logging purposes.
	 */
	static Logger log = Logger.getLogger("Minecraft");

	/**
	 * Adds a recipe.
	 *
	 * @param recipe
	 */
	public void add(Recipe recipe) {
		recipes.add(recipe);
	}

    /**
     * Gets a recipe by its ingredients. If multiple recipies have the all of
     * the specified ingredients, the first one that matches will be selected
     * (the list is checked in the same order as recipes are entered in the
     * config file).
     * 
     * @param ingredients
     * @return a recipe matching the given ingredients
     */
	public Recipe find(Map<Integer,Integer> ingredients) {
		for (Recipe recipe : recipes) {
			if (recipe.hasAllIngredients(ingredients)) {
				return recipe;
			}
		}
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
	private CauldronCookbook readCauldronRecipes(String path)
	throws IOException {
		File file = new File("plugins/CraftBookMechanisms", path);
		FileReader input = null;
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
				if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
					continue;
				}
				String[] parts = line.split(":");
				if (parts.length < 3) {
					log.log(Level.WARNING, "Invalid cauldron recipe line in "
							+ file.getName() + ": '" + line + "'");
				} else {
					String name = parts[0];
					List<Integer> ingredients = parseCauldronItems(parts[1]);
					List<Integer> results = parseCauldronItems(parts[2]);
					String[] groups = null;
					if (parts.length >= 4 && parts[3].trim().length() > 0) {
						groups = parts[3].split(",");
					}
					CauldronCookbook.Recipe recipe =
						new CauldronCookbook.Recipe(name, ingredients, results, groups);
					add(recipe);
				}
			}
			return this;
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Parse a list of cauldron items.
	 */
	private List<Integer> parseCauldronItems(String list) {
		String[] parts = list.split(",");

		List<Integer> out = new ArrayList<Integer>();

		for (String part : parts) {
			int multiplier = 1;

			try {
				// Multiplier
				if (part.matches("^.*\\*([0-9]+)$")) {
					int at = part.lastIndexOf("*");
					multiplier = Integer.parseInt(
							part.substring(at + 1, part.length()));
					part = part.substring(0, at);
				}

				try {
					for (int i = 0; i < multiplier; i++) {
						out.add(Integer.valueOf(part));
					}
				} catch (NumberFormatException e) {
					/*int item = server.getConfiguration().getItemId(part);

					if (item > 0) {
						for (int i = 0; i < multiplier; i++) {
							out.add(item);
						}
					} else {*/
					log.log(Level.WARNING, "Cauldron: Unknown item " + part);
					//}
				}
			} catch (NumberFormatException e) { // Bad multiplier
				log.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
			}
		}
		return out;
	}
	
	/**
	 *
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
		private final List<Integer> ingredients;
		/**
		 * Stores a list of ingredients.
		 */
		private final Map<Integer,Integer> ingredientLookup
		= new HashMap<Integer,Integer>();
		/**
		 * List of resulting items or blocks.
		 */
		private final List<Integer> results;
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
		public Recipe(String name, List<Integer> ingredients,
				List<Integer> results, String[] groups) {
			this.name = name;
			this.ingredients = Collections.unmodifiableList(ingredients);
			this.results = Collections.unmodifiableList(results);
			this.groups = groups;

			// Make a list of required ingredients by item ID
			for (Integer id : ingredients) {
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
		public List<Integer> getIngredients() {
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
		public boolean hasAllIngredients(Map<Integer,Integer> check) {
			for (Map.Entry<Integer,Integer> entry : ingredientLookup.entrySet()) {
				int id = entry.getKey();
				if (!check.containsKey(id)) {
					return false;
				} else if (check.get(id) < entry.getValue()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * @return the results
		 */
		public List<Integer> getResults() {
			return results;
		}
	}
}
