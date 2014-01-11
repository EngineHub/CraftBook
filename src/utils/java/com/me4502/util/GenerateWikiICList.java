package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFamily;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import com.sk89q.craftbook.circuits.plc.PlcFactory;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateWikiICList {

    public static void main(String[] args) {

        try {
            CraftBookPlugin plugin = new CraftBookPlugin();
            Field f = plugin.getClass().getDeclaredField("config");
            f.setAccessible(true);
            f.set(plugin, new BukkitConfiguration(new YAMLProcessor(new File("config.yml"), true, YAMLFormat.EXTENDED), Logger.getAnonymousLogger()));
            plugin.getConfiguration().ICsDisabled = new ArrayList<String>();
            CircuitCore core = new CircuitCore();

            Method meth = CircuitCore.class.getDeclaredMethod("registerICs");
            meth.setAccessible(true);
            meth.invoke(core);

            File file = new File("ICList.txt");
            if(!file.exists())
                file.createNewFile();

            PrintWriter writer = new PrintWriter(file);

            writer.println("{| class=\"wiki-table sortable\"");
            writer.println("! IC ID");
            writer.println("! Shorthand");
            writer.println("! Allows ST");
            writer.println("! Families");
            writer.println("! Name");
            writer.println("! Description");
            for(RegisteredICFactory ric : core.getICList()) {
                if(ric.getFactory() instanceof PlcFactory)
                    continue;

                boolean isSelfTriggering = false;
                if(SelfTriggeredIC.class.isAssignableFrom(ric.getFactory().getClass().getEnclosingClass()))
                    isSelfTriggering = true;

                boolean isRestricted = false;
                if(RestrictedIC.class.isAssignableFrom(ric.getFactory().getClass()))
                    isRestricted = true;

                String family = "";
                for(ICFamily fam : ric.getFamilies()) {
                    if(!family.isEmpty())
                        family = family + ",";
                    family = family + "[[../IC families/#" + fam.getClass().getSimpleName().replace("Family", "") + "|" + fam.getClass().getSimpleName().replace("Family", "") + "]]";
                }

                IC ic = ric.getFactory().create(null);

                writer.println("|-");
                writer.println("| [[../" + ric.getId() + "/]] || " + ric.getShorthand() + " || " + String.valueOf(isSelfTriggering) + " || " + family + " || " + ic.getTitle() + (isRestricted ? "<strong style=\"color: red\">*</strong>" : "") + " || " + ric.getFactory().getShortDescription());
            }

            writer.println("|}");
            writer.println("<strong style=\"color: red\">*</strong>Requires the permission '''craftbook.ic.restricted.*''' or the respective '''craftbook.ic.mc''XXXX''''' permission.");

            writer.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}