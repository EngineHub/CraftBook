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
package com.sk89q.craftbook.sponge.command;

import com.sk89q.craftbook.core.util.report.PastebinPoster;
import com.sk89q.craftbook.core.util.report.ReportWriter;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.util.report.SpongeReportWriter;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReportCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        File dest = new File(CraftBookPlugin.inst().getWorkingDirectory(), "report.txt");
        ReportWriter report = new SpongeReportWriter();

        report.generate();

        try {
            report.write(dest);
            src.sendMessage(Text.of(TextColors.YELLOW, "CraftBook report written to " + dest.getAbsolutePath()));
        } catch (IOException e) {
            src.sendMessage(Text.of(TextColors.RED, "Failed to write report: " + e.getMessage()));
            e.printStackTrace();
            return CommandResult.empty();
        }

        if (args.hasAny("p")) {
            src.sendMessage(Text.of(TextColors.YELLOW, "Now uploading to Pastebin..."));
            PastebinPoster.paste(report.toString(), new PastebinPoster.PasteCallback() {
                @Override
                public void handleSuccess(String url) {
                    // Hope we don't have a thread safety issue here
                    try {
                        URL javaUrl = new URL(url);
                        src.sendMessage(Text.of(TextColors.YELLOW, "CraftBook report (1 day): ", Text.builder(url)
                                .onClick(TextActions.openUrl(javaUrl)).build()));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of(TextColors.YELLOW, "CraftBook report (1 day): " + url));
                    }
                }

                @Override
                public void handleError(String err) {
                    // Hope we don't have a thread safety issue here
                    src.sendMessage(Text.of(TextColors.RED, "CraftBook report pastebin error: " + err));
                }
            });
        }

        return CommandResult.success();
    }
}
