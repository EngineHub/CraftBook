package com.sk89q.craftbook.mech.cauldron;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author Silthus
 */
public class ImprovedCauldron extends AbstractMechanic implements Listener {

    public static class Factory extends AbstractMechanicFactory<ImprovedCauldron> {

        protected final MechanismsPlugin plugin;
        protected final ImprovedCauldronCookbook recipes;

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
            recipes = new ImprovedCauldronCookbook(
                    YamlConfiguration.loadConfiguration(
                            new File(plugin.getDataFolder(), "cauldron-recipes.yml")
                    ), plugin.getDataFolder());
        }

        @Override
        public ImprovedCauldron detect(BlockWorldVector pos) throws InvalidMechanismException {

            if (isCauldron(pos)) {
                return new ImprovedCauldron(plugin, BukkitUtil.toBlock(pos), recipes);
            }
            return null;
        }

        private boolean isCauldron(BlockWorldVector pos) {

            Block block = BukkitUtil.toBlock(pos);
            if (block.getType() == Material.CAULDRON) {
                Cauldron cauldron = (Cauldron) block.getState().getData();
                return block.getRelative(BlockFace.DOWN).getType() == Material.FIRE && cauldron.isFull();
            }
            return false;
        }

    }

    private MechanismsPlugin plugin;
    private Block block;
    private ImprovedCauldronCookbook cookbook;

    private ImprovedCauldron(MechanismsPlugin plugin, Block block, ImprovedCauldronCookbook recipes) {

        super();
        this.plugin = plugin;
        this.block = block;
        cookbook = recipes;
    }

    /*TODO@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
	if(block == null) return;
	if(event.getItem().getLocation().getBlock().getLocation().distance(block.getLocation()) < 1) {
	    if(!(event.getPlayer().getLocation().getBlock().getLocation().distance(block.getLocation()) < 1)) {
		event.setCancelled(true);
	    }
	}
    }*/

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().cauldronSettings.enableNew) return;
        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (block.equals(event.getClickedBlock())) {
            if (!player.hasPermission("craftbook.mech.cauldron.use")) {
                player.printError("mech.use-permission");
                return;
            }
            try {
                Collection<Item> items = getItems();
                ImprovedCauldronCookbook.Recipe recipe = cookbook.getRecipe(CauldronItemStack.convert(items));
                if (!plugin.getLocalConfiguration().cauldronSettings.newSpoons) {
                    cook(recipe, items);
                    event.getPlayer().sendMessage(
                            ChatColor.YELLOW + "You have cooked the " + ChatColor.AQUA + recipe.getName() + ChatColor
                                    .YELLOW + " recipe.");
                    block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                    event.setCancelled(true);
                } else { //Spoons
                    if (event.getPlayer().getItemInHand() == null) return;
                    if (isItemSpoon(event.getPlayer().getItemInHand().getTypeId())) {
                        double chance = getSpoonChance(event.getPlayer().getItemInHand(), recipe.getChance());
                        Random r = new Random();
                        double ran = r.nextDouble();
                        event.getPlayer().getItemInHand().setDurability((short) (event.getPlayer().getItemInHand()
                                .getDurability() - (short) 1));
                        if (chance <= ran) {
                            cook(recipe, items);
                            event.getPlayer().sendMessage(
                                    ChatColor.YELLOW + "You have cooked the " + ChatColor.AQUA + recipe.getName() +
                                            ChatColor.YELLOW + " recipe.");
                            block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F,
                                    false);
                            event.setCancelled(true);
                        } else {
                            player.print("mech.cauldron.stir");
                        }
                    }
                }
            } catch (UnknownRecipeException e) {
                event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
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
        int mutliplier = 0;
        if (id == 269) mutliplier = 1;
        if (id == 273) mutliplier = 2;
        if (id == 256) mutliplier = 3;
        if (id == 277) mutliplier = 4;
        if (id == 284) mutliplier = 5;
        mutliplier += item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        return temp + tenth * mutliplier;
    }

    /**
     * When this is called we know that all ingredients match.
     * This means we destroy all items inside the cauldron and spawn the result of the recipe.
     *
     * @param recipe
     * @param items
     */
    private void cook(ImprovedCauldronCookbook.Recipe recipe, Collection<Item> items) {
        // first lets destroy all items inside the cauldron
        for (Item item : items) {
            item.remove();
        }
        // then give out the result items
        for (CauldronItemStack stack : recipe.getResults()) {
	        // here we need to reset the data value to 0 or problems will occur later on
	        // when trying to remove items from the inventory for example
	        if (stack.getData() < 0) {
		        stack.setData((short) 0);
	        }
	        block.getWorld().dropItemNaturally(block.getLocation(), stack.getItemStack());
        }
    }

    private Collection<Item> getItems() {

        List<Item> items = new ArrayList<Item>();
        for (Entity entity : block.getChunk().getEntities()) {
            if (entity instanceof Item) {
                Location location = entity.getLocation();
                if (location.getBlockX() == block.getX()
                        && location.getBlockY() == block.getY()
                        && location.getBlockZ() == block.getZ()) {
                    items.add((Item) entity);
                }
            }
        }
        return items;
    }

    @Override
    public void unload() {
        // do nothing
    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {
        // do nothing
    }

    @Override
    public boolean isActive() {

        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        // do nothing
    }
}
