package com.sk89q.craftbook.mech.cauldron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Silthus
 */
public class ImprovedCauldron extends AbstractCraftBookMechanic {

    public static ImprovedCauldron instance;
    public ImprovedCauldronCookbook recipes;

    @Override
    public boolean enable() {

        instance = this;
        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "cauldron-recipes.yml"), "cauldron-recipes.yml");
        recipes = new ImprovedCauldronCookbook(new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "cauldron-recipes.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());

        return recipes.hasRecipes();
    }

    @Override
    public void disable() {
        recipes = null;
        instance = null;
    }

    private boolean isCauldron(Block block) {

        if (block.getType() == Material.CAULDRON && block.getRelative(BlockFace.DOWN).getType() == Material.FIRE)
            return ((Cauldron) block.getState().getData()).isFull();
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(!isCauldron(event.getClickedBlock())) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.cauldron.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }
        try {
            Collection<Item> items = getItems(event.getClickedBlock());
            ImprovedCauldronCookbook.Recipe recipe = recipes.getRecipe(CauldronItemStack.convert(items));

            // lets check permissions for that recipe
            if (!player.hasPermission("craftbook.mech.cauldron.recipe.*")
                    && !player.hasPermission("craftbook.mech.cauldron.recipe." + recipe.getId())) {
                player.printError("mech.cauldron.permissions");
                return;
            }

            if (!CraftBookPlugin.inst().getConfiguration().cauldronUseSpoons) {
                cook(event.getClickedBlock(), recipe, items);
                player.print("You have cooked the " + ChatColor.AQUA + recipe.getName() + ChatColor.YELLOW + " recipe.");
                event.getClickedBlock().getWorld().createExplosion(event.getClickedBlock().getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                event.setCancelled(true);
            } else { // Spoons
                if (event.getPlayer().getItemInHand() == null) return;
                if (isItemSpoon(event.getPlayer().getItemInHand().getType())) {
                    double chance = getSpoonChance(event.getPlayer().getItemInHand(), recipe.getChance());
                    double ran = CraftBookPlugin.inst().getRandom().nextDouble();
                    event.getPlayer().getItemInHand().setDurability((short) (event.getPlayer().getItemInHand().getDurability() - (short) 1));
                    if (chance <= ran) {
                        cook(event.getClickedBlock(), recipe, items);
                        player.print(player.translate("mech.cauldron.cook") + " " + ChatColor.AQUA + recipe.getName());
                        event.getClickedBlock().getWorld().createExplosion(event.getClickedBlock().getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                        event.setCancelled(true);
                    } else {
                        player.print("mech.cauldron.stir");
                    }
                }
            }
        } catch (UnknownRecipeException e) {
            player.printError(e.getMessage());
        }
    }

    public boolean isItemSpoon(Material id) {

        return id == Material.WOOD_SPADE || id == Material.STONE_SPADE || id == Material.IRON_SPADE || id == Material.DIAMOND_SPADE || id == Material.GOLD_SPADE;
    }

    public double getSpoonChance(ItemStack item, double chance) {

        Material id = item.getType();
        double temp = chance / 100;
        if (temp > 1) return 1;
        double toGo = temp = 1 - temp;
        double tenth = toGo / 10;
        int multiplier = 0;
        switch(id) {
            case WOOD_SPADE:
                multiplier = 1;
                break;
            case STONE_SPADE:
                multiplier = 2;
                break;
            case IRON_SPADE:
                multiplier = 3;
                break;
            case DIAMOND_SPADE:
                multiplier = 4;
                break;
            case GOLD_SPADE:
                multiplier = 5;
                break;
            default:
                break;
        }
        multiplier += item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        return temp + tenth * multiplier;
    }

    /**
     * When this is called we know that all ingredients match. This means we destroy all items inside the cauldron
     * and spawn the result of the recipe.
     *
     * @param recipe
     * @param items
     */
    private void cook(Block block, ImprovedCauldronCookbook.Recipe recipe, Collection<Item> items) {
        // first lets destroy all items inside the cauldron
        for (Item item : items)
            item.remove();
        // then give out the result items
        for (CauldronItemStack stack : recipe.getResults()) {
            block.getWorld().dropItemNaturally(block.getLocation(), stack.getItemStack());
        }
    }

    private Collection<Item> getItems(Block block) {

        List<Item> items = new ArrayList<Item>();
        for (Entity entity : block.getChunk().getEntities()) {
            if (entity instanceof Item) {
                if (EntityUtil.isEntityInBlock(entity, block) || EntityUtil.isEntityInBlock(entity, block.getRelative(BlockFace.UP))) {
                    items.add((Item) entity);
                }
            }
        }
        return items;
    }

    /**
     * @author Silthus
     */
    public static class UnknownRecipeException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public UnknownRecipeException(String message) {

            super(message);
        }
    }
}