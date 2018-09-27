// $Id$
/*
 * Copyright (C) 2012 Lymia <https://lymiahugs.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.drops.legacy;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.drops.BlockCustomDropDefinition;
import com.sk89q.craftbook.mechanics.drops.CustomDrops;
import com.sk89q.craftbook.mechanics.drops.DropItemStack;
import com.sk89q.craftbook.mechanics.drops.EntityCustomDropDefinition;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * Storage class for custom drop definitions.
 *
 * @author Lymia
 */
public final class LegacyCustomDropManager {

    public static final int BLOCK_ID_COUNT = 256;
    public static final int DATA_VALUE_COUNT = 127;

    private CustomItemDrop[] blockDropDefinitions = new CustomItemDrop[BLOCK_ID_COUNT];
    private Map<String, DropDefinition[]> mobDropDefinitions = new TreeMap<>();

    public LegacyCustomDropManager(File source) {

        //CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "custom-block-drops.txt"), "custom-block-drops.txt");
        //CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "custom-mob-drops.txt"), "custom-mob-drops.txt");

        File blockDefinitions = new File(source, "custom-block-drops.txt");
        File mobDefinitions = new File(source, "custom-mob-drops.txt");

        if (blockDefinitions.exists()) {
            try {
                loadDropDefinitions(blockDefinitions, false);
            } catch (CustomDropParseException e) {
                CraftBookPlugin.logger().log(Level.WARNING, "Custom block drop definitions failed to parse", e);
            } catch (IOException e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "Unknown IO error while loading custom block drop definitions", e);
            } catch (Exception e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "Unknown exception while loading custom block drop definitions", e);
            }
        }

        if (mobDefinitions.exists()) {
            try {
                loadDropDefinitions(mobDefinitions, true);
            } catch (CustomDropParseException e) {
                CraftBookPlugin.logger().log(Level.WARNING, "Custom mob drop definitions failed to parse", e);
            } catch (IOException e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "Unknown IO error while loading custom mob drop definitions", e);
            } catch (Exception e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "Unknown exception while loading custom mob drop definitions", e);
            }
        }
    }

    public CustomItemDrop getBlockDrops(int block) {

        if (block < 0 || block >= BLOCK_ID_COUNT) return null;
        else return blockDropDefinitions[block];
    }

    public DropDefinition[] getMobDrop(LivingEntity mob) {

        if(mob.getCustomName() != null && mobDropDefinitions.containsKey(ChatColor.translateAlternateColorCodes('&', (mob.getType().name() + "|" + mob.getCustomName()).toLowerCase(Locale.ENGLISH))))
            return mobDropDefinitions.get(ChatColor.translateAlternateColorCodes('&', (mob.getType().name() + "|" + mob.getCustomName()).toLowerCase(Locale.ENGLISH)));
        return mobDropDefinitions.get(mob.getType().name().toLowerCase(Locale.ENGLISH));
    }

    int converted = 0;

    public void loadDropDefinitions(File file, boolean isMobDrop) throws IOException {

        String prelude = "on unknown line";
        try {
            CustomItemDrop[] blockDropDefinitions = isMobDrop ? null : new CustomItemDrop[BLOCK_ID_COUNT];
            Map<String, DropDefinition[]> mobDropDefinitions = isMobDrop ? new TreeMap<>() : null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                    String line;
                    int currentLine = 0;
                    while ((line = reader.readLine()) != null) {
                        if (line.length() == 0 || line.trim().startsWith("#")) continue;
                        currentLine++;
                        prelude = "Error on line " + currentLine + " of drop definition file " + file.getAbsolutePath() + ": " +
                                "" + line + "\n"; // Error prelude
                        // used for
                        // parse
                        // messages.

                        try {
                            if (line.contains("#")) line = RegexUtil.COMMENT_PATTERN.split(line)[0]; // Remove comments
                        } catch (Exception ignored) {
                        }

                        line = line.trim(); // Remove excess whitespace

                        if (line.length() == 0) {
                            continue; // Don't try to parse empty lines
                        }

                        String[] split = RegexUtil.FIELD_SEPARATOR_PATTERN.split(line, 2); // Split primary field separator

                        if (split.length != 2) {
                            reader.close();
                            throw new CustomDropParseException(prelude + "-> not found");
                        }

                        String itemsSource = StringUtils.replace(split[0], "+", "").trim();
                        String targetDrops = split[1].trim();
                        if (itemsSource.length() == 0 || targetDrops.length() == 0) {
                            reader.close();
                            throw new CustomDropParseException(prelude + "unexpected empty field");
                        }

                        DropDefinition[] drops = readDrops(targetDrops, prelude, split[0].contains("+"));

                        if (!isMobDrop) {
                            split = RegexUtil.COLON_PATTERN.split(itemsSource);
                            if (split.length > 2) {
                                reader.close();
                                throw new CustomDropParseException(prelude + "too many source block fields");
                            }

                            int sourceId = Integer.parseInt(split[0]);
                            if (sourceId >= BLOCK_ID_COUNT || sourceId < 0) {
                                reader.close();
                                throw new CustomDropParseException(prelude + "block id out of range");
                            }
                            if (blockDropDefinitions[sourceId] == null) {
                                blockDropDefinitions[sourceId] = new CustomItemDrop();
                            }
                            CustomItemDrop drop = blockDropDefinitions[sourceId];

                            int data = 0;

                            if (split.length == 1) {
                                if (drop.defaultDrop != null) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude + "double drop definition");
                                }
                                drop.defaultDrop = drops;
                            } else {
                                data = Integer.parseInt(split[1]);
                                if (data >= DATA_VALUE_COUNT || data < 0) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude + "block data value out of range");
                                }
                                if (drop.drops[data] != null) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude + "double drop definition");
                                }
                                drop.drops[data] = drops;
                            }

                            List<DropItemStack> stacks = new ArrayList<>();

                            boolean append = false;

                            for(DropDefinition dd : drops) {
                                DropItemStack nstack = new DropItemStack(dd.stack);
                                nstack.setChance(dd.chance);
                                nstack.setMaximum(dd.countMax);
                                nstack.setMinimum(dd.countMin);
                                stacks.add(nstack);
                                if(dd.append)
                                    append = true;
                            }

                            BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(sourceId, data);
                            if (state == null) {
                                throw new CustomDropParseException("Unknown block: " + sourceId + ":" + data);
                            }

                            BlockCustomDropDefinition converted = new BlockCustomDropDefinition(sourceId + "" + data + "" + this.converted++,stacks, null, TernaryState.NONE, state);
                            converted.setAppend(append);

                            ((CustomDrops) CraftBookPlugin.inst().getMechanic(CustomDrops.class)).addDefinition(converted);
                        } else {
                            itemsSource = ChatColor.translateAlternateColorCodes('&', itemsSource.toLowerCase(Locale.ENGLISH));
                            if (mobDropDefinitions.containsKey(itemsSource)) {
                                reader.close();
                                throw new CustomDropParseException(prelude + "double drop definition");
                            }
                            mobDropDefinitions.put(itemsSource, drops);
                        }
                    }

                    reader.close();

                    if (isMobDrop) {
                        this.mobDropDefinitions = mobDropDefinitions;

                        for(Entry<String, DropDefinition[]> def : mobDropDefinitions.entrySet()) {

                            List<DropItemStack> stacks = new ArrayList<>();

                            boolean append = false;

                            for(DropDefinition dd : def.getValue()) {
                                DropItemStack nstack = new DropItemStack(dd.stack);
                                nstack.setChance(dd.chance);
                                nstack.setMaximum(dd.countMax);
                                nstack.setMinimum(dd.countMin);
                                stacks.add(nstack);
                                if(dd.append)
                                    append = true;
                            }

                            EntityCustomDropDefinition converted = new EntityCustomDropDefinition(def.getKey().toUpperCase() + "" + this.converted++, stacks, null, TernaryState.NONE, EntityType.valueOf(def.getKey().toUpperCase()));
                            converted.setAppend(append);

                            ((CustomDrops) CraftBookPlugin.inst().getMechanic(CustomDrops.class)).addDefinition(converted);
                        }
                    } else {
                        this.blockDropDefinitions = blockDropDefinitions;
                    }
        } catch (NumberFormatException e) {
            throw new CustomDropParseException(prelude + "number field failed to parse", e);
        }
    }

    private static DropDefinition[] readDrops(String s, String prelude, boolean append) throws IOException {

        String[] split = RegexUtil.COMMA_PATTERN.split(s);
        DropDefinition[] drops = new DropDefinition[split.length]; // Java really needs a map function...
        for (int i = 0; i < split.length; i++) {
            drops[i] = readDrop(split[i].trim(), prelude, append); // Strip excess whitespace and parse
        }
        return drops;
    }

    private static DropDefinition readDrop(String s, String prelude, boolean append) throws IOException {

        String[] split = RegexUtil.X_PATTERN.split(RegexUtil.PERCENT_PATTERN.split(s)[0]);
        if(split.length > 2) {
            List<String> temp = new ArrayList<>();
            for(int i = 0; i < split.length; i++) {
                if(temp.isEmpty())
                    temp.add(split[i]);
                else if (i < split.length - 1)
                    temp.set(0, temp.get(0) + "x" + split[i]);
                else
                    temp.add(split[i]);
            }
            split = temp.toArray(new String[temp.size()]);
        }
        if (split.length > 2) throw new CustomDropParseException(prelude + ": too many drop item fields");
        ItemStack stack = ItemUtil.makeItemValid(ItemSyntax.getItem(split[0]));
        String[] split3 = RegexUtil.MINUS_PATTERN.split(split[1].trim());
        if (split3.length > 2) throw new CustomDropParseException(prelude + ": invalid number drops range");
        int countMin = Integer.parseInt(split3[0]);
        int countMax = split3.length == 1 ? countMin : Integer.parseInt(split3[1]);
        double chance = 100;
        try {
            chance = Double.parseDouble(RegexUtil.PERCENT_PATTERN.split(s)[1]);
        } catch (Exception ignored) {
        }

        return new DropDefinition(stack, countMin, countMax, chance, append);
    }

    public static class CustomDropParseException extends IOException {

        private static final long serialVersionUID = -1147409575702887124L;

        public CustomDropParseException(String message) {

            super(message);
        }

        public CustomDropParseException(String message, Throwable cause) {

            super(message);
        }
    }

    public static class CustomItemDrop {

        public final DropDefinition[][] drops = new DropDefinition[DATA_VALUE_COUNT][];
        public DropDefinition[] defaultDrop;

        public DropDefinition[] getDrop(int data) {

            if (data < 0 || data >= DATA_VALUE_COUNT) return defaultDrop;
            DropDefinition[] drop = drops[data];
            if (drop == null) return defaultDrop;
            else return drop;
        }
    }

    public static class DropDefinition {

        public final ItemStack stack;
        public final int countMin;
        public final int countMax;
        public final boolean append;
        public final double chance;

        public DropDefinition(ItemStack stack, int countMin, int countMax, double chance, boolean append) {

            if (countMax < countMin) {
                int temp = countMin;
                countMin = countMax;
                countMax = temp;
            }

            this.chance = Math.min(100, Math.max(0, chance));

            this.stack = stack;
            this.countMin = countMin;
            this.countMax = countMax;
            this.append = append;
        }

        public ItemStack getItemStack() {

            if (CraftBookPlugin.inst().getRandom().nextInt(100) > chance) return null;
            ItemStack stack = this.stack.clone();
            stack.setAmount(countMin == countMax ? countMin : countMin + CraftBookPlugin.inst().getRandom().nextInt(countMax - countMin + 1));
            return stack;
        }
    }
}