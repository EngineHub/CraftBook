package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import com.sk89q.craftbook.util.InventoryUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

public class PlayerInventorySensor extends AbstractSelfTriggeredIC {

    public PlayerInventorySensor (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think (ChipState chip) {

        chip.setOutput(0, isDetected());
    }

    @Override
    public String getTitle () {
        return "Player Inventory Sensor";
    }

    @Override
    public String getSignTitle () {
        return "PLAYER INV SENSOR";
    }

    @Override
    public void trigger (ChipState chip) {
        if (chip.getInput(0))
            chip.setOutput(0, isDetected());
    }

    SearchArea area;
    ItemStack item;
    int minPlayers;
    int slot;
    boolean inHand;

    @Override
    public void load() {

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));

        String[] parts = RegexUtil.EQUALS_PATTERN.split(getLine(3));
        item = ItemUtil.makeItemValid(ItemSyntax.getItem(parts[0]));
        if(parts.length > 1) {

            String[] data = RegexUtil.COLON_PATTERN.split(parts[1]);
            try {
                minPlayers = Integer.parseInt(data[0]);
                inHand = Boolean.parseBoolean(data[1]);
                try {
                    slot = Integer.parseInt(data[2]);
                } catch(Exception e){
                    slot = -1;
                }
            } catch(Exception e){
                if(minPlayers <= 0)
                    minPlayers = 1;
                inHand = false;
            }
        } else {
            minPlayers = 1;
            inHand = false;
            slot = -1;
        }
    }

    public boolean isDetected() {

        int players = 0;

        for (Player e : area.getPlayersInArea()) {
            if (e == null || !e.isValid())
                continue;

            if(testPlayer(e))
                players += 1;

            if(players >= minPlayers)
                return true;
        }

        return false;
    }

    public boolean testPlayer(Player e) {

        if(slot == -1 && !inHand)
            return InventoryUtil.doesInventoryContain(e.getInventory(), false, item);
        else if (inHand) { //Eclipse messes with indentation without these {'s
            return (e.getInventory().getItemInMainHand() != null && ItemUtil.areItemsIdentical(e.getInventory().getItemInMainHand(), item) && e.getInventory().getItemInMainHand().getAmount() >= item.getAmount())
                    || (e.getInventory().getItemInOffHand() != null && ItemUtil.areItemsIdentical(e.getInventory().getItemInOffHand(), item) && e.getInventory().getItemInOffHand().getAmount() >= item.getAmount());
        }
        else if (slot > -1) {
            return e.getInventory().getItem(slot) != null && ItemUtil.areItemsIdentical(e.getInventory().getItem(slot), item) && e.getInventory().getItem(slot).getAmount() >= item.getAmount();
        }

        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerInventorySensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Detects if a certain number of players have an item in their inventory.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {
                    "+oSearchArea",
                    "item=minPlayers:inHand:slot"
            };
        }
    }
}