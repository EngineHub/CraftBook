package com.sk89q.craftbook.mech.crafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RecipeManager extends LocalConfiguration {

    public static RecipeManager INSTANCE;
    private HashSet<Recipe> recipes;
    protected static YAMLProcessor config;

    public RecipeManager(YAMLProcessor config) {

        INSTANCE = this;
        RecipeManager.config = config;
        load();
    }

    @Override
    public void load() {

        recipes = new HashSet<Recipe>();
        if (config == null) {
            CraftBookPlugin.logger().severe("Failure loading recipes! Config is null!");
            return; // If the config is null, it can't continue.
        }

        try {
            config.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.setHeader(
                "# CraftBook Custom Recipes. CraftBook Version: " + CraftBookPlugin.inst().getDescription().getVersion(),
                "# For more information on setting up custom recipes, see the wiki:",
                "# http://wiki.sk89q.com/wiki/CraftBook/Custom_crafting",
                "",
                "");

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
            CraftBookPlugin.logger().severe("Failure saving recipes! Config is null!");
            return; // If the config is null, it can't continue.
        }

        config.clear();

        config.setHeader(
                "# CraftBook Custom Recipes. CraftBook Version: " + CraftBookPlugin.inst().getDescription().getVersion(),
                "# For more information on setting up custom recipes, see the wiki:",
                "# http://wiki.sk89q.com/wiki/CraftBook/Custom_crafting",
                "",
                "");

        config.addNode("crafting-recipes");
        for(Recipe recipe : recipes) {
            recipe.save();
        }

        config.save();

        load();
    }

    public Collection<Recipe> getRecipes() {

        return recipes;
    }

    public void addRecipe(Recipe rec) {

        recipes.add(rec);
    }

    public boolean removeRecipe(String name) {

        Iterator<Recipe> recs = recipes.iterator();
        while(recs.hasNext()) {

            Recipe rec = recs.next();
            if(rec.getId().equalsIgnoreCase(name)) {
                recs.remove();
                return true;
            }
        }

        return false;
    }

    public static final class Recipe {

        private final String id;

        private RecipeType type;
        private Collection<CraftingItemStack> ingredients;
        private LinkedHashMap<CraftingItemStack, Character> items;
        private CraftingItemStack result;
        private List<String> shape;

        @Override
        public boolean equals(Object o) {

            if(o instanceof Recipe && o != null)
                return ((Recipe) o).getId() == id;
            else
                return false;
        }

        @Override
        public int hashCode() {

            return id.hashCode();
        }

        public boolean hasAdvancedData() {
            if(ingredients != null) {
                for(CraftingItemStack stack : ingredients)
                    if(stack.hasAdvancedData())
                        return true;
            }
            if(items != null) {
                for(CraftingItemStack stack : items.keySet())
                    if(stack.hasAdvancedData())
                        return true;
            }
            if(result.hasAdvancedData())
                return true;

            return !advancedData.isEmpty();
        }

        private Recipe(String id, YAMLProcessor config) throws InvalidCraftingException {

            this.id = id;
            ingredients = new ArrayList<CraftingItemStack>();
            items = new LinkedHashMap<CraftingItemStack, Character>();
            load();
        }

        public Recipe(String id, RecipeType type, LinkedHashMap<CraftingItemStack, Character> items, List<String> shape, CraftingItemStack result, HashMap<String, Object> advancedData) throws InvalidCraftingException {

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

            config.addNode("crafting-recipes." + id);
            config.setProperty("crafting-recipes." + id + ".type", type.name);
            if(type != RecipeType.SHAPED) {
                LinkedHashMap<String, Integer> resz = new LinkedHashMap<String, Integer>();
                for(CraftingItemStack stack : ingredients)
                    resz.put(stack.toString() + " ", stack.getItemStack().getAmount());
                config.setProperty("crafting-recipes." + id + ".ingredients", resz);
            } else {
                LinkedHashMap<String, Character> resz = new LinkedHashMap<String, Character>();
                for(CraftingItemStack stack : items.keySet())
                    resz.put(stack.toString() + " ", items.get(stack));
                config.setProperty("crafting-recipes." + id + ".ingredients", resz);
                config.setProperty("crafting-recipes." + id + ".shape", shape);
            }

            LinkedHashMap<String, Integer> resz = new LinkedHashMap<String, Integer>();
            resz.put(result.toString() + " ", result.getItemStack().getAmount());
            if(hasAdvancedData("extra-results")) {

                ArrayList<CraftingItemStack> extraResults = new ArrayList<CraftingItemStack>();
                extraResults.addAll((Collection<? extends CraftingItemStack>) getAdvancedData("extra-results"));
                for(CraftingItemStack s : extraResults)
                    resz.put(s.toString() + " ", s.getItemStack().getAmount());
            }
            config.setProperty("crafting-recipes." + id + ".results", resz);
            if(hasAdvancedData("permission-node"))
                config.setProperty("crafting-recipes." + id + ".permission-node", getAdvancedData("permission-node"));
        }

        private LinkedHashMap<CraftingItemStack, Character> getShapeIngredients(String path) {

            LinkedHashMap<CraftingItemStack, Character> items = new LinkedHashMap<CraftingItemStack, Character>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String okey = String.valueOf(oitem);
                    String item = okey.trim();

                    ItemStack stack = ItemUtil.makeItemValid(ItemUtil.getItem(item));

                    if (stack != null) {

                        stack.setAmount(1);
                        CraftingItemStack itemStack = new CraftingItemStack(stack);
                        items.put(itemStack, config.getString(path + "." + okey, "a").charAt(0));
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
                    String okey = String.valueOf(oitem);
                    String item = okey.trim();

                    ItemStack stack = ItemUtil.makeItemValid(ItemUtil.getItem(item));

                    if (stack != null) {

                        stack.setAmount(config.getInt(path + "." + okey, 1));
                        CraftingItemStack itemStack = new CraftingItemStack(stack);
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

        public LinkedHashMap<CraftingItemStack, Character> getShapedIngredients() {

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
            if(CraftBookPlugin.isDebugFlagEnabled("advanced-data"))
                CraftBookPlugin.logger().info("Adding advanced data of type: " + key + " to an ItemStack!");
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
