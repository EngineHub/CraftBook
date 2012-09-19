package com.sk89q.craftbook.gates.world;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
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
import com.sk89q.craftbook.util.SignUtil;

//XXX Not fully implemented.
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

    @Override
    public void trigger(ChipState chip) {
        Block crafter = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, 1, 0);
        if(crafter.getType() == Material.DISPENSER) {
            Dispenser disp = (Dispenser) crafter.getState();
            craft: {
                Inventory inv = disp.getInventory();
                for(ItemStack it : inv.getContents()) {
                    if(it == null || it.getTypeId() == 0) continue;
                    if(it.getAmount() < 2) break craft;
                }
                Iterator<Recipe> recipes = Bukkit.recipeIterator();
                try {
                    while(recipes.hasNext()) { //Mega laggy loop - TODO optimize someday
                        thisRecipe: {
                        Recipe recipe = recipes.next();
                        if(recipe instanceof ShapedRecipe) { //XXX Shaped don't work.
                            ShapedRecipe shape = (ShapedRecipe)recipe;
                            for(int i = 0; i < inv.getContents().length; i++) {
                                try {
                                    ItemStack it = inv.getContents()[i];
                                    int index = (int) Math.ceil(i/shape.getShape().length);
                                    String shapeSection = shape.getShape()[index];
                                    Character item = shapeSection.charAt(Math.round(i/3));
                                    ItemStack require = shape.getIngredientMap().get(item);
                                    if(require == null) require = new ItemStack(0,0);
                                    if(it == null || it.getTypeId() == 0)
                                        if(require.getTypeId() == 0)
                                            continue;
                                        else
                                            break thisRecipe;
                                    else
                                        if(require.getTypeId() == it.getTypeId() && require.getDurability() == it.getDurability())
                                            continue;
                                        else
                                            break thisRecipe;
                                }
                                catch(Exception e){
                                    Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                                }
                            }
                        }
                        else if(recipe instanceof ShapelessRecipe) {
                            ShapelessRecipe shape = (ShapelessRecipe)recipe;
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
                                break thisRecipe;
                        }
                        else
                            continue;
                        Inventory replace = disp.getInventory();
                        disp.getInventory().clear();
                        disp.getInventory().addItem(recipe.getResult());
                        disp.dispense();
                        disp.getInventory().setContents(replace.getContents());
                        break craft;
                    }
                    }
                }
                catch(Exception e){
                    Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                    disp.getInventory().setContents(inv.getContents());
                }
            }
            //TODO pick up items for recipe at sign.
        }
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