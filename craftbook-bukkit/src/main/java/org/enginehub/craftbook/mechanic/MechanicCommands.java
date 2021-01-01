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

package org.enginehub.craftbook.mechanic;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.component.InvalidComponentException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.BukkitCraftBookPlatform;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
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

            builder.addPart(SubCommandPart
                .builder(TranslatableComponent.of("worldedit.argument.action"), TextComponent.of("Sub-command to run."))
                .withCommands(innerManager.getAllCommands().collect(Collectors.toList()))
                .required()
                .build());
        });
    }

    public MechanicCommands() {
    }

    @Command(name = "enable", desc = "Enable a mechanic")
    @CommandPermissions({ "craftbook.mechanic.enable" })
    public void enable(Actor actor,
                       @Arg(desc = "The mechanic to enable")
                           MechanicType<?> mechanicType,
                       @ArgFlag(name = 'l', desc = "Show the list box", def = "0")
                               int listBoxPage) throws InvalidComponentException {
        CraftBookPlugin plugin = CraftBookPlugin.inst();
        try {
            CraftBook.getInstance().getPlatform().getMechanicManager().enableMechanic(mechanicType);
            CraftBook.getInstance().getPlatform().getConfiguration().enabledMechanics.add(mechanicType.getId());
            CraftBook.getInstance().getPlatform().getConfiguration().save();

            if (plugin.getCommandManager().getMechanicRegistrar().isDirty()) {
                ((BukkitCraftBookPlatform) CraftBook.getInstance().getPlatform()).resetCommandRegistration(plugin);
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            }

            actor.printInfo(TranslatableComponent.of(
                "craftbook.mechanisms.enable-success",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE)
            ));
        } catch (MechanicInitializationException e) {
            actor.printError(TranslatableComponent.of(
                "craftbook.mechanisms.enable-failed",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE),
                e.getRichMessage()
            ));
        }

        if (listBoxPage > 0) {
            showListBox(actor, listBoxPage);
        }
    }

    @Command(name = "disable", desc = "Disable a mechanic")
    @CommandPermissions({ "craftbook.mechanic.disable" })
    public void disable(Actor actor,
                        @Arg(desc = "The mechanic to disable")
                            MechanicType<?> mechanicType,
                        @ArgFlag(name = 'l', desc = "Show the list box", def = "0")
                                int listBoxPage) throws InvalidComponentException {
        CraftBookPlugin plugin = CraftBookPlugin.inst();
        Optional<?> mech = CraftBook.getInstance().getPlatform().getMechanicManager().getMechanic(mechanicType);
        if (mech.isPresent() && CraftBook.getInstance().getPlatform().getMechanicManager().disableMechanic((CraftBookMechanic) mech.get())) {
            CraftBook.getInstance().getPlatform().getConfiguration().enabledMechanics.remove(mechanicType.getId());
            CraftBook.getInstance().getPlatform().getConfiguration().save();

            if (plugin.getCommandManager().getMechanicRegistrar().isDirty()) {
                ((BukkitCraftBookPlatform) CraftBook.getInstance().getPlatform()).resetCommandRegistration(plugin);
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            }

            actor.printInfo(TranslatableComponent.of(
                "craftbook.mechanisms.disable-success",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE)
            ));
        } else {
            actor.printError(TranslatableComponent.of(
                "craftbook.mechanisms.not-enabled",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE)
            ));
        }

        if (listBoxPage > 0) {
            showListBox(actor, listBoxPage);
        }
    }

    @Command(name = "list", desc = "List mechanics")
    @CommandPermissions({ "craftbook.mechanic.list" })
    public void list(Actor actor, @ArgFlag(name = 'p', desc = "The page number", def = "1") int page) throws InvalidComponentException {
        showListBox(actor, page);
    }

    private void showListBox(Actor actor, int page) throws InvalidComponentException {
        MechanicListBox mechanicListBox = new MechanicListBox(actor);

        actor.print(mechanicListBox.create(page));
    }
}
