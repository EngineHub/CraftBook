package com.sk89q.craftbook.gates.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;

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
            File f = new File("plugins/CraftBookCircuits/ROM/", getSign().getLine(2));
            if (!f.exists()) {
                f.createNewFile();
                return false;
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int linenum = 0;
            while ((line = br.readLine()) != null || linenum > 2) {
                chip.setOutput(linenum, line.equalsIgnoreCase("1"));
                linenum++;
            }
            br.close();
        } catch (Exception ignored) {

        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MemoryAccess(getServer(), sign, this);
        }
    }
}
