package com.sk89q.craftbook.gates.world;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

public class AutomaticCrafter extends AbstractIC {

    public AutomaticCrafter(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Automatic Crafter";
    }

    @Override
    public String getSignTitle() {
        return "AUTO CRAFT";
    }

    //Cache the recipe - makes it faster
    private Recipe recipe = null;

    @Override
    public void trigger(ChipState chip) {
        if(chip.getInput(0))
            chip.setOutput(0, doStuff());
    }

    public boolean doStuff() {
        boolean ret = false;
        Block crafter = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, 1, 0);
        if(crafter.getType() == Material.DISPENSER) {
            Dispenser disp = (Dispenser) crafter.getState();
            craft: {
                Inventory inv = disp.getInventory();
                for(ItemStack it : inv.getContents()) {
                    if(it == null || it.getTypeId() == 0) continue;
                    if(it.getAmount() < 2) break craft;
                }

                if(recipe == null) {

                    Iterator<Recipe> recipes = Bukkit.recipeIterator();
                    try {
                        while(recipes.hasNext()) {
                            Recipe temprecipe = recipes.next();
                            if(isValidRecipe(temprecipe,inv))
                                recipe = temprecipe;
                        }
                    }
                    catch(Exception e){
                        Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                        disp.getInventory().setContents(inv.getContents());
                    }
                }

                if(recipe == null) break craft;

                if(!isValidRecipe(recipe, inv)) {
                    recipe = null;
                    doStuff();
                    return false;
                }

                ItemStack[] replace = new ItemStack[9];
                for(int i = 0; i < disp.getInventory().getContents().length; i++) {
                    if(disp.getInventory().getContents()[i] == null) continue;
                    replace[i] = new ItemStack(disp.getInventory().getContents()[i]);
                }
                disp.getInventory().clear();
                disp.getInventory().addItem(recipe.getResult());
                disp.dispense();
                disp.getInventory().setContents(replace);
                ret = true;
                break craft;
            }

            collect: {
                for (Entity en : getSign().getChunk().getEntities()) {
                    if (!(en instanceof Item)) continue;
                    Item item = (Item) en;
                    if(!ItemUtil.isStackValid(item.getItemStack()) || item.isDead() || !item.isValid()) continue;
                    int ix = item.getLocation().getBlockX();
                    int iy = item.getLocation().getBlockY();
                    int iz = item.getLocation().getBlockZ();
                    if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {
                        for(ItemStack it : disp.getInventory().getContents()) {
                            if(!ItemUtil.isStackValid(it))
                                continue;
                            if(ItemUtil.areItemsIdentical(it,item.getItemStack())) {
                                it.setAmount(it.getAmount() + item.getItemStack().getAmount());
                                item.remove();
                                break collect;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public boolean isValidRecipe(Recipe r, Inventory inv) {
        if(r instanceof ShapedRecipe) {
            ShapedRecipe shape = (ShapedRecipe)r;
            boolean large = shape.getShape().length == 3;
            int c = -1, in = 0;
            for(int i = 0; i < inv.getContents().length; i++) {
                try {
                    c++;
                    if(c > (large ? 2 : 1)) {
                        c = 0;
                        in++;
                        if(in > (large ? 2 : 1))
                            break;
                    }
                    ItemStack it = inv.getContents()[i];
                    String shapeSection = shape.getShape()[in];
                    Character item = shapeSection.charAt(c);
                    ItemStack require = shape.getIngredientMap().get(item);
                    if(require == null) require = new ItemStack(0,0);
                    if(it == null || it.getTypeId() == 0)
                        if(require.getTypeId() == 0)
                            continue;
                        else
                            return false;
                    else
                        if(require.getTypeId() == it.getTypeId() && require.getDurability() == it.getDurability())
                            continue;
                        else
                            return false;
                }
                catch(Exception e){
                    Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                    return false;
                }
            }
            return true;
        }
        else if(r instanceof ShapelessRecipe) {
            ShapelessRecipe shape = (ShapelessRecipe)r;
            List<ItemStack> ing = shape.getIngredientList();
            for(int i = 0; i < inv.getContents().length; i++) {
                ItemStack it = inv.getContents()[i];
                if(it == null) continue;
                for(ItemStack stack : ing) {
                    if(stack == null) continue;
                    if(it.getTypeId() == stack.getTypeId() && it.getDurability() == stack.getDurability()) {
                        ing.remove(stack);
                        break;
                    }
                }
            }
            if(ing.size() != 0)
                return false;

            return true;
        }
        else
            return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC { //Temporatily Restricted... until it gets unlaggy

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new AutomaticCrafter(getServer(), sign);
        }
    }
}