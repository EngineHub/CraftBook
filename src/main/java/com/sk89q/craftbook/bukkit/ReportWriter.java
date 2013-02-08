package com.sk89q.craftbook.bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.util.LogListBlock;

/**
 * Writes reports.
 * 
 * Based off of WorldGuard.
 */
public class ReportWriter {

    private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd kk:mm Z");

    private Date date = new Date();
    private StringBuilder output = new StringBuilder();

    public ReportWriter(CraftBookPlugin plugin) {
        appendReportHeader(plugin);
        appendServerInformation(plugin.getServer());
        appendPluginInformation(plugin.getServer().getPluginManager().getPlugins());
        appendCraftBookInformation(plugin);
        appendGlobalConfiguration(plugin.getConfiguration());
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

        Class<? extends LocalConfiguration> cls = config.getClass();
        for (Field field : cls.getFields()) {
            try {
                if (field.getName().equalsIgnoreCase("config")) continue;
                Object val = field.get(config);
                configLog.put(field.getName(), val);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ignore) {
            }
        }
        Class<? extends BukkitConfiguration> cls2 = config.getClass();
        for (Field field : cls2.getFields()) {
            try {
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
        log.put("Server ID", server.getServerId());
        log.put("Server name", server.getServerName());
        log.put("Implementation", server.getVersion());
        //log.put("Address", server.getIp(), server.getPort());
        log.put("Player count", "%d/%d",
                server.getOnlinePlayers().length, server.getMaxPlayers());

        append(log);
        appendln();
    }

    private void appendCraftBookInformation(CraftBookPlugin plugin) {
        appendHeader("CraftBook Information");

        LogListBlock log = new LogListBlock();

        int amount = 0;
        for(MechanicManager mech : plugin.managerAdapter.getManagers())
            amount += mech.factories.size();
        log.put("Factories Loaded:", "%d", amount);

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
        FileWriter writer = null;
        BufferedWriter out;

        try {
            writer = new FileWriter(file);
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
}