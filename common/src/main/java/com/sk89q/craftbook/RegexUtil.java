package com.sk89q.craftbook;

import java.util.regex.Pattern;

public class RegexUtil {

    public static final Pattern ASTERISK_PATTERN = Pattern.compile("*", Pattern.LITERAL);
    public static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    public static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", Pattern.LITERAL);
    public static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
}