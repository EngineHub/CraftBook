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

package com.sk89q.craftbook.core.mechanic;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.CraftBookException;
import com.sk89q.craftbook.util.exceptions.MechanicInitializationException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Optional;
import java.util.stream.Collectors;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class MechanicCommands {

    public static void register(CommandManagerService service, CommandManager commandManager, CommandRegistrationHandler registration) {
        commandManager.register("mechanic", builder -> {
            builder.aliases(Lists.newArrayList("mech", "mechs", "mechanics"));
            builder.description(TextComponent.of("Mechanic Commands"));

            CommandManager innerManager = service.newCommandManager();
            registration.register(
                    innerManager,
                    MechanicCommandsRegistration.builder(),
                    new MechanicCommands()
            );

            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"), TextComponent.of("Sub-command to run."))
                    .withCommands(innerManager.getAllCommands().collect(Collectors.toList()))
                    .required()
                    .build());
        });
    }

    public MechanicCommands() {
    }

    @Command(name = "enable", desc = "Enable a mechanic")
    @CommandPermissions({"craftbook.enable-mechanic"})
    public void enable(Actor actor, @Arg(desc = "The mechanic to enable") MechanicType<?> mechanicType) throws CraftBookException {
        CraftBookPlugin plugin = CraftBookPlugin.inst();
        try {
            plugin.getMechanicManager().enableMechanic(mechanicType);
            plugin.getConfiguration().enabledMechanics.add(mechanicType.getId());
            plugin.getConfiguration().save();

            if (plugin.getCommandManager().getMechanicRegistrar().isDirty()) {
                plugin.getCommandManager().resetCommandRegistration(plugin);
            }

            actor.print("Sucessfully enabled " + mechanicType.getName());
        } catch (MechanicInitializationException e) {
            actor.printError("Failed to load " + mechanicType.getName());
        }
    }

    @Command(name = "disable", desc = "Disable a mechanic")
    @CommandPermissions({"craftbook.disable-mechanic"})
    public void disable(Actor actor, @Arg(desc = "The mechanic to enable") MechanicType<?> mechanicType) throws CommandPermissionsException {
        CraftBookPlugin plugin = CraftBookPlugin.inst();
        Optional<?> mech = plugin.getMechanicManager().getMechanic(mechanicType);
        if (mech.isPresent() && plugin.getMechanicManager().disableMechanic((CraftBookMechanic) mech.get())) {
            plugin.getConfiguration().enabledMechanics.remove(mechanicType.getId());
            plugin.getConfiguration().save();

            if (plugin.getCommandManager().getMechanicRegistrar().isDirty()) {
                plugin.getCommandManager().resetCommandRegistration(plugin);
            }

            actor.print("Sucessfully disabled " + mechanicType.getName());
        } else {
            actor.printError("Failed to remove " + mechanicType.getName());
        }
    }

}
