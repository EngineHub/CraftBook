package com.sk89q.craftbook.circuits.gates.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;

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
            File f = new File(ICManager.inst().getRomFolder(), getSign().getLine(2) + ".dat");
            if (!f.exists()) {
                f.createNewFile();
                return false;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line = br.readLine();
            for (int i = 0; i < chip.getOutputCount(); i++) {
                if(line == null || line.length() < i+1)
                    chip.setOutput(i, false);
                else
                    chip.setOutput(i, line.charAt(i) == '1');
            }
            br.close();
        } catch (Exception e) {
            BukkitUtil.printStacktrace(e);
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
