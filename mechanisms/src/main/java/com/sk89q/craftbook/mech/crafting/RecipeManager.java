package com.sk89q.craftbook.mech.crafting;

import com.sk89q.craftbook.BaseConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class RecipeManager extends BaseConfiguration {

    public static RecipeManager INSTANCE;
    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private Collection<Recipe> recipes;
    private File config;
    private File dataFolder;

    public RecipeManager(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
        INSTANCE = this;
        this.dataFolder = dataFolder;
    }

    @Override
    public void load() {

        recipes = new ArrayList<Recipe>();
        config = new File(dataFolder, "crafting-recipes.yml");
        load(cfg.getConfigurationSection("crafting-recipes"));
    }

    public Collection<Recipe> getRecipes() {

        return recipes;
    }

    public boolean reload() {

        recipes.clear();
        load(YamlConfiguration.loadConfiguration(config).getConfigurationSection("crafting-recipes"));

        return true;
    }

    private void load(ConfigurationSection cfg) {
        // lets load all recipes
        if (cfg == null) return; // If the config is null, it can't continue.
        Set<String> keys = cfg.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                recipes.add(new Recipe(key, cfg));
            }
        }
    }

    public static final class Recipe {

        private final String id;
        private final ConfigurationSection config;

        private RecipeType type;
        private Collection<CraftingItemStack> ingredients;
        private HashMap<CraftingItemStack, Character> items;
        private Collection<CraftingItemStack> results;
        private List<String> shape;

        private Recipe(String id, ConfigurationSection cfg) {

            this.id = id;
            config = cfg.getConfigurationSection(id);
            ingredients = new ArrayList<CraftingItemStack>();
            results = new ArrayList<CraftingItemStack>();
            items = new HashMap<CraftingItemStack, Character>();
            load();
        }

        private void load() {

            type = RecipeType.getTypeFromName(config.getString("type"));
            if (type != RecipeType.SHAPED2X2 && type != RecipeType.SHAPED3X3) {
                ingredients = getItems(config.getConfigurationSection("ingredients"));
            } else {
                items = getHashItems(config.getConfigurationSection("ingredients"));
                shape = config.getStringList("shape");
            }
            results = getItems(config.getConfigurationSection("results"));
        }

        private HashMap<CraftingItemStack, Character> getHashItems(ConfigurationSection section) {

            HashMap<CraftingItemStack, Character> items = new HashMap<CraftingItemStack, Character>();
            try {
                for (String item : section.getKeys(false)) {
                    if (item == null || item.isEmpty()) continue;
                    String[] split = COLON_PATTERN.split(item);
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
                        items.put(itemStack, section.getString(item).toCharArray()[0]);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("An error occured generating ingredients for recipe: " + section.getName());
            }
            return items;
        }

        private Collection<CraftingItemStack> getItems(ConfigurationSection section) {

            Collection<CraftingItemStack> items = new ArrayList<CraftingItemStack>();
            try {
                for (String item : section.getKeys(false)) {
                    if (item == null || item.isEmpty()) continue;
                    String[] split = COLON_PATTERN.split(item);
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
                        itemStack.setAmount(section.getInt(item, 1));
                        items.add(itemStack);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("An error occured generating ingredients for recipe: " + section.getName());
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
