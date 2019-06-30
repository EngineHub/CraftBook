package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.mechanics.crafting.CraftingItemStack;
import com.sk89q.craftbook.mechanics.crafting.RecipeManager;
import com.sk89q.craftbook.mechanics.crafting.RecipeManager.Recipe;
import com.sk89q.craftbook.mechanics.crafting.RecipeManager.RecipeType;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.util.LogListBlock;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

/**
 * Writes reports.
 * 
 * Based off of WorldGuard.
 */
public class ReportWriter {

    private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd kk:mm Z");

    private Date date = new Date();
    private StringBuilder output = new StringBuilder();

    private String flags = "";

    private CraftBookPlugin plugin;

    public ReportWriter(CraftBookPlugin plugin) {
        this.plugin = plugin;
    }

    public void generate() {
        appendReportHeader(plugin);
        appendServerInformation(plugin.getServer());
        appendPluginInformation(plugin.getServer().getPluginManager().getPlugins());
        appendCraftBookInformation(plugin);
        appendCustomCraftingInformation(plugin);
        appendGlobalConfiguration(plugin.getConfiguration());
        appendMechanicConfiguration(plugin.getMechanics());
        appendln("-------------");
        appendln("END OF REPORT");
        appendln();
    }

    protected static String repeat(String str, int n) {
        if(str == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(str);
        }

        return sb.toString();
    }

    protected void append(LogListBlock log) {
        output.append(log.toString());
    }

    protected void appendln(String text) {
        output.append(text);
        output.append("\r\n");
    }

    protected void appendln(String text, Object ... args) {
        output.append(String.format(text, args));
        output.append("\r\n");
    }

    protected void appendln() {
        output.append("\r\n");
    }

    protected void appendHeader(String text) {
        String rule = repeat("-", text.length());
        output.append(rule);
        output.append("\r\n");
        appendln(text);
        output.append(rule);
        output.append("\r\n");
        appendln();
    }

    private void appendReportHeader(CraftBookPlugin plugin) {
        appendln("CraftBook Configuration Report");
        appendln("Generated " + dateFmt.format(date));
        appendln();
        appendln("Version: " + plugin.getDescription().getVersion());
        appendln();
    }

    private void appendGlobalConfiguration(BukkitConfiguration config) {
        appendHeader("Global Configuration");

        LogListBlock log = new LogListBlock();
        LogListBlock configLog = log.putChild("Configuration");

        Class<? extends BukkitConfiguration> cls = config.getClass();
        for (Field field : cls.getFields()) {
            try {
                if (field.getName().equalsIgnoreCase("config")) continue;
                if (field.getName().equalsIgnoreCase("plugin")) continue;
                Object val = field.get(config);
                configLog.put(field.getName(), val);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ignore) {
            }
        }
        append(log);
        appendln();
    }

    private void appendMechanicConfiguration(List<CraftBookMechanic> mechanics) {
        appendHeader("Mechanic Configurations");

        LogListBlock log = new LogListBlock();
        for (CraftBookMechanic mechanic : mechanics) {
            LogListBlock configLog = log.putChild(mechanic.getClass().getSimpleName());

            Class<? extends CraftBookMechanic> cls = mechanic.getClass();
            for (Field field : cls.getDeclaredFields()) {
                try {
                    if (field.getName().equalsIgnoreCase("instance") || field.getName().equalsIgnoreCase("queue"))
                        continue;
                    field.setAccessible(true); // Some of these will be private
                    Object val = field.get(mechanic);
                    configLog.put(field.getName(), val);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException ignore) {
                }
            }
        }
        append(log);
        appendln();
    }

