package com.sk89q.craftbook.mechanics.signcopier;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.CompatabilityUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;
import java.util.Map;

public class SignCopier extends AbstractCraftBookMechanic {

    public static Map<String, String[]> signs;

    @Override
    public boolean enable() {

        signs = new HashMap<>();
        return true;
    }

    @Override
    public void disable() {

        signs = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        CraftBookPlayer player = event.getWrappedPlayer();

        if (player.getItemInHand(HandSide.MAIN_HAND).getType() != item) return;

        if (!player.hasPermission("craftbook.mech.signcopy.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canBuild(event.getPlayer(), event.getClickedBlock().getLocation(), false)) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            signs.put(player.getName(), ((Sign) event.getClickedBlock().getState()).getLines());
            player.print("mech.signcopy.copy");
            event.setCancelled(true);
        } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if(signs.containsKey(player.getName())) {

                Sign s = (Sign) event.getClickedBlock().getState();
                String[] lines = signs.get(player.getName());

                CompatabilityUtil.disableInterferences(event.getPlayer());
                SignChangeEvent sev = new SignChangeEvent(event.getClickedBlock(), event.getPlayer(), lines);
                Bukkit.getPluginManager().callEvent(sev);

                if(!sev.isCancelled()) {
                    for(int i = 0; i < lines.length; i++)
                        s.setLine(i, lines[i]);
                    s.update();
                }
                CompatabilityUtil.enableInterferences(event.getPlayer());

                player.print("mech.signcopy.paste");
                event.setCancelled(true);
            }
        }
    }

    private ItemType item;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "item", "The item the Sign Copy mechanic uses.");
        item = BukkitAdapter.asItemType(ItemSyntax.getItem(config.getString(path + "item", ItemTypes.FLINT.getId())).getType());
    }
}