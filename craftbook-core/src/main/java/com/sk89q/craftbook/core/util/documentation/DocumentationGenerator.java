/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.core.util.documentation;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.PermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentationGenerator {

    private static final Pattern PERMS_PATTERN = Pattern.compile("%PERMS%", Pattern.LITERAL);
    private static final Pattern CONFIG_PATTERN = Pattern.compile("%CONFIG%", Pattern.LITERAL);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("%IMPORT (.*)%");

    private static String[] searchLocations = {"../docs", "docs", "config/craftbook/docs", "../config/craftbook/docs"};
    private static File rootDirectory;

    private static File getRootDirectory() {
        if (rootDirectory == null) {
            File wd = new File(".");
            File rootDir;
            for (String searchLocation : searchLocations) {
                rootDir = new File(wd, searchLocation);
                if (rootDir.exists()) {
                    rootDirectory = rootDir;
                    break;
                }
            }
        }

        return rootDirectory;
    }

    public static void generateDocumentation(DocumentationProvider provider) {
        File docFile = new File(getRootDirectory(), "source/" + provider.getPath() + ".rst");
        docFile.getParentFile().mkdirs();

        File template = new File(getRootDirectory(), "templates/" + provider.getTemplatePath() + ".rst");
        if (!template.exists()) {
            CraftBookAPI.inst().getLogger().warn("Failed to find template for " + provider.getPath());
            return;
        }

        String output = makeReplacements(loadFile(template), provider);

        try(PrintWriter writer = new PrintWriter(docFile)) {
            writer.write(output);
        } catch(IOException e) {
            CraftBookAPI.inst().getLogger().error("An IO Exception occured.", e);
        }
    }

    public static String loadFile(File inputFile) {
        StringBuilder output = new StringBuilder();
        if (inputFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                String temp;
                while ((temp = reader.readLine()) != null)
                    output.append(temp).append('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output.toString();
    }

    public static String makeReplacements(String input, DocumentationProvider provider) {
        StringBuilder configSection = new StringBuilder();

        if(provider.getConfigurationNodes().length > 0) {

            configSection.append("Configuration\n");
            configSection.append("=============\n\n");

            int nodeLength = "Node".length(), commentLength = "Comment".length(), typeLength = "Type".length(), defaultLength = "Default".length();

            for(ConfigValue<?> configValue : provider.getConfigurationNodes()) {
                if(configValue.getKey().length() > nodeLength)
                    nodeLength = configValue.getKey().length();
                if(configValue.getComment().length() > commentLength)
                    commentLength = configValue.getComment().length();
                if(configValue.getTypeToken().getRawType().getSimpleName().length() > typeLength)
                    typeLength = configValue.getTypeToken().getRawType().getSimpleName().length();

                ConfigurationNode node = SimpleCommentedConfigurationNode.root();
                configValue.serializeDefault(node);

                if(node.getString("null").length() > defaultLength)
                    defaultLength = node.getString("null").length();
            }

            String border = createStringOfLength(nodeLength, '=') + ' ' + createStringOfLength(commentLength, '=') + ' ' + createStringOfLength(typeLength, '=') + ' ' + createStringOfLength(defaultLength, '=');

            configSection.append(border).append('\n');
            configSection.append(padToLength("Node", nodeLength + 1)).append(padToLength("Comment", commentLength + 1)).append(padToLength("Type", typeLength + 1)).append(padToLength("Default", defaultLength + 1)).append('\n');
            configSection.append(border).append('\n');
            for(ConfigValue<?> configValue : provider.getConfigurationNodes()) {
                ConfigurationNode node = SimpleCommentedConfigurationNode.root();
                configValue.serializeDefault(node);

                configSection.append(padToLength(configValue.getKey(), nodeLength + 1)).append(padToLength(configValue.getComment(), commentLength + 1)).append(padToLength(configValue.getTypeToken().getRawType().getSimpleName(), typeLength + 1)).append(padToLength(node.getString("null"), defaultLength + 1)).append('\n');
            }
            configSection.append(border).append('\n');
        }

        input = CONFIG_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(configSection.toString()));

        StringBuilder permissionsSection = new StringBuilder();

        if(provider.getPermissionNodes().length > 0) {

            permissionsSection.append("Permissions\n");
            permissionsSection.append("===========\n\n");

            int nodeLength = "Node".length(), descriptionLength = "Description".length(), defaultRoleLength = "Default Role".length();

            for(PermissionNode permissionNode : provider.getPermissionNodes()) {
                if(permissionNode.getNode().length() > nodeLength)
                    nodeLength = permissionNode.getNode().length();
                if(permissionNode.getDescription().length() > descriptionLength)
                    descriptionLength = permissionNode.getDescription().length();
                if(permissionNode.getDefaultRole().length() > defaultRoleLength)
                    defaultRoleLength = permissionNode.getDefaultRole().length();
            }

            String border = createStringOfLength(nodeLength, '=') + ' ' + createStringOfLength(descriptionLength, '=') + ' ' + createStringOfLength(defaultRoleLength, '=');

            permissionsSection.append(border).append('\n');
            permissionsSection.append(padToLength("Node", nodeLength + 1)).append(padToLength("Description", descriptionLength + 1)).append(padToLength("Default Role", defaultRoleLength + 1)).append('\n');
            permissionsSection.append(border).append('\n');
            for(PermissionNode permissionNode : provider.getPermissionNodes()) {
                permissionsSection.append(padToLength(permissionNode.getNode(), nodeLength + 1)).append(padToLength(permissionNode.getDescription(), descriptionLength + 1)).append(padToLength(permissionNode.getDefaultRole(), defaultRoleLength + 1)).append('\n');
            }
            permissionsSection.append(border).append('\n');
        }

        input = PERMS_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(permissionsSection.toString()));

        input = provider.performCustomConversions(input);

        Matcher importMatcher = IMPORT_PATTERN.matcher(input);
        while(importMatcher.find()) {
            String fileDir = importMatcher.group(1);
            File file = new File(new File(getRootDirectory(), "templates/" + provider.getTemplatePath() + ".rst").getParentFile(), fileDir + ".rst");
            input = input.replace("%IMPORT " + fileDir + '%', makeReplacements(loadFile(file), provider));
        }

        return input;
    }

    public static String createStringOfLength(int length, char character) {
        StringBuilder ret = new StringBuilder();

        for(int i = 0; i < length; i++)
            ret.append(character);

        return ret.toString();
    }

    public static String padToLength(String input, int length) {
        StringBuilder builder = new StringBuilder(input);
        while(builder.length() < length)
            builder.append(' ');
        return builder.toString();
    }
}
