package com.sk89q.craftbook.util;

import java.util.regex.Pattern;

public class RegexUtil {

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
    public static final Pattern PLUS_PATTERN = Pattern.compile("+", Pattern.LITERAL);
    public static final Pattern COMMENT_PATTERN = Pattern.compile("#", Pattern.LITERAL);
    public static final Pattern FIELD_SEPARATOR_PATTERN = Pattern.compile("->", Pattern.LITERAL);
    public static final Pattern X_PATTERN = Pattern.compile("x", Pattern.LITERAL);
    public static final Pattern PERCENT_PATTERN = Pattern.compile("%", Pattern.LITERAL);
    public static final Pattern AMPERSAND_PATTERN = Pattern.compile("&", Pattern.LITERAL);
    public static final Pattern PIPE_PATTERN = Pattern.compile("|", Pattern.LITERAL);
    public static final Pattern FSLASH_PATTERN = Pattern.compile("/", Pattern.LITERAL);
    public static final Pattern BSLASH_PATTERN = Pattern.compile("\\", Pattern.LITERAL);
    public static final Pattern IC_PATTERN = Pattern.compile("^\\[(([A-Z]{1,3})[0-9]{1,4})\\][A-Z]?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern PLC_NAME_PATTERN = Pattern.compile("[-_a-z0-9]+", Pattern.CASE_INSENSITIVE);
    public static final Pattern VARIABLE_KEY_PATTERN = Pattern.compile("[a-z_]+", Pattern.CASE_INSENSITIVE);
    public static final Pattern VARIABLE_VALUE_PATTERN = Pattern.compile("[a-z0-9.,:;_]+", Pattern.CASE_INSENSITIVE);
    public static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\r?\\n");
}