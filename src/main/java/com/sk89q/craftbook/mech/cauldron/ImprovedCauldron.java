package com.sk89q.craftbook.mech.cauldron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * @author Silthus
 */
public class ImprovedCauldron extends AbstractMechanic implements Listener {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<ImprovedCauldron> {

        private CraftBookPlugin plugin = CraftBookPlugin.inst();
        public static Factory INSTANCE;
        public ImprovedCauldronCookbook recipes;

        public Factory() {

            INSTANCE = this;
            plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "cauldron-recipes.yml"), "cauldron-recipes.yml");
            recipes = new ImprovedCauldronCookbook(new YAMLProcessor(new File(plugin.getDataFolder(), "cauldron-recipes.yml"), true, YAMLFormat.EXTENDED), plugin.getLogger());
        }

        @Override
        public ImprovedCauldron detect(BlockWorldVector pos) throws InvalidMechanismException {

            if (isCauldron(pos)) return new ImprovedCauldron(BukkitUtil.toBlock(pos), recipes);
            return null;
        }

        private boolean isCauldron(BlockWorldVector pos) {

            Block block = BukkitUtil.toBlock(pos);
            if (block.getTypeId() == BlockID.CAULDRON) {
                Cauldron cauldron = (Cauldron) block.getState().getData();
                return block.getRelative(BlockFace.DOWN).getTypeId() == BlockID.FIRE && cauldron.isFull();
            }
            return false;
        }

    }

    private Block block;
    private ImprovedCauldronCookbook cookbook;

    private ImprovedCauldron(Block block, ImprovedCauldronCookbook recipes) {

        super();
        this.block = block;
        cookbook = recipes;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().cauldronEnabled) return;
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        if (block.equals(event.getClickedBlock())) {
            if (!player.hasPermission("craftbook.mech.cauldron.use")) {
                player.printError("mech.use-permission");
                return;
            }
            try {
                Collection<Item> items = getItems();
                ImprovedCauldronCookbook.Recipe recipe = cookbook.getRecipe(CauldronItemStack.convert(items));

                // lets check permissions for that recipe
                if (!player.hasPermission("craftbook.mech.cauldron.recipe.*")
                        && !player.hasPermission("craftbook.mech.cauldron.recipe." + recipe.getId())) {
                    player.printError("You dont have permission to cook this recipe.");
                    return;
                }

                if (!plugin.getConfiguration().cauldronUseSpoons) {
                    cook(recipe, items);
                    player.print("You have cooked the " + ChatColor.AQUA + recipe.getName() + ChatColor.YELLOW + " recipe.");
                    block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                    event.setCancelled(true);
                } else { // Spoons
                    if (event.getPlayer().getItemInHand() == null) return;
                    if (isItemSpoon(event.getPlayer().getItemInHand().getTypeId())) {
                        double chance = getSpoonChance(event.getPlayer().getItemInHand(), recipe.getChance());
                        double ran = plugin.getRandom().nextDouble();
                        event.getPlayer().getItemInHand().setDurability((short) (event.getPlayer().getItemInHand().getDurability() - (short) 1));
                        if (chance <= ran) {
                            cook(recipe, items);
                            player.print("You have cooked the " + ChatColor.AQUA + recipe.getName() + ChatColor.YELLOW + " recipe.");
                            block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F, false);
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
    }

    public boolean isItemSpoon(int id) {

        return id == 256 || id == 269 || id == 273 || id == 277 || id == 284;
    }

    public double getSpoonChance(ItemStack item, double chance) {

        int id = item.getTypeId();
        double temp = chance / 100;
        if (temp > 1) return 1;
        double toGo = temp = 1 - temp;
        double tenth = toGo / 10;
        int multiplier = 0;
        switch(id) {
            case 269:
                multiplier = 1;
                break;
            case 273:
                multiplier = 2;
                break;
            case 256:
                multiplier = 3;
                break;
            case 277:
                multiplier = 4;
                break;
            case 284:
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
    private void cook(ImprovedCauldronCookbook.Recipe recipe, Collection<Item> items) {
        // first lets destroy all items inside the cauldron
        for (Item item : items)
            item.remove();
        // then give out the result items
        for (CauldronItemStack stack : recipe.getResults()) {
            block.getWorld().dropItemNaturally(block.getLocation(), stack.getItemStack());
        }
    }

    private Collection<Item> getItems() {

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