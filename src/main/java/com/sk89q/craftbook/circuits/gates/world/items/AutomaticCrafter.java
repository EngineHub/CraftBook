package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.PipeInputIC;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class AutomaticCrafter extends AbstractSelfTriggeredIC implements PipeInputIC {

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

    public boolean craft(InventoryHolder disp) {

        Inventory inv = disp.getInventory();
        for (ItemStack it : inv.getContents()) {
            if (it == null || it.getTypeId() == 0) {
                continue;
            }
            if (it.getAmount() < 2) return false;
        }

        if (recipe == null) {

            Iterator<Recipe> recipes = Bukkit.recipeIterator();
            try {
                while (recipes.hasNext()) {
                    Recipe temprecipe = recipes.next();
                    if (isValidRecipe(temprecipe, inv)) {
                        recipe = temprecipe;
                    }
                }
            } catch (Exception e) {
                BukkitUtil.printStacktrace(e);
                disp.getInventory().setContents(inv.getContents());
            }
        }

        if (recipe == null) return false;

        if (!isValidRecipe(recipe, inv)) {
            recipe = null;
            return craft(disp);
        }

        ItemStack result = CustomCrafting.craftItem(recipe);

        if(!ItemUtil.isStackValid(result)) {
            CraftBookPlugin.inst().getLogger().warning("An Automatic Crafter IC had a valid recipe, but there was no result!");
            return false;
        }

        ItemStack[] replace = new ItemStack[9];
        for (int i = 0; i < disp.getInventory().getContents().length; i++) {
            if (disp.getInventory().getContents()[i] == null) {
                continue;
            }
            replace[i] = new ItemStack(disp.getInventory().getContents()[i]);
            replace[i].setAmount(replace[i].getAmount() - 1);
        }
        disp.getInventory().clear();

        boolean pipes = false;

        if(CraftBookPlugin.isDebugFlagEnabled("ic-mc1219")) {
            CraftBookPlugin.logger().info("AutoCrafter is dispensing a " + result.getTypeId() + " with data: " + result.getDurability() + " and amount: " + result.getAmount());
        }

        if(Pipes.Factory.setupPipes(((BlockState) disp).getBlock().getRelative(((org.bukkit.material.Directional) ((BlockState) disp).getData()).getFacing()), ((BlockState) disp).getBlock(), Arrays.asList(result)) != null)
            pipes = true;

        if (!pipes) {
            if(disp.getInventory().addItem(result).isEmpty())
                for(int i = 0; i < result.getAmount(); i++)
                    if(disp instanceof Dispenser)
                        ((Dispenser) disp).dispense();
                    else if(disp instanceof Dropper)
                        ((Dropper) disp).drop();
        }
        disp.getInventory().setContents(replace);
        return true;
    }

    public boolean collect(InventoryHolder disp) {

        for (Item item : ItemUtil.getItemsAtBlock(BukkitUtil.toSign(getSign()).getBlock())) {

            boolean delete = true;

            ItemStack stack = item.getItemStack();

            int newAmount = stack.getAmount();
            for (int i = 0; i < stack.getAmount(); i++) {
                ItemStack it = ItemUtil.getSmallestStackOfType(disp.getInventory().getContents(), stack);
                if (it == null) break;
                if (it.getAmount() < it.getMaxStackSize()) {
                    it.setAmount(it.getAmount() + 1);
                    newAmount -= 1;
                } else if (newAmount > 0) {
                    delete = false;
                    break;
                }
            }

            stack.setAmount(newAmount);
            item.setItemStack(stack);

            if (newAmount > 0) delete = false;

            if (delete) item.remove();
        }
        return false;
    }

    /**
     * @param craft
     * @param collect
     *
     * @return
     */
    public boolean doStuff(boolean craft, boolean collect) {

        boolean ret = false;
        Block crafter = getBackBlock().getRelative(0, 1, 0);
        if (crafter.getTypeId() == BlockID.DISPENSER || crafter.getTypeId() == BlockID.DROPPER) {
            if (collect) {
                collect((InventoryHolder) crafter.getState());
            }
            if (craft) {
                craft((InventoryHolder) crafter.getState());
            }
        }
        return ret;
    }

    public boolean isValidRecipe(Recipe r, Inventory inv) {

        if (r instanceof ShapedRecipe && (recipe == null || recipe instanceof ShapedRecipe)) {
            ShapedRecipe shape = (ShapedRecipe) r;
            Map<Character, ItemStack> ingredientMap = shape.getIngredientMap();
            String[] shapeArr = shape.getShape();
            if (shape.getShape().length != shapeArr.length  || shapeArr[0].length() != shape.getShape()[0].length()) return false;
            int c = -1, in = 0;
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
                        BukkitUtil.printStacktrace(e);
                    }
                    if (!ItemUtil.areItemsIdentical(require, stack))
                        return false;
                } catch (Exception e) {
                    BukkitUtil.printStacktrace(e);
                    return false;
                }
            }
            return true;
        } else if (r instanceof ShapelessRecipe && (recipe == null || recipe instanceof ShapelessRecipe)) {
            ShapelessRecipe shape = (ShapelessRecipe) r;
            List<ItemStack> ing = new ArrayList<ItemStack>(VerifyUtil.<ItemStack>withoutNulls(shape.getIngredientList()));
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
                        ing.remove(stack);
                        continue;
                    }
                    if (ItemUtil.areItemsIdentical(it, stack)) {
                        ing.remove(stack);
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
    public List<ItemStack> onPipeTransfer(BlockWorldVector pipe, List<ItemStack> items) {

        Block crafter = getBackBlock().getRelative(0, 1, 0);
        if (crafter.getTypeId() == BlockID.DISPENSER || crafter.getTypeId() == BlockID.DROPPER) {
            InventoryHolder disp = (InventoryHolder) crafter.getState();

            boolean delete = true;
            List<ItemStack> newItems = new ArrayList<ItemStack>();
            newItems.addAll(items);
            for (ItemStack ite : items) {
                if (!ItemUtil.isStackValid(ite)) continue;
                int iteind = newItems.indexOf(ite);
                int newAmount = ite.getAmount();
                for (int i = 0; i < ite.getAmount(); i++) {
                    ItemStack it = ItemUtil.getSmallestStackOfType(disp.getInventory().getContents(), ite);
                    if (!ItemUtil.isStackValid(it) || !ItemUtil.areItemsIdentical(ite, it)) continue;
                    if (it.getAmount() < it.getMaxStackSize()) {
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
            items.clear();
            items.addAll(newItems);
        }
        return items;
    }
}
