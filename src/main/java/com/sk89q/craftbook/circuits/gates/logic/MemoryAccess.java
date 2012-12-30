package com.sk89q.craftbook.circuits.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.GeneralUtil;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MemoryAccess extends AbstractIC {

    public MemoryAccess(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "ROM Accessor";
    }

    @Override
    public String getSignTitle() {

        return "ROM";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            readMemory(chip);
        }
    }

    public boolean readMemory(ChipState chip) {

        try {
            File f = new File(((CircuitCore) CircuitCore.inst()).getRomFolder(), getSign().getLine(2) + ".dat");
            if (!f.exists()) {
                f.createNewFile();
                return false;
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            for (int i = 0; i < chip.getOutputCount(); i++) {
                chip.setOutput(i, line.charAt(i) == '1');
            }
            br.close();
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

            return new MemoryAccess(getServer(), sign, this);
        }
    }
}
