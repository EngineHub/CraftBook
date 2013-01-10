package com.sk89q.craftbook.mech.crafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RecipeManager extends LocalConfiguration {

    public static RecipeManager INSTANCE;
    private Collection<Recipe> recipes;
    protected final YAMLProcessor config;
    protected final Logger logger;

    public RecipeManager(YAMLProcessor config, Logger logger) {

        INSTANCE = this;
        this.config = config;
        this.logger = logger;
        load();
    }

    @Override
    public void load() {

        recipes = new ArrayList<Recipe>();
        if (config == null) {
            Bukkit.getLogger().severe("Failure loading recipes! Config is null!");
            return; // If the config is null, it can't continue.
        }

        try {
            config.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> keys = config.getKeys("crafting-recipes");
        if (keys != null) {
            for (String key : keys) {
                recipes.add(new Recipe(key, config));
            }
        }
    }

    public Collection<Recipe> getRecipes() {

        return recipes;
    }

    public static final class Recipe {

        private final String id;
        private final YAMLProcessor config;

        private RecipeType type;
        private Collection<CraftingItemStack> ingredients;
        private HashMap<CraftingItemStack, Character> items;
        private Collection<CraftingItemStack> results;
        private List<String> shape;

        private Recipe(String id, YAMLProcessor config) {

            this.id = id;
            this.config = config;
            ingredients = new ArrayList<CraftingItemStack>();
            results = new ArrayList<CraftingItemStack>();
            items = new HashMap<CraftingItemStack, Character>();
            load();
        }

        private void load() {

            type = RecipeType.getTypeFromName(config.getString("crafting-recipes." + id + "type"));
            if (type != RecipeType.SHAPED2X2 && type != RecipeType.SHAPED3X3) {
                ingredients = getItems("crafting-recipes." + id + ".ingredients");
            } else {
                items = getHashItems("crafting-recipes." + id + ".ingredients");
                shape = config.getStringList("crafting-recipes." + id + ".shape", Arrays.asList(""));
            }
            results = getItems("crafting-recipes." + id + ".results");
        }

        private HashMap<CraftingItemStack, Character> getHashItems(String path) {

            HashMap<CraftingItemStack, Character> items = new HashMap<CraftingItemStack, Character>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String item = String.valueOf(oitem);
                    if (item == null || item.isEmpty()) continue;
                    String[] split = RegexUtil.COLON_PATTERN.split(item);
                    Material material;
                    try {
                        material = Material.getMaterial(Integer.parseInt(split[0]));
                    } catch (NumberFormatException e) {
                        // use the name
                        material = Material.getMaterial(split[0].toUpperCase());
                    }
                    if (material != null) {
                        CraftingItemStack itemStack = new CraftingItemStack(material);
                        if (split.length > 1) {
                            itemStack.setData(Short.parseShort(split[1]));
                        } else {
                            itemStack.setData((short) 0);
                        }
                        itemStack.setAmount(1);
                        items.put(itemStack, config.getString(item).toCharArray()[0]);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("An error occured generating ingredients for recipe: " + id);
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
            return items;
        }

        private Collection<CraftingItemStack> getItems(String path) {

            Collection<CraftingItemStack> items = new ArrayList<CraftingItemStack>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String item = String.valueOf(oitem);
                    if (item == null || item.isEmpty()) continue;
                    String[] split = RegexUtil.COLON_PATTERN.split(item);
                    Material material;
                    try {
                        material = Material.getMaterial(Integer.parseInt(split[0]));
                    } catch (NumberFormatException e) {
                        // use the name
                        material = Material.getMaterial(split[0].toUpperCase());
                    }
                    if (material != null) {
                        CraftingItemStack itemStack = new CraftingItemStack(material);
                        if (split.length > 1) {
                            itemStack.setData(Short.parseShort(split[1]));
                        } else {
                            itemStack.setData((short) 0);
                        }
                        itemStack.setAmount(config.getInt(item, 1));
                        items.add(itemStack);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("An error occured generating ingredients for recipe: " + id);
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
            return items;
        }

        public String getId() {

            return id;
        }

        public RecipeType getType() {

            return type;
        }

        public Collection<CraftingItemStack> getIngredients() {

            return ingredients;
        }

        public String[] getShape() {

            return shape.toArray(new String[shape.size()]);
        }

        public HashMap<CraftingItemStack, Character> getShapedIngredients() {

            return items;
        }

        public CraftingItemStack getResult() {

            try {
                return results.iterator().next();
            } catch (Exception e) {
                return null;
            }
        }

        public enum RecipeType {
            SHAPELESS("Shapeless"), SHAPED3X3("Shaped3x3"), SHAPED2X2("Shaped2x2"), FURNACE("Furnace");

            private String name;

            private RecipeType(String name) {

                this.name = name;
            }

            public String getName() {

                return name;
            }

            public static RecipeType getTypeFromName(String name) {

                for (RecipeType t : RecipeType.values()) { if (t.getName().equalsIgnoreCase(name)) return t; }
                return SHAPELESS; // Default to shapeless
            }
        }
    }
}
