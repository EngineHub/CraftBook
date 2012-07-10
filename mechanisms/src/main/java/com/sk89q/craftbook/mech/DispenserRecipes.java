package com.sk89q.craftbook.mech;

import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class DispenserRecipes implements Listener{

    MechanismsPlugin plugin;

    int[] XPShooter = new int[]{0, 331, 0, 331, 374, 331, 0, 331, 0};

    public DispenserRecipes(MechanismsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if(event.getBlock().getState() instanceof Dispenser && plugin.getLocalConfiguration().dispenserSettings.enable) {
            Dispenser dis = (Dispenser)event.getBlock().getState();
            if(dispenseNew(dis,event.getItem(),event.getVelocity(), event)) {
                event.setCancelled(true);
            }
        }
    }

    public boolean dispenseNew(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        ItemStack[] stacks = dis.getInventory().getContents();
        XPShoot: {
            if(XPShooter[0] == stacks[0].getTypeId()) {
                for(int i = 1; i < stacks.length; i++)
                {
                    if(XPShooter[i] == stacks[i].getTypeId())
                        continue;
                    else
                        break XPShoot; //This recipe is invalid.
                }
                event.setItem(new ItemStack(384,1));
                for(int i = 1; i < stacks.length; i++)
                {
                    if(XPShooter[i] == stacks[i].getTypeId()) {
                        if(stacks[i].getAmount() == 1)
                            stacks[i].setTypeId(0);
                        else
                            stacks[i].setAmount(stacks[i].getAmount() - 1);
                    } else
                        return true; //Cancel the event, as obviously something went wrong.
                }
                dis.getInventory().setContents(stacks);
            }
            break XPShoot;
        }
        return false; //Leave it be.
    }
}