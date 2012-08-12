package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EnumArt;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class PaintingSwitch implements Listener {

    MechanismsPlugin plugin;

    public PaintingSwitch(MechanismsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        if(!plugin.getLocalConfiguration().paintingSettings.enabled) return;
        if(!event.getPlayer().hasPermission("craftbook.mech.paintings.switch")) return;
        boolean isForwards = false;
        if(event.getNewSlot() > event.getPreviousSlot()) isForwards = true;
        else if(event.getNewSlot() < event.getPreviousSlot()) isForwards = false;
        else return;

        try {
            if(event.getPlayer().getItemInHand().getTypeId() != ItemType.PAINTING.getID()) return;
            BukkitPlayer p = plugin.worldEdit.wrapPlayer(event.getPlayer());
            Location loc = BukkitUtil.toLocation(p.getBlockTrace(8));
            Location ploc = event.getPlayer().getLocation();
            CraftWorld cWorld = (CraftWorld)event.getPlayer().getWorld();
            double x1 = loc.getX() + 0.2D;
            double y1 = loc.getY() + 0.2D;
            double z1 = loc.getZ() + 0.2D;
            double x2 = ploc.getX();
            double y2 = loc.getY() + 0.3D;
            double z2 = ploc.getZ();

            AxisAlignedBB bb = AxisAlignedBB.a(Math.min(x1, x2), y1, Math.min(z1, z2), Math.max(x1, x2), y2, Math.max(z1, z2));

            List<?> entities = cWorld.getHandle().getEntities(((CraftPlayer)event.getPlayer()).getHandle(), bb);
            if (entities.size() == 1 && entities.get(0) instanceof EntityPainting)
            {
                EntityPainting oldPainting = (EntityPainting)entities.get(0);
                EntityPainting newPainting = new EntityPainting(cWorld.getHandle(), oldPainting.x, oldPainting.y, oldPainting.z, oldPainting.direction % 4);

                newPainting.art = oldPainting.art;
                oldPainting.dead = true;

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PaintingRespawner(newPainting, isForwards), 1L);
            }
        }
        catch(Exception e) {
            return;
        }
    }

    public class PaintingRespawner implements Runnable
    {
        private EntityPainting painting;
        private Boolean reverse;

        public PaintingRespawner(EntityPainting painting, Boolean reverse)
        {
            this.painting = painting;
            this.reverse = reverse;
        }

        @Override
        public void run()
        {
            ArrayList<EnumArt> possiblePaintings = new ArrayList<EnumArt>();

            EnumArt oldArt = painting.art;

            for (EnumArt localEnumArt : EnumArt.values()) {
                painting.art = localEnumArt;
                painting.setDirection(painting.direction);
                if (painting.survives()) {
                    possiblePaintings.add(localEnumArt);
                }
            }

            if (possiblePaintings.size() != 0)
            {
                painting.art = possiblePaintings.get((possiblePaintings.indexOf(oldArt) + (reverse.booleanValue() ? -1 : 1) + possiblePaintings.size()) % possiblePaintings.size());
                painting.setDirection(painting.direction);
                painting.world.addEntity(painting);
            }
        }
    }
}
