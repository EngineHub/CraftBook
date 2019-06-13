package com.sk89q.craftbook.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * The Standard Item Syntax. This class is built to be able to survive on its own, without CraftBook.
 * 
 * @author Me4502
 *
 */
public final class ItemSyntax {

    private static final Pattern ASTERISK_PATTERN = Pattern.compile("(?<=[^\\\\])([*])");
    private static final Pattern COLON_PATTERN = Pattern.compile("(?<=[^\\\\])([:])");
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile("(?<=[^\\\\])([;])");
    private static final Pattern COMMA_PATTERN = Pattern.compile("(?<=[^\\\\])([,])");
    private static final Pattern PIPE_PATTERN = Pattern.compile("(?<=[^\\\\])([|])");
    private static final Pattern FSLASH_PATTERN = Pattern.compile("(?<=[^\\\\])([/])");

    /**
     * The plugin that stores this ItemSyntax reference. Only set this if you have a method: "public String parseItemSyntax(String item) {" in your plugin class.
     */
    public static JavaPlugin plugin;

    /**
     * The opposite of {@link ItemSyntax#getItem(String)}. Returns the String made by an {@link ItemStack}. This can be used in getItem() to return the same {@link ItemStack}.
     * 
     * @author me4502
     * 
     * @param item The {@link ItemStack} to convert into a {@link String}.
     * @return The {@link String} that represents the {@link ItemStack}.
     */
    public static String getStringFromItem(ItemStack item) {

        StringBuilder builder = new StringBuilder();
        builder.append(item.getType().getKey().toString());

        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if(meta.hasEnchants()) {
                for (Entry<Enchantment, Integer> enchants : meta.getEnchants().entrySet()) {
                    builder.append(';').append(enchants.getKey().getName()).append(':').append(enchants.getValue());
                }
            }
            if(meta.hasDisplayName()) {
                builder.append('|').append(meta.getDisplayName());
            }
            if(meta.hasLore()) {
                if(!meta.hasDisplayName()) {
                    builder.append("|$IGNORE");
                }
                List<String> list = meta.getLore();
                for(String s : list) {
                    builder.append('|').append(s);
                }
            }

            if (meta.isUnbreakable()) {
                builder.append("/unbreakable:true");
            }
            List<String> flags = new ArrayList<>();
            for (ItemFlag flag : ItemFlag.values()) {
                if (meta.hasItemFlag(flag)) {
                    flags.add(flag.name());
                }
            }
            if (!flags.isEmpty()) {
                builder.append("/flags:").append(StringUtils.join(flags, ","));
            }

            if (meta instanceof SkullMeta) {
                if(((SkullMeta) meta).hasOwner())
                    builder.append("/player:").append(((SkullMeta) meta).getOwner());
            } else if (meta instanceof BookMeta) {
                if(((BookMeta) meta).hasTitle())
                    builder.append("/title:").append(((BookMeta) meta).getTitle());
                if(((BookMeta) meta).hasAuthor())
                    builder.append("/author:").append(((BookMeta) meta).getAuthor());
                if(((BookMeta) meta).hasPages())
                    for(String page : ((BookMeta) meta).getPages())
                        builder.append("/page:").append(page);
            } else if (meta instanceof LeatherArmorMeta) {
                if(!((LeatherArmorMeta) meta).getColor().equals(Bukkit.getItemFactory().getDefaultLeatherColor()))
                    builder.append("/color:").append(((LeatherArmorMeta) meta).getColor().getRed()).append(',').append(((LeatherArmorMeta) meta).getColor().getGreen()).append(',').append(((LeatherArmorMeta) meta).getColor().getBlue());
            } else if (meta instanceof PotionMeta) {
                if(!((PotionMeta) meta).hasCustomEffects())
                    for(PotionEffect eff : ((PotionMeta) meta).getCustomEffects())
                        builder.append("/potion:").append(eff.getType().getName()).append(';').append(eff.getDuration()).append(';').append(eff.getAmplifier());
            } else if (meta instanceof EnchantmentStorageMeta) {
                if(!((EnchantmentStorageMeta) meta).hasStoredEnchants())
                    for(Entry<Enchantment, Integer> eff : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet())
                        builder.append("/enchant:").append(eff.getKey().getKey().toString()).append(';').append(eff.getValue());
            } else if (meta instanceof Damageable && ((Damageable) meta).getDamage() > 0) {
                builder.append("/damage:").append(((Damageable) meta).getDamage());
            }
        }

