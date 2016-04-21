package com.sk89q.craftbook.mechanics;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.util.yaml.YAMLProcessor;

public class XPStorer extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if(block.getType() != Material.AIR && event.getAction() == Action.RIGHT_CLICK_AIR) return;
        else if(block.getType() != Material.AIR)
            if(!block.isSame(event.getClickedBlock())) return;

        if (!EventUtil.passesFilter(event) || event.getHand() != EquipmentSlot.HAND) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!sneakingState.doesPass(player.isSneaking()) || event.getPlayer().getLevel() < 1)
            return;

        int max = Integer.MAX_VALUE;

        if(requireBottle) {
            if(player.getHeldItemInfo().getType() != Material.GLASS_BOTTLE && block.getType() != Material.AIR) {
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

        int bottleCount = (int) Math.min(max, Math.floor(xp / xpPerBottle));

        CraftBookPlugin.logDebugMessage("Bottles: " + bottleCount, "xpstorer");

        event.getPlayer().getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleCount));
        if(event.getClickedBlock() == null)
            for(ItemStack leftOver : event.getPlayer().getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, bottleCount)).values())
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), leftOver);
        else
            event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(Material.EXP_BOTTLE, bottleCount));

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

    boolean requireBottle;
    int xpPerBottle;
    ItemInfo block;
    TernaryState sneakingState;

    @Override
    public void loadConfiguration(YAMLProcessor config, String path) {

        config.setComment(path + "require-bottle", "Requires the player to be holding a glass bottle to use.");
        requireBottle = config.getBoolean(path + "require-bottle", false);

        config.setComment(path + "xp-per-bottle", "Sets the amount of XP points required per each bottle.");
        xpPerBottle = config.getInt(path + "xp-per-bottle", 16);

        config.setComment(path + "block", "The block that is an XP Storer.");
        block = new ItemInfo(config.getString(path + "block", "MOB_SPAWNER"));

        config.setComment(path + "require-sneaking-state", "Sets how the player must be sneaking in order to use the XP Storer.");
        sneakingState = TernaryState.getFromString(config.getString(path + "require-sneaking-state", "no"));
    }
}