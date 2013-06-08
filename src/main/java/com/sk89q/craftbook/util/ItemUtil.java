package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.blocks.ItemType;

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

    public static boolean areItemsSimilar(ItemStack item, int type) {

        return areItemsSimilar(item, new MaterialData(type, (byte) 0));
    }

    public static boolean areItemsSimilar(ItemStack item, MaterialData data) {

        return areItemsSimilar(item.getData(), data);
    }

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {

        return areItemsSimilar(item.getData(), item2.getData());
    }

    public static boolean areItemsSimilar(MaterialData data, MaterialData comparedData) {

        return data.getItemTypeId() == comparedData.getItemTypeId();
    }

    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_RESET_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[Rr]");

    //TODO Move to a StringUtil.
    public static String stripResetChar(String message) {

        if (message == null) {
            return null;
        }

        return STRIP_RESET_PATTERN.matcher(message).replaceAll("");
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
                CraftBookPlugin.logDebugMessage("Both have metadata", "item-checks");
                if(item.getItemMeta().hasDisplayName() == item2.getItemMeta().hasDisplayName()) {
                    CraftBookPlugin.logDebugMessage("Both have names", "item-checks");
                    CraftBookPlugin.logDebugMessage("ItemStack1 Display Name: " + item.getItemMeta().getDisplayName() + ". ItemStack2 Display Name: " + item2.getItemMeta().getDisplayName(), "item-checks");
                    if(!stripResetChar(item.getItemMeta().getDisplayName().trim().replace("'", "")).equals(stripResetChar(item2.getItemMeta().getDisplayName().trim().replace("'", ""))))
                        return false;
                    CraftBookPlugin.logDebugMessage("Items share display names", "item-checks");
                } else
                    return false;
                if(item.getItemMeta().hasLore() == item2.getItemMeta().hasLore()) {
                    CraftBookPlugin.logDebugMessage("Both have lore", "item-checks");
                    if(item.getItemMeta().hasLore()) {
                        if(item.getItemMeta().getLore().size() != item2.getItemMeta().getLore().size())
                            return false;
                        for(int i = 0; i < item.getItemMeta().getLore().size(); i++) {
                            CraftBookPlugin.logDebugMessage("ItemStack1 Lore: " + item.getItemMeta().getLore().get(i) + ". ItemStack2 Lore: " + item2.getItemMeta().getLore().get(i), "item-checks");
                            if(!stripResetChar(item.getItemMeta().getLore().get(i).trim().replace("'", "")).equals(stripResetChar(item2.getItemMeta().getLore().get(i).trim().replace("'", ""))))
                                return false;
                            CraftBookPlugin.logDebugMessage("Items share same lore", "item-checks");
                        }
                    }
                } else
                    return false;
                if(item.getItemMeta().hasEnchants() == item2.getItemMeta().hasEnchants()) {
                    CraftBookPlugin.logDebugMessage("Both share enchant existance", "item-checks");
                    if(item.getItemMeta().hasEnchants()) {
                        if(item.getItemMeta().getEnchants().size() != item2.getItemMeta().getEnchants().size())
                            return false;
                        for(Enchantment ench : item.getItemMeta().getEnchants().keySet()) {

                            if(!item2.getItemMeta().getEnchants().containsKey(ench)) {
                                CraftBookPlugin.logDebugMessage("Item2 does not have enchantment: " + ench.getName(), "item-checks");
                                return false;
                            }
                            CraftBookPlugin.logDebugMessage("Enchant Name: " + ench.getName() + "ItemStack1 level " + item.getItemMeta().getEnchants().get(ench) + " ItemStack2 level " + item2.getItemMeta().getEnchants().get(ench), "item-checks");
                            if(item.getItemMeta().getEnchantLevel(ench) != item2.getItemMeta().getEnchantLevel(ench))
                                return false;
                            CraftBookPlugin.logDebugMessage("Item2 does not have enchantment: " + ench.getName(), "item-checks");
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

            if(item.getTypeId() != item2.getTypeId())
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

        return getCookedResult(item) != null && !item.hasItemMeta();
    }

    public static ItemStack getCookedResult(ItemStack item) {

        switch (item.getTypeId()) {
            case ItemID.RAW_BEEF:
                return new ItemStack(ItemID.COOKED_BEEF);
            case ItemID.RAW_CHICKEN:
                return new ItemStack(ItemID.COOKED_CHICKEN);
            case ItemID.RAW_FISH:
                return new ItemStack(ItemID.COOKED_FISH);
            case ItemID.RAW_PORKCHOP:
                return new ItemStack(ItemID.COOKED_PORKCHOP);
            case ItemID.POTATO:
                return new ItemStack(ItemID.BAKED_POTATO);
            default:
                return null;
        }
    }

    public static boolean isSmeltable(ItemStack item) {

        return getSmeletedResult(item) != null && !item.hasItemMeta();
    }

    public static ItemStack getSmeletedResult(ItemStack item) {

        switch (item.getTypeId()) {
            case BlockID.COBBLESTONE:
                return new ItemStack(BlockID.STONE);
            case BlockID.CACTUS:
                return new ItemStack(ItemID.INK_SACK, 1, (short) 2);
            case BlockID.LOG:
                return new ItemStack(ItemID.COAL, 1, (short) 1);
            case BlockID.IRON_ORE:
                return new ItemStack(ItemID.IRON_BAR);
            case BlockID.REDSTONE_ORE:
                return new ItemStack(ItemID.REDSTONE_DUST);
            case BlockID.EMERALD_ORE:
                return new ItemStack(ItemID.EMERALD);
            case BlockID.GOLD_ORE:
                return new ItemStack(ItemID.GOLD_BAR);
            case BlockID.DIAMOND_ORE:
                return new ItemStack(ItemID.DIAMOND);
            case BlockID.SAND:
                return new ItemStack(BlockID.GLASS);
            case ItemID.CLAY_BALL:
                return new ItemStack(ItemID.BRICK_BAR);
            case BlockID.NETHERRACK:
                return new ItemStack(ItemID.NETHER_BRICK);
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

        switch(item.getTypeId()) {
            case ItemID.COAL:
            case BlockID.LOG:
            case BlockID.WOOD:
            case BlockID.WOODEN_STEP:
            case BlockID.SAPLING:
            case ItemID.WOOD_AXE:
            case ItemID.WOOD_HOE:
            case ItemID.WOOD_PICKAXE:
            case ItemID.WOOD_SHOVEL:
            case ItemID.WOOD_SWORD:
            case BlockID.WOODEN_PRESSURE_PLATE:
            case ItemID.STICK:
            case BlockID.FENCE:
            case BlockID.FENCE_GATE:
            case BlockID.WOODEN_STAIRS:
            case BlockID.TRAP_DOOR:
            case BlockID.WORKBENCH:
            case BlockID.CHEST:
            case BlockID.TRAPPED_CHEST:
            case BlockID.JUKEBOX:
            case BlockID.NOTE_BLOCK:
            case BlockID.BROWN_MUSHROOM_CAP:
            case BlockID.RED_MUSHROOM_CAP:
            case ItemID.BLAZE_ROD:
            case ItemID.LAVA_BUCKET:
            case BlockID.BOOKCASE:
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

        switch(item.getTypeId()) {
            case ItemID.NETHER_WART_SEED:
            case ItemID.LIGHTSTONE_DUST:
            case ItemID.REDSTONE_DUST:
            case ItemID.SPIDER_EYE:
            case ItemID.MAGMA_CREAM:
            case ItemID.SUGAR:
            case ItemID.GLISTERING_MELON:
            case ItemID.GHAST_TEAR:
            case ItemID.BLAZE_POWDER:
            case ItemID.FERMENTED_SPIDER_EYE:
            case ItemID.SULPHUR:
            case ItemID.GOLDEN_CARROT:
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

    public static boolean isFurnacable(ItemStack item) {

        return isCookable(item) || isSmeltable(item);
    }

    public static boolean isItemEdible(ItemStack item) {

        return item.getType().isEdible();
    }

    public static ItemStack getUsedItem(ItemStack item) {

        if (item.getTypeId() == ItemID.MUSHROOM_SOUP) {
            item.setTypeId(ItemID.BOWL); // Get your bowl back
        } else if (item.getTypeId() == ItemID.POTION) {
            item.setTypeId(ItemID.GLASS_BOTTLE); // Get your bottle back
        } else if (item.getTypeId() == ItemID.LAVA_BUCKET || item.getTypeId() == ItemID.WATER_BUCKET || item
                .getTypeId() == ItemID.MILK_BUCKET) {
            item.setTypeId(ItemID.BUCKET); // Get your bucket back
        } else if (item.getAmount() == 1) {
            item.setTypeId(0);
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

    /**
     * Parse an item from a line of text.
     * 
     * @param line The line to parse it from.
     * @return The item to create.
     */
    public static ItemStack getItem(String line) {

        if (line == null || line.isEmpty())
            return null;

        int id = 0;
        int data = -1;
        int amount = 1;

        String[] nameLoreSplit = RegexUtil.PIPE_PATTERN.split(line);
        String[] enchantSplit = RegexUtil.SEMICOLON_PATTERN.split(nameLoreSplit[0]);
        String[] amountSplit = RegexUtil.ASTERISK_PATTERN.split(enchantSplit[0], 2);
        String[] dataSplit = RegexUtil.COLON_PATTERN.split(amountSplit[0], 2);
        try {
            id = Integer.parseInt(dataSplit[0]);
        } catch (NumberFormatException e) {
            try {
                id = BlockType.lookup(dataSplit[0]).getID();
                if (id < 1) id = 1;
            } catch (Exception ee) {
                try {
                    id = ItemType.lookup(dataSplit[0]).getID();
                    if (id < 1) id = 1;
                }
                catch(Exception eee){
                    id = Material.getMaterial(dataSplit[0]).getId();
                    if (id < 1) id = 1;
                }
            }
        }
        try {
            if (dataSplit.length > 1)
                data = Integer.parseInt(dataSplit[1]);
        }
        catch(Exception e){}
        try {
            if(amountSplit.length > 1)
                amount = Integer.parseInt(amountSplit[1]);
        }
        catch(Exception e){}

        if(id < 1)
            id = 1;

        ItemStack rVal = new ItemStack(id, amount, (short) data);
        rVal.setData(new MaterialData(id, (byte)data));

        if(nameLoreSplit.length > 1 && id > 0) {

            ItemMeta meta = rVal.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[1]));
            if(nameLoreSplit.length > 2) {

                List<String> lore = new ArrayList<String>();
                for(int i = 2; i < nameLoreSplit.length; i++)
                    lore.add(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[i]));

                meta.setLore(lore);
            }

            rVal.setItemMeta(meta);
        }
        if(enchantSplit.length > 1) {

            for(int i = 1; i < enchantSplit.length; i++) {

                try {
                    String[] sp = RegexUtil.COLON_PATTERN.split(enchantSplit[i]);
                    Enchantment ench = Enchantment.getByName(sp[0]);
                    if(ench == null)
                        ench = Enchantment.getById(Integer.parseInt(sp[0]));
                    rVal.addUnsafeEnchantment(ench, Integer.parseInt(sp[1]));
                }
                catch(Exception e){}
            }
        }

        return rVal;
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