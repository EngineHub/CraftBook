package com.sk89q.craftbook.bukkit.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.MechanicalCore;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mech.crafting.CraftingItemStack;
import com.sk89q.craftbook.mech.crafting.InvalidCraftingException;
import com.sk89q.craftbook.mech.crafting.RecipeManager;
import com.sk89q.craftbook.mech.crafting.RecipeManager.RecipeType;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;

public class RecipeCommands {

    public RecipeCommands(CraftBookPlugin plugin) {
    }

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(aliases = {"save"}, desc = "Saves the current recipe", usage = "RecipeName RecipeType -p permission node", flags = "p:", min = 2)
    public void saveRecipe(CommandContext context, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrapPlayer((Player) sender);

        String name = context.getString(0);
        RecipeType type = RecipeType.getTypeFromName(context.getString(1));
        HashMap<String, Object> advancedData = new HashMap<String, Object>();

        if(!player.hasPermission("craftbook.mech.recipes.add"))
            throw new CommandPermissionsException();

        if (context.hasFlag('p')) {
            advancedData.put("permission-node", context.getFlag('p'));
        }

        ItemStack[] slots = new ItemStack[]{((Player) sender).getInventory().getItem(9),((Player) sender).getInventory().getItem(10),
                ((Player) sender).getInventory().getItem(11),((Player) sender).getInventory().getItem(18),((Player) sender).getInventory().getItem(19),
                ((Player) sender).getInventory().getItem(20),((Player) sender).getInventory().getItem(27),((Player) sender).getInventory().getItem(28),
                ((Player) sender).getInventory().getItem(29)};

        if(type == RecipeType.SHAPED) {

            //String[] shape = new String[3];
            //HashMap<CraftingItemStack, Character> items = new HashMap<CraftingItemStack, Character>();

            //TODO Too tired to work this out.
        } else if (type == RecipeType.SHAPELESS || type == RecipeType.FURNACE) {

            ArrayList<CraftingItemStack> ingredients = new ArrayList<CraftingItemStack>();

            for(ItemStack slot : slots) {

                if(!ItemUtil.isStackValid(slot))
                    continue;

                CraftingItemStack stack = new CraftingItemStack(slot);

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


            try {
                MechanicalCore.inst().getCustomCrafting().addRecipe(new RecipeManager.Recipe(name, type, ingredients, results.get(0), advancedData));
            } catch (InvalidCraftingException e) {
                player.printError("Error adding recipe! See console for more details!");
                BukkitUtil.printStacktrace(e);
            }
        }
    }

    public List<CraftingItemStack> getResults(Inventory inv) {

        List<CraftingItemStack> results = new ArrayList<CraftingItemStack>();

        for(int i = 21; i < 27; i++) {

            ItemStack slot = inv.getItem(i);
            if(!ItemUtil.isStackValid(slot))
                break;

            results.add(new CraftingItemStack(slot));
        }

        return results;
    }
}