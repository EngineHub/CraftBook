package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFamily;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import com.sk89q.craftbook.circuits.ic.families.FamilyAISO;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;

public class GenerateWikiICPages extends ExternalUtilityBase {

    @Override
    public void generate () {
        try {
            File file = new File(getGenerationFolder(), "IC-Pages/");
            if(!file.exists())
                file.mkdir();

            BlockState oldState = Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).getState();
            Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).setType(Material.WALL_SIGN);

            for(RegisteredICFactory ric : ICManager.inst().getICList()) {

                PrintWriter writer = new PrintWriter(new File(file, ric.getId() + ".txt"), "UTF-8");

                IC ic = ric.getFactory().create(null);

                for(ICFamily family : ric.getFamilies()) {
                    if(family instanceof FamilyAISO) continue;
                    writer.println("{{" + family.getName() + "|id=" + ric.getId() + "|name=" + ic.getTitle() + "}}");
                }

                writer.println(ric.getFactory().getLongDescription());

                writer.println("== Sign parameters ==");
                writer.println("# " + ic.getSignTitle());
                writer.println("# [" + ric.getId() + "]");
                for(String line : ric.getFactory().getLineHelp())
                    writer.println("# " + (line == null ? "Blank" : line));

                writer.println("== Pins ==");

                writer.println("=== Input ===");
                int pins = 0;

                ChipState state = ric.getFamilies()[0].detect(BukkitUtil.toWorldVector(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0)), BukkitUtil.toChangedSign(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0)));

                for(String pin : ric.getFactory().getPinDescription(state)) {

                    if(pins == state.getInputCount())
                        writer.println("=== Output ===");

                    writer.println("# " + (pin == null ? "Nothing" : pin));

                    pins++;
                }

                writer.println();
                writer.print("[[Category:IC]]");
                for(ICFamily family : ric.getFamilies())
                    writer.print("[[Category:" + family.getName() + "]]");
                writer.close();
            }

            oldState.update(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}