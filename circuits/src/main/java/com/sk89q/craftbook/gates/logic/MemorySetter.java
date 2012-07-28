package com.sk89q.craftbook.gates.logic;

import java.io.File;
import java.io.PrintWriter;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

public class MemorySetter extends AbstractIC {

    public MemorySetter(Server server, Sign block) {
        super(server, block);
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

    public boolean setMemory(ChipState chip) {
        try {
            File f = new File("plugins/CraftBookCircuits/ROM/", getSign().getLine(2));
            if(!f.exists()) f.createNewFile();
            PrintWriter pw = new PrintWriter(f);
            for(int i = 0; i < 2; i++)
                if(chip.getInput(i))
                    pw.println("1");
                else
                    pw.println("0");
            pw.close();
        }
        catch(Exception e) {

        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new MemorySetter(getServer(), sign);
        }
    }
}