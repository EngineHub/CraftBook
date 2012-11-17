package com.sk89q.craftbook.mech;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class XPStorer extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<XPStorer> {

        MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        @Override
        public XPStorer detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == plugin.getLocalConfiguration().xpStorerSettings.material) return new XPStorer(pt, plugin);

            return null;
        }
    }

    MechanismsPlugin plugin;

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private XPStorer(BlockWorldVector pt, MechanismsPlugin plugin) {

        super();
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.wrap(event.getPlayer()).hasPermission("craftbook.mech.xpstore.use")) return;
        if (event.getPlayer().isSneaking() || event.getPlayer().getTotalExperience() < 30 || event.getPlayer().getLevel() < 1) {
            return;
        }

        event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(ItemID.BOTTLE_O_ENCHANTING, event.getPlayer().getTotalExperience() / 30));

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        event.setCancelled(true);
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {

        return false;
    }
}