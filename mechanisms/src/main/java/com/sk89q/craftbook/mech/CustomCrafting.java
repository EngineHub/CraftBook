package com.sk89q.craftbook.mech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

@Deprecated
public class CustomCrafting implements Listener {

    private static final Pattern COMMENT_PATTERN = Pattern.compile("#", Pattern.LITERAL);
    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    private static final Pattern X_PATTERN = Pattern.compile("x", Pattern.LITERAL);
    @SuppressWarnings("MalformedRegex")
    private static final Pattern LEFT_BRACKET_PATTERN = Pattern.compile("[", Pattern.LITERAL);
    @SuppressWarnings("MalformedRegex")
    private static final Pattern AT_LEFT_BRACKET_PATTERN = Pattern.compile("@[", Pattern.LITERAL);
    @SuppressWarnings("MalformedRegex")
    private static final Pattern DOLLAR_LEFT_BRACKET_PATTERN = Pattern.compile("$[", Pattern.LITERAL);
    @SuppressWarnings("MalformedRegex")
    private static final Pattern AMPERSAND_LEFT_BRACKET_PATTERN = Pattern.compile("&[", Pattern.LITERAL);
    @SuppressWarnings("MalformedRegex")
    private static final Pattern ASTERISK_LEFT_BRACKET_PATTERN = Pattern.compile("*[", Pattern.LITERAL);
    final MechanismsPlugin plugin;

    public CustomCrafting(MechanismsPlugin plugin) {

        this.plugin = plugin;
        addRecipes();
    }

