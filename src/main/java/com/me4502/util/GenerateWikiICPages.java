package com.me4502.util;

import static com.me4502.util.GenerateWikiConfigLists.createStringOfLength;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.CommandIC;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICConfiguration;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.mechanics.ic.RegisteredICFactory;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenerateWikiICPages extends ExternalUtilityBase {

    public GenerateWikiICPages (String[] args) {
        super(args);
    }

    @Override
    public void generate(String[] args) {
        try {
            final File file = new File(getGenerationFolder(), "IC-Pages/");
            if(!file.exists())
                file.mkdir();

            BlockState oldState = Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).getState();
            Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).setType(Material.OAK_WALL_SIGN);

            CraftBookPlugin.inst().createDefaultConfiguration(new File(getGenerationFolder(), "ic-config.yml"), "ic-config.yml");
            ICConfiguration icConfiguration = new ICConfiguration(new YAMLProcessor(new File(getGenerationFolder(), "ic-config.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());

            icConfiguration.load();

            int missingComments = 0;

            final Set<String> missingDocuments = new HashSet<>();

            for(RegisteredICFactory ric : ICManager.inst().getICList()) {

                PrintWriter writer = new PrintWriter(new File(file, ric.getId().toUpperCase() + ".rst"), "UTF-8");

                IC ic = ric.getFactory().create(null);

                writer.println(":doc:`index`");
                writer.println();

                writer.println(createStringOfLength(ric.getId().length(), '='));
                writer.println(ric.getId());
                writer.println(createStringOfLength(ric.getId().length(), '='));
                writer.println();

                if(ric.getFactory().getLongDescription() == null || ric.getFactory().getLongDescription().length == 0 || ric.getFactory().getLongDescription()[0].equals("Missing Description")) {
                    CraftBookPlugin.logger().info("Missing Long Description for: " + ric.getId());
                    missingDocuments.add(ric.getId());
                }

                for(String line : ric.getFactory().getLongDescription())
                    writer.println(line);

                writer.println();
                writer.println("Sign parameters");
                writer.println("===============");
                writer.println();
                writer.println("#. " + ic.getSignTitle());
                writer.println("#. [" + ric.getId() + "]");
                for(String line : ric.getFactory().getLineHelp()) {
                    if(line == null) line = "Blank";

                    if(line.contains("{") && line.contains("}")) line = StringUtils.replace(StringUtils.replace(line, "}", "</span>''"), "{", "''<span style='color:#808080'>"); //Optional Syntax.

                    if(line.contains("SearchArea")) line = StringUtils.replace(line, "SearchArea", ":doc:`../../search_area`");
                    if(line.contains("ItemSyntax")) line = StringUtils.replace(line, "ItemSyntax", ":doc:`../../item_syntax`");
                    if(line.contains("PlayerType")) line = StringUtils.replace(line, "PlayerType", ":doc:`../../player_type`");
                    writer.println("#. " + line);
                }

                writer.println();
                writer.println("Pins");
                writer.println("====");

                writer.println();
                writer.println("Input");
                writer.println("-----");
                writer.println();
                int pins = 0;

                ChipState state = ric.getFamilies()[0].detect(BukkitAdapter.adapt(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0).getLocation()),
                        CraftBookBukkitUtil.toChangedSign(Bukkit.getWorlds().get(0).getBlockAt(0, 255, 0)));

                for(String pin : ric.getFactory().getPinDescription(state)) {

                    if(pins == state.getInputCount()) {
                        writer.println();
                        writer.println("Output");
                        writer.println("------");
                        writer.println();
                    }

                    writer.println("#. " + (pin == null ? "Nothing" : pin));

                    if(pin == null) {
                        CraftBookPlugin.logger().info("Missing pin: " + pins + " for IC: " + ric.getId());
                        missingDocuments.add(ric.getId());
                    }

                    pins++;
                }

                writer.println();

                if(ric.getFactory() instanceof ConfigurableIC) {

                    writer.println("Configuration");
                    writer.println("=============");
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

                    writer.println("Commands");
                    writer.println("========");
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

                writer.close();
            }

            System.out.println(missingComments + " Comments Are Missing");

            oldState.update(true);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}