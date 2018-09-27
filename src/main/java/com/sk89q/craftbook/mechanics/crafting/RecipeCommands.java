package com.sk89q.craftbook.mechanics.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.crafting.RecipeManager.RecipeType;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class RecipeCommands {

    public RecipeCommands(CraftBookPlugin plugin) {
    }

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(aliases = {"delete", "remove"}, desc = "Delete a recipe", usage = "RecipeName", min = 1, max = 1)
    @CommandPermissions(value = "craftbook.mech.recipes.remove")
    public void deleteRecipe(CommandContext context, CommandSender sender) throws CommandException {

        if(RecipeManager.INSTANCE == null) {
            sender.sendMessage(ChatColor.RED + "CustomCrafting is not enabled!");
            return;
        }

        if(RecipeManager.INSTANCE.removeRecipe(context.getString(0))) {
            sender.sendMessage(ChatColor.RED + "Recipe removed successfully! This will be in effect after a restart!");
            RecipeManager.INSTANCE.save();
        } else
            sender.sendMessage(ChatColor.RED + "Recipe doesn't exist!");
    }

    @Command(aliases = {"save", "add"}, desc = "Saves the current recipe", usage = "RecipeName RecipeType -p permission node", flags = "p:", min = 2)
    @CommandPermissions(value = "craftbook.mech.recipes.add")
    public void saveRecipe(CommandContext context, CommandSender sender) throws CommandException {

        if(RecipeManager.INSTANCE == null) {
            sender.sendMessage(ChatColor.RED + "CustomCrafting is not enabled!");
            return;
        }

        if (!(sender instanceof Player)) return;
        CraftBookPlayer player = plugin.wrapPlayer((Player) sender);

        String name = context.getString(0);
        RecipeType type = RecipeType.getTypeFromName(context.getString(1));
        HashMap<String, Object> advancedData = new HashMap<>();

        if (context.hasFlag('p')) {
            advancedData.put("permission-node", context.getFlag('p'));
        }

        ItemStack[] slots = new ItemStack[]{((Player) sender).getInventory().getItem(9),((Player) sender).getInventory().getItem(10),
                ((Player) sender).getInventory().getItem(11),((Player) sender).getInventory().getItem(18),((Player) sender).getInventory().getItem(19),
                ((Player) sender).getInventory().getItem(20),((Player) sender).getInventory().getItem(27),((Player) sender).getInventory().getItem(28),
                ((Player) sender).getInventory().getItem(29)};

        if(type == RecipeType.SHAPED) {

            LinkedHashMap<CraftingItemStack, Character> items = new LinkedHashMap<>();

            int furtherestX = -1;
            int furtherestY = -1;

            for (int slot = 0; slot < 3; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtil.isStackValid(stack)) {
                    furtherestY = 0;
                    if(furtherestX < slot)
                        furtherestX = slot;
                }
            }
            for (int slot = 3; slot < 6; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtil.isStackValid(stack)) {
                    furtherestY = 1;
                    if(furtherestX < slot-3)
                        furtherestX = slot-3;
                }
            }
            for (int slot = 6; slot < 9; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtil.isStackValid(stack)) {
                    furtherestY = 2;
                    if(furtherestX < slot-6)
                        furtherestX = slot-6;
                }
            }

            if(furtherestX > 2)
                furtherestX = 2;

            String[] shape = new String[furtherestY+1];
            Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
            int curChar = 0;

            for(int y = 0; y < furtherestY+1; y++) {
                for(int x = 0; x < furtherestX+1; x++) {

                    String c = " ";
                    CraftingItemStack stack = new CraftingItemStack(slots[x+y*3]);
                    if(ItemUtil.isStackValid(stack.getItemStack())) {

                        boolean found = false;
                        for(Entry<CraftingItemStack, Character> st : items.entrySet()) {
                            if(st.getKey().isSameType(stack)) {
                                c = st.getValue().toString();
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            items.put(stack, characters[curChar]);
                            c = characters[curChar].toString();
                            curChar++;
                        }
                    }

                    if(x == 0)
                        shape[y] = c;
                    else
                        shape[y] = shape[y] + c;
                }
            }

            List<CraftingItemStack> results = getResults(((Player) sender).getInventory());
            if(results.size() > 1)
                advancedData.put("extra-results", results.subList(1, results.size()));
            else if (results.isEmpty()) {
                player.printError("Results are required to create a recipe!");
                return;
            }

            try {
                RecipeManager.Recipe recipe = RecipeManager.INSTANCE.new Recipe(name, type, items, Arrays.asList(shape), results.get(0), advancedData);
                RecipeManager.INSTANCE.addRecipe(recipe);
                if (CustomCrafting.INSTANCE == null) {
                    player.printError("You do not have CustomCrafting enabled, or Java has bugged and unloaded it (Did you use /reload?)!");
                    return;
                }
                CustomCrafting.INSTANCE.addRecipe(recipe);
                player.print("Successfully added a new " + type.name() + " recipe!");
            } catch (Exception e) {
                player.printError("Error adding recipe! See console for more details!");
                CraftBookBukkitUtil.printStacktrace(e);
            }

        } else if (type == RecipeType.SHAPELESS || type == RecipeType.FURNACE) {

            ArrayList<CraftingItemStack> ingredients = new ArrayList<>();

            for(ItemStack slot : slots) {

                if(!ItemUtil.isStackValid(slot))
                    continue;

                CraftingItemStack stack = new CraftingItemStack(slot.clone());

                boolean used = false;
                for(CraftingItemStack compare : ingredients) {

                    if(compare.isSameType(stack)) {
                        ingredients.set(ingredients.indexOf(compare), compare.add(stack));
                        used = true;
                        break;
                    }
                }

                if(!used)
                    ingredients.add(stack);
            }

            List<CraftingItemStack> results = getResults(((Player) sender).getInventory());
            if(results.size() > 1)
                advancedData.put("extra-results", results.subList(1, results.size()));
            else if (results.isEmpty()) {
                player.printError("Results are required to create a recipe!");
                return;
            }

            try {
                RecipeManager.Recipe recipe = RecipeManager.INSTANCE.new Recipe(name, type, ingredients, results.get(0), advancedData);
                RecipeManager.INSTANCE.addRecipe(recipe);
                CustomCrafting.INSTANCE.addRecipe(recipe);
                player.print("Successfully added a new " + type.name() + " recipe!");
            } catch (Exception e) {
                player.printError("Error adding recipe! See console for more details!");
                CraftBookBukkitUtil.printStacktrace(e);
            }
        }
    }

    public List<CraftingItemStack> getResults(Inventory inv) {

        List<CraftingItemStack> results = new ArrayList<>();

        for(int i = 21; i < 27; i++) {

            ItemStack slot = inv.getItem(i);
            if(!ItemUtil.isStackValid(slot))
                break;

            results.add(new CraftingItemStack(slot));
        }

        return results;
    }
}