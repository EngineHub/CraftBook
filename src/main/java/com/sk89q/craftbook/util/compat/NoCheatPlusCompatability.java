package com.sk89q.craftbook.util.compat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class NoCheatPlusCompatability extends CraftBookCompatability {

    Map<String, EnumSet<CheckType>> disabledChecks = new HashMap<String, EnumSet<CheckType>>();
    CheckType[] toDisable = new CheckType[]{CheckType.BLOCKBREAK_NOSWING};

    @Override
    public void enable (Player player) {
        EnumSet<CheckType> dis;
        if(disabledChecks.containsKey(player.getName()))
            dis = disabledChecks.get(player.getName());
        else
            dis = EnumSet.noneOf(CheckType.class);
        for(CheckType to : toDisable) {
            if(!NCPExemptionManager.isExempted(player, to)) {
                NCPExemptionManager.exemptPermanently(player, to);
                dis.add(to);
            }
        }
        if(dis.size() > 0)
            disabledChecks.put(player.getName(), dis);
    }

    @Override
    public void disable (Player player) {
        if(disabledChecks.containsKey(player.getName())) {
            for(CheckType type : disabledChecks.get(player.getName()))
                NCPExemptionManager.unexempt(player, type);
            disabledChecks.remove(player.getName());
        }
    }
}