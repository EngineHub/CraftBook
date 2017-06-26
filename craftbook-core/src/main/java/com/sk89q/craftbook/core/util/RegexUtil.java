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
package com.sk89q.craftbook.core.util;

import java.util.regex.Pattern;

public final class RegexUtil {
    public static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    public static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", Pattern.LITERAL);
    public static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    public static final Pattern EQUALS_PATTERN = Pattern.compile("=", Pattern.LITERAL);
    public static final Pattern PERCENT_PATTERN = Pattern.compile("%", Pattern.LITERAL);
    public static final Pattern PIPE_PATTERN = Pattern.compile("|", Pattern.LITERAL);
    public static final Pattern MINUS_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    public static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);
}