    public final HashMap<Integer, Integer> fuels = new HashMap<Integer, Integer>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {

        if (event.getInventory() instanceof FurnaceInventory) {
            if (fuels == null || fuels.size() <= 0) return;
            FurnaceInventory inv = (FurnaceInventory) event.getInventory();
            if (event.getSlot() == 1 && inv.getHolder().getBurnTime() < 1 && inv.getItem(1) != null)
                if (fuels.get(inv.getItem(1).getTypeId()) > 0) {
                    inv.getHolder().setBurnTime(fuels.get(inv.getItem(1).getTypeId()).shortValue());
                    inv.getItem(1).setAmount(inv.getItem(1).getAmount() - 1);
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {

        if (event.getFuel() == null) return;
        if (event.getBurnTime() > 0) return;
        Furnace f = (Furnace) event.getBlock().getState();
        if (f.getInventory().getSmelting() != null) return;
        if (f.getInventory().getItem(0) != null) return;
        if (fuels.get(event.getFuel().getTypeId()) == null) return;
        if (fuels.get(event.getFuel().getTypeId()) > 0) {
            short burnTime = fuels.get(event.getFuel().getTypeId()).shortValue();
            event.setBurnTime(burnTime);
            event.setBurning(true);
        }
    }

    public void addRecipes() {

        try {
            File recipeFile = new File(plugin.getDataFolder(), "recipes.txt");
            if (!recipeFile.exists()) {
                recipeFile.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(recipeFile));
            String lastLine;
            while ((lastLine = br.readLine()) != null) { //read file until the end
                //Skip useless lines
                lastLine = COMMENT_PATTERN.split(lastLine)[0];
                lastLine = lastLine.trim();
                if (lastLine.isEmpty()) {
                    continue;
                }
                if (lastLine.startsWith("@[")) { //Shapeless Recipe
                    String output = AT_LEFT_BRACKET_PATTERN.split(lastLine)[1].replace("]", "");
                    ShapelessRecipe r = new ShapelessRecipe(parseItemStack(output));
                    String contents = br.readLine();
                    if (contents == null) {
                        continue;
                    }
                    contents = COMMENT_PATTERN.split(contents)[0];
                    contents = contents.trim();
                    if (contents.isEmpty()) {
                        continue;
                    }
                    String[] items = COMMA_PATTERN.split(contents);
                    for (String item : items) {
                        String[] itemSplit = COLON_PATTERN.split(item);
                        int iid = Integer.parseInt(itemSplit[0]);
                        String idata = itemSplit[1];
                        int iidata;
                        if (idata.equals("*")) {
                            iidata = -1;
                        } else {
                            iidata = Integer.parseInt(idata);
                        }

                        r.addIngredient(Material.getMaterial(iid), iidata);
                    }
                    if (plugin.getServer().addRecipe(r)) {
                        plugin.getLogger().info("Recipe Added!");
                    } else {
                        plugin.getLogger().warning("Failed to add recipe!");
                    }
                } else if (lastLine.startsWith("$[")) { //Furnace Recipe
                    String output = DOLLAR_LEFT_BRACKET_PATTERN.split(lastLine)[1].replace("]", "");
                    FurnaceRecipe r = new FurnaceRecipe(tryParseItemStack(output), Material.AIR);
                    String contents = br.readLine();
                    if (contents == null) {
                        continue;
                    }
                    contents = COMMENT_PATTERN.split(contents)[0];
                    contents = contents.trim();
                    if (contents.isEmpty()) {
                        continue;
                    }
                    String[] items = COMMA_PATTERN.split(contents);
                    for (String item : items) {
                        String[] itemSplit = COLON_PATTERN.split(item);
                        int iid = Integer.parseInt(itemSplit[0]);
                        String idata = itemSplit[1];
                        int iidata;
                        if (idata.equals("*")) {
                            iidata = -1;
                        } else {
                            iidata = Integer.parseInt(idata);
                        }

                        r.setInput(Material.getMaterial(iid), iidata);
                    }
                    if (plugin.getServer().addRecipe(r)) {
                        plugin.getLogger().info("Recipe Added!");
                    } else {
                        plugin.getLogger().warning("Failed to add recipe!");
                    }
                } else if (lastLine.startsWith("&[")) { //Furnace Fuel
                    String output = AMPERSAND_LEFT_BRACKET_PATTERN.split(lastLine)[1].replace("]", "");
                    int id = Integer.parseInt(COLON_PATTERN.split(output)[0]);
                    String contents = br.readLine();
                    if (contents == null) {
                        continue;
                    }
                    contents = COMMENT_PATTERN.split(contents)[0];
                    contents = contents.trim();
                    if (contents.isEmpty()) {
                        continue;
                    }
                    int burnTime = Integer.parseInt(contents);
                    fuels.put(id, burnTime);
                    plugin.getLogger().info("Furnace Fuel Added!");
                } else if (lastLine.startsWith("*[")) { //2x2 Shaped Recipe
                    String output = ASTERISK_LEFT_BRACKET_PATTERN.split(lastLine)[1].replace("]", "");
                    ShapedRecipe r = new ShapedRecipe(parseItemStack(output));
                    String contents = br.readLine();
                    if (contents == null) {
                        continue;
                    }
                    contents = COMMENT_PATTERN.split(contents)[0];
                    contents = contents.trim();
                    if (contents.isEmpty()) {
                        continue;
                    }
                    String[] items = COMMA_PATTERN.split(contents);
                    r.shape(getShapeData(COLON_PATTERN.split(items[0])[0]) + getShapeData(COLON_PATTERN.split(items[1])[0]),
                            getShapeData(COLON_PATTERN.split(items[2])[0]) + getShapeData(COLON_PATTERN.split(items[3])[0]));
                    plugin.getLogger().severe(Arrays.toString(r.getShape()));
                    for (String item : items) {
                        String[] itemSplit = COLON_PATTERN.split(item);
                        int iid = Integer.parseInt(itemSplit[0]);
                        String idata = itemSplit[1];
                        int iidata;
                        if (idata.equals("*")) {
                            iidata = -1;
                        } else {
                            iidata = Integer.parseInt(idata);
                        }

                        r.setIngredient((String.valueOf(iid)).charAt(0), Material.getMaterial(iid), iidata);
                    }
                    if (plugin.getServer().addRecipe(r)) {
                        plugin.getLogger().info("Recipe Added!");
                    } else {
                        plugin.getLogger().warning("Failed to add recipe!");
                    }
                } else if (lastLine.startsWith("[")) { //Shaped Recipe
                    String output = LEFT_BRACKET_PATTERN.split(lastLine)[1].replace("]", "");
                    ShapedRecipe r = new ShapedRecipe(parseItemStack(output));
                    String contents = br.readLine();
                    if (contents == null) {
                        continue;
                    }
                    contents = COMMENT_PATTERN.split(contents)[0];
                    contents = contents.trim();
                    if (contents.isEmpty()) {
                        continue;
                    }
                    String[] items = COMMA_PATTERN.split(contents);
                    r.shape(getShapeData(COLON_PATTERN.split(items[0])[0]) + getShapeData(COLON_PATTERN.split(items[1])[0]) +
                            getShapeData(COLON_PATTERN.split(items[2])[0]),
                            getShapeData(COLON_PATTERN.split(items[3])[0]) + getShapeData(COLON_PATTERN.split(items[4])[0]) +
                            getShapeData(COLON_PATTERN.split(items[5])[0]),
                            getShapeData(COLON_PATTERN.split(items[6])[0]) + getShapeData(COLON_PATTERN.split(items[7])[0]) +
                            getShapeData(COLON_PATTERN.split(items[8])[0]));
                    for (String item : items) {
                        String[] itemSplit = COLON_PATTERN.split(item);
                        int iid = Integer.parseInt(itemSplit[0]);
                        String idata = itemSplit[1];
                        int iidata;
                        if (idata.equals("*")) {
                            iidata = -1;
                        } else {
                            iidata = Integer.parseInt(idata);
                        }

                        r.setIngredient((String.valueOf(iid)).charAt(0), Material.getMaterial(iid), iidata);
                    }
                    if (plugin.getServer().addRecipe(r)) {
                        plugin.getLogger().info("Recipe Added!");
                    } else {
                        plugin.getLogger().warning("Failed to add recipe!");
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            try {
                plugin.getLogger().severe("Failed to add Custom Recipes!");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                plugin.getLogger().severe(sw.toString());
            } catch (Exception ignored) {
            }
        }
    }

    private ItemStack tryParseItemStack(String output) {
        // Is this necessary?
        String[] split = COLON_PATTERN.split(output);
        int id = Integer.parseInt(split[0]);
        String[] split2 = X_PATTERN.split(split[1]);
        short data = Short.parseShort(split2[0]);
        int amount = 1;
        try {
            amount = Integer.parseInt(split2[1]);
        } catch (Exception ignored) {
        }
        return new ItemStack(id, amount, data);
    }

    private ItemStack parseItemStack(String output) {
        String[] split = COLON_PATTERN.split(output);
        int id = Integer.parseInt(split[0]);
        String[] split2 = X_PATTERN.split(split[1]);
        short data = Short.parseShort(split2[0]);
        int amount = Integer.parseInt(split2[1]);
        return new ItemStack(id, amount, data);
    }

    public String getShapeData(String s) {

        s = String.valueOf(s.charAt(0));
        return s;
    }
}