    private void appendServerInformation(Server server) {
        appendHeader("Server Information");

        LogListBlock log = new LogListBlock();

        Runtime runtime = Runtime.getRuntime();

        log.put("Java", "%s %s (%s)",
                System.getProperty("java.vendor"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor.url"));
        log.put("Operating system", "%s %s (%s)",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        log.put("Available processors", runtime.availableProcessors());
        log.put("Free memory", runtime.freeMemory() / 1024 / 1024 + " MB");
        log.put("Max memory", runtime.maxMemory() / 1024 / 1024 + " MB");
        log.put("Total memory", runtime.totalMemory() / 1024 / 1024 + " MB");
        log.put("Implementation", server.getVersion());
        //log.put("Address", server.getIp(), server.getPort());
        log.put("Player count", "%d/%d",
                server.getOnlinePlayers().size(), server.getMaxPlayers());

        append(log);
        appendln();
    }

    private void appendCraftBookInformation(CraftBookPlugin plugin) {
        appendHeader("CraftBook Information");

        LogListBlock log = new LogListBlock();

        int i = CraftBookPlugin.inst().getMechanics().size();
        log.put("Mechanics Loaded", "%d", i);
        log.put("ST Mechanics Loaded", "%d", plugin.getSelfTriggerManager() == null ? 0 : plugin.getSelfTriggerManager().getSelfTriggeringMechanics().size());

        append(log);
        appendln();

        appendHeader("Loaded Mechanics");

        log = new LogListBlock();

        for(CraftBookMechanic mech : CraftBookPlugin.inst().getMechanics())
            log.put(mech.getClass().getSimpleName(), mech.getClass().getPackage().toString());

        append(log);
        appendln();

        if(flags.contains("i")) {

            appendHeader("Loaded Self Triggering ICs");

            log = new LogListBlock();
            for(Entry<Location, IC> mech : ICManager.getCachedICs().entrySet()) {
                log.put(mech.getKey().toString(), "%s", mech.getValue().getSign().toString());
            }
            append(log);
            appendln();
        }
    }

    private void appendCustomCraftingInformation(CraftBookPlugin plugin) {
        appendHeader("Custom Crafting");

        LogListBlock log = new LogListBlock();

        if(RecipeManager.INSTANCE == null) {
            log.put("CustomCrafting is disabled!","");
            append(log);
            appendln();
            return;
        }

        for(Recipe rec : RecipeManager.INSTANCE.getRecipes()) {

            log.put("Recipe ID", "%s", rec.getId());
            log.put("Recipe Type", "%s", rec.getType().name());
            if(rec.getType() == RecipeType.SHAPED) {
                log.put("Recipe Shape", Arrays.toString(rec.getShape()));
                for(Entry<CraftingItemStack, Character> bits : rec.getShapedIngredients().entrySet()) {
                    log.put("Ingredient", "%s %c", bits.getKey().toString(), bits.getValue());
                }
            } else {
                for(CraftingItemStack bits : rec.getIngredients())
                    log.put("Ingredient", "%s", bits.toString());
            }
            log.put("Result", "%s", rec.getResult().toString());
            log.put("Advanced-Data", rec.getAdvancedDataMap());
        }

        append(log);
        appendln();
    }

    private void appendPluginInformation(Plugin[] plugins) {
        appendHeader("Plugins (" + plugins.length + ")");

        LogListBlock log = new LogListBlock();

        for (Plugin plugin : plugins) {
            log.put(plugin.getDescription().getName(), plugin.getDescription().getVersion());
        }

        append(log);
        appendln();

        /*appendHeader("Plugin Information");

            log = new LogListBlock();

            for (Plugin plugin : plugins) {
                log.putChild(plugin.getDescription().getName())
                    .put("Data folder", plugin.getDataFolder())
                    .put("Website", plugin.getDescription().getWebsite())
                    .put("Entry point", plugin.getDescription().getMain());
            }

            append(log);
            appendln();*/
    }

    public void write(File file) throws IOException {
        OutputStreamWriter writer = null;
        BufferedWriter out;

        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            out = new BufferedWriter(writer);
            out.write(output.toString());
            out.close();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Override
    public String toString() {
        return output.toString();
    }

    public String getFlags () {
        return flags;
    }

    public void appendFlags (String flags) {
        this.flags = this.flags + flags;
    }
}
