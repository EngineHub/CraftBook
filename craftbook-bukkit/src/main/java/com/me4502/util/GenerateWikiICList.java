/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFamily;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.mechanics.ic.RegisteredICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.mechanics.ic.SelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.plc.PlcFactory;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;

public class GenerateWikiICList extends ExternalUtilityBase {

    public GenerateWikiICList (String[] args) {
        super(args);
    }

    @Override
    public void generate(String[] args) {
        try {
            File file = new File(getGenerationFolder(), "ICList.txt");
            if(!file.exists())
                file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }

            PrintWriter writer = new PrintWriter(file, "UTF-8");

            writer.println("{| class=\"wiki-table sortable\"");
            writer.println("! IC ID");
            writer.println("! Shorthand");
            writer.println("! Allows ST");
            writer.println("! Families");
            writer.println("! Name");
            writer.println("! Description");
            for(RegisteredICFactory ric : ICManager.inst().getICList()) {
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
                        family = family + " ";
                    family = family + "[[../IC families/#" + StringUtils.replace(fam.getClass().getSimpleName(), "Family", "") + "|" + StringUtils.replace(fam.getClass().getSimpleName(), "Family", "") + "]]";
                }

                IC ic = ric.getFactory().create(null);

                if(ric.getShorthand().length() > (isSelfTriggering && !((SelfTriggeredIC) ic).isAlwaysST() ? 11 : 14))
                    System.err.println("Shorthand " + ric.getShorthand() + " is longer than max chars!");

                if(ric.getFactory().getShortDescription().equalsIgnoreCase("No Description."))
                    System.out.println("Missing short description for: " + ric.getId());

                writer.println("|-");
                writer.println("| [[../" + ric.getId() + "/]] || " + ric.getShorthand() + " || " + String.valueOf(isSelfTriggering) + " || " + family + " || " + ic.getTitle() + (isRestricted ? "<strong style=\"color: red\">*</strong>" : "") + " || " + ric.getFactory().getShortDescription());
            }

            writer.println("|}");
            writer.println("<strong style=\"color: red\">*</strong>Requires the permission '''craftbook.ic.restricted.*''' or the respective '''craftbook.ic.mc''XXXX''''' permission.");

            writer.close();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}