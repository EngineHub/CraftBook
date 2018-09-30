package com.sk89q.craftbook.mechanics.crafting;

import java.io.IOException;
import java.util.*;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class RecipeManager {

    public static RecipeManager INSTANCE;
    private Set<Recipe> recipes;
    protected YAMLProcessor config;

    public RecipeManager(YAMLProcessor config) {
        INSTANCE = this;
        this.config = config;
        load();
    }

    public void load() {
        recipes = new LinkedHashSet<>();
        if (config == null) {
            CraftBookPlugin.logger().severe("Failure loading recipes! Config is null!");
            return; // If the config is null, it can't continue.
        }

        try {
            config.load();
        } catch (IOException e) {
            CraftBookPlugin.logger().severe("Corrupt Custom Crafting crafting-recipes.yml File! Make sure that the correct syntax has been used, and that there are no tabs!");
            e.printStackTrace();
        }

        config.setHeader(
                "# CraftBook Custom Recipes. CraftBook Version: " + CraftBookPlugin.inst().getDescription().getVersion(),
                "# For more information on setting up custom recipes, see the wiki:",
                "# " + CraftBookPlugin.getWikiDomain() + "/Custom_crafting",
                "",
                "");

        List<String> keys = config.getKeys("crafting-recipes");
        if (keys != null) {
            for (String key : keys) {
                try {
                    recipes.add(new Recipe(key, config));
                } catch (InvalidCraftingException e) {
                    CraftBookBukkitUtil.printStacktrace(e);
                }
            }
        }
    }

    public void disable() {
        INSTANCE = null;
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
                "# " + CraftBookPlugin.getWikiDomain() + "/Custom_crafting",
                "",
                "");

        config.addNode("crafting-recipes");
        for(Recipe recipe : recipes)
            recipe.save();

        config.save();

        load();
    }

    public Collection<Recipe> getRecipes() {
        return recipes;
    }

    public void addRecipe(Recipe rec) {
        recipes.add(rec);
        save();
    }

    public boolean removeRecipe(String name) {
        Iterator<Recipe> recs = recipes.iterator();
        while(recs.hasNext()) {

            Recipe rec = recs.next();
            if(rec.getId().equalsIgnoreCase(name)) {
                recs.remove();
                save();
                return true;
            }
        }

        return false;
    }

    public final class Recipe {
        private final String id;

        private RecipeType type;
        private List<CraftingItemStack> ingredients;
        private LinkedHashMap<CraftingItemStack, Character> items;
        private CraftingItemStack result;
        private List<String> shape;

        private float experience;
        private int cookTime;

        @Override
        public boolean equals(Object o) {
            if(o instanceof Recipe) {
                if(shape != null)
                    if(shape.size() != ((Recipe)o).shape.size())
                        return false;
                if(ingredients != null) {
                    if(ingredients.size() != ((Recipe)o).ingredients.size())
                        return false;
                    List<CraftingItemStack> stacks = new ArrayList<>(ingredients);
                    for(CraftingItemStack st : ((Recipe)o).ingredients) {
                        if(stacks.size() <= 0)
                            return false;
                        Iterator<CraftingItemStack> it = stacks.iterator();
                        while(it.hasNext()) {
                            CraftingItemStack sta = it.next();
                            if(st.equals(sta)) {
                                it.remove();
                                break;
                            }
                        }
                    }

                    if(stacks.size() > 0)
                        return false;
                }
                if(items != null) {
                    if(items.size() != ((Recipe)o).items.size())
                        return false;

                    List<CraftingItemStack> stacks = new ArrayList<>(items.keySet());
                    for(CraftingItemStack st : ((Recipe)o).items.keySet()) {
                        if(stacks.size() <= 0)
                            return false;
                        Iterator<CraftingItemStack> it = stacks.iterator();
                        while(it.hasNext()) {
                            CraftingItemStack sta = it.next();
                            if(st.equals(sta)) {
                                it.remove();
                                break;
                            }
                        }
                    }

                    if(stacks.size() > 0)
                        return false;
                }
                if(advancedData != null)
                    if(advancedData.size() != ((Recipe)o).advancedData.size())
                        return false;
                return ((Recipe) o).id.equals(id) && type == ((Recipe)o).type && result.equals(((Recipe)o).result);
            }
            else
                return false;
        }

        @Override
        public int hashCode() {
            int ret = id.hashCode();
            if(ingredients != null)
                ret += ingredients.hashCode();
            else if (items != null)
                ret += items.hashCode();
            ret += result.hashCode();
            if(shape != null)
                ret += shape.hashCode();
            return ret + advancedData.hashCode();
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
            return result.hasAdvancedData() || !advancedData.isEmpty();

        }

        private Recipe(String id, YAMLProcessor config) throws InvalidCraftingException {
            this.id = id;
            ingredients = new ArrayList<>();
            items = new LinkedHashMap<>();
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
                if (type == RecipeType.FURNACE) {
                    cookTime = config.getInt("crafting-recipes." + id + ".cook-time", 200);
                    experience = (float) config.getDouble("crafting-recipes." + id + ".experience", 0.0);
                }
            } else {
                items = getShapeIngredients("crafting-recipes." + id + ".ingredients");
                shape = config.getStringList("crafting-recipes." + id + ".shape", Collections.singletonList(""));
            }
            Iterator<CraftingItemStack> iterator = getItems("crafting-recipes." + id + ".results").iterator();
            if(iterator.hasNext())
                result = iterator.next();
            else
                throw new InvalidCraftingException("Result is invalid in recipe: "+ id);

            if(iterator.hasNext()) {
                ArrayList<CraftingItemStack> extraResults = new ArrayList<>();
                while(iterator.hasNext())
                    extraResults.add(iterator.next());
                addAdvancedData("extra-results", extraResults);
            }

            String permNode = config.getString("crafting-recipes." + id + ".permission-node", null);
            if (permNode != null)
                addAdvancedData("permission-node", permNode);

            String permError = config.getString("crafting-recipes." + id + ".permission-error", null);
            if (permError != null)
                addAdvancedData("permission-error", permError);

            List<String> actions = config.getKeys("crafting-recipes." + id + ".craft-actions");

            if(actions != null && !actions.isEmpty()) {

                for(String s : actions) {
                    if(s.equalsIgnoreCase("commands-console"))
                        addAdvancedData("commands-console", config.getStringList("crafting-recipes." + id + ".craft-actions." + s, new ArrayList<>()));
                    else if(s.equalsIgnoreCase("commands-player"))
                        addAdvancedData("commands-player", config.getStringList("crafting-recipes." + id + ".craft-actions." + s, new ArrayList<>()));
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void save() {
            config.addNode("crafting-recipes." + id);
            config.setProperty("crafting-recipes." + id + ".type", type.name);
            if(type != RecipeType.SHAPED) {
                LinkedHashMap<String, Integer> resz = new LinkedHashMap<>();
                for(CraftingItemStack stack : ingredients) {
                    resz.put(stack.toString() + ' ', stack.getItemStack().getAmount());
                }
                config.setProperty("crafting-recipes." + id + ".ingredients", resz);
                if (type == RecipeType.FURNACE) {
                    config.setProperty("crafting-recipes." + id + ".cook-time", cookTime);
                    config.setProperty("crafting-recipes." + id + ".experience", experience);
                }
            } else {
                LinkedHashMap<String, Character> resz = new LinkedHashMap<>();
                for(Map.Entry<CraftingItemStack, Character> craftingItemStackCharacterEntry : items.entrySet())
                    resz.put(craftingItemStackCharacterEntry.getKey().toString() + ' ', craftingItemStackCharacterEntry.getValue());
                config.setProperty("crafting-recipes." + id + ".ingredients", resz);
                config.setProperty("crafting-recipes." + id + ".shape", shape);
            }

            LinkedHashMap<String, Integer> resz = new LinkedHashMap<>();
            resz.put(result.toString() + ' ', result.getItemStack().getAmount());
            if(hasAdvancedData("extra-results")) {

                ArrayList<CraftingItemStack> extraResults =
                        new ArrayList<>((Collection<? extends CraftingItemStack>) getAdvancedData("extra-results"));
                for(CraftingItemStack s : extraResults)
                    resz.put(s.toString() + ' ', s.getItemStack().getAmount());
            }
            config.setProperty("crafting-recipes." + id + ".results", resz);
            if(hasAdvancedData("permission-node"))
                config.setProperty("crafting-recipes." + id + ".permission-node", getAdvancedData("permission-node"));
            if(hasAdvancedData("permission-error"))
                config.setProperty("crafting-recipes." + id + ".permission-error", getAdvancedData("permission-error"));
            if(hasAdvancedData("commands-player") || hasAdvancedData("commands-console")) {
                config.addNode("crafting-recipes." + id + ".craft-actions");
                if(hasAdvancedData("commands-player"))
                    config.setProperty("crafting-recipes." + id + ".craft-actions.commands-player", getAdvancedData("commands-player"));
                if(hasAdvancedData("commands-console"))
                    config.setProperty("crafting-recipes." + id + ".craft-actions.commands-console", getAdvancedData("commands-console"));
            }
        }

        private LinkedHashMap<CraftingItemStack, Character> getShapeIngredients(String path) {
            LinkedHashMap<CraftingItemStack, Character> items = new LinkedHashMap<>();
            try {
                for (String item : config.getKeys(path)) {
                    ItemStack stack = ItemUtil.makeItemValid(ItemSyntax.getItem(RegexUtil.PERCENT_PATTERN.split(item.trim())[0]));

                    if (stack != null) {
                        stack.setAmount(1);
                        CraftingItemStack itemStack = new CraftingItemStack(stack);
                        if(RegexUtil.PERCENT_PATTERN.split(item).length > 1)
                            itemStack.addAdvancedData("chance", Double.parseDouble(RegexUtil.PERCENT_PATTERN.split(item.trim())[1]));
                        items.put(itemStack, config.getString(path + '.' + item, "a").charAt(0));
                    }
                }
            } catch (Exception e) {
                CraftBookPlugin.inst().getLogger().severe("An error occured generating ingredients for recipe: " + id);
                CraftBookBukkitUtil.printStacktrace(e);
            }
            return items;
        }

        private List<CraftingItemStack> getItems(String path) {
            List<CraftingItemStack> items = new ArrayList<>();
            try {
                for (Object oitem : config.getKeys(path)) {
                    String okey = String.valueOf(oitem);
                    String item = okey.trim();

                    ItemStack stack = ItemUtil.makeItemValid(ItemSyntax.getItem(RegexUtil.PERCENT_PATTERN.split(item)[0]));

                    if (stack != null) {
                        stack.setAmount(config.getInt(path + '.' + okey, 1));
                        CraftingItemStack itemStack = new CraftingItemStack(stack);
                        if(RegexUtil.PERCENT_PATTERN.split(item).length > 1)
                            itemStack.addAdvancedData("chance", Double.parseDouble(RegexUtil.PERCENT_PATTERN.split(item)[1]));
                        items.add(itemStack);
                    }
                }
            } catch (Exception e) {
                CraftBookPlugin.inst().getLogger().severe("An error occured generating ingredients for recipe: " + id);
                CraftBookBukkitUtil.printStacktrace(e);
            }
            return items;
        }

        public String getId() {
            return id;
        }

        public RecipeType getType() {
            return type;
        }

        public List<CraftingItemStack> getIngredients() {
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

        // Furnace
        public float getExperience() {
            return this.experience;
        }

        public void setExperience(float experience) {
            this.experience = experience;
        }

        public int getCookTime() {
            return this.cookTime;
        }

        public void setCookTime(int cookTime) {
            this.cookTime = cookTime;
        }

        //Advanced data
        private HashMap<String, Object> advancedData = new HashMap<>();

        public boolean hasAdvancedData(String key) {
            return advancedData.containsKey(key);
        }

        public Object getAdvancedData(String key) {
            return advancedData.get(key);
        }

        public void addAdvancedData(String key, Object data) {
            CraftBookPlugin.logDebugMessage("Adding advanced data of type: " + key + " to an ItemStack! {" + String.valueOf(data) + '}',
                    "advanced-data.init");
            advancedData.put(key, data);
        }

        public HashMap<String,Object> getAdvancedDataMap() {
            return advancedData;
        }
    }

    public enum RecipeType {
        SHAPELESS("Shapeless"), FURNACE("Furnace"), SHAPED("Shaped");

        private String name;

        RecipeType(String name) {
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
                if (t.name.equalsIgnoreCase(name))
                    return t;
            }
            return SHAPELESS; // Default to shapeless
        }
    }
}
