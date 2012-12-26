package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.GeneralUtil;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.io.File;
import java.io.PrintWriter;

public class MemorySetter extends AbstractIC {

    public MemorySetter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Memory Setter";
    }

    @Override
    public String getSignTitle() {

        return "MEMORY SET";
    }

    @Override
    public void trigger(ChipState chip) {

        setMemory(chip);
    }

    File f;

    @Override
    public void load() {

        f = new File(CircuitsPlugin.getInst().romFolder, getSign().getLine(2) + ".dat");
    }

    public boolean setMemory(ChipState chip) {

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (int i = 0; i < chip.getInputCount(); i++)
                pw.print(chip.getInput(i) ? "1" : "0");
            pw.close();
        } catch (Exception e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MemorySetter(getServer(), sign, this);
        }
    }
}