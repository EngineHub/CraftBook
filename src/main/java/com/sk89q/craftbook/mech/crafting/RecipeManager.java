package com.sk89q.craftbook.mech.crafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RecipeManager extends LocalConfiguration {

    public static RecipeManager INSTANCE;
    private Collection<Recipe> recipes;
    protected static YAMLProcessor config;

    public RecipeManager(YAMLProcessor config) {

        INSTANCE = this;
        RecipeManager.config = config;
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
                try {
                    recipes.add(new Recipe(key, config));
                } catch (InvalidCraftingException e) {
                    BukkitUtil.printStacktrace(e);
                }
            }
        }
    }

    public void save() {

        if (config == null) {
            Bukkit.getLogger().severe("Failure saving recipes! Config is null!");
            return; // If the config is null, it can't continue.
        }

        for(Recipe recipe : recipes) {
            recipe.save();
        }

        config.save();

        load();
    }

    public Collection<Recipe> getRecipes() {

        return recipes;
    }

    public static final class Recipe {

        private final String id;

        private RecipeType type;
        private Collection<CraftingItemStack> ingredients;
        private HashMap<CraftingItemStack, Character> items;
        private CraftingItemStack result;
        private List<String> shape;

        public boolean hasAdvancedData() {
            for(CraftingItemStack stack : ingredients)
                if(stack.hasAdvancedData())
                    return true;
            for(CraftingItemStack stack : items.keySet())
                if(stack.hasAdvancedData())
                    return true;
            if(result.hasAdvancedData())
                return true;

            return !advancedData.isEmpty();
        }

        private Recipe(String id, YAMLProcessor config) throws InvalidCraftingException {

            this.id = id;
            ingredients = new ArrayList<CraftingItemStack>();
            items = new HashMap<CraftingItemStack, Character>();
            load();
        }

        public Recipe(String id, RecipeType type, HashMap<CraftingItemStack, Character> items, List<String> shape, CraftingItemStack result, HashMap<String, Object> advancedData) throws InvalidCraftingException {

            this.id = id;
            this.type = type;
            this.items = items;
            this.shape = shape;
            this.result = result;
            this.advancedData = advancedData;
        }

        public Recipe(String id, RecipeType type, List<CraftingItemStack> ingredients, CraftingItemStack result, HashMap<String, Object> advancedData) throws InvalidCraftingException {

            this.id = id;
            this.type = type;
            this.ingredients = ingredients;
            this.result = result;
            this.advancedData = advancedData;
        }

        private void load() throws InvalidCraftingException {

            type = RecipeType.getTypeFromName(config.getString("crafting-recipes." + id + ".type"));
            if (type != RecipeType.SHAPED) {
                ingredients = getItems("crafting-recipes." + id + ".ingredients");
            } else {
                items = getShapeIngredients("crafting-recipes." + id + ".ingredients");
                shape = config.getStringList("crafting-recipes." + id + ".shape", Arrays.asList(""));
            }
            Iterator<CraftingItemStack> iterator = getItems("crafting-recipes." + id + ".results").iterator();
            if(iterator.hasNext())
                result = iterator.next();
            else
                throw new InvalidCraftingException("Result is invalid in recipe: "+ id);

            if(iterator.hasNext()) {
                ArrayList<CraftingItemStack> extraResults = new ArrayList<CraftingItemStack>();
                while(iterator.hasNext())
                    extraResults.add(iterator.next());
                addAdvancedData("extra-results", extraResults);
            }

            String permNode = config.getString("crafting-recipes." + id + ".permission-node", null);
            if (permNode != null)
                addAdvancedData("permission-node", permNode);
        }

        @SuppressWarnings("unchecked")
        public void save() {

            config.setProperty("crafting-recipes." + id + ".type", type.name);
            if(type != RecipeType.SHAPED) {
                for(CraftingItemStack stack : ingredients)
                    config.setProperty("crafting-recipes." + id + ".ingredients." + stack.toString(), stack.getItemStack().getAmount());
            } else {
                for(CraftingItemStack stack : items.keySet())
                    config.setProperty("crafting-recipes." + id + ".ingredients." + stack.toString(), items.get(stack));
                config.setProperty("crafting-recipes." + id + ".shape", shape);
            }

            config.setProperty("crafting-recipes." + id + ".results." + result.toString(), result.getItemStack().getAmount());
            if(hasAdvancedData("extra-results")) {

                ArrayList<CraftingItemStack> extraResults = (ArrayList<CraftingItemStack>) getAdvancedData("extra-results");
                for(CraftingItemStack s : extraResults)
                    config.setProperty("crafting-recipes." + id + ".results." + s.toString(), s.getItemStack().getAmount());
            }
            if(hasAdvancedData("permission-node"))
                config.setProperty("crafting-recipes." + id + ".permission-node", getAdvancedData("permission-node"));
        }

        private HashMap<CraftingItemStack, Character> getShapeIngredients(String path) {

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
                        ItemStack stack = new ItemStack(material);
                        if (split.length > 1) {
                            stack.setDurability(Short.parseShort(split[1]));
                        } else {
                            stack.setDurability((short) 0);
                        }
                        stack.setAmount(1);
                        items.put(new CraftingItemStack(stack), config.getString(path + "." + item, "a").charAt(0));
                    }
                }
            } catch (Exception e) {
                CraftBookPlugin.inst().getLogger().severe("An error occured generating ingredients for recipe: " + id);
                BukkitUtil.printStacktrace(e);
            }
            return items;
        }

        private Collection<CraftingItemStack> getItems(String path) {

            Collection<CraftingItemStack> items = new ArrayList<CraftingItemStack>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String item = String.valueOf(oitem);
                    if (item == null || item.isEmpty()) continue;
                    item = RegexUtil.PIPE_PATTERN.split(item)[0];
                    String[] split = RegexUtil.COLON_PATTERN.split(item);
                    Material material;
                    try {
                        material = Material.getMaterial(Integer.parseInt(split[0]));
                    } catch (NumberFormatException e) {
                        // use the name
                        material = Material.getMaterial(split[0].toUpperCase());
                    }
                    if (material != null) {
                        ItemStack stack = new ItemStack(material);
                        if (split.length > 1) {
                            stack.setDurability(Short.parseShort(split[1]));
                        } else {
                            stack.setDurability((short) 0);
                        }
                        stack.setAmount(config.getInt(path + "." + item, 1));
                        CraftingItemStack itemStack = new CraftingItemStack(stack);
                        if(RegexUtil.PIPE_PATTERN.split(String.valueOf(oitem)).length > 1) {
                            itemStack.addAdvancedData("name", RegexUtil.PIPE_PATTERN.split(String.valueOf(oitem))[1]);
                        }
                        if(RegexUtil.PIPE_PATTERN.split(String.valueOf(oitem)).length > 2) {
                            itemStack.addAdvancedData("lore", Arrays.asList(RegexUtil.PIPE_PATTERN.split(String.valueOf(oitem))).subList(2, RegexUtil.PIPE_PATTERN.split(String.valueOf(oitem)).length));
                        }
                        items.add(itemStack);
                    }
                }
            } catch (Exception e) {
                CraftBookPlugin.inst().getLogger().severe("An error occured generating ingredients for recipe: " + id);
                BukkitUtil.printStacktrace(e);
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

            return result;
        }

        //Advanced data
        private HashMap<String, Object> advancedData = new HashMap<String, Object>();

        public boolean hasAdvancedData(String key) {
            return advancedData.containsKey(key);
        }

        public Object getAdvancedData(String key) {
            return advancedData.get(key);
        }

        public void addAdvancedData(String key, Object data) {
            Bukkit.getLogger().info("Adding advanced data of type: " + key + " to an ItemStack!");
            advancedData.put(key, data);
        }
    }

    public enum RecipeType {
        SHAPELESS("Shapeless"), FURNACE("Furnace"), SHAPED("Shaped");

        private String name;

        private RecipeType(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }

        public static RecipeType getTypeFromName(String name) {

            if(name.equalsIgnoreCase("Shaped2x2") || name.equalsIgnoreCase("Shaped3x3")) {
                CraftBookPlugin.logger().warning("You are using deprecated recipe type '" + name + "', we recommend you change it to 'shaped'!");
                return SHAPED;
            }

            for (RecipeType t : RecipeType.values()) {
                if (t.getName().equalsIgnoreCase(name))
                    return t;
            }
            return SHAPELESS; // Default to shapeless
        }
    }
}
