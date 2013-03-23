package com.sk89q.craftbook.mech;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class SignCopier extends AbstractMechanic {

    public static final HashMap<String, String[]> signs = new HashMap<String, String[]>();

    public SignCopier() {

        super();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if(event.getClickedBlock().getState() instanceof Sign) {

            signs.put(player.getName(), ((Sign) event.getClickedBlock().getState()).getLines());
            player.print("mech.signcopy.copy");
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        if(event.getClickedBlock().getState() instanceof Sign && signs.containsKey(player.getName())) {

            Sign s = (Sign) event.getClickedBlock().getState();

            SignChangeEvent sev = new SignChangeEvent(event.getClickedBlock(), event.getPlayer(), signs.get(player.getName()));
            Bukkit.getPluginManager().callEvent(sev);

            if(!sev.isCancelled()) {
                for(int i = 0; i < signs.get(player.getName()).length; i++) {
                    s.setLine(i, signs.get(player.getName())[i]);
                }

                s.update();
            }

            player.print("mech.signcopy.paste");
        }
    }

    public static class Factory extends AbstractMechanicFactory<SignCopier> {

        public Factory() {

        }

        @Override
        public SignCopier detect (BlockWorldVector pt, LocalPlayer player) throws InvalidMechanismException {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (player.hasPermission("craftbook.mech.signcopy.use") && block != null && player.getHeldItemType() == CraftBookPlugin.inst().getConfiguration().signCopyItem && player.getHeldItemData() == 0 && block.getState() instanceof Sign) return new SignCopier();

            return null;
        }
    }
}