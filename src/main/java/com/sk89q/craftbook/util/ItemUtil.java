package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import org.bukkit.material.MaterialData;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.ItemID;

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
    public static boolean doesItemPassFilters(ItemStack stack, HashSet<ItemStack> inclusions, HashSet<ItemStack> exclusions) {

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

        return areItemsSimilar(item, new MaterialData(type, (byte) 0));
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

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {

        if(!isStackValid(item) || !isStackValid(item2)) {
            CraftBookPlugin.logDebugMessage("An invalid item was compared", "item-checks");
            return !isStackValid(item) && !isStackValid(item2);
        }
        else {
            if(!areBaseItemsIdentical(item,item2))
                return false;
            CraftBookPlugin.logDebugMessage("The items are basically identical", "item-checks");
            if(item.hasItemMeta() != item2.hasItemMeta())
                return false;
            CraftBookPlugin.logDebugMessage("Both share the existance of metadata", "item-checks");
            if(item.hasItemMeta()) {
                CraftBookPlugin.logDebugMessage("Both have metadata", "item-checks.meta");
                if(item.getItemMeta().hasDisplayName() == item2.getItemMeta().hasDisplayName()) {
                    CraftBookPlugin.logDebugMessage("Both have names", "item-checks.meta.names");
                    if(item.getItemMeta().hasDisplayName()) {
                        CraftBookPlugin.logDebugMessage("ItemStack1 Display Name: " + item.getItemMeta().getDisplayName() + ". ItemStack2 Display Name: " + item2.getItemMeta().getDisplayName(), "item-checks.meta.names");
                        if(!item.getItemMeta().getDisplayName().equalsIgnoreCase("$IGNORE") && !item2.getItemMeta().getDisplayName().equalsIgnoreCase("$IGNORE") && !ChatColor.translateAlternateColorCodes('&', stripResetChar(item.getItemMeta().getDisplayName().trim().replace("'", ""))).equals(ChatColor.translateAlternateColorCodes('&', stripResetChar(item2.getItemMeta().getDisplayName().trim().replace("'", "")))))
                            return false;
                        CraftBookPlugin.logDebugMessage("Items share display names", "item-checks.meta.names");
                    }
                } else {
                    if(!(item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equalsIgnoreCase("$IGNORE")) && !(item2.getItemMeta().hasDisplayName() && item2.getItemMeta().getDisplayName().equalsIgnoreCase("$IGNORE")))
                        return false;
                }
                if(item.getItemMeta().hasLore() == item2.getItemMeta().hasLore()) {
                    CraftBookPlugin.logDebugMessage("Both have lore", "item-checks.meta.lores");
                    if(item.getItemMeta().hasLore()) {
                        if(item.getItemMeta().getLore().size() != item2.getItemMeta().getLore().size())
                            return false;
                        for(int i = 0; i < item.getItemMeta().getLore().size(); i++) {
                            CraftBookPlugin.logDebugMessage("ItemStack1 Lore: " + item.getItemMeta().getLore().get(i) + ". ItemStack2 Lore: " + item2.getItemMeta().getLore().get(i), "item-checks.meta.lores");
                            if(!item.getItemMeta().getLore().get(i).equalsIgnoreCase("$IGNORE") && !item2.getItemMeta().getLore().get(i).equalsIgnoreCase("$IGNORE") && !ChatColor.translateAlternateColorCodes('&', stripResetChar(item.getItemMeta().getLore().get(i).trim().replace("'", ""))).equals(ChatColor.translateAlternateColorCodes('&', stripResetChar(item2.getItemMeta().getLore().get(i).trim().replace("'", "")))))
                                return false;
                            CraftBookPlugin.logDebugMessage("Items share same lore", "item-checks.meta.lores");
                        }
                    }
                } else {
                    if(!(item.getItemMeta().hasLore() && item.getItemMeta().getLore().size() == 1 && item.getItemMeta().getLore().get(0).equalsIgnoreCase("$IGNORE")))
                        if(!(item2.getItemMeta().hasLore() && item2.getItemMeta().getLore().size() == 1 && item2.getItemMeta().getLore().get(0).equalsIgnoreCase("$IGNORE")))
                            return false;
                }
                if(item.getItemMeta().hasEnchants() == item2.getItemMeta().hasEnchants()) {
                    CraftBookPlugin.logDebugMessage("Both share enchant existance", "item-checks.meta.enchants");
                    if(item.getItemMeta().hasEnchants()) {
                        if(item.getItemMeta().getEnchants().size() != item2.getItemMeta().getEnchants().size())
                            return false;
                        for(Enchantment ench : item.getItemMeta().getEnchants().keySet()) {

                            if(!item2.getItemMeta().getEnchants().containsKey(ench)) {
                                CraftBookPlugin.logDebugMessage("Item2 does not have enchantment: " + ench.getName(), "item-checks.meta.enchants");
                                return false;
                            }
                            CraftBookPlugin.logDebugMessage("Enchant Name: " + ench.getName() + " ItemStack1 level " + item.getItemMeta().getEnchants().get(ench) + " ItemStack2 level " + item2.getItemMeta().getEnchants().get(ench), "item-checks.meta.enchants");
                            if(item.getItemMeta().getEnchantLevel(ench) != item2.getItemMeta().getEnchantLevel(ench))
                                return false;
                            CraftBookPlugin.logDebugMessage("Items share enchantment: " + ench.getName(), "item-checks.meta.enchants");
                        }
                    }
                } else
                    return false;
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

        return item != null && item.getAmount() > 0 && item.getTypeId() > 0 && (getMaxDurability(item.getTypeId()) == 0 || item.getDurability() < getMaxDurability(item.getTypeId()));
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
                return new ItemStack(Material.COOKED_FISH);
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
                return new ItemStack(Material.BRICK);
            case NETHERRACK:
                return new ItemStack(Material.NETHER_BRICK);
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
            case NETHER_WARTS:
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

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isCookable(it))
                return true;
        }
        return false;
    }

    public static boolean containsRawMinerals(Inventory inv) {

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isSmeltable(it))
                return true;
        }
        return false;
    }

    public static boolean containsRawMaterials(Inventory inv) {

        for (ItemStack it : inv.getContents()) {
            if (isStackValid(it) && isFurnacable(it))
                return true;
        }
        return false;
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
        if(valid.getTypeId() < 1)
            valid.setTypeId(1);
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
    public static short getMaxDurability(int typeId) {

        switch(typeId) {

            case ItemID.DIAMOND_AXE:
            case ItemID.DIAMOND_HOE:
            case ItemID.DIAMOND_PICKAXE:
            case ItemID.DIAMOND_SHOVEL:
            case ItemID.DIAMOND_SWORD:
                return 1562;
            case ItemID.IRON_AXE:
            case ItemID.IRON_HOE:
            case ItemID.IRON_PICK:
            case ItemID.IRON_SHOVEL:
            case ItemID.IRON_SWORD:
                return 251;
            case ItemID.STONE_AXE:
            case ItemID.STONE_HOE:
            case ItemID.STONE_PICKAXE:
            case ItemID.STONE_SHOVEL:
            case ItemID.STONE_SWORD:
                return 132;
            case ItemID.WOOD_AXE:
            case ItemID.WOOD_HOE:
            case ItemID.WOOD_PICKAXE:
            case ItemID.WOOD_SHOVEL:
            case ItemID.WOOD_SWORD:
                return 60;
            case ItemID.GOLD_AXE:
            case ItemID.GOLD_HOE:
            case ItemID.GOLD_PICKAXE:
            case ItemID.GOLD_SHOVEL:
            case ItemID.GOLD_SWORD:
                return 33;
            default:
                return 0;
        }
    }
}