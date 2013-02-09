package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.PistonBaseMaterial;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.PipeInputIC;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class AutomaticCrafter extends AbstractIC implements PipeInputIC {

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

    public boolean craft(Dispenser disp) {

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
            craft(disp);
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

        if (CircuitCore.inst().getPipeFactory() != null) {
            Block b = disp.getBlock().getRelative(((org.bukkit.material.Dispenser) disp.getData()).getFacing());
            if (b.getTypeId() == BlockID.PISTON_STICKY_BASE) {

                PistonBaseMaterial p = (PistonBaseMaterial) b.getState().getData();
                if (p.getFacing() == ((org.bukkit.material.Dispenser) disp.getData()).getFacing().getOppositeFace()) {
                    List<ItemStack> items = new ArrayList<ItemStack>();
                    items.add(CustomCrafting.craftItem(recipe));
                    if (CircuitCore.inst().getPipeFactory() != null)
                        if (CircuitCore.inst().getPipeFactory()
                                .detect(BukkitUtil.toWorldVector(b), items) != null) {
                            pipes = true;
                        }
                }
            }
        }

        if (!pipes) {
            disp.getInventory().addItem(CustomCrafting.craftItem(recipe));
            for(int i = 0; i < recipe.getResult().getAmount(); i++)
                disp.dispense();
        }
        disp.getInventory().setContents(replace);
        return true;
    }

    public boolean collect(Dispenser disp) {

        outer:
            for (Entity en : BukkitUtil.toSign(getSign()).getChunk().getEntities()) {
                if (!(en instanceof Item)) {
                    continue;
                }
                Item item = (Item) en;
                if (!ItemUtil.isStackValid(item.getItemStack()) || item.isDead() || !item.isValid()) {
                    continue;
                }
                Location loc = item.getLocation();
                int ix = loc.getBlockX();
                int iy = loc.getBlockY();
                int iz = loc.getBlockZ();
                boolean delete = true;
                if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {
                    int newAmount = item.getItemStack().getAmount();
                    for (int i = 0; i < item.getItemStack().getAmount(); i++) {
                        ItemStack it = ItemUtil.getSmallestStackOfType(disp.getInventory().getContents(),
                                item.getItemStack());
                        if (it == null) continue outer;
                        if (it.getAmount() < 64) {
                            it.setAmount(it.getAmount() + 1);
                            newAmount -= 1;
                        } else if (newAmount > 0) {
                            delete = false;
                            break;
                        }
                    }

                    item.getItemStack().setAmount(newAmount);

                    if (newAmount > 0) delete = false;

                    if (delete) item.remove();
                }
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
        Block crafter = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (crafter.getTypeId() == BlockID.DISPENSER) {
            Dispenser disp = (Dispenser) crafter.getState();
            if (craft) {
                craft(disp);
            }
            if (collect) {
                collect(disp);
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
                    if (c == 3) {
                        c = 0;
                        in++;
                        if (in == 3) {
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
                    if (ItemUtil.areItemsIdentical(require, stack)) {
                    } else return false;
                } catch (Exception e) {
                    BukkitUtil.printStacktrace(e);
                    return false;
                }
            }
            return true;
        } else if (r instanceof ShapelessRecipe && (recipe == null || recipe instanceof ShapelessRecipe)) {
            ShapelessRecipe shape = (ShapelessRecipe) r;
            List<ItemStack> ing = new ArrayList<ItemStack>();
            ing.addAll(shape.getIngredientList());
            for (ItemStack it : inv.getContents()) {
                if (!ItemUtil.isStackValid(it)) continue;
                Iterator<ItemStack> ingIterator = ing.iterator();
                while (ingIterator.hasNext()) {
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

        } else return false;
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

            return "Auto-crafts recipes in the above dispenser.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {null, null};
            return lines;
        }
    }

    @Override
    public List<ItemStack> onPipeTransfer(BlockWorldVector pipe, List<ItemStack> items) {

        Block crafter = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (crafter.getTypeId() == BlockID.DISPENSER) {
            Dispenser disp = (Dispenser) crafter.getState();

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
            items.clear();
            items.addAll(newItems);
        }
        return items;
    }
}