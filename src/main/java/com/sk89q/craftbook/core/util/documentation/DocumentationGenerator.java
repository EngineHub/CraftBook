/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import java.io.*;
import java.net.URL;

public class DocumentationGenerator {

    public static void generateDocumentation(DocumentationProvider provider) {

        File docsFile = new File(CraftBookAPI.inst().getWorkingDirectory(), "documentation");
        docsFile.mkdir();

        File docFile = new File(docsFile, provider.getPath() + ".rst");
        docFile.getParentFile().mkdirs();

        URL resource = DocumentationGenerator.class.getClassLoader().getResource("docs/" + provider.getPath() + ".rst");
        if (resource == null) {
            CraftBookAPI.inst().getLogger().warn("Failed to find template for " + provider.getPath());
            return;
        }

        File template = new File(resource.getFile());

        String output = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(template))) {
            String temp;
            while((temp = reader.readLine()) != null)
                output += temp + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                if(configValue.getDefaultValue().toString().length() > defaultLength)
                    defaultLength = configValue.getDefaultValue().toString().length();
            }

            String border = createStringOfLength(nodeLength, '=') + ' ' + createStringOfLength(commentLength, '=') + ' ' + createStringOfLength(typeLength, '=') + ' ' + createStringOfLength(defaultLength, '=');

            configSection.append(border + "\n");
            configSection.append(padToLength("Node", nodeLength+1) + padToLength("Comment", commentLength+1) + padToLength("Type", typeLength+1) + padToLength("Default", defaultLength+1) + "\n");
            configSection.append(border + "\n");
            for(ConfigValue<?> configValue : provider.getConfigurationNodes()) {
                configSection.append(padToLength(configValue.getKey(), nodeLength+1) + padToLength(configValue.getComment(), commentLength+1) + padToLength(configValue.getTypeToken().getRawType().getSimpleName(), typeLength+1) + padToLength(configValue.getDefaultValue().toString(), defaultLength+1) + "\n");
            }
            configSection.append(border + "\n");
        }

        output = output.replace("%CONFIG%", configSection.toString());

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

            permissionsSection.append(border + "\n");
            permissionsSection.append(padToLength("Node", nodeLength+1) + padToLength("Description", descriptionLength+1) + padToLength("Default Role", defaultRoleLength+1) + "\n");
            permissionsSection.append(border + "\n");
            for(PermissionNode permissionNode : provider.getPermissionNodes()) {
                permissionsSection.append(padToLength(permissionNode.getNode(), nodeLength+1) + padToLength(permissionNode.getDescription(), descriptionLength+1) + padToLength(permissionNode.getDefaultRole(), defaultRoleLength+1) + "\n");
            }
            permissionsSection.append(border + "\n");
        }

        output = output.replace("%PERMS%", permissionsSection.toString());

        output = provider.performCustomConversions(output);

        try(PrintWriter writer = new PrintWriter(docFile)) {
            writer.write(output);
        } catch(IOException e) {
            CraftBookAPI.inst().getLogger().error("An IO Exception occured.", e);
        }
    }

    public static String createStringOfLength(int length, char character) {
        String ret = "";

        for(int i = 0; i < length; i++)
            ret += character;

        return ret;
    }

    public static String padToLength(String string, int length) {
        while(string.length() < length)
            string += ' ';
        return string;
    }
}
