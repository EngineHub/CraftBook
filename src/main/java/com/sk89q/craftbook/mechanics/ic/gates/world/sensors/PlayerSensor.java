package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.PlayerType;
import com.sk89q.craftbook.util.SearchArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            chip.setOutput(0, invertOutput != isDetected());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, invertOutput != isDetected());
    }

    private static final Pattern NAME_STRIPPER = Pattern.compile("[gpnta!^]*:");

    private SearchArea area;

    private PlayerType type;
    private String nameLine;
    private boolean invertOutput = false;
    private boolean invertDetection = false;

    @Override
    public void load() {
        if (getLine(3).contains(":")) {
            type = PlayerType.getFromChar(getLine(3).replace("!", "").replace("^", "").trim().toCharArray()[0]);
        } else {
            type = PlayerType.NAME;
        }

        invertOutput = getLine(3).contains("!");
        invertDetection = getLine(3).contains("^");

        nameLine = NAME_STRIPPER.matcher(getLine(3)).replaceAll(Matcher.quoteReplacement("")).trim();

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));
    }

    private boolean isDetected() {
        if (!nameLine.isEmpty() && type == PlayerType.NAME && !invertDetection) {
            Player p = Bukkit.getPlayerExact(nameLine);
            return p != null && area.isWithinArea(p.getLocation());
        }

        for (Player p : area.getPlayersInArea()) {
            if (p == null || !p.isValid()) {
                continue;
            }

            if (nameLine.isEmpty()) {
                return true;
            } else if (invertDetection != type.doesPlayerPass(p, nameLine)) {
                return true;
            }
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

            if(!SearchArea.createArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)).isValid())
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