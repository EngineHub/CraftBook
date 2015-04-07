package com.me4502.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.wikipedia.Wiki;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.CommandIC;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICConfiguration;
import com.sk89q.craftbook.mechanics.ic.ICFamily;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.mechanics.ic.RegisteredICFactory;
import com.sk89q.craftbook.mechanics.ic.families.FamilyAISO;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateWikiICPages extends ExternalUtilityBase {

    public GenerateWikiICPages (String[] args) {
        super(args);
    }

    String username, password;

    @Override
    public void generate(String[] args) {
        try {

            boolean upload = false;
            final List<String> toUpload = new ArrayList<String>();

            for(String arg : args) {

                if(arg.equalsIgnoreCase("upload"))
                    upload = true;
                else if(arg.startsWith("u:"))
                    username = arg.substring(2);
                else if(arg.startsWith("p:"))
                    password = arg.substring(2);
                else if(upload)
                    toUpload.add(arg.toUpperCase());
            }

            final File file = new File(getGenerationFolder(), "IC-Pages/");
            if(!file.exists())
                file.mkdir();

            BlockState oldState = Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).getState();
            Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).setType(Material.WALL_SIGN);

            CraftBookPlugin.inst().createDefaultConfiguration(new File(getGenerationFolder(), "ic-config.yml"), "ic-config.yml");
            ICConfiguration icConfiguration = new ICConfiguration(new YAMLProcessor(new File(getGenerationFolder(), "ic-config.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());

            icConfiguration.load();

            int missingComments = 0;

            final Set<String> missingDocuments = new HashSet<String>();

            for(RegisteredICFactory ric : ICManager.inst().getICList()) {

                PrintWriter writer = new PrintWriter(new File(file, ric.getId() + ".txt"), "UTF-8");

                IC ic = ric.getFactory().create(null);

                writer.println("[[../Integrated_circuits#IC_Types_List|< Return to ICs]]");
                writer.println();

                for(ICFamily family : ric.getFamilies()) {
                    if(family instanceof FamilyAISO) continue;
                    writer.println("{{" + family.getName() + "|id=" + ric.getId() + "|name=" + ic.getTitle() + "}}");
                }

                if(ric.getFactory().getLongDescription() == null || ric.getFactory().getLongDescription().length == 0 || ric.getFactory().getLongDescription()[0].equals("Missing Description")) {
                    CraftBookPlugin.logger().info("Missing Long Description for: " + ric.getId());
                    missingDocuments.add(ric.getId());
                }

                for(String line : ric.getFactory().getLongDescription())
                    writer.println(line);

                writer.println();
                writer.println("== Sign parameters ==");
                writer.println("# " + ic.getSignTitle());
                writer.println("# [" + ric.getId() + "]");
                for(String line : ric.getFactory().getLineHelp()) {
                    if(line == null) line = "Blank";

                    if(line.contains("{") && line.contains("}")) line = StringUtils.replace(StringUtils.replace(line, "}", "</span>''"), "{", "''<span style='color:#808080'>"); //Optional Syntax.

                    if(line.contains("SearchArea")) line = StringUtils.replace(line, "SearchArea", "[[../Search_Area|Search Area]]");
                    if(line.contains("ItemSyntax")) line = StringUtils.replace(line, "ItemSyntax", "[[../Item_Syntax|Item Syntax]]");
                    if(line.contains("PlayerType")) line = StringUtils.replace(line, "PlayerType", "[[../Player_Type|Player Type]]");
                    writer.println("# " + line);
                }

                writer.println();
                writer.println("== Pins ==");

                writer.println();
                writer.println("=== Input ===");
                int pins = 0;

                ChipState state = ric.getFamilies()[0].detect(BukkitUtil.toWorldVector(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0)), BukkitUtil.toChangedSign(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0)));

                for(String pin : ric.getFactory().getPinDescription(state)) {

                    if(pins == state.getInputCount()) {
                        writer.println();
                        writer.println("=== Output ===");
                    }

                    writer.println("# " + (pin == null ? "Nothing" : pin));

                    if(pin == null) {
                        CraftBookPlugin.logger().info("Missing pin: " + pins + " for IC: " + ric.getId());
                        missingDocuments.add(ric.getId());
                    }

                    pins++;
                }

                writer.println();

                if(ric.getFactory() instanceof ConfigurableIC) {

                    writer.println("== Configuration ==");
                    writer.println();
                    writer.println("{| class=\"wiki-table sortable\"");
                    writer.println("|-");
                    writer.println("! Configuration Node and Path");
                    writer.println("! Default Value");
                    writer.println("! Effect");

                    String path = "ics." + ric.getId();

                    for(String key : icConfiguration.config.getKeys(path)) {
                        if(icConfiguration.config.getProperty(path + "." + key) != null && !(icConfiguration.config.getProperty(path + "." + key) instanceof Map)) {
                            writer.println("|-");
                            writer.println("| " + path + "." + key);
                            writer.println("| " + String.valueOf(icConfiguration.config.getProperty(path + "." + key)));
                            String comment = icConfiguration.config.getComment(path + "." + key);
                            if(comment == null) {
                                System.out.println("[WARNING] Key " + path + "." + key + " is missing a comment!");
                                missingComments++;
                                missingDocuments.add(ric.getId());
                                comment = "";
                            }
                            if(!comment.trim().isEmpty()) comment = comment.trim().substring(2);
                            writer.println("| " + comment);
                        }
                    }

                    writer.println("|}");
                    writer.println();
                }

                if(ric.getFactory() instanceof CommandIC) {

                    writer.println("== Commands ==");
                    writer.println();

                    writer.println("{| class=\"wiki-table\"");
                    writer.println("! Command");
                    writer.println("! Permission");
                    writer.println("! Description");
                    for(String[] bits : ((CommandIC) ric.getFactory()).getCommandInformation()) {
                        writer.println("|-");
                        writer.println("| /ic ic " + ric.getId().toLowerCase() + " " + bits[0]);
                        for(int i = 1; i < bits.length; i++)
                            writer.println("| " + bits[i]);
                    }
                    writer.println("|}");
                    writer.println();
                }


                writer.print("[[Category:IC]]");
                for(ICFamily family : ric.getFamilies())
                    writer.print("[[Category:" + family.getName() + "]]");
                writer.close();
            }

            System.out.println(missingComments + " Comments Are Missing");

            oldState.update(true);

            if(upload) {

                Bukkit.getScheduler().runTaskAsynchronously(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run () {
                        Bukkit.getLogger().info("Starting Upload");
                        Wiki wiki = new Wiki("wiki.sk89q.com");
                        wiki.setMaxLag(0);
                        wiki.setThrottle(5000);
                        wiki.setResolveRedirects(true);

                        try {
                            Bukkit.getLogger().info("Logging In");
                            wiki.login(username, password);
                            Bukkit.getLogger().info("Logged in Successfully!");

                            int amount = 0;
                            String failed = "";

                            for(RegisteredICFactory ric : ICManager.inst().getICList()) {
                                if(toUpload.contains("ALL") || toUpload.contains(ric.getId())) {

                                    if(missingDocuments.contains(ric.getId())) {
                                        if(failed.length() == 0)
                                            failed = ric.getId();
                                        else
                                            failed = failed + "," + ric.getId();
                                        continue; //Ignore this, bad docs.
                                    }

                                    Bukkit.getLogger().info("Uploading " + ric.getId() + "...");

                                    StringBuilder builder = new StringBuilder();

                                    BufferedReader reader = new BufferedReader(new FileReader(new File(file, ric.getId() + ".txt")));

                                    String line = null;

                                    while((line = reader.readLine()) != null) {
                                        builder.append(line);
                                        builder.append("\n");
                                    }

                                    reader.close();

                                    wiki.edit("CraftBook/" + ric.getId(), builder.toString(), "Automated update of '" + ric.getId() + "' by " + username);

                                    Bukkit.getLogger().info("Uploaded: " + ric.getId());

                                    amount++;
                                }
                            }

                            Bukkit.getLogger().info("Finished uploading! Uploaded " + amount + " IC Pages!");
                            if(failed.length() > 0)
                                Bukkit.getLogger().warning("Failed to upload ICs: " + failed);
                        } catch (FailedLoginException e) {
                            Bukkit.getLogger().warning("Failed to login to wiki!");
                        } catch (LoginException e) {
                            e.printStackTrace();
                            Bukkit.getLogger().warning("Failed to login to wiki!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}