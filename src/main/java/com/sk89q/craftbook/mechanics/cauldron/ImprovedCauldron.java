package com.sk89q.craftbook.mechanics.cauldron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[Cauldron]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.cauldron")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[Cauldron]");
        player.print("mech.cauldron.create");
    }

    private boolean isCauldron(Block block) {

        if (block.getType() == Material.CAULDRON && block.getRelative(BlockFace.DOWN).getType() == Material.FIRE) {
            if(requireSign) {
                BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                boolean found = false;
                for(BlockFace face : faces) {
                    Block sign = block.getRelative(face);
                    if(sign.getType() == Material.WALL_SIGN) {
                        ChangedSign s = BukkitUtil.toChangedSign(sign);
                        if(s.getLine(1).equals("[Cauldron]")) {
                            found = true;
                            break;
                        }
                    }
                }
                if(!found)
                    return false;
            }
            return ((Cauldron) block.getState().getData()).isFull();
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(!EventUtil.passesFilter(event)) return;

        if(!isCauldron(event.getClickedBlock())) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(performCauldron(event.getClickedBlock(), player))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstoneUpdate(SourcedBlockRedstoneEvent event) {

        if(!allowRedstone) return;

        if(!EventUtil.passesFilter(event)) return;

        if(!isCauldron(event.getBlock())) return;

        performCauldron(event.getBlock(), null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(final PlayerDropItemEvent event) {

        if(!itemTracking) return;

        if(!event.getPlayer().hasPermission("craftbook.mech.cauldron.use")) return; //If they can't use cauldrons, don't track it.
        if(!EventUtil.passesFilter(event)) return;

        new ItemTracker(event.getItemDrop()).runTaskTimer(CraftBookPlugin.inst(), 1L, 1L);
    }

    public class ItemTracker extends BukkitRunnable {

        private Location lastLocation;
        private Item item;

        public ItemTracker(Item item) {

            //Set it to some absurd value.
            lastLocation = new Location(item.getLocation().getWorld(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            this.item = item;
        }

        @Override
        public void run () {

            if(item == null) {
                cancel();
                return;
            }

            if (trackCauldronItem(item) || BukkitUtil.equals(lastLocation, item.getLocation()))
                cancel();
        }
    }

    public boolean trackCauldronItem(Item item) {

        Block cauldron = null;
        if(isCauldron(item.getLocation().getBlock()))
            cauldron = item.getLocation().getBlock();
        else if(isCauldron(item.getLocation().getBlock().getRelative(BlockFace.DOWN)))
            cauldron = item.getLocation().getBlock().getRelative(BlockFace.DOWN);
        else
            return false;

        new CauldronItemTracker(cauldron, item).runTaskTimer(CraftBookPlugin.inst(), 1L, 1L);

        return true;
    }

    public class CauldronItemTracker extends BukkitRunnable {

        private Item item;
        private Block block;

        public CauldronItemTracker(Block block, Item item) {

            this.item = item;
            this.block = block;
        }

        @Override
        public void run () {

            if(item == null) {
                cancel();
                return;
            }

            if(!isCauldron(block)) {
                cancel();
                return;
            }

            item.teleport(BlockUtil.getBlockCentre(block).add(0, 0.5, 0));
            item.setVelocity(new Vector(0,0.01,0));
        }
    }

    public boolean performCauldron(Block block, LocalPlayer player) {

        if (player != null && !player.hasPermission("craftbook.mech.cauldron.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return false;
        }
        try {
            Collection<Item> items = getItems(block);
            ImprovedCauldronCookbook.Recipe recipe = recipes.getRecipe(CauldronItemStack.convert(items));

            // lets check permissions for that recipe
            if (player != null && !player.hasPermission("craftbook.mech.cauldron.recipe.*")
                    && !player.hasPermission("craftbook.mech.cauldron.recipe." + recipe.getId())) {
                player.printError("mech.cauldron.permissions");
                return false;
            }

            if (!useSpoons || player == null && allowRedstone) {
                cook(block, recipe, items);
                if(player != null) player.print("You have cooked the " + ChatColor.AQUA + recipe.getName() + ChatColor.YELLOW + " recipe.");
                block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                return true;
            } else if(player != null) { // Spoons
                if (isItemSpoon(player.getHeldItemInfo().getType())) {
                    double chance = getSpoonChance(((BukkitPlayer) player).getPlayer().getItemInHand(), recipe.getChance());
                    double ran = CraftBookPlugin.inst().getRandom().nextDouble();
                    ((BukkitPlayer) player).getPlayer().getItemInHand().setDurability((short) (((BukkitPlayer) player).getPlayer().getItemInHand().getDurability() - (short) 1));
                    if (chance <= ran) {
                        cook(block, recipe, items);
                        player.print(player.translate("mech.cauldron.cook") + " " + ChatColor.AQUA + recipe.getName());
                        block.getWorld().createExplosion(block.getRelative(BlockFace.UP).getLocation(), 0.0F, false);
                        return true;
                    } else {
                        player.print("mech.cauldron.stir");
                    }
                }
            }
        } catch (UnknownRecipeException e) {
            if(player != null) player.printError(e.getMessage());
        }

        return false;
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

    boolean useSpoons;
    boolean allowRedstone;
    boolean itemTracking;
    boolean requireSign;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "spoons", "Require spoons to cook cauldron recipes.");
        useSpoons = config.getBoolean(path + "spoons", true);

        config.setComment(path + "enable-redstone", "Allows use of cauldrons via redstone.");
        allowRedstone = config.getBoolean(path + "enable-redstone", false);

        config.setComment(path + "item-tracking", "Tracks items and forces them to to tracked by the cauldron. Fixes mc bugs by holding item in place.");
        itemTracking = config.getBoolean(path + "item-tracking", false);

        config.setComment(path + "require-sign", "Requires a [Cauldron] sign to be on the side of a cauldron. Useful for requiring creation permissions.");
        requireSign = config.getBoolean(path + "require-sign", false);
    }
}