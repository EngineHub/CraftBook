package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ManualIC;

public class Payment extends ManualIC {

    public Payment(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Payment";
    }

    @Override
    public String getSignTitle() {
        return "PAYMENT";
    }

    @Override
    public void click(ChipState chip, Player player) {
        if(getSign().getLine(2).length()>0 && getSign().getLine(3).length()>0) {
            chip.setOutput(0, takePayment(player));
        }
    }

    public boolean takePayment(Player p) {
        double money = Double.parseDouble(getSign().getLine(2));
        String toPlayer = getSign().getLine(3);
        if(CircuitsPlugin.economy.withdrawPlayer(p.getName(), money).transactionSuccess()) {
            CircuitsPlugin.economy.depositPlayer(toPlayer, money);
            return true;
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new Payment(getServer(), sign);
        }
    }

    @Override
    public void trigger(ChipState chip) {
    }
}
