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

package com.sk89q.craftbook.bukkit.commands;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.report.GlobalConfigReport;
import com.sk89q.craftbook.bukkit.report.LoadedICsReport;
import com.sk89q.craftbook.bukkit.report.MechanicReport;
import com.sk89q.craftbook.core.mechanic.MechanicCommands;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.developer.ExternalUtilityManager;
import com.sk89q.craftbook.util.exceptions.CraftbookException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
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
import com.sk89q.worldguard.bukkit.util.report.PerformanceReport;
import com.sk89q.worldguard.bukkit.util.report.PluginReport;
import com.sk89q.worldguard.bukkit.util.report.SchedulerReport;
import com.sk89q.worldguard.bukkit.util.report.ServerReport;
import com.sk89q.worldguard.bukkit.util.report.ServicesReport;
import com.sk89q.worldguard.bukkit.util.report.WorldReport;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.part.SubCommandPart;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Command(name = "version", aliases = {"ver"}, desc = "Get CraftBook version.")
    public void version(Actor actor) {
        actor.printInfo(TranslatableComponent.of("craftbook.version.version", TextComponent.of(CraftBookPlugin.getVersion())));
        actor.printInfo(
                TextComponent.of("https://github.com/EngineHub/CraftBook/")
                        .clickEvent(ClickEvent.openUrl("https://github.com/EngineHub/CraftBook/"))
        );
    }

    @Command(name = "iteminfo", aliases = {"itemsyntax", "item"}, desc = "Provides item syntax for held item.")
    public void itemInfo(CraftBookPlayer player) {
        player.print("Main hand: " + ItemSyntax.getStringFromItem(BukkitAdapter.adapt(player.getItemInHand(HandSide.MAIN_HAND))));
        player.print("Off hand: " + ItemSyntax.getStringFromItem(BukkitAdapter.adapt(player.getItemInHand(HandSide.OFF_HAND))));
    }

    @Command(name = "cbid", aliases = {"craftbookid"}, desc = "Gets the players CBID.")
    public void cbid(CraftBookPlayer player) {
        player.print("CraftBook ID: " + player.getCraftBookId());
    }

    @Command(name = "report", desc = "Writes a report on CraftBook")
    @CommandPermissions({"craftbook.report"})
    public void report(Actor actor,
            @Switch(name = 'i', desc = "Include the loaded ICs Report.")
                boolean loadedIcReport,
            @Switch(name = 'p', desc = "Submit the report to pastebin.")
                boolean pastebin) throws CraftbookException, AuthorizationException {
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
            File dest = new File(CraftBookPlugin.inst().getDataFolder(), "report.txt");
            Files.write(result, dest, StandardCharsets.UTF_8);
            actor.print("CraftBook report written to " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CraftbookException("Failed to write report: " + e.getMessage());
        }

        if (pastebin) {
            CraftBookPlugin.inst().checkPermission(BukkitAdapter.adapt(actor), "craftbook.report.pastebin");

            ActorCallbackPaste.pastebin(
                    CraftBookPlugin.inst().getSupervisor(),
                    actor,
                    result,
                    "CraftBook report: %s.report"
            );
        }
    }

    @Command(name = "dev", desc = "Advanced developer commands")
    @CommandPermissions({"craftbook.developer"})
    public void dev(Actor actor, @Arg(desc = "The class to run") String className) throws CommandPermissionsException {
        try {
            ExternalUtilityManager.performExternalUtility(className);
            actor.print("Performed utility successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            actor.printError("Failed to perform utility: See console for details!");
        }
    }

//    @Command(aliases = {"ic", "circuit"}, desc = "Commands to manage Craftbook IC's")
//    @NestedCommand(ICCommands.class)
//    public void icCmd(CommandContext context, CommandSender sender) {
//    }

}
