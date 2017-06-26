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
package com.sk89q.craftbook.core.util.report;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogListBlock {
    private Map<String, Object> items = new LinkedHashMap<>();
    private int maxKeyLength = 0;

    private void updateKey(String key) {
        if (key.length() > maxKeyLength) {
            maxKeyLength = key.length();
        }
    }

    public LogListBlock put(String key, String value) {
        updateKey(key);
        items.put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, LogListBlock value) {
        updateKey(key);
        items.put(key, value);
        return this;
    }

    public LogListBlock put(String key, Object value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, String value, Object ... args) {
        put(key, String.format(value, args));
        return this;
    }

    public LogListBlock put(String key, int value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, byte value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, double value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, float value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, short value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, long value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock put(String key, boolean value) {
        put(key, String.valueOf(value));
        return this;
    }

    public LogListBlock putChild(String key) {
        updateKey(key);
        LogListBlock block = new LogListBlock();
        items.put(key, block);
        return block;
    }

    private static String padKey(String key, int len) {
        return String.format("%-" + len + 's', key);
    }

    protected String getOutput(String prefix) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Object> entry : items.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof LogListBlock) {
                out.append(prefix);
                out.append(padKey(entry.getKey(), maxKeyLength));
                out.append(":\r\n");
                out.append(((LogListBlock) val).getOutput(prefix + "    "));
            } else {
                out.append(prefix);
                out.append(padKey(entry.getKey(), maxKeyLength));
                out.append(": ");
                out.append(val.toString());
                out.append("\r\n");
            }
        }
        return out.toString();
    }

    @Override
    public String toString() {
        return getOutput("");
    }
}