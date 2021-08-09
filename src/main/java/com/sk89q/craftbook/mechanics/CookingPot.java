package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.*;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class CookingPot extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[Cook]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.cook")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[Cook]");
        event.setLine(2, "0");
        event.setLine(3, cookingPotFuel ? "0" : "1");
        player.print("mech.cook.create");

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());
    }

    private HashSet<String> cookingSet = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(SelfTriggerPingEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        if(cookingPotChunkLimit) {
            if(cookingSet.contains(event.getBlock().getChunk().getX() + ";" + event.getBlock().getChunk().getZ()))
                return;

            cookingSet.add(event.getBlock().getChunk().getX() + ";" + event.getBlock().getChunk().getZ());
        }

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler
    public void onUnregister(SelfTriggerUnregisterEvent event) {

        if(cookingPotChunkLimit)
            cookingSet.remove(event.getBlock().getChunk().getX() + ";" + event.getBlock().getChunk().getZ());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        event.setHandled(true);

        if(cookingPotHeating && sign.getLine(0).equals("HEATING")) {

            //So it's waiting.
            if(CraftBookPlugin.inst().getRandom().nextInt(200) != 0)
                return;
            sign.setLine(0, "");
        }

        int lastTick = 0, oldTick;

        try {
            lastTick = Math.max(0, Integer.parseInt(sign.getLine(2)));
        } catch (Exception e) {
            sign.setLine(2, String.valueOf(0));
        }
        oldTick = lastTick;
        Block b = SignUtil.getBackBlock(event.getBlock());
        Block cb = b.getRelative(0, 2, 0);
        if (cb.getType() == Material.CHEST) {
            Block fire = b.getRelative(0, 1, 0);
            if (Tag.FIRE.isTagged(fire.getType())) {
                Chest chest = (Chest) cb.getState();
                Inventory inventory = chest.getInventory();

                List<ItemStack> items;
                if(cookingPotOres)
                    items = ItemUtil.getRawMaterials(inventory);
                else
                    items = ItemUtil.getRawFood(inventory);

                if(items.size() == 0) {
                    if(cookingPotHeating) {
                        sign.setLine(0, "HEATING");
                        sign.update(false);
                    }
                    return;
                }

                if(lastTick < 500) {

                    int multiplier = getMultiplier(sign);

                    lastTick = cookingPotSuperFast ? lastTick + multiplier : lastTick + Math.min(multiplier, 5);
                    if(multiplier > 0)
                        setMultiplier(sign, multiplier - 1);
                }
                if (lastTick >= 50) {
                    for (ItemStack i : items) {

                        if (!ItemUtil.isStackValid(i)) continue;
                        ItemStack cooked = ItemUtil.getCookedResult(i);
                        if (cooked == null) {
                            if (cookingPotOres)
                                cooked = ItemUtil.getSmeltedResult(i);
                            else
                                continue;
                        }
                        if (inventory.addItem(cooked).isEmpty()) {
                            ItemStack toRemove = i.clone();
                            toRemove.setAmount(1);
                            inventory.removeItem(toRemove);
                            // chest.update(); Oii 
                            lastTick -= 50;
                            break;
                        }
                    }
                }
            } else
                lastTick = 0;
        }

        if(oldTick != lastTick) {
            sign.setLine(2, String.valueOf(lastTick));
            sign.update(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        ChangedSign sign = event.getSign();

        if(!sign.getLine(1).equals("[Cook]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getClickedBlock().getLocation());

        CraftBookPlayer p = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = SignUtil.getBackBlock(event.getClickedBlock());
            Block cb = b.getRelative(0, 2, 0);
            if (cb.getType() == Material.CHEST) {
                Player player = event.getPlayer();
                if(!player.hasPermission("craftbook.mech.cook.refuel")) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        p.printError("mech.restock-permission");
                    event.setCancelled(true);
                    return;
                }
                if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        p.printError("area.use-permissions");
                    return;
                }
                if (ItemUtil.isStackValid(player.getItemInHand()) && Ingredients.isIngredient(player.getItemInHand().getType())) {
                    Material itemID = player.getItemInHand().getType();
                    increaseMultiplier(sign, Ingredients.getTime(itemID));
                    if (player.getItemInHand().getAmount() <= 1) {
                        player.setItemInHand(null);
                    } else {
                        player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    }
                    if(itemID == Material.LAVA_BUCKET && !cookingPotDestroyBuckets)
                        player.getInventory().addItem(new ItemStack(Material.BUCKET, 1));
                    p.print("mech.cook.add-fuel");
                    event.setCancelled(true);
                } else if (cookingPotSignOpen) {
                    player.openInventory(((Chest) cb.getState()).getBlockInventory());
                    event.setCancelled(true);
                }
            }

            if(sign.hasChanged())
                sign.update(false);
        } else {
            event.getPlayer().setFireTicks(getMultiplier(sign)+40);
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
            player.printError("mech.cook.ouch");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDestroy(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().unregisterSelfTrigger(event.getBlock().getLocation(), UnregisterReason.BREAK);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!cookingPotAllowRedstone) return;

        if(!SignUtil.isSign(event.getBlock())) return;

        if(!EventUtil.passesFilter(event)) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[Cook]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());

        if (event.isOn() && !event.isMinor())
            increaseMultiplier(sign, event.getNewCurrent() - event.getOldCurrent());

        if(sign.hasChanged())
            sign.update(false);
    }

    public void setMultiplier(ChangedSign sign, int amount) {

        if(!cookingPotFuel)
            amount = Math.max(amount, 1);
        sign.setLine(3, String.valueOf(amount));
    }

    public void increaseMultiplier(ChangedSign sign, int amount) {

        if(sign.getLine(0).equals("HEATING"))
            sign.setLine(0, "");
        setMultiplier(sign, getMultiplier(sign) + amount);
    }

    public int getMultiplier(ChangedSign sign) {

        int multiplier;
        try {
            multiplier = Integer.parseInt(sign.getLine(3));
        } catch (Exception e) {
            multiplier = cookingPotFuel ? 0 : 1;
            setMultiplier(sign, multiplier);
        }
        if (multiplier <= 0 && !cookingPotFuel) return 1;
        return Math.max(0, multiplier);
    }

    private enum Ingredients {
        COAL(Material.COAL, 40), COALBLOCK(Material.COAL_BLOCK, 360), LAVA(Material.LAVA_BUCKET, 6000), BLAZE(Material.BLAZE_ROD, 500),
        BLAZEDUST(Material.BLAZE_POWDER, 250), SNOWBALL(Material.SNOWBALL, -40), SNOW(Material.SNOW_BLOCK, -100), ICE(Material.ICE, -1000);

        private Material id;
        private int mult;

        Ingredients(Material id, int mult) {

            this.id = id;
            this.mult = mult;
        }

        public static boolean isIngredient(Material id) {

            for (Ingredients in : values()) { if (in.id == id) return true; }
            return false;
        }

        public static int getTime(Material id) {

            for (Ingredients in : values()) { if (in.id == id) return in.mult; }
            return 0;
        }
    }

    private boolean cookingPotAllowRedstone;
    private boolean cookingPotFuel;
    private boolean cookingPotOres;
    private boolean cookingPotSignOpen;
    private boolean cookingPotDestroyBuckets;
    private boolean cookingPotSuperFast;

    private boolean cookingPotChunkLimit;
    private boolean cookingPotHeating;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-redstone", "Allows for redstone to be used as a fuel source.");
        cookingPotAllowRedstone = config.getBoolean(path + "allow-redstone", true);

        config.setComment(path + "require-fuel", "Require fuel to cook.");
        cookingPotFuel = config.getBoolean(path + "require-fuel", true);

        config.setComment(path + "cook-ores", "Allows the cooking pot to cook ores and other smeltable items.");
        cookingPotOres = config.getBoolean(path + "cook-ores", false);

        config.setComment(path + "sign-click-open", "When enabled, right clicking the [Cook] sign will open the cooking pot.");
        cookingPotSignOpen = config.getBoolean(path + "sign-click-open", true);

        config.setComment(path + "take-buckets", "When enabled, lava buckets being used as fuel will consume the bucket.");
        cookingPotDestroyBuckets = config.getBoolean(path + "take-buckets", false);

        config.setComment(path + "super-fast-cooking", "When enabled, cooking pots cook at incredibly fast speeds. Useful for semi-instant cooking systems.");
        cookingPotSuperFast = config.getBoolean(path + "super-fast-cooking", false);

        cookingPotHeating = config.getBoolean(path + "heating", false);

        cookingPotChunkLimit = config.getBoolean(path + "chunk-limit", false);
    }
}