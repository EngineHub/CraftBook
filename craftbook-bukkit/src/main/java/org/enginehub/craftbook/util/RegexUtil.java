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

package org.enginehub.craftbook.util;

import java.util.regex.Pattern;

public class RegexUtil {

    private RegexUtil() {
    }

    public static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);
    public static final Pattern PERIOD_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    public static final Pattern EQUALS_PATTERN = Pattern.compile("=", Pattern.LITERAL);
    public static final Pattern ASTERISK_PATTERN = Pattern.compile("*", Pattern.LITERAL);
    public static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    public static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", Pattern.LITERAL);
    public static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    public static final Pattern RIGHT_BRACKET_PATTERN = Pattern.compile("]", Pattern.LITERAL);
    public static final Pattern LEFT_BRACKET_PATTERN = Pattern.compile("[", Pattern.LITERAL);
    public static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">", Pattern.LITERAL);
    public static final Pattern LESS_THAN_PATTERN = Pattern.compile("<", Pattern.LITERAL);
    public static final Pattern MINUS_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    public static final Pattern PERCENT_PATTERN = Pattern.compile("%", Pattern.LITERAL);
    public static final Pattern AMPERSAND_PATTERN = Pattern.compile("&", Pattern.LITERAL);
    public static final Pattern PIPE_PATTERN = Pattern.compile("|", Pattern.LITERAL);
    public static final Pattern IC_PATTERN = Pattern.compile("^\\[(([A-Z]{1,3})[0-9]{1,4})\\][A-Z]?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern PLC_NAME_PATTERN = Pattern.compile("[-_a-z0-9]+", Pattern.CASE_INSENSITIVE);
}
