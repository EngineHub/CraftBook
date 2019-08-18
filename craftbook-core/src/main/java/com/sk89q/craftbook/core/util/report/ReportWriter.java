/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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

import com.sk89q.craftbook.core.CraftBookAPI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ReportWriter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm Z");

    private Date date = new Date();
    private StringBuilder output = new StringBuilder();

    protected String flags = "";

    public void generate() {
        appendReportHeader(CraftBookAPI.inst());
        appendPlatformSections();
        appendln("-------------");
        appendln("END OF REPORT");
        appendln();
    }

    public abstract void appendPlatformSections();

    private static String repeat(String str, int n) {
        if(str == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(str);
        }

        return sb.toString();
    }

    protected void append(LogListBlock log) {
        output.append(log.toString());
    }

    private void appendln(String text) {
        output.append(text);
        output.append("\r\n");
    }

    protected void appendln(String text, Object ... args) {
        output.append(String.format(text, args));
        output.append("\r\n");
    }

    protected void appendln() {
        output.append("\r\n");
    }

    protected void appendHeader(String text) {
        String rule = repeat("-", text.length());
        output.append(rule);
        output.append("\r\n");
        appendln(text);
        output.append(rule);
        output.append("\r\n");
        appendln();
    }

    private void appendReportHeader(CraftBookAPI plugin) {
        appendln("CraftBook Report");
        appendln("Generated " + dateFormat.format(date));
        appendln();
        appendln("Version: " + plugin.getVersionString());
        appendln();
    }

    public void write(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            writer.write(output.toString());
        }
    }

    @Override
    public String toString() {
        return output.toString();
    }

    public String getFlags () {
        return flags;
    }

    public void appendFlags (String flags) {
        this.flags = this.flags + flags;
    }
}
