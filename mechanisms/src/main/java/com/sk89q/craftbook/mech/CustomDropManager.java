// $Id$
/*
 * Copyright (C) 2012 Lymia <https://lymiahugs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Storage class for custom drop definitions.
 *
 * @author Lymia
 */
public final class CustomDropManager {
    private static final Random RNG = new Random();

    public static final int BLOCK_ID_COUNT = 256;
    public static final int DATA_VALUE_COUNT = 256;

    private CustomItemDrop[] blockDropDefinitions = new CustomItemDrop[BLOCK_ID_COUNT];
    private Map<String,DropDefinition[]> mobDropDefinitions = new TreeMap<String,DropDefinition[]>();

    public CustomDropManager(File source) {
        File blockDefinitions = new File(source, "custom-block-drops.txt");
        File mobDefinitions = new File(source, "custom-mob-drops.txt");

        try {
            loadDropDefinitions(blockDefinitions, false);
        } catch(CustomDropParseException e) {
            Bukkit.getLogger().log(Level.WARNING, "Custom block drop definitions failed to parse", e);
        } catch(IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unknown IO error while loading custom block drop definitions", e);
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unknown exception while loading custom block drop definitions", e);
        }

        if(mobDefinitions.exists()) try {
            loadDropDefinitions(mobDefinitions, true);
        } catch(CustomDropParseException e) {
            Bukkit.getLogger().log(Level.WARNING, "Custom mob drop definitions failed to parse", e);
        } catch(IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unknown IO error while loading custom mob drop definitions", e);
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unknown exception while loading custom mob drop definitions", e);
        }
    }

    public CustomItemDrop getBlockDrops(int block) {
        if(block<0||block>=BLOCK_ID_COUNT) return null;
        else return blockDropDefinitions[block];
    }
    public DropDefinition[] getMobDrop(String mobName) {
        return mobDropDefinitions.get(mobName.toLowerCase());
    }

    public void loadDropDefinitions(File file, boolean isMobDrop) throws IOException {
        String prelude  = "on unknown line";
        try {
            CustomItemDrop[] blockDropDefinitions =
                    isMobDrop?null:new CustomItemDrop[BLOCK_ID_COUNT];
            Map<String,DropDefinition[]> mobDropDefinitions =
                    isMobDrop?new TreeMap<String,DropDefinition[]>():null;
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    int currentLine = 0;
                    while((line=reader.readLine())!=null) {
                        currentLine++;
                        prelude = "Error on line "+currentLine+" of drop definition file "+
                                file.getAbsolutePath()+": "+line+"\n"; //Error prelude used for parse messages.

                        line = line.split("#")[0]; //Remove comments
                        line = line.trim(); //Remove excess whitespace

                        if(line.isEmpty()) continue; //Don't try to parse empty lines

                        String[] split = line.split("->",2); //Split primary field separator

                        if(split.length!=2) {
                            reader.close();
                            throw new CustomDropParseException(prelude+"-> not found");
                        }

                        String itemsSource = split[0].trim();
                        String targetDrops = split[1].trim();
                        if(itemsSource.isEmpty()||
                                targetDrops.isEmpty()) {
                            reader.close();
                            throw new CustomDropParseException(prelude+"unexpected empty field");
                        }

                        DropDefinition[] drops = readDrops(targetDrops, prelude);

                        if(!isMobDrop) {
                            split = itemsSource.split(":");
                            if(split.length>2) {
                                reader.close();
                                throw new CustomDropParseException(prelude+"too many source block fields");
                            }

                            int sourceId = Integer.parseInt(split[0]);
                            if(sourceId>=BLOCK_ID_COUNT || sourceId<0) {
                                reader.close();
                                throw new CustomDropParseException(prelude+"block id out of range");
                            }
                            if(blockDropDefinitions[sourceId]==null) blockDropDefinitions[sourceId] = new CustomItemDrop();
                            CustomItemDrop drop = blockDropDefinitions[sourceId];

                            if(split.length==1) {
                                if(drop.defaultDrop!=null) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude+"double drop definition");
                                }
                                drop.defaultDrop = drops;
                            } else {
                                int data = Integer.parseInt(split[1]);
                                if(data>=DATA_VALUE_COUNT || data<0) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude+"block data value out of range");
                                }
                                if(drop.drops[data]!=null) {
                                    reader.close();
                                    throw new CustomDropParseException(prelude+"double drop definition");
                                }
                                drop.drops[data] = drops;
                            }
                        } else {
                            itemsSource = itemsSource.toLowerCase();
                            if(mobDropDefinitions.containsKey(itemsSource)) {
                                reader.close();
                                throw new CustomDropParseException(prelude+"double drop definition");
                            }
                            mobDropDefinitions.put(itemsSource, drops);
                        }
                    }

                    reader.close();

                    if(isMobDrop) this.mobDropDefinitions = mobDropDefinitions;
                    else          this.blockDropDefinitions = blockDropDefinitions;
        } catch(NumberFormatException e) {
            throw new CustomDropParseException(prelude+"number field failed to parse",e);
        }
    }

    private static DropDefinition[] readDrops(String s, String prelude) throws IOException {
        String[] split = s.split(",");
        DropDefinition[] drops = new DropDefinition[split.length]; //Java really needs a map function...
        for(int i=0;i<split.length;i++)
            drops[i] = readDrop(split[i].trim(),prelude); //Strip excess whitespace and parse
        return drops;
    }
    private static DropDefinition readDrop(String s, String prelude) throws IOException {
        String[] split = s.split("x");
        if(split.length>2) throw new CustomDropParseException(prelude+": too many drop item fields");
        String[] split2 = split[0].trim().split(":");
        if(split2.length>2) throw new CustomDropParseException(prelude+": too many drop item fields");
        int itemId = Integer.parseInt(split2[0].trim());
        int data = split2.length==1?0:Integer.parseInt(split2[1].trim());
        if(data>=DATA_VALUE_COUNT || data<0)
            throw new CustomDropParseException(prelude+"block data value out of range");
        String[] split3 = split[1].trim().split("-");
        if(split3.length>2) throw new CustomDropParseException(prelude+": invalid number drops range");
        int countMin = Integer.parseInt(split3[0]);
        int countMax = split3.length==1?countMin:Integer.parseInt(split3[1]);
        return new DropDefinition(itemId, (byte) data, countMin, countMax);
    }

    public static class CustomDropParseException extends IOException {
        /**
         * 
         */
        private static final long serialVersionUID = -1147409575702887124L;
        public CustomDropParseException(String message) {
            super(message);
        }
        public CustomDropParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CustomItemDrop {
        public DropDefinition[][] drops = new DropDefinition[DATA_VALUE_COUNT][];
        public DropDefinition[] defaultDrop;

        public DropDefinition[] getDrop(int data) {
            if(data<0||data>=DATA_VALUE_COUNT) return defaultDrop;
            DropDefinition[] drop = drops[data];
            if(drop==null) return defaultDrop;
            else return drop;
        }
    }
    public static class DropDefinition {
        public final int id;
        public final byte data;
        public final int countMin;
        public final int countMax;

        public DropDefinition(int id, byte data, int countMin, int countMax) {
            if(countMax<countMin) {
                int temp = countMin;
                countMin = countMax;
                countMax = temp;
            }

            this.id = id;
            this.data = data;
            this.countMin = countMin;
            this.countMax = countMax;
        }

        public ItemStack createStack() {
            return new ItemStack(id,countMin==countMax?countMin:countMin+RNG.nextInt(countMax-countMin+1),data);
        }
    }
}
