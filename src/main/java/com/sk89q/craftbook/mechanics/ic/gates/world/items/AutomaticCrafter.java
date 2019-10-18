package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.crafting.CustomCrafting;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AutomaticCrafter extends AbstractSelfTriggeredIC implements PipeInputIC {

    private static boolean hasWarned = false;
    private static boolean hasWarnedNoResult = false;

    public AutomaticCrafter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Automatic Crafter";
    }

    @Override
    public String getSignTitle() {

        return "AUTO CRAFT";
    }

    // Cache the recipe - makes it faster
    private Recipe recipe;

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, doStuff(true, true));
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, doStuff(true, true));
    }

    private void computeRecipe(InventoryHolder disp) {
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        try {
            while (recipes.hasNext()) {
                Recipe temprecipe = recipes.next();
                if (isValidRecipe(temprecipe, disp.getInventory())) {
                    recipe = temprecipe;
                    break; //There should only be 1 valid recipe.
                }
            }
        } catch (Exception e) {
            CraftBookBukkitUtil.printStacktrace(e);
            disp.getInventory().setContents(disp.getInventory().getContents());
        }
    }

    public boolean craft(InventoryHolder disp) {

        Inventory inv = disp.getInventory();
        for (ItemStack it : inv.getContents()) {
            if (!ItemUtil.isStackValid(it))
                continue;
            if (it.getAmount() < 2) return false;
        }

        if (recipe == null) {
            computeRecipe(disp);
        }

        if (recipe == null) return false;

        if (!isValidRecipe(recipe, inv)) {
            recipe = null;
            return craft(disp);
        }

        ItemStack result = CustomCrafting.craftItem(recipe);

        if(!ItemUtil.isStackValid(result)) {
            if (!hasWarnedNoResult) {
                CraftBookPlugin.inst().getLogger().warning("An Automatic Crafter IC had a valid recipe, but there was no result! This means Bukkit"
                        + " has an invalid recipe! Result: " + result);
                hasWarnedNoResult = true;
            }
            return false;
        }

        List<ItemStack> items = new ArrayList<>();

        ItemStack[] replace = new ItemStack[9];
        for (int i = 0; i < disp.getInventory().getContents().length; i++) {
            if (disp.getInventory().getContents()[i] == null) {
                continue;
            }
            replace[i] = new ItemStack(disp.getInventory().getContents()[i]);

            if(replace[i].getType() == Material.WATER_BUCKET || replace[i].getType() == Material.LAVA_BUCKET || replace[i].getType() == Material.MILK_BUCKET)
                items.add(new ItemStack(Material.BUCKET, 1));

            replace[i].setAmount(replace[i].getAmount() - 1);
        }
        disp.getInventory().clear();

        CraftBookPlugin.logDebugMessage("AutoCrafter is dispensing a " + result.getType().name() + " with data: " + result.getDurability() + " and amount: " + result.getAmount(), "ic-mc1219");

        items.add(result);

        Block pipe = ((BlockState) disp).getBlock().getRelative(((org.bukkit.material.Directional) ((BlockState) disp).getData()).getFacing());
        Block base = ((BlockState) disp).getBlock();

        PipeRequestEvent event = new PipeRequestEvent(pipe, items, base);
        Bukkit.getPluginManager().callEvent(event);

        items = event.getItems();

        if(!items.isEmpty()) {
            for(ItemStack stack : items) {
                if(disp.getInventory().addItem(stack).isEmpty())
                    for(int i = 0; i < stack.getAmount(); i++)
                        if(disp instanceof Dispenser)
                            ((Dispenser) disp).dispense();
                        else if(disp instanceof Dropper)
                            ((Dropper) disp).drop();
            }
        }
        disp.getInventory().setContents(replace);
        return true;
    }

    private boolean collect(InventoryHolder disp) {
        if (recipe == null) {
            computeRecipe(disp);
            if (recipe == null) {
                return false; // Only collect items if valid recipe.
            }
        }

        for (Item item : ItemUtil.getItemsAtBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock())) {
            boolean delete = true;

            ItemStack stack = item.getItemStack();

            int newAmount = stack.getAmount();
            for (int i = 0; i < stack.getAmount(); i++) {
                ItemStack it = ItemUtil.getSmallestStackOfType(disp.getInventory().getContents(), stack);
                if (it == null) break;
                if (it.getAmount() < 64) {
                    it.setAmount(it.getAmount() + 1);
                    newAmount -= 1;
                } else if (newAmount > 0) {
                    delete = false;
                    break;
                }
            }

            if (newAmount > 0) delete = false;

            if (delete) {
                item.remove();
            } else {
                stack.setAmount(newAmount);
                item.setItemStack(stack);
            }
        }
        return false;
    }

    /**
     * @param craft Whether to craft.
     * @param collect Whether to collect.
     *
     * @return If it performed an action
     */
    private boolean doStuff(boolean craft, boolean collect) {

        boolean ret = false;
        Block crafter = getBackBlock().getRelative(0, 1, 0);
        if (crafter.getType() == Material.DISPENSER || crafter.getType() == Material.DROPPER) {
            if (collect)
                ret = collect((InventoryHolder) crafter.getState());
            if (craft)
                ret = craft((InventoryHolder) crafter.getState());
        }
        return ret;
    }

    private boolean isValidRecipe(Recipe r, Inventory inv) {
        if (r instanceof ShapedRecipe && (recipe == null || recipe instanceof ShapedRecipe)) {
            ShapedRecipe shape = (ShapedRecipe) r;
            Map<Character, ItemStack> ingredientMap = shape.getIngredientMap();
            String[] shapeArr = shape.getShape();
            if (shape.getShape().length != shapeArr.length  || shapeArr[0].length() != shape.getShape()[0].length()) return false;
            int c = -1, in = 0;
            int validRecipeItems = 0;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = inv.getItem(slot);
                try {
                    c++;
                    if (c >= 3) {
                        c = 0;
                        in++;
                        if (in >= 3) {
                            break;
                        }
                    }
                    String shapeSection;
                    if(in < shapeArr.length)
                        shapeSection = shapeArr[in];
                    else
                        shapeSection = "   ";
                    ItemStack require = null;
                    try {
                        Character item;
                        if(c < shapeSection.length())
                            item = shapeSection.charAt(c);
                        else
                            item = ' ';
                        if(item == ' ')
                            require = null;
                        else
                            require = ingredientMap.get(item);
                    }
                    catch(Exception e){
                        CraftBookBukkitUtil.printStacktrace(e);
                    }
                    if (require != null && require.getType() != Material.AIR) {
                        validRecipeItems ++;
                    }
                    if (!ItemUtil.areItemsIdentical(require, stack))
                        return false;
                } catch (Exception e) {
                    CraftBookBukkitUtil.printStacktrace(e);
                    return false;
                }
            }
            if (validRecipeItems == 0) {
                if (!hasWarned) {
                    CraftBookPlugin.logger().warning("Found invalid recipe! This is an issue with Bukkit/Spigot/etc, please report to them. All recipe ingredients are air. Recipe result: " + r.getResult().toString());
                    hasWarned = true;
                }
                return false;
            }

            return true;
        } else if (r instanceof ShapelessRecipe && (recipe == null || recipe instanceof ShapelessRecipe)) {
            if (((ShapelessRecipe) r).getKey().getKey().equals("shulker_box_coloring")) {
                return false;
            }
            ShapelessRecipe shape = (ShapelessRecipe) r;
            List<ItemStack> ing = new ArrayList<>(VerifyUtil.withoutNulls(shape.getIngredientList()));
            if (ing.isEmpty()) {
                return false; // If it's empty already, something is wrong with the recipe.
            }
            for (ItemStack it : inv.getContents()) {
                if (!ItemUtil.isStackValid(it)) continue;
                if(ing.isEmpty())
                    return false;
                Iterator<ItemStack> ingIterator = ing.iterator();
                while (ingIterator.hasNext()) {
                    if(ing.isEmpty())
                        break;
                    ItemStack stack = ingIterator.next();
                    if (!ItemUtil.isStackValid(stack)) {
                        ingIterator.remove();
                        continue;
                    }
                    if (ItemUtil.areItemsIdentical(it, stack)) {
                        ingIterator.remove();
                        break;
                    }
                }
            }
            return ing.isEmpty();

        } else
            return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AutomaticCrafter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Auto-crafts recipes in the above dispenser/dropper.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {null, null};
        }
    }

    @Override
    public void onPipeTransfer(PipePutEvent event) {

        Block crafter = getBackBlock().getRelative(0, 1, 0);
        if (crafter.getType() == Material.DISPENSER || crafter.getType() == Material.DROPPER) {
            InventoryHolder disp = (InventoryHolder) crafter.getState();

            boolean delete = true;
            List<ItemStack> newItems = new ArrayList<>(event.getItems());
            for (ItemStack ite : event.getItems()) {
                if (!ItemUtil.isStackValid(ite)) continue;
                int iteind = newItems.indexOf(ite);
                int newAmount = ite.getAmount();
                for (int i = 0; i < ite.getAmount(); i++) {
                    ItemStack it = ItemUtil.getSmallestStackOfType(disp.getInventory().getContents(), ite);
                    if (!ItemUtil.isStackValid(it) || !ItemUtil.areItemsIdentical(ite, it)) continue;
                    if (it.getAmount() < 64) {
                        it.setAmount(it.getAmount() + 1);
                        newAmount -= 1;
                    } else {
                        if (newAmount > 0) {
                            delete = false;
                            break;
                        }
                    }
                }
                if (newAmount > 0) delete = false;
                if(newAmount != ite.getAmount())
                    ite.setAmount(newAmount);
                if (delete) newItems.remove(iteind);
                else newItems.set(iteind, ite);
            }
            event.getItems().clear();
            event.setItems(newItems);
        }
    }
}
