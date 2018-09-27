package com.sk89q.craftbook.mechanics.cauldron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.cauldron.ImprovedCauldron.UnknownRecipeException;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Silthus
 */
public class ImprovedCauldronCookbook {

    private Collection<Recipe> recipes;
    protected final YAMLProcessor config;
    protected final Logger logger;

    public ImprovedCauldronCookbook(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
        load();
    }

    public void load() {

        recipes = new ArrayList<>();

        if (config == null) return; // If the config is null, it can't continue.

        try {
            config.load();
        } catch (IOException e) {
            CraftBookPlugin.logger().severe("Corrupt Cauldron cauldron-recipes.yml File! Make sure that the correct syntax has been used, and that there are no tabs!");
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
                CraftBookPlugin.inst().getLogger().severe("An error occured generating ingredients for cauldron recipe: " + id);
                CraftBookBukkitUtil.printStacktrace(e);
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