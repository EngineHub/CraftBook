package com.sk89q.craftbook.mechanics;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;

public class XPStorer extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if(CraftBookPlugin.inst().getConfiguration().xpStorerBlock.getType() != Material.AIR && event.getAction() == Action.RIGHT_CLICK_AIR) return;
        else if(CraftBookPlugin.inst().getConfiguration().xpStorerBlock.getType() != Material.AIR)
            if(!CraftBookPlugin.inst().getConfiguration().xpStorerBlock.isSame(event.getClickedBlock())) return;

        if (!EventUtil.passesFilter(event)) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!CraftBookPlugin.inst().getConfiguration().xpStorerSneaking.doesPass(player.isSneaking()) || event.getPlayer().getLevel() < 1)
            return;

        int max = Integer.MAX_VALUE;

        if(CraftBookPlugin.inst().getConfiguration().xpStorerRequireBottle) {
            if(player.getHeldItemInfo().getType() != Material.GLASS_BOTTLE && CraftBookPlugin.inst().getConfiguration().xpStorerBlock.getType() != Material.AIR) {
                player.printError("mech.xp-storer.bottle");
                return;
            }

            max = event.getPlayer().getItemInHand().getAmount();
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

        event.getPlayer().setExp(0);
        xp += (int)(event.getPlayer().getExpToLevel()*pcnt);

        while (event.getPlayer().getLevel() > 0) {
            event.getPlayer().setLevel(event.getPlayer().getLevel() - 1);
            xp += event.getPlayer().getExpToLevel();
        }

        event.getPlayer().setLevel(level);
        event.getPlayer().setExp(pcnt);

        if (xp < CraftBookPlugin.inst().getConfiguration().xpStorerPerBottle) {
            player.print("mech.xp-storer.not-enough-xp");
            return;
        }

        int bottleCount = (int) Math.min(max, Math.floor(xp / CraftBookPlugin.inst().getConfiguration().xpStorerPerBottle));

        event.getPlayer().getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleCount));
        if(event.getClickedBlock() == null)
            for(ItemStack leftOver : event.getPlayer().getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, bottleCount)).values())
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), leftOver);
        else
            event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(Material.EXP_BOTTLE, bottleCount));

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        float levelPercentage = 0;

        int remainingXP = xp - bottleCount*CraftBookPlugin.inst().getConfiguration().xpStorerPerBottle;

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
}