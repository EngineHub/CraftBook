package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class XPStorer extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if(block.getBlockType() != BlockTypes.AIR && event.getAction() == Action.RIGHT_CLICK_AIR) return;
        else if(block.getBlockType() != BlockTypes.AIR)
            if(!block.equalsFuzzy(BukkitAdapter.adapt(event.getClickedBlock().getBlockData()))) return;

        if (!EventUtil.passesFilter(event) || event.getHand() != EquipmentSlot.HAND) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!sneakingState.doesPass(player.isSneaking()) || event.getPlayer().getLevel() < 1)
            return;

        int max = Integer.MAX_VALUE;

        if(requireBottle) {
            if(player.getItemInHand(HandSide.MAIN_HAND).getType() != ItemTypes.GLASS_BOTTLE && block.getBlockType() != BlockTypes.AIR) {
                player.printError("mech.xp-storer.bottle");
                return;
            }

            max = event.getPlayer().getInventory().getItemInMainHand().getAmount();
        }

        if(!player.hasPermission("craftbook.mech.xpstore.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(event.getClickedBlock() != null && !ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        int xp = 0;

        float pcnt = event.getPlayer().getExp();
        int level = event.getPlayer().getLevel();
        CraftBookPlugin.logDebugMessage("Percent: " + pcnt + ". Level: " + level, "xpstorer");

        event.getPlayer().setExp(0);
        xp += (int)(event.getPlayer().getExpToLevel()*pcnt);

        CraftBookPlugin.logDebugMessage("XP: " + xp, "xpstorer");

        while (event.getPlayer().getLevel() > 0) {
            event.getPlayer().setLevel(event.getPlayer().getLevel() - 1);
            xp += event.getPlayer().getExpToLevel();
            CraftBookPlugin.logDebugMessage("XP: " + xp + ". Level: " + event.getPlayer().getLevel(), "xpstorer");
        }

        event.getPlayer().setLevel(level);
        event.getPlayer().setExp(pcnt);

        if (xp < xpPerBottle) {
            player.print("mech.xp-storer.not-enough-xp");
            return;
        }

        int bottleCount = (int) Math.min(max, Math.floor(xp / (double) xpPerBottle));

        CraftBookPlugin.logDebugMessage("Bottles: " + bottleCount, "xpstorer");

        if(requireBottle) {
            event.getPlayer().getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleCount));
        }

        int tempBottles = bottleCount;

        while(tempBottles > 0) {
            ItemStack bottles = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.min(tempBottles, 64));
            if (event.getClickedBlock() == null)
                for (ItemStack leftOver : event.getPlayer().getInventory().addItem(bottles).values())
                    event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), leftOver);
            else
                event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), bottles);

            tempBottles -= 64;
        }

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        float levelPercentage;

        int remainingXP = xp - bottleCount*xpPerBottle;

        CraftBookPlugin.logDebugMessage("Leftover XP: " + remainingXP, "xpstorer");

        do {
            levelPercentage = (float)remainingXP / event.getPlayer().getExpToLevel();

            if(levelPercentage > 1) {
                remainingXP -= event.getPlayer().getExpToLevel();
                event.getPlayer().setLevel(event.getPlayer().getLevel() + 1);
            } else if(levelPercentage == 1) {
                event.getPlayer().setLevel(event.getPlayer().getLevel() + 1);
                event.getPlayer().setExp(0f);
                remainingXP = 0;
            } else {
                event.getPlayer().setExp(levelPercentage);
                remainingXP = 0;
            }
        } while(levelPercentage > 1);

        player.print("mech.xp-storer.success");

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!autonomousMode) return;
        if(!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[XP]") || !block.equalsFuzzy(BukkitAdapter.adapt(SignUtil.getBackBlock(event.getBlock()).getBlockData()))) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.xpstore")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        int signRadius = radius;
        try {
            signRadius = Math.max(radius, Integer.parseInt(event.getLine(2)));
        } catch (Exception e) {
        }

        event.setLine(1, "[XP]");
        event.setLine(2, String.valueOf(signRadius));
        player.print("mech.xp-storer.create");

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(SelfTriggerPingEvent event) {

        if (!autonomousMode) return;
        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getBlock()) || !block.equalsFuzzy(BukkitAdapter.adapt(SignUtil.getBackBlock(event.getBlock()).getBlockData()))) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if(!sign.getLine(1).equals("[XP]")) return;

        CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getBlock())) return;

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!sign.getLine(1).equals("[XP]")) return;

        event.setHandled(true);

        int signRadius = radius;
        try {
            signRadius = Math.max(radius, Integer.parseInt(sign.getLine(2)));
        } catch (Exception e) {
        }

        int xp = 0;

        List<ExperienceOrb> orbs = new ArrayList<>();

        for (Entity entity : LocationUtil.getNearbyEntities(SignUtil.getBackBlock(event.getBlock()).getLocation(), Vector3.at(signRadius, signRadius, signRadius))) {
            if (entity instanceof ExperienceOrb && entity.getTicksLived() > 20) {
                xp += ((ExperienceOrb) entity).getExperience();
                orbs.add((ExperienceOrb) entity);
            }
        }

        int max = Integer.MAX_VALUE;
        Inventory inventory = null;

        if (InventoryUtil.doesBlockHaveInventory(SignUtil.getBackBlock(event.getBlock()).getRelative(BlockFace.UP))) {
            inventory = ((InventoryHolder) SignUtil.getBackBlock(event.getBlock()).getRelative(BlockFace.UP).getState()).getInventory();
            if (requireBottle) {
                max = 0;
                for (ItemStack stack : inventory.getContents()) {
                    if (ItemUtil.isStackValid(stack) && stack.getType() == Material.GLASS_BOTTLE) {
                        max += stack.getAmount();
                    }
                }
            }
        } else if (requireBottle) {
            return;
        }

        int bottleCount = (int) Math.min(max, Math.floor(xp / (double) xpPerBottle));

        int tempBottles = bottleCount;

        while(tempBottles > 0) {
            ItemStack bottles = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.min(tempBottles, 64));
            if (inventory != null) {
                for (ItemStack leftover : inventory.addItem(bottles).values()) {
                    event.getBlock().getWorld().dropItemNaturally(LocationUtil.getCenterOfBlock(SignUtil.getBackBlock(event.getBlock())), leftover);
                }
            } else {
                event.getBlock().getWorld().dropItemNaturally(LocationUtil.getCenterOfBlock(SignUtil.getBackBlock(event.getBlock())), bottles);
            }

            tempBottles -= 64;
        }

        if(requireBottle && inventory != null) {
            inventory.removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleCount));
        }

        int remainingXP = xp - bottleCount * xpPerBottle;
        for (ExperienceOrb orb : orbs) {
            if (remainingXP > 0) {
                orb.setExperience(Math.min(5, remainingXP));
                remainingXP -= 5;
            } else {
                orb.remove();
            }
        }
    }

    private boolean requireBottle;
    private int xpPerBottle;
    private BlockStateHolder block;
    private TernaryState sneakingState;
    private boolean autonomousMode;
    private int radius;

    @Override
    public void loadConfiguration(YAMLProcessor config, String path) {

        config.setComment(path + "require-bottle", "Requires the player to be holding a glass bottle to use.");
        requireBottle = config.getBoolean(path + "require-bottle", false);

        config.setComment(path + "xp-per-bottle", "Sets the amount of XP points required per each bottle.");
        xpPerBottle = config.getInt(path + "xp-per-bottle", 16);

        config.setComment(path + "block", "The block that is an XP Storer.");
        block = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.SPAWNER.getId()), true);

        config.setComment(path + "require-sneaking-state", "Sets how the player must be sneaking in order to use the XP Storer.");
        sneakingState = TernaryState.getFromString(config.getString(path + "require-sneaking-state", "no"));

        config.setComment(path + "radius-mode", "Allows XP Storer mechanics with a sign attached to work in a radius.");
        autonomousMode = config.getBoolean(path + "radius-mode", false);

        config.setComment(path + "radius", "The radius for radius-mode.");
        radius = config.getInt(path + "radius", 5);
    }
}