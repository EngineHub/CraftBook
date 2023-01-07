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

package org.enginehub.craftbook.bukkit.commands;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.report.LoadedICsReport;
import org.enginehub.craftbook.bukkit.report.MechanicReport;
import org.enginehub.craftbook.bukkit.report.PerformanceReport;
import org.enginehub.craftbook.bukkit.report.PluginReport;
import org.enginehub.craftbook.bukkit.report.SchedulerReport;
import org.enginehub.craftbook.bukkit.report.ServerReport;
import org.enginehub.craftbook.bukkit.report.ServicesReport;
import org.enginehub.craftbook.bukkit.report.WorldReport;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanic.MechanicCommands;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.report.GlobalConfigReport;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.part.SubCommandPart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class CraftBookCommands {

    public static void register(CommandManagerService service, CommandManager commandManager, CommandRegistrationHandler registration) {
        commandManager.register("craftbook", builder -> {
            builder.aliases(Lists.newArrayList("cb"));
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
    @CommandPermissions("craftbook.reload")
    public void reload(Actor actor) {

        try {
            CraftBookPlugin.inst().reloadConfiguration();
        } catch (Throwable e) {
            e.printStackTrace();
            actor.print("An error occurred while reloading the CraftBook config.");
            return;
        }
        actor.print("The CraftBook config has been reloaded.");
    }

    @Command(name = "version", aliases = { "ver" }, desc = "Get CraftBook version.")
    public void version(Actor actor) {
        actor.printInfo(TranslatableComponent.of("craftbook.version.version", TextComponent.of(CraftBook.getInstance().getPlatform().getPlatformName())));
        actor.printInfo(
            TextComponent.of("https://github.com/EngineHub/CraftBook/")
                .clickEvent(ClickEvent.openUrl("https://github.com/EngineHub/CraftBook/"))
        );
    }

    @Command(name = "iteminfo", aliases = { "itemsyntax", "item" }, desc = "Provides item syntax for held item.")
    public void itemInfo(CraftBookPlayer player) {
        player.print("Main hand: " + ItemSyntax.getStringFromItem(BukkitAdapter.adapt(player.getItemInHand(HandSide.MAIN_HAND))));
        player.print("Off hand: " + ItemSyntax.getStringFromItem(BukkitAdapter.adapt(player.getItemInHand(HandSide.OFF_HAND))));
    }

    @Command(name = "report", desc = "Writes a report on CraftBook")
    @CommandPermissions({ "craftbook.report" })
    public void report(Actor actor,
                       @Switch(name = 'i', desc = "Include the loaded ICs Report.")
                           boolean loadedIcReport,
                       @Switch(name = 'p', desc = "Submit the report to pastebin.")
                           boolean pastebin) throws CraftBookException, AuthorizationException {
        ReportList report = new ReportList("Report");

        report.add(new ServerReport());
        report.add(new PluginReport());
        report.add(new SchedulerReport());
        report.add(new ServicesReport());
        report.add(new WorldReport());
        report.add(new PerformanceReport());
        report.add(new SystemInfoReport());
        report.add(new GlobalConfigReport());
        report.add(new MechanicReport());

        if (loadedIcReport) {
            report.add(new LoadedICsReport());
        }

        String result = report.toString();

        try {
            Path dest = CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("report.txt");
            Files.write(result, dest.toFile(), StandardCharsets.UTF_8);
            actor.print("CraftBook report written to " + dest.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new CraftBookException("Failed to write report: " + e.getMessage());
        }

        if (pastebin) {
            CraftBookPlugin.inst().checkPermission(BukkitAdapter.adapt(actor), "craftbook.report.pastebin");

            ActorCallbackPaste.pastebin(
                CraftBook.getInstance().getSupervisor(),
                actor,
                result,
                "CraftBook report: %s.report"
            );
        }
    }

}
