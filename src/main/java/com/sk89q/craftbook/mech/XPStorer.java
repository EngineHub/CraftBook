package com.sk89q.craftbook.mech;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class XPStorer extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<XPStorer> {

        public Factory() {

        }

        @Override
        public XPStorer detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == CraftBookPlugin.inst().getConfiguration().xpStorerBlock) return new XPStorer(pt);

            return null;
        }
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private XPStorer(BlockWorldVector pt) {

        super();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.xpstore.use")) return;
        if (event.getPlayer().isSneaking() || event.getPlayer().getLevel() < 1) {
            return;
        }

        int xp = 0;

        event.getPlayer().setExp(0);

        while (event.getPlayer().getLevel() > 0) {
            event.getPlayer().setLevel(event.getPlayer().getLevel() - 1);
            xp += event.getPlayer().getExpToLevel();
        }

        if (xp < 16) {
            event.getPlayer().giveExp(xp);
            return;
        }

        event.getClickedBlock().getWorld()
                .dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(ItemID.BOTTLE_O_ENCHANTING,
                        xp / 16));

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        event.setCancelled(true);
    }
}