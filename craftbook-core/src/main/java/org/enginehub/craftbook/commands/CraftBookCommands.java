/*
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

package org.enginehub.craftbook.commands;

import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanic.MechanicCommands;
import org.enginehub.craftbook.util.report.GlobalConfigReport;
import org.enginehub.craftbook.util.report.ReportFlag;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.part.SubCommandPart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class CraftBookCommands {

    public static void register(CommandManagerService service, CommandManager commandManager, CommandRegistrationHandler registration) {
        commandManager.register("craftbook", builder -> {
            builder.aliases(List.of("cb"));
            builder.description(TextComponent.of("CraftBook Commands"));

            CommandManager manager = service.newCommandManager();
            registration.register(
                manager,
                CraftBookCommandsRegistration.builder(),
                new CraftBookCommands()
            );

            MechanicCommands.register(service, manager, registration);

            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"), TextComponent.of("Sub-command to run."))
                .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                .required()
                .build());
        });
    }

    @Command(name = "reload", desc = "Reloads the CraftBook Common config")
    @CommandPermissions({ "craftbook.reload" })
    public void reload(Actor actor) {
        try {
            CraftBook.getInstance().getPlatform().reloadConfiguration();
        } catch (Throwable e) {
            CraftBook.LOGGER.error(e);
            actor.printError(TranslatableComponent.of("craftbook.reload.failed"));
            return;
        }

        actor.printInfo(TranslatableComponent.of("craftbook.reload.reloaded"));
    }

    @Command(name = "version", aliases = { "ver" }, desc = "Get CraftBook version.")
    @CommandPermissions({ "craftbook.version" })
    public void version(Actor actor) {
        actor.printInfo(TranslatableComponent.of("craftbook.version.version", TextComponent.of(CraftBook.getInstance().getPlatform().getPlatformName())));
        actor.printInfo(
            TextComponent.of("https://github.com/EngineHub/CraftBook/")
                .clickEvent(ClickEvent.openUrl("https://github.com/EngineHub/CraftBook/"))
        );
    }

    @Command(name = "report", desc = "Writes a report on CraftBook")
    @CommandPermissions({ "craftbook.report" })
    public void report(Actor actor,
                       @Switch(name = 'i', desc = "Include the loaded ICs Report.")
                           boolean loadedIcReport,
                       @Switch(name = 'p', desc = "Submit the report to pastebin.")
                           boolean pastebin) throws CraftBookException, AuthorizationException {
        ReportList report = new ReportList("Report");
        EnumSet<ReportFlag> reportFlags = EnumSet.noneOf(ReportFlag.class);

        if (loadedIcReport) {
            reportFlags.add(ReportFlag.IC_REPORT);
        }

        report.add(new SystemInfoReport());
        report.add(new GlobalConfigReport());

        CraftBook.getInstance().getPlatform().addPlatformReports(report, reportFlags.toArray(new ReportFlag[0]));

        String result = report.toString();

        try {
            Path dest = CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("report.txt");
            Files.writeString(dest, result, StandardCharsets.UTF_8);
            actor.printInfo(TranslatableComponent.of("craftbook.report.written", TextComponent.of(dest.toAbsolutePath().toString())));
        } catch (IOException e) {
            actor.printError(TranslatableComponent.of("craftbook.report.error", TextComponent.of(e.getMessage())));
            return;
        }

        if (pastebin) {
            if (!actor.hasPermission("craftbook.report.pastebin")) {
                throw new AuthorizationException();
            }

            ActorCallbackPaste.pastebin(
                CraftBook.getInstance().getSupervisor(),
                actor,
                result,
                TranslatableComponent.builder("craftbook.report.success")
            );
        }
    }
}
