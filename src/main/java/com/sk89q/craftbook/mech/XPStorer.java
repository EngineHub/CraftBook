package com.sk89q.craftbook.mech;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class XPStorer extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<XPStorer> {

        @Override
        public XPStorer detect(BlockWorldVector pt, LocalPlayer player) {

            if (BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt)) == CraftBookPlugin.inst().getConfiguration().xpStorerBlock && player.hasPermission("craftbook.mech.xpstore.use")) return new XPStorer();
            else return null;
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getPlayer().isSneaking() || event.getPlayer().getLevel() < 1)
            return;

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

        event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(ItemID.BOTTLE_O_ENCHANTING, xp / 16));

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        event.setCancelled(true);
    }
}