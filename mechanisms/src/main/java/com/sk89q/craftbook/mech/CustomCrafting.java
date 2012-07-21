package com.sk89q.craftbook.mech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class CustomCrafting {

    public static void addRecipes(MechanismsPlugin plugin) {
        try {
            File recipeFile = new File(plugin.getDataFolder(), "recipes.txt");
            if(!recipeFile.exists()) recipeFile.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(recipeFile));
            String lastLine = "";
            while((lastLine = br.readLine()) != null) { //read file until the end
                //Skip useless lines
                lastLine = lastLine.split("#")[0];
                lastLine = lastLine.trim();
                if(lastLine.length() == 0) continue;
                if(lastLine.startsWith("@[")) { //Shapeless Recipe
                    String output = lastLine.split("@[")[1].replace("]", "");
                    int id = Integer.parseInt(output.split(":")[0]);
                    short data = Short.parseShort(output.split(":")[1].split("x")[0]);
                    int amount = Integer.parseInt(output.split(":")[1].split("x")[1]);
                    ShapelessRecipe r = new ShapelessRecipe(new ItemStack(id,amount,data));
                    String contents = br.readLine();
                    if(contents == null) continue;
                    contents = contents.split("#")[0];
                    contents = contents.trim();
                    if(contents.length() == 0) continue;
                    String[] items = contents.split(",");
                    for(String item : items) {
                        int iid = Integer.parseInt(item.split(":")[0]);
                        String idata = item.split(":")[1];
                        int iidata = -1;
                        if(idata.equals("*"))
                            iidata = -1;
                        else
                            iidata = Integer.parseInt(idata);

                        r.addIngredient(Material.getMaterial(iid), iidata);
                    }
                    if(plugin.getServer().addRecipe(r))
                        plugin.getLogger().info("Recipe Added!");
                    else
                        plugin.getLogger().warning("Failed to add recipe!");
                }
                /*TODOelse if(lastLine.startsWith("*[")) { //2x2 Shaped Recipe
                    String output = lastLine.split("*[")[1].replace("]", "");
                    int id = Integer.parseInt(output.split(":")[0]);
                    short data = Short.parseShort(output.split(":")[1].split("x")[0]);
                    int amount = Integer.parseInt(output.split(":")[1].split("x")[1]);
                    ShapedRecipe r = new ShapedRecipe(new ItemStack(id,amount,data));
                    String contents = br.readLine();
                    if(contents == null) continue;
                    contents = contents.split("#")[0];
                    contents = contents.trim();
                    if(contents.length() == 0) continue;
                    String[] items = contents.split(",");
                    for(String item : items) {
                        r.shape(items[0],items[1],items[2],items[3]);
                        int iid = Integer.parseInt(item.split(":")[0]);
                        String idata = item.split(":")[1];
                        int iidata = -1;
                        if(idata.equals("*"))
                            iidata = -1;
                        else
                            iidata = Integer.parseInt(idata);

                        r.setIngredient(new String(iid + "").charAt(0), Material.getMaterial(iid), iidata);
                    }
                }*/
                else if(lastLine.startsWith("$[")) { //Furnace Recipe
                    String output = lastLine.split("$[")[1].replace("]", "");
                    int id = Integer.parseInt(output.split(":")[0]);
                    short data = Short.parseShort(output.split(":")[1].split("x")[0]);
                    int amount = Integer.parseInt(output.split(":")[1].split("x")[1]);
                    FurnaceRecipe r = new FurnaceRecipe(new ItemStack(id,amount,data),Material.AIR);
                    String contents = br.readLine();
                    if(contents == null) continue;
                    contents = contents.split("#")[0];
                    contents = contents.trim();
                    if(contents.length() == 0) continue;
                    String[] items = contents.split(",");
                    for(String item : items) {
                        int iid = Integer.parseInt(item.split(":")[0]);
                        String idata = item.split(":")[1];
                        int iidata = -1;
                        if(idata.equals("*"))
                            iidata = -1;
                        else
                            iidata = Integer.parseInt(idata);

                        r.setInput(Material.getMaterial(iid), iidata);
                    }
                    if(plugin.getServer().addRecipe(r))
                        plugin.getLogger().info("Recipe Added!");
                    else
                        plugin.getLogger().warning("Failed to add recipe!");
                }
                else if(lastLine.startsWith("[")) { //Shaped Recipe
                    String output = lastLine.split("[")[1].replace("]", "");
                    int id = Integer.parseInt(output.split(":")[0]);
                    short data = Short.parseShort(output.split(":")[1].split("x")[0]);
                    int amount = Integer.parseInt(output.split(":")[1].split("x")[1]);
                    ShapedRecipe r = new ShapedRecipe(new ItemStack(id,amount,data));
                    String contents = br.readLine();
                    if(contents == null) continue;
                    contents = contents.split("#")[0];
                    contents = contents.trim();
                    if(contents.length() == 0) continue;
                    String[] items = contents.split(",");
                    for(String item : items) {
                        r.shape(items[0],items[1],items[2],items[3],items[4],items[5],items[6],items[7],items[8]);
                        int iid = Integer.parseInt(item.split(":")[0]);
                        String idata = item.split(":")[1];
                        int iidata = -1;
                        if(idata.equals("*"))
                            iidata = -1;
                        else
                            iidata = Integer.parseInt(idata);

                        r.setIngredient(new String(iid + "").charAt(0), Material.getMaterial(iid), iidata);
                    }
                    if(plugin.getServer().addRecipe(r))
                        plugin.getLogger().info("Recipe Added!");
                    else
                        plugin.getLogger().warning("Failed to add recipe!");
                }
                else
                    continue;
            }
            br.close();
        }
        catch(Exception e) {
            try {
                plugin.getLogger().severe("Failed to add Custom Recipes!");
                String error = "";
                e.printStackTrace(new PrintStream(error));
                plugin.getLogger().severe(error);
            }
            catch(Exception ee){}
        }
    }
}