        return StringUtils.replace(builder.toString(), "\u00A7", "&");
    }

    private static ParserContext ITEM_CONTEXT = new ParserContext();

    static {
        ITEM_CONTEXT.setPreferringWildcard(true);
        ITEM_CONTEXT.setRestricted(false);
    }

    private static final LoadingCache<String, ItemStack> itemCache = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<String, ItemStack>() {

        @Override
        public ItemStack load(String line) throws Exception {
            int amount = 1;

            String[] advMetadataSplit = FSLASH_PATTERN.split(line);
            String[] nameLoreSplit = PIPE_PATTERN.split(advMetadataSplit[0].replace("\\/", "/"));
            String[] enchantSplit = SEMICOLON_PATTERN.split(nameLoreSplit[0].replace("\\;", ";"));
            String[] amountSplit = ASTERISK_PATTERN.split(enchantSplit[0].replace("\\*", "*"), 2);

            BaseItem item = null;
            try {
                item = WorldEdit.getInstance().getItemFactory().parseFromInput(amountSplit[0], ITEM_CONTEXT);
            } catch (InputParseException e) {
                String[] dataSplit = COLON_PATTERN.split(amountSplit[0].replace("\\:", ":"), 2);
                Material material = Material.getMaterial(dataSplit[0], true);
                if (material != null) {
                    int data = 0;
                    if (dataSplit.length > 1) {
                        data = Integer.parseInt(dataSplit[1]);
                        if (data < 0 || data > 15) {
                            data = 0;
                        }
                    }
                    try {
                        int type = LegacyMapper.getInstance().getLegacyFromItem(BukkitAdapter.asItemType(material))[0];
                        item = new BaseItem(LegacyMapper.getInstance().getItemFromLegacy(type, data));
                    } catch (Exception ee) {
                        CraftBookPlugin.logger().warning("Failed to convert legacy item: " + material.getId() + ':' + data);
                        ee.printStackTrace();
                    }
                }
            }
            try {
                if(amountSplit.length > 1)
                    amount = Integer.parseInt(amountSplit[1]);
            } catch(Exception ignored){}

            ItemStack rVal;

            if (item == null || item.getType() == null) {
                rVal = new ItemStack(Material.STONE);
            } else {
                rVal = BukkitAdapter.adapt(new BaseItemStack(item.getType(), item.getNbtData(), amount));
            }

            if(nameLoreSplit.length > 1) {

                ItemMeta meta = rVal.getItemMeta();
                //if(!nameLoreSplit[1].equalsIgnoreCase("$IGNORE"))
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[1]));
                if(nameLoreSplit.length > 2) {

                    List<String> lore = new ArrayList<>();
                    for(int i = 2; i < nameLoreSplit.length; i++)
                        lore.add(ChatColor.translateAlternateColorCodes('&', nameLoreSplit[i]));

                    meta.setLore(lore);
                }

                rVal.setItemMeta(meta);
            }
            if(enchantSplit.length > 1) {

                for(int i = 1; i < enchantSplit.length; i++) {

                    try {
                        String[] sp = COLON_PATTERN.split(enchantSplit[i]);
                        Enchantment ench = Enchantment.getByName(sp[0]);
                        if(ench == null)
                            ench = Enchantment.getByKey(NamespacedKey.minecraft(sp[0]));
                        rVal.addUnsafeEnchantment(ench, Integer.parseInt(sp[1]));
                    }
                    catch(NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException ignored){}
                }
            }
            if(advMetadataSplit.length > 1) {

                ItemMeta meta = rVal.getItemMeta();
                for(int i = 1; i < advMetadataSplit.length; i++) {
                    String section = advMetadataSplit[i];
                    String[] bits = COLON_PATTERN.split(section, 2);

                    //Invalid Bit check
                    if (bits.length < 2) continue;

                    if(bits[0].equalsIgnoreCase("player") && meta instanceof SkullMeta)
                        ((SkullMeta) meta).setOwner(bits[1]);
                    else if(bits[0].equalsIgnoreCase("author") && meta instanceof BookMeta)
                        ((BookMeta) meta).setAuthor(bits[1]);
                    else if(bits[0].equalsIgnoreCase("title") && meta instanceof BookMeta)
                        ((BookMeta) meta).setTitle(bits[1]);
                    else if(bits[0].equalsIgnoreCase("page") && meta instanceof BookMeta)
                        ((BookMeta) meta).addPage(bits[1]);
                    else if(bits[0].equalsIgnoreCase("color") && meta instanceof LeatherArmorMeta) {
                        String[] cols = COMMA_PATTERN.split(bits[1]);
                        ((LeatherArmorMeta) meta).setColor(org.bukkit.Color.fromRGB(Integer.parseInt(cols[0]),Integer.parseInt(cols[1]),Integer.parseInt(cols[2])));
                    } else if(bits[0].equalsIgnoreCase("potion") && meta instanceof PotionMeta) {
                        String[] effects = SEMICOLON_PATTERN.split(bits[1]);
                        try {
                            PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effects[0]), Integer.parseInt(effects[1]), Integer.parseInt(effects[2]));
                            ((PotionMeta) meta).addCustomEffect(effect, true);
                        } catch(Exception ignored){}
                    } else if(bits[0].equalsIgnoreCase("enchant") && meta instanceof EnchantmentStorageMeta) {
                        try {
                            String[] sp = SEMICOLON_PATTERN.split(bits[1]);
                            Enchantment ench = Enchantment.getByName(sp[0]);
                            if(ench == null) {
                                ench = Enchantment.getByKey(NamespacedKey.minecraft(sp[0]));
                            }
                            ((EnchantmentStorageMeta) meta).addStoredEnchant(ench, Integer.parseInt(sp[1]), true);
                        } catch(Exception ignored){}
                    } else if (bits[0].equalsIgnoreCase("unbreakable")) {
                        boolean unbreakable = Boolean.parseBoolean(bits[1]);
                        meta.setUnbreakable(unbreakable);
                    } else if (bits[0].equalsIgnoreCase("flags")) {
                        String[] flags = COMMA_PATTERN.split(bits[1]);
                        for (String flag : flags) {
                            meta.addItemFlags(ItemFlag.valueOf(flag));
                        }
                    } else if (bits[0].equalsIgnoreCase("damage") && meta instanceof Damageable) {
                        try {
                            int damage = Integer.parseInt(bits[1]);
                            ((Damageable) meta).setDamage(damage);
                        } catch(Exception ignored){}
                    }
                }
                rVal.setItemMeta(meta);
            }

            return rVal;
        }
    });

    /**
     * Parse an item from a line of text.
     * 
     * @author me4502
     * 
     * @param line The line to parse it from.
     * @return The item to create.
     */
    public static ItemStack getItem(String line) {

        if (line == null || line.isEmpty())
            return null;

        if(plugin != null) {
            try {
                line = (String) plugin.getClass().getMethod("parseItemSyntax", String.class).invoke(plugin, line);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
                plugin = null;
                e.printStackTrace();
            }
        }

        return itemCache.getUnchecked(line);
    }
}
