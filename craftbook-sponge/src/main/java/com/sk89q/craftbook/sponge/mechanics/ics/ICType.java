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
package com.sk89q.craftbook.sponge.mechanics.ics;

import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.createStringOfLength;

import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICType<T extends IC> implements DocumentationProvider {

    private static final Pattern IC_HEADER_PATTERN = Pattern.compile("%IC_HEADER%", Pattern.LITERAL);
    private static final Pattern IC_PINS_PATTERN = Pattern.compile("%IC_PINS%", Pattern.LITERAL);
    private static final Pattern IC_LINES_PATTERN = Pattern.compile("%IC_LINES%", Pattern.LITERAL);
    private static final Pattern IC_ID_PATTERN = Pattern.compile("%IC_ID%", Pattern.LITERAL);

    private String name;
    private String description;
    private String modelId;
    private String shorthandId;
    private String defaultPinset;

    private ICFactory<T> icFactory;

    public ICType(String modelId, String shorthandId, String name, String description, ICFactory<T> icFactory) {
        this.modelId = modelId;
        this.shorthandId = shorthandId;
        this.name = name;
        this.description = description;
        this.icFactory = icFactory;
    }

    public ICType(String modelId, String shorthandId, String name, String description, ICFactory<T> icFactory, String defaultPinset) {
        this(modelId, shorthandId, name, description, icFactory);
        this.defaultPinset = defaultPinset;
    }

    public String getDefaultPinSet() {
        return defaultPinset != null ? defaultPinset : "SISO";
    }

    public String getName() {
        return this.name;
    }

    public String getModel() {
        return this.modelId;
    }

    public String getShorthand() {
        return this.shorthandId;
    }

    public String getDescription() {
        return this.description;
    }

    public ICFactory<T> getFactory() {
        return this.icFactory;
    }

    @Override
    public String getPath() {
        return "mechanics/ics/" + modelId;
    }

    @Override
    public String getTemplatePath() {
        return "mechanics/ics/template";
    }

    @Override
    public String performCustomConversions(String input) {
        input = IC_ID_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(modelId));

        StringBuilder icPins = new StringBuilder();
        icPins.append("IC Pins\n").append("=======\n\n");
        icPins.append("\nInputs\n").append("------\n\n");
        for (int i = 0; i < icFactory.getPinHelp()[0].length; i++) {
            String line = icFactory.getPinHelp()[0][i];
            if (line.isEmpty()) {
                line = "None";
            }
            icPins.append("- ").append(line).append('\n');
        }
        icPins.append("\nOutputs\n").append("-------\n\n");
        for (int i = 0; i < icFactory.getPinHelp()[1].length; i++) {
            String line = icFactory.getPinHelp()[1][i];
            if (line.isEmpty()) {
                line = "None";
            }
            icPins.append("- ").append(line).append('\n');
        }
        input = IC_PINS_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(icPins.toString()));

        StringBuilder icLines = new StringBuilder();
        icLines.append("Sign Lines\n").append("==========\n\n");
        icLines.append("1. ").append(this.shorthandId.toUpperCase()).append('\n');
        icLines.append("2. ").append('[').append(this.modelId).append(']').append('\n');
        for (int i = 3; i < 5; i++) {
            String line = icFactory.getLineHelp()[i-3];
            if (line.isEmpty()) {
                line = "Blank";
            }
            icLines.append(i).append(". ").append(line).append('\n');
        }
        input = IC_LINES_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(icLines.toString()));

        String icHeader = createStringOfLength(modelId.length(), '=') + '\n' + modelId + '\n' + createStringOfLength(modelId.length(), '=');
        return IC_HEADER_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(icHeader));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ICType<?> icType = (ICType<?>) o;

        return modelId.equals(icType.modelId);
    }

    @Override
    public int hashCode() {
        return modelId.hashCode();
    }
}
