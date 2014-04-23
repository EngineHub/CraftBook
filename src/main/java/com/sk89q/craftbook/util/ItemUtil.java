package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ItemUtil {

    /**
     * Add an itemstack to an existing itemstack.
     * 
     * @param base The itemstack to be added to.
     * @param toAdd The itemstack to add to the base.
     * @return The unaddable items.
     */
    public static ItemStack addToStack(ItemStack base, ItemStack toAdd) {

        if (!areItemsIdentical(base, toAdd)) return toAdd;

        if (base.getAmount() + toAdd.getAmount() > base.getMaxStackSize()) {

            toAdd.setAmount(base.getAmount() + toAdd.getAmount() - base.getMaxStackSize());
            base.setAmount(base.getMaxStackSize());
            return toAdd;
        } else {
            base.setAmount(base.getAmount() + toAdd.getAmount());
            return null;
        }
    }

    /**
     * Filter a list of items by inclusions and exclusions.
     * 
     * @param stacks The base list of items.
     * @param inclusions The list of items to include, skipped if empty.
     * @param exclusions The list of items to exclude, skipped if empty.
     * @return The list of items that have been filtered.
     */
    public static List<ItemStack> filterItems(List<ItemStack> stacks, HashSet<ItemStack> inclusions, HashSet<ItemStack> exclusions) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for(ItemStack stack : stacks) {

            if(doesItemPassFilters(stack, inclusions, exclusions))
                ret.add(stack);
        }

        return ret;
    }

    /**
     * Check whether or not an item passes filters.
     * 
     * @param stacks The item to check if it passes.
     * @param inclusions The list of items to include, skipped if empty.
     * @param exclusions The list of items to exclude, skipped if empty.
     * @return If the item passes the filters.
     */
    public static boolean doesItemPassFilters(ItemStack stack, Set<ItemStack> inclusions, Set<ItemStack> exclusions) {

        boolean passesFilters = true;
        if(inclusions != null && inclusions.size() > 0) {
            for (ItemStack fil : inclusions) {

                if(!ItemUtil.isStackValid(fil))
                    continue;

                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = true;
                    break;
                } else
                    passesFilters = false;
            }
            if(!passesFilters)
                return false;
        }
        if(exclusions != null && exclusions.size() > 0) {
            for (ItemStack fil : exclusions) {

                if(!ItemUtil.isStackValid(fil))
                    continue;
                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = false;
                    break;
                }
            }
            if(!passesFilters)
                return false;
        }

        return passesFilters;
    }

    public static boolean areItemsSimilar(ItemStack item, Material type) {

        return areItemsSimilar(item, new MaterialData(type));
    }

    public static boolean areItemsSimilar(ItemStack item, MaterialData data) {

        return areItemsSimilar(item.getData(), data);
    }

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {

        return areItemsSimilar(item.getData(), item2.getData());
    }

    public static boolean areItemsSimilar(MaterialData data, MaterialData comparedData) {

        return data.getItemType() == comparedData.getItemType();
    }

    private static final Pattern STRIP_RESET_PATTERN = Pattern.compile("(?i)" + String.valueOf('\u00A7') + "[Rr]");

    //TODO Move to a StringUtil.
    public static String stripResetChar(String message) {

        if (message == null)
            return null;

        return STRIP_RESET_PATTERN.matcher(message).replaceAll("");
    }

    public static boolean areRecipesIdentical(Recipe rec1, Recipe rec2) {

        if(ItemUtil.areItemsIdentical(rec1.getResult(), rec2.getResult())) {
            CraftBookPlugin.logDebugMessage("Recipes have same results!", "advanced-data.compare-recipes");
            if(rec1 instanceof ShapedRecipe && rec2 instanceof ShapedRecipe) {
                CraftBookPlugin.logDebugMessage("Shaped recipe!", "advanced-data.compare-recipes.shaped");
                ShapedRecipe recipe1 = (ShapedRecipe) rec1;
                ShapedRecipe recipe2 = (ShapedRecipe) rec2;
                if(recipe1.getShape().length == recipe2.getShape().length) {
                    CraftBookPlugin.logDebugMessage("Same size!", "advanced-data.compare-recipes.shaped");
                    List<ItemStack> stacks1 = new ArrayList<ItemStack>();

                    for(String s : recipe1.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe1.getIngredientMap().entrySet())
                                if(entry.getKey().charValue() == c)
                                    stacks1.add(entry.getValue());
                    List<ItemStack> stacks2 = new ArrayList<ItemStack>();

                    for(String s : recipe2.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe2.getIngredientMap().entrySet())
                                if(entry.getKey().charValue() == c)
                                    stacks2.add(entry.getValue());

                    if(stacks2.size() != stacks1.size()) {
                        CraftBookPlugin.logDebugMessage("Recipes have different amounts of ingredients!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                    List<ItemStack> test = new ArrayList<ItemStack>();
                    test.addAll(stacks1);
                    if(test.size() == 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes.shaped");
                        return true;
                    }
                    if(!test.removeAll(stacks2) && test.size() > 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                    if(test.size() > 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                }
            } else if(rec1 instanceof ShapelessRecipe && rec2 instanceof ShapelessRecipe) {

                CraftBookPlugin.logDebugMessage("Shapeless Recipe!", "advanced-data.compare-recipes.shapeless");
                ShapelessRecipe recipe1 = (ShapelessRecipe) rec1;
                ShapelessRecipe recipe2 = (ShapelessRecipe) rec2;

                if(VerifyUtil.withoutNulls(recipe1.getIngredientList()).size() != VerifyUtil.withoutNulls(recipe2.getIngredientList()).size()) {
                    CraftBookPlugin.logDebugMessage("Recipes have different amounts of ingredients!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }

                CraftBookPlugin.logDebugMessage("Same Size!", "advanced-data.compare-recipes.shapeless");

                List<ItemStack> test = new ArrayList<ItemStack>();
                test.addAll(VerifyUtil.<ItemStack>withoutNulls(recipe1.getIngredientList()));
                if(test.size() == 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes.shapeless");
                    return true;
                }
                if(!test.removeAll(VerifyUtil.<ItemStack>withoutNulls(recipe2.getIngredientList())) && test.size() > 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }
                if(test.size() > 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }
            }

            CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes");

            return true;
        }

        return false;
    }

    public static boolean isValidItemMeta(ItemMeta meta) {

        if(meta.hasDisplayName())
            if(!meta.getDisplayName().equals("$IGNORE"))
                return true;

        if(meta.hasLore())
            for(String lore : meta.getLore())
                if(!lore.equals("$IGNORE"))
                    return true;

        if(meta.hasEnchants())
            return true;

        return false;
    }

    public static boolean areItemMetaIdentical(ItemMeta meta, ItemMeta meta2) {

        //Display Names
        String displayName1 = null;
        if(meta.hasDisplayName())
            displayName1 = ChatColor.translateAlternateColorCodes('&', stripResetChar(meta.getDisplayName().trim()));
        else
            displayName1 = "$IGNORE";

        String displayName2 = null;
        if(meta2.hasDisplayName())
            displayName2 = ChatColor.translateAlternateColorCodes('&', stripResetChar(meta2.getDisplayName().trim()));
        else
            displayName2 = "$IGNORE";

        if(!displayName1.equals(displayName2)) {
            if(!displayName1.equals("$IGNORE") && !displayName2.equals("$IGNORE"))
                return false;
        }
        CraftBookPlugin.logDebugMessage("Display names are the same", "item-checks.meta.names");

        //Lore
        List<String> lore1 = new ArrayList<String>();
        if(meta.hasLore())
            for(String lore : meta.getLore())
                lore1.add(ChatColor.translateAlternateColorCodes('&', stripResetChar(lore.trim())));

        List<String> lore2 = new ArrayList<String>();
        if(meta2.hasLore())
            for(String lore : meta2.getLore())
                lore2.add(ChatColor.translateAlternateColorCodes('&', stripResetChar(lore.trim())));

        if(lore1.size() != lore2.size())
            return false;
        CraftBookPlugin.logDebugMessage("Has same lore lengths", "item-checks.meta.lores");

        for(int i = 0; i < lore1.size(); i++) {
            if(lore1.get(i).contains("$IGNORE") || lore2.get(i).contains("$IGNORE")) continue; //Ignore this line.
            if(!lore1.get(i).equals(lore2.get(i)))
                return false;
        }

        CraftBookPlugin.logDebugMessage("Lore is the same", "item-checks.meta.lores");

        //Enchants
        List<Enchantment> ench1 = new ArrayList<Enchantment>();
        if(meta.hasEnchants())
            ench1.addAll(meta.getEnchants().keySet());

        List<Enchantment> ench2 = new ArrayList<Enchantment>();
        if(meta2.hasEnchants())
            ench2.addAll(meta2.getEnchants().keySet());

        if(ench1.size() != ench2.size())
            return false;
        CraftBookPlugin.logDebugMessage("Has same enchantment lengths", "item-checks.meta.enchants");

        for(Enchantment ench : ench1) {
            if(!ench2.contains(ench))
                return false;
            if(meta.getEnchantLevel(ench) != meta2.getEnchantLevel(ench))
                return false;
        }

        CraftBookPlugin.logDebugMessage("Enchants are the same", "item-checks.meta.enchants");

        return true;
    }

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {

        if(!isStackValid(item) || !isStackValid(item2)) {
            CraftBookPlugin.logDebugMessage("An invalid item was compared", "item-checks");
            return !isStackValid(item) && !isStackValid(item2);
        }
        else {
            if(!areBaseItemsIdentical(item,item2))
                return false;
            CraftBookPlugin.logDebugMessage("The items are basically identical", "item-checks");

            if(item.hasItemMeta() != item2.hasItemMeta()) {
                if(item.hasItemMeta() && isValidItemMeta(item.getItemMeta())) return false;
                else if(item2.hasItemMeta() && isValidItemMeta(item2.getItemMeta())) return false;
            }

            CraftBookPlugin.logDebugMessage("Both share the existance of metadata", "item-checks");
            if(item.hasItemMeta()) {
                CraftBookPlugin.logDebugMessage("Both have metadata", "item-checks.meta");
                if(!areItemMetaIdentical(item.getItemMeta(), item2.getItemMeta())) {
                    CraftBookPlugin.logDebugMessage("Metadata is different", "item-checks.meta");
                    return false;
                }
            }

            CraftBookPlugin.logDebugMessage("Items are identical", "item-checks");
            return true;
        }
    }

    public static boolean areBaseItemsIdentical(ItemStack item, ItemStack item2) {

        if(!isStackValid(item) || !isStackValid(item2))
            return !isStackValid(item) && !isStackValid(item2);
        else {

            if(item.getType() != item2.getType())
                return false;
            if(item.getData().getData() != item2.getData().getData() && item.getData().getData() >= 0 && item2.getData().getData() >= 0)
                return false;

            return true;
        }
    }

    public static boolean isStackValid(ItemStack item) {

        return item != null && item.getAmount() > 0 && item.getTypeId() > 0 && (getMaxDurability(item.getType()) == 0 || item.getDurability() < getMaxDurability(item.getType()));
    }

    /**
     * Removes a specified amount from an item entity.
     * 
     * @param item
     * @return true if success, otherwise false.
     */
    public static boolean takeFromItemEntity(Item item, int amount) {

        if (item == null || item.isDead()) return false;

        ItemStack newStack = item.getItemStack();

        if (!isStackValid(newStack)) {
            item.remove();
            return false;
        }

        if(newStack.getAmount() < amount)
            return false;

        newStack.setAmount(newStack.getAmount() - amount);

        if (!isStackValid(newStack))
            item.remove();
        else
            item.setItemStack(newStack);

        return true;
    }

    public static boolean isCookable(ItemStack item) {

        return getCookedResult(item) != null;
    }

    public static ItemStack getCookedResult(ItemStack item) {

        switch (item.getType()) {
            case RAW_BEEF:
                return new ItemStack(Material.COOKED_BEEF);
            case RAW_CHICKEN:
                return new ItemStack(Material.COOKED_CHICKEN);
            case RAW_FISH:
                return new ItemStack(Material.COOKED_FISH, 1, item.getDurability());
            case PORK:
                return new ItemStack(Material.GRILLED_PORK);
            case POTATO_ITEM:
                return new ItemStack(Material.BAKED_POTATO);
            default:
                return null;
        }
    }

    public static boolean isSmeltable(ItemStack item) {

        return getSmeletedResult(item) != null;
    }

    public static ItemStack getSmeletedResult(ItemStack item) {

        switch (item.getType()) {
            case COBBLESTONE:
                return new ItemStack(Material.STONE);
            case CACTUS:
                return new ItemStack(Material.INK_SACK, 1, (short) 2);
            case LOG:
            case LOG_2:
                return new ItemStack(Material.COAL, 1, (short) 1);
            case IRON_ORE:
                return new ItemStack(Material.IRON_INGOT);
            case REDSTONE_ORE:
                return new ItemStack(Material.REDSTONE, 4);
            case EMERALD_ORE:
                return new ItemStack(Material.EMERALD);
            case GOLD_ORE:
                return new ItemStack(Material.GOLD_INGOT);
            case DIAMOND_ORE:
                return new ItemStack(Material.DIAMOND);
            case SAND:
                return new ItemStack(Material.GLASS);
            case CLAY_BALL:
                return new ItemStack(Material.CLAY_BRICK);
            case NETHERRACK:
                return new ItemStack(Material.NETHER_BRICK_ITEM);
            case CLAY:
                return new ItemStack(Material.HARD_CLAY);
            case QUARTZ_ORE:
                return new ItemStack(Material.QUARTZ);
            default:
                return null;
        }
    }

    /**
     * Checks whether the item is usable as a fuel in a furnace.
     * 
     * @param item The item to check.
     * @return Whether it is usable in a furnace.
     */
    public static boolean isAFuel(ItemStack item) {

        switch(item.getType()) {
            case COAL:
            case COAL_BLOCK:
            case LOG:
            case LOG_2:
            case LEAVES:
            case LEAVES_2:
            case WOOD:
            case WOOD_STEP:
            case SAPLING:
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
            case WOOD_PLATE:
            case STICK:
            case FENCE:
            case FENCE_GATE:
            case WOOD_STAIRS:
            case TRAP_DOOR:
            case WORKBENCH:
            case CHEST:
            case TRAPPED_CHEST:
            case JUKEBOX:
            case NOTE_BLOCK:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case BLAZE_ROD:
            case LAVA_BUCKET:
            case BOOKSHELF:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether an item is a potion ingredient.
     * 
     * @param item The item to check.
     * @return If the item is a potion ingredient.
     */
    public static boolean isAPotionIngredient(ItemStack item) {

        switch(item.getType()) {
            case NETHER_STALK:
            case GLOWSTONE_DUST:
            case REDSTONE:
            case SPIDER_EYE:
            case MAGMA_CREAM:
            case SUGAR:
            case SPECKLED_MELON:
            case GHAST_TEAR:
            case BLAZE_POWDER:
            case FERMENTED_SPIDER_EYE:
            case SULPHUR:
            case GOLDEN_CARROT:
                return true;
            default:
                return false;
        }
    }

    public static boolean containsRawFood(Inventory inv) {

        return getRawFood(inv).size() > 0;
    }

    public static List<ItemStack> getRawFood(Inventory inv) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isCookable(it))
                ret.add(it);
        }
        return ret;
    }

    public static boolean containsRawMinerals(Inventory inv) {

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isSmeltable(it))
                return true;
        }
        return false;
    }

    public static List<ItemStack> getRawMinerals(Inventory inv) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isSmeltable(it))
                ret.add(it);
        }
        return ret;
    }

    public static boolean containsRawMaterials(Inventory inv) {

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isFurnacable(it))
                return true;
        }
        return false;
    }

    public static List<ItemStack> getRawMaterials(Inventory inv) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isFurnacable(it))
                ret.add(it);
        }
        return ret;
    }

    public static boolean isFurnacable(ItemStack item) {

        return isCookable(item) || isSmeltable(item);
    }

    public static boolean isItemEdible(ItemStack item) {

        return item.getType().isEdible();
    }

    public static ItemStack getUsedItem(ItemStack item) {

        if (item.getType() == Material.MUSHROOM_SOUP) {
            item.setType(Material.BOWL); // Get your bowl back
        } else if (item.getType() == Material.POTION) {
            item.setType(Material.GLASS_BOTTLE); // Get your bottle back
        } else if (item.getType() == Material.LAVA_BUCKET || item.getType() == Material.WATER_BUCKET || item.getType() == Material.MILK_BUCKET) {
            item.setType(Material.BUCKET); // Get your bucket back
        } else if (item.getAmount() == 1) {
            item.setType(Material.AIR);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        return item;
    }

    public static ItemStack getSmallestStackOfType(ItemStack[] stacks, ItemStack item) {

        ItemStack smallest = null;
        for (ItemStack it : stacks) {
            if (!ItemUtil.isStackValid(it)) {
                continue;
            }
            if (ItemUtil.areItemsIdentical(it, item)) {
                if (smallest == null) {
                    smallest = it;
                }
                if (it.getAmount() < smallest.getAmount()) {
                    smallest = it;
                }
            }
        }

        return smallest;
    }

    public static ItemStack makeItemValid(ItemStack invalid) {

        if(invalid == null)
            return null;

        ItemStack valid = invalid.clone();

        if(valid.getDurability() < 0)
            valid.setDurability((short) 0);
        if(valid.getData().getData() < 0)
            valid.getData().setData((byte) 0);
        if(valid.getType() == null || valid.getType() == Material.PISTON_MOVING_PIECE)
            valid.setType(Material.STONE);
        if(valid.getAmount() < 1)
            valid.setAmount(1);

        return valid;
    }

    /**
     * Gets all {@link Item}s at a certain {@link Block}.
     * 
     * @param block The {@link Block} to check for items at.
     * @return A {@link ArrayList} of {@link Item}s.
     */
    public static List<Item> getItemsAtBlock(Block block) {

        List<Item> items = new ArrayList<Item>();

        for (Entity en : block.getChunk().getEntities()) {
            if (!(en instanceof Item)) {
                continue;
            }
            Item item = (Item) en;
            if (item.isDead() || !item.isValid())
                continue;

            if (EntityUtil.isEntityInBlock(en, block)) {

                items.add(item);
            }
        }

        return items;
    }

    /**
     * Returns the maximum durability that an item can have.
     * 
     * @param typeId
     * @return
     */
    public static short getMaxDurability(Material type) {

        switch(type) {

            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_SWORD:
                return 1562;
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SPADE:
            case IRON_SWORD:
                return 251;
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SPADE:
            case STONE_SWORD:
                return 132;
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
                return 60;
            case GOLD_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SPADE:
            case GOLD_SWORD:
                return 33;
            default:
                return 0;
        }
    }
}