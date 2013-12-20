package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class XPStorer extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!CraftBookPlugin.inst().getConfiguration().xpStorerBlock.isSame(event.getClickedBlock())) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (player.isSneaking() || event.getPlayer().getLevel() < 1)
            return;

        if(!player.hasPermission("craftbook.mech.xpstore.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        int xp = 0;

        float pcnt = event.getPlayer().getExp();
        event.getPlayer().setExp(0);
        xp += (int)(event.getPlayer().getExpToLevel()*pcnt);

        while (event.getPlayer().getLevel() > 0) {
            event.getPlayer().setLevel(event.getPlayer().getLevel() - 1);
            xp += event.getPlayer().getExpToLevel();
        }

        if (xp < 16) {
            event.getPlayer().giveExp(xp);
            return;
        }

        event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(Material.EXP_BOTTLE, xp / 16));

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        event.setCancelled(true);
    }
}