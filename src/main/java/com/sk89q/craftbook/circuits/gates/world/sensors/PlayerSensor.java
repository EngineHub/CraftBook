package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.PlayerType;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Me4502
 */
public class PlayerSensor extends AbstractSelfTriggeredIC {

    public PlayerSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Player Detection";
    }

    @Override
    public String getSignTitle() {

        return "P-DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, invertOutput ? !isDetected() : isDetected());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    SearchArea area;

    PlayerType type;
    String nameLine;
    boolean invertOutput = false;

    @Override
    public void load() {

        if (getLine(3).contains(":"))
            type = PlayerType.getFromChar(getLine(3).replace("!", "").trim().toCharArray()[0]);
        else
            type = PlayerType.NAME;

        invertOutput = getLine(3).contains("!");

        nameLine = getLine(3).replace("g:", "").replace("p:", "").replace("n:", "").replace("t:", "").replace("a:", "").replace("!", "").trim();

        area = SearchArea.createArea(BukkitUtil.toSign(getSign()).getBlock(), getLine(2));
    }

    protected boolean isDetected() {

        if (!nameLine.isEmpty() && type == PlayerType.NAME) {
            Player p = Bukkit.getPlayerExact(nameLine);
            if (p != null && area.isWithinArea(p.getLocation())) return true;
        }

        for (Player p : area.getPlayersInArea()) {

            if (p == null || !p.isValid())
                continue;

            if (nameLine.isEmpty())
                return true;
            else if (type.doesPlayerPass(p, nameLine))
                return true;
        }

        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if(!SearchArea.createArea(BukkitUtil.toSign(sign).getBlock(), sign.getLine(2)).isValid())
                throw new ICVerificationException("Invalid SearchArea on line 3!");
        }

        @Override
        public String getShortDescription() {

            return "Detects players within a radius.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {
                    "SearchArea",
                    "PlayerType"
            };
        }
    }
}