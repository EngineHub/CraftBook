package com.sk89q.craftbook.mech.cauldron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Silthus
 */
public class ImprovedCauldronCookbook extends LocalConfiguration {

    public static ImprovedCauldronCookbook INSTANCE;
    private Collection<Recipe> recipes;
    protected final YAMLProcessor config;
    protected final Logger logger;

    public ImprovedCauldronCookbook(YAMLProcessor config, Logger logger) {

        INSTANCE = this;
        this.config = config;
        this.logger = logger;
        load();
    }

    @Override
    public void load() {

        recipes = new ArrayList<Recipe>();

        if (config == null) return; // If the config is null, it can't continue.

        List<String> keys = config.getKeys("cauldron-recipes");
        if (keys != null) {
            for (String key : keys) {
                recipes.add(new Recipe(key, config));
            }
        }
    }
    public Recipe getRecipe(Collection<CauldronItemStack> items) throws UnknownRecipeException {

        for (Recipe recipe : recipes) { if (recipe.checkIngredients(items)) return recipe; }
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
            ingredients = new ArrayList<CauldronItemStack>();
            results = new ArrayList<CauldronItemStack>();
            chance = 60;
            load();
        }

        private void load() {

            name = config.getString("name");
            description = config.getString("description");
            ingredients = getItems("cauldron-recipes." + id + ".ingredients");
            results = getItems("cauldron-recipes." + id + ".results");
            chance = config.getDouble("chance", 60);
        }

        private Collection<CauldronItemStack> getItems(String path) {

            Collection<CauldronItemStack> items = new ArrayList<CauldronItemStack>();
            try {
                for (String item : config.getKeys(path)) {
                    String[] split = RegexUtil.COLON_PATTERN.split(item);
                    Material material;
                    try {
                        material = Material.getMaterial(Integer.parseInt(split[0]));
                    } catch (NumberFormatException e) {
                        // use the name
                        material = Material.getMaterial(split[0].toUpperCase());
                    }
                    if (material != null) {
                        CauldronItemStack itemStack = new CauldronItemStack(material);
                        if (split.length > 1) {
                            itemStack.setData(Short.parseShort(split[1]));
                        } else {
                            itemStack.setData((short) -1);
                        }
                        itemStack.setAmount(config.getInt(item, 1));
                        items.add(itemStack);
                    }
                }
            } catch (Exception ignored) {

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
         *
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