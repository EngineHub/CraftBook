package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public final class ItemUtil {

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

        List<ItemStack> ret = new ArrayList<>();

        for(ItemStack stack : stacks) {

            if(doesItemPassFilters(stack, inclusions, exclusions))
                ret.add(stack);
        }

        return ret;
    }

    /**
     * Check whether or not an item passes filters.
     * 
     * @param stack The item to check if it passes.
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

        return true;
    }

    public static boolean areItemsSimilar(ItemStack item, Material type) {

        return areItemsSimilar(item.getType(), type);
    }

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {

        return areItemsSimilar(item.getType(), item2.getType());
    }

    public static boolean areItemsSimilar(Material data, Material comparedData) {

        return data == comparedData;
    }

    private static final Pattern STRIP_RESET_PATTERN = Pattern.compile("(?i)" + '\u00A7' + "[Rr]");

    //TODO Move to a StringUtil.
    public static String stripResetChar(String message) {

        if (message == null)
            return null;

        return STRIP_RESET_PATTERN.matcher(message).replaceAll("");
    }

    public static boolean areRecipesIdentical(Recipe rec1, Recipe rec2) {

        if(rec1 == null || rec2 == null)
            return rec1 == rec2;
        if(ItemUtil.areItemsIdentical(rec1.getResult(), rec2.getResult())) {
            CraftBookPlugin.logDebugMessage("Recipes have same results!", "advanced-data.compare-recipes");
            if(rec1 instanceof ShapedRecipe && rec2 instanceof ShapedRecipe) {
                CraftBookPlugin.logDebugMessage("Shaped recipe!", "advanced-data.compare-recipes.shaped");
                ShapedRecipe recipe1 = (ShapedRecipe) rec1;
                ShapedRecipe recipe2 = (ShapedRecipe) rec2;
                if(recipe1.getShape().length == recipe2.getShape().length) {
                    CraftBookPlugin.logDebugMessage("Same size!", "advanced-data.compare-recipes.shaped");
                    List<ItemStack> stacks1 = new ArrayList<>();

                    for(String s : recipe1.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe1.getIngredientMap().entrySet())
                                if(entry.getKey() == c)
                                    stacks1.add(entry.getValue());
                    List<ItemStack> stacks2 = new ArrayList<>();

                    for(String s : recipe2.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe2.getIngredientMap().entrySet())
                                if(entry.getKey() == c)
                                    stacks2.add(entry.getValue());

                    if(stacks2.size() != stacks1.size()) {
                        CraftBookPlugin.logDebugMessage("Recipes have different amounts of ingredients!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                    List<ItemStack> test = new ArrayList<>(stacks1);
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

                List<ItemStack> test = new ArrayList<>(VerifyUtil.withoutNulls(recipe1.getIngredientList()));
                if(test.size() == 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes.shapeless");
                    return true;
                }
                if(!test.removeAll(VerifyUtil.withoutNulls(recipe2.getIngredientList())) && test.size() > 0) {
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

        return meta.hasEnchants();

    }

    public static boolean areItemMetaIdentical(ItemMeta meta, ItemMeta meta2) {
        return areItemMetaIdentical(meta, meta2, true);
    }

    public static boolean areItemMetaIdentical(ItemMeta meta, ItemMeta meta2, boolean checkEnchants) {
        //Display Names
        String displayName1;
        if(meta.hasDisplayName())
            displayName1 = ChatColor.translateAlternateColorCodes('&', stripResetChar(meta.getDisplayName().trim()));
        else
            displayName1 = "$IGNORE";

        String displayName2;
        if(meta2.hasDisplayName())
            displayName2 = ChatColor.translateAlternateColorCodes('&', stripResetChar(meta2.getDisplayName().trim()));
        else
            displayName2 = "";

        if(!displayName1.equals(displayName2)) {
            if(!displayName1.equals("$IGNORE") && !displayName2.equals("$IGNORE"))
                return false;
        }
        CraftBookPlugin.logDebugMessage("Display names are the same", "item-checks.meta.names");

        //Lore
        List<String> lore1 = new ArrayList<>();
        if(meta.hasLore())
            for(String lore : meta.getLore())
                lore1.add(ChatColor.translateAlternateColorCodes('&', stripResetChar(lore.trim())));

        List<String> lore2 = new ArrayList<>();
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

        if(checkEnchants) {
            //Enchants
            List<Enchantment> ench1 = new ArrayList<>();
            if(meta.hasEnchants())
                ench1.addAll(meta.getEnchants().keySet());

            List<Enchantment> ench2 = new ArrayList<>();
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

            //StoredEnchants
            if (meta instanceof EnchantmentStorageMeta) {
                if (!(meta2 instanceof EnchantmentStorageMeta))
                    return false; // meta type mismatch

                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
                List<Enchantment> storedEnchantments = new ArrayList<>();
                if (storageMeta.hasStoredEnchants())
                    storedEnchantments.addAll(storageMeta.getStoredEnchants().keySet());

                EnchantmentStorageMeta storageMeta2 = (EnchantmentStorageMeta) meta2;
                List<Enchantment> storedEnchantments2 = new ArrayList<>();
                if (storageMeta2.hasStoredEnchants())
                    storedEnchantments2.addAll(storageMeta2.getStoredEnchants().keySet());

                if (storedEnchantments.size() != storedEnchantments2.size())
                    return false; // mismatch enchantment counts
                CraftBookPlugin.logDebugMessage("Has same stored enchantment lengths", "item-checks.meta.enchants");

                for (Enchantment ench : storedEnchantments) {
                    if (!storedEnchantments2.contains(ench))
                        return false; // mismatch stored enchantments
                    if (storageMeta.getStoredEnchantLevel(ench) != storageMeta2.getStoredEnchantLevel(ench))
                        return false; // mismatch stored enchantment levels
                }

                CraftBookPlugin.logDebugMessage("Stored enchants are the same", "item-checks.meta.enchants");
            } else if (meta2 instanceof EnchantmentStorageMeta)
                return false; // meta type mismatch
        }

        if (meta instanceof BookMeta) {
            if (!(meta2 instanceof BookMeta))
                return false;

            BookMeta bookMeta = (BookMeta) meta;
            BookMeta bookMeta2 = (BookMeta) meta2;

            if (bookMeta.hasAuthor() != bookMeta2.hasAuthor())
                return false;
            if (bookMeta.hasAuthor() && !bookMeta.getAuthor().equals(bookMeta2.getAuthor()))
                return false;
            if (bookMeta.hasTitle() != bookMeta2.hasTitle())
                return false;
            if (bookMeta.hasTitle() && !bookMeta.getTitle().equals(bookMeta2.getTitle()))
                return false;
            if (bookMeta.hasPages() != bookMeta2.hasPages())
                return false;
            if (bookMeta.hasPages()) {
                if (bookMeta.getPageCount() != bookMeta2.getPageCount())
                    return false;
                for (int i = 1; i <= bookMeta.getPageCount(); i++) {
                    if (!bookMeta.getPage(i).equals(bookMeta2.getPage(i)))
                        return false;
                }
            }
        } else if (meta2 instanceof BookMeta)
            return false;

        return true;
    }

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {

        if(!isStackValid(item) || !isStackValid(item2)) {
            CraftBookPlugin.logDebugMessage("An invalid item was compared. Was first? " + !isStackValid(item), "item-checks");
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
            return item.getType() == item2.getType();
        }
    }

    public static boolean isStackValid(ItemStack item) {
        if (item == null) {
            CraftBookPlugin.logDebugMessage("item-checks", "Item is null.");
            return false;
        } else if (item.getAmount() <= 0) {
            CraftBookPlugin.logDebugMessage("item-checks", "Item has amount of " + item.getAmount());
            return false;
        }
        return true;
    }

    public static boolean hasDisplayNameOrLore(ItemStack item) {
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            return meta.hasDisplayName() || meta.hasLore();
        }
        return false;
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
            case BEEF:
                return new ItemStack(Material.COOKED_BEEF);
            case CHICKEN:
                return new ItemStack(Material.COOKED_CHICKEN);
            case COD:
                return new ItemStack(Material.COOKED_COD);
            case SALMON:
                return new ItemStack(Material.COOKED_SALMON);
            case PORKCHOP:
                return new ItemStack(Material.COOKED_PORKCHOP);
            case POTATO:
                return new ItemStack(Material.BAKED_POTATO);
            case MUTTON:
                return new ItemStack(Material.COOKED_MUTTON);
            case RABBIT:
                return new ItemStack(Material.COOKED_RABBIT);
            case CHORUS_FRUIT:
                return new ItemStack(Material.POPPED_CHORUS_FRUIT);
            case KELP:
                return new ItemStack(Material.DRIED_KELP);
            default:
                return null;
        }
    }

    public static boolean isSmeltable(ItemStack item) {

        return getSmeltedResult(item) != null;
    }

    public static ItemStack getSmeltedResult(ItemStack item) {

        switch (item.getType()) {
            case COBBLESTONE:
                return new ItemStack(Material.STONE);
            case COBBLED_DEEPSLATE:
                return new ItemStack(Material.DEEPSLATE);
            case DEEPSLATE_BRICKS:
                return new ItemStack(Material.CRACKED_DEEPSLATE_BRICKS);
            case DEEPSLATE_TILES:
                return new ItemStack(Material.CRACKED_DEEPSLATE_TILES);
            case POLISHED_BLACKSTONE_BRICKS:
                return new ItemStack(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
            case BASALT:
                return new ItemStack(Material.SMOOTH_BASALT);
            case CACTUS:
                return new ItemStack(Material.GREEN_DYE);
            case IRON_ORE:
            case RAW_IRON:
            case DEEPSLATE_IRON_ORE:
                return new ItemStack(Material.IRON_INGOT);
            case COAL_ORE:
                return new ItemStack(Material.COAL);
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return new ItemStack(Material.LAPIS_LAZULI);
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return new ItemStack(Material.REDSTONE, 4);
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return new ItemStack(Material.EMERALD);
            case GOLD_ORE:
            case NETHER_GOLD_ORE:
            case RAW_GOLD:
            case DEEPSLATE_GOLD_ORE:
                return new ItemStack(Material.GOLD_INGOT);
            case ANCIENT_DEBRIS:
                return new ItemStack(Material.NETHERITE_SCRAP);
            case COPPER_ORE:
            case RAW_COPPER:
            case DEEPSLATE_COPPER_ORE:
                return new ItemStack(Material.COPPER_INGOT);
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return new ItemStack(Material.DIAMOND);
            case SAND:
                return new ItemStack(Material.GLASS);
            case CLAY_BALL:
                return new ItemStack(Material.BRICK);
            case NETHERRACK:
                return new ItemStack(Material.NETHER_BRICK);
            case CLAY:
                return new ItemStack(Material.TERRACOTTA);
            case NETHER_QUARTZ_ORE:
                return new ItemStack(Material.QUARTZ);
            case STONE_BRICKS:
                return new ItemStack(Material.CRACKED_STONE_BRICKS);
            case WET_SPONGE:
                return new ItemStack(Material.SPONGE);
            case WHITE_TERRACOTTA:
                return new ItemStack(Material.WHITE_GLAZED_TERRACOTTA);
            case ORANGE_TERRACOTTA:
                return new ItemStack(Material.ORANGE_GLAZED_TERRACOTTA);
            case MAGENTA_TERRACOTTA:
                return new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
            case LIGHT_BLUE_TERRACOTTA:
                return new ItemStack(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
            case YELLOW_TERRACOTTA:
                return new ItemStack(Material.YELLOW_GLAZED_TERRACOTTA);
            case LIME_TERRACOTTA:
                return new ItemStack(Material.LIME_GLAZED_TERRACOTTA);
            case PINK_TERRACOTTA:
                return new ItemStack(Material.PINK_GLAZED_TERRACOTTA);
            case GRAY_TERRACOTTA:
                return new ItemStack(Material.GRAY_GLAZED_TERRACOTTA);
            case LIGHT_GRAY_TERRACOTTA:
                return new ItemStack(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
            case CYAN_TERRACOTTA:
                return new ItemStack(Material.CYAN_GLAZED_TERRACOTTA);
            case PURPLE_TERRACOTTA:
                return new ItemStack(Material.PURPLE_GLAZED_TERRACOTTA);
            case BLUE_TERRACOTTA:
                return new ItemStack(Material.BLUE_GLAZED_TERRACOTTA);
            case BROWN_TERRACOTTA:
                return new ItemStack(Material.BROWN_GLAZED_TERRACOTTA);
            case GREEN_TERRACOTTA:
                return new ItemStack(Material.GREEN_GLAZED_TERRACOTTA);
            case RED_TERRACOTTA:
                return new ItemStack(Material.RED_GLAZED_TERRACOTTA);
            case BLACK_TERRACOTTA:
                return new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
            case IRON_SWORD:
            case IRON_PICKAXE:
            case IRON_AXE:
            case IRON_SHOVEL:
            case IRON_HOE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case IRON_HORSE_ARMOR:
                return new ItemStack(Material.IRON_NUGGET);
            case GOLDEN_SWORD:
            case GOLDEN_PICKAXE:
            case GOLDEN_AXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_HOE:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case GOLDEN_HORSE_ARMOR:
                return new ItemStack(Material.GOLD_NUGGET);
            case STONE:
                return new ItemStack(Material.SMOOTH_STONE);
            case QUARTZ_BLOCK:
                return new ItemStack(Material.SMOOTH_QUARTZ);
            case SANDSTONE:
                return new ItemStack(Material.SMOOTH_SANDSTONE);
            case RED_SANDSTONE:
                return new ItemStack(Material.SMOOTH_RED_SANDSTONE);
            case CHORUS_FRUIT:
                return new ItemStack(Material.POPPED_CHORUS_FRUIT);
            case SEA_PICKLE:
                return new ItemStack(Material.LIME_DYE);
            default:
                if (Tag.LOGS.isTagged(item.getType())) {
                    return new ItemStack(Material.CHARCOAL);
                }
                return null;
        }
    }

    public static Material getWoolFromColour(DyeColor color) {
        switch (color) {
            case ORANGE:
                return Material.ORANGE_WOOL;
            case MAGENTA:
                return Material.MAGENTA_WOOL;
            case LIGHT_BLUE:
                return Material.LIGHT_BLUE_WOOL;
            case YELLOW:
                return Material.YELLOW_WOOL;
            case LIME:
                return Material.LIME_WOOL;
            case PINK:
                return Material.PINK_WOOL;
            case GRAY:
                return Material.GRAY_WOOL;
            case LIGHT_GRAY:
                return Material.LIGHT_GRAY_WOOL;
            case CYAN:
                return Material.CYAN_WOOL;
            case PURPLE:
                return Material.PURPLE_WOOL;
            case BLUE:
                return Material.BLUE_WOOL;
            case BROWN:
                return Material.BROWN_WOOL;
            case GREEN:
                return Material.GREEN_WOOL;
            case RED:
                return Material.RED_WOOL;
            case BLACK:
                return Material.BLACK_WOOL;
            default:
                return Material.WHITE_WOOL;
        }
    }

    public static Material getBoatFromTree(TreeSpecies treeSpecies) {
        switch (treeSpecies) {
            case REDWOOD:
                return Material.SPRUCE_BOAT;
            case BIRCH:
                return Material.BIRCH_BOAT;
            case JUNGLE:
                return Material.JUNGLE_BOAT;
            case ACACIA:
                return Material.ACACIA_BOAT;
            case DARK_OAK:
                return Material.DARK_OAK_BOAT;
            default:
                return Material.OAK_BOAT;
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
            case CHARCOAL:
            case COAL_BLOCK:
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
            case STICK:
            case OAK_FENCE:
            case OAK_FENCE_GATE:
            case OAK_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case CRAFTING_TABLE:
            case CHEST:
            case TRAPPED_CHEST:
            case JUKEBOX:
            case NOTE_BLOCK:
            case BROWN_MUSHROOM_BLOCK:
            case RED_MUSHROOM_BLOCK:
            case BLAZE_ROD:
            case LAVA_BUCKET:
            case BOOKSHELF:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case SPRUCE_FENCE:
            case JUNGLE_FENCE:
            case BIRCH_FENCE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
            case SPRUCE_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case FISHING_ROD:
            case BOW:
            case LADDER:
            case SCAFFOLDING:
            case DRIED_KELP_BLOCK:
            case BAMBOO:
                return true;
            default:
                return Tag.ITEMS_BOATS.isTagged(item.getType())
                        || Tag.WOODEN_DOORS.isTagged(item.getType())
                        || Tag.CARPETS.isTagged(item.getType())
                        || Tag.WOODEN_BUTTONS.isTagged(item.getType())
                        || Tag.ITEMS_BANNERS.isTagged(item.getType())
                        || Tag.LOGS.isTagged(item.getType())
                        || Tag.LEAVES.isTagged(item.getType())
                        || Tag.PLANKS.isTagged(item.getType())
                        || Tag.WOODEN_STAIRS.isTagged(item.getType())
                        || Tag.WOODEN_SLABS.isTagged(item.getType())
                        || Tag.SAPLINGS.isTagged(item.getType())
                        || Tag.WOOL.isTagged(item.getType())
                        || Tag.WOODEN_PRESSURE_PLATES.isTagged(item.getType())
                        || Tag.SIGNS.isTagged(item.getType());
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
            case NETHER_WART:
            case GLOWSTONE_DUST:
            case REDSTONE:
            case SPIDER_EYE:
            case MAGMA_CREAM:
            case SUGAR:
            case GLISTERING_MELON_SLICE:
            case GHAST_TEAR:
            case BLAZE_POWDER:
            case FERMENTED_SPIDER_EYE:
            case GUNPOWDER:
            case GOLDEN_CARROT:
            case RABBIT_FOOT:
            case PUFFERFISH:
            case PHANTOM_MEMBRANE:
            case DRAGON_BREATH:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    public static boolean containsRawFood(Inventory inv) {

        return getRawFood(inv).size() > 0;
    }

    public static List<ItemStack> getRawFood(Inventory inv) {

        List<ItemStack> ret = new ArrayList<>();

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

        List<ItemStack> ret = new ArrayList<>();

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

        List<ItemStack> ret = new ArrayList<>();

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

        if (item.getType() == Material.MUSHROOM_STEW) {
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
            return new ItemStack(Material.STONE);

        ItemStack valid = invalid.clone();

        if(valid.getDurability() < 0)
            valid.setDurability((short) 0);
        if(valid.getType() == null || valid.getType() == Material.MOVING_PISTON)
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

        List<Item> items = new ArrayList<>();

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

    public static boolean isArmor(Material type){
        switch(type) {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the maximum durability that an item can have.
     * 
     * @param type
     * @return
     */
    public static short getMaxDurability(Material type) {
        switch(type) {
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
                return 1562;
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
                return 251;
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
                return 132;
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
                return 60;
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
                return 33;
            case SHEARS:
                return 238;
            case FLINT_AND_STEEL:
            case FISHING_ROD:
                return 65;
            case SHIELD:
                return 337;
            default:
                return type.getMaxDurability();
        }
    }

    private static boolean shouldDamageItem(ItemStack stack) {
        Map<Enchantment, Integer> enchants = stack.getEnchantments();
        int level = enchants.getOrDefault(Enchantment.DURABILITY, 0);

        if (level > 0) {
            int chance = (int) (100d / (level + 1));
            if(isArmor(stack.getType())) {
                chance = (int)(60d + (40d / (level + 1)));
            }
            int roll = CraftBookPlugin.inst().getRandom().nextInt(100);
            return !(roll < chance);
        }

        return true;
    }

    public static void damageHeldItem(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemMeta meta = heldItem.getItemMeta();
        if(meta instanceof Damageable && getMaxDurability(heldItem.getType()) > 0) {
            if (!shouldDamageItem(heldItem)) {
                return;
            }
            ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + 1);
            heldItem.setItemMeta(meta);
            if(((Damageable) meta).getDamage() <= getMaxDurability(heldItem.getType()))
                player.getInventory().setItemInMainHand(heldItem);
            else
                player.getInventory().setItemInMainHand(null);
        }
    }

    public static boolean isStainedGlass(Material typeId) {
        switch(typeId) {
            case BLACK_STAINED_GLASS:
            case BLUE_STAINED_GLASS:
            case BROWN_STAINED_GLASS:
            case CYAN_STAINED_GLASS:
            case GRAY_STAINED_GLASS:
            case GREEN_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS:
            case LIME_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS:
            case ORANGE_STAINED_GLASS:
            case PINK_STAINED_GLASS:
            case PURPLE_STAINED_GLASS:
            case RED_STAINED_GLASS:
            case WHITE_STAINED_GLASS:
            case YELLOW_STAINED_GLASS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isStainedGlassPane(Material typeId) {
        switch(typeId) {
            case BLACK_STAINED_GLASS_PANE:
            case BLUE_STAINED_GLASS_PANE:
            case BROWN_STAINED_GLASS_PANE:
            case CYAN_STAINED_GLASS_PANE:
            case GRAY_STAINED_GLASS_PANE:
            case GREEN_STAINED_GLASS_PANE:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
            case LIGHT_GRAY_STAINED_GLASS_PANE:
            case LIME_STAINED_GLASS_PANE:
            case MAGENTA_STAINED_GLASS_PANE:
            case ORANGE_STAINED_GLASS_PANE:
            case PINK_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case RED_STAINED_GLASS_PANE:
            case WHITE_STAINED_GLASS_PANE:
            case YELLOW_STAINED_GLASS_PANE:
                return true;
            default:
                return false;
        }
    }

    public static DyeColor getStainedColor(Material material) {
        switch (material) {
            case BLACK_STAINED_GLASS:
            case BLACK_STAINED_GLASS_PANE:
                return DyeColor.BLACK;
            case BLUE_STAINED_GLASS:
            case BLUE_STAINED_GLASS_PANE:
                return DyeColor.BLUE;
            case BROWN_STAINED_GLASS:
            case BROWN_STAINED_GLASS_PANE:
                return DyeColor.BROWN;
            case CYAN_STAINED_GLASS:
            case CYAN_STAINED_GLASS_PANE:
                return DyeColor.CYAN;
            case GRAY_STAINED_GLASS:
            case GRAY_STAINED_GLASS_PANE:
                return DyeColor.GRAY;
            case GREEN_STAINED_GLASS:
            case GREEN_STAINED_GLASS_PANE:
                return DyeColor.GREEN;
            case LIGHT_BLUE_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
                return DyeColor.LIGHT_BLUE;
            case LIGHT_GRAY_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS_PANE:
                return DyeColor.LIGHT_GRAY;
            case LIME_STAINED_GLASS:
            case LIME_STAINED_GLASS_PANE:
                return DyeColor.LIME;
            case MAGENTA_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS_PANE:
                return DyeColor.MAGENTA;
            case ORANGE_STAINED_GLASS:
            case ORANGE_STAINED_GLASS_PANE:
                return DyeColor.ORANGE;
            case PINK_STAINED_GLASS:
            case PINK_STAINED_GLASS_PANE:
                return DyeColor.PINK;
            case PURPLE_STAINED_GLASS:
            case PURPLE_STAINED_GLASS_PANE:
                return DyeColor.PURPLE;
            case RED_STAINED_GLASS:
            case RED_STAINED_GLASS_PANE:
                return DyeColor.RED;
            case WHITE_STAINED_GLASS:
            case WHITE_STAINED_GLASS_PANE:
                return DyeColor.WHITE;
            case YELLOW_STAINED_GLASS:
            case YELLOW_STAINED_GLASS_PANE:
                return DyeColor.YELLOW;
            default:
                return DyeColor.WHITE;
        }
    }

    public static boolean isShulkerBox(Material type) {
        switch (type) {
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }
}
