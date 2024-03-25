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

package org.enginehub.craftbook.mechanics.items;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitCraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanics.items.CommandItemDefinition.CommandType;
import org.enginehub.craftbook.util.EnumUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.TernaryState;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.ArrayList;
import java.util.List;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class CommandItemCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
            commandManager,
            CommandItemCommandsRegistration.builder(),
            new CommandItemCommands()
        );
    }

    public CommandItemCommands() {
        setupAddCommand(CraftBookPlugin.inst());
    }

    @Command(name = "give", desc = "Gives the player the item.")
    public void giveItem(Actor actor,
                         @Arg(desc = "The commanditem to give") String name,
                         @ArgFlag(name = 'p', desc = "The player to target") String otherPlayer,
                         @ArgFlag(name = 'a', desc = "Amount to give", def = "1") int amount,
                         @Switch(name = 's', desc = "Silence output") boolean silent
    ) throws CraftBookException, AuthorizationException {

        Player player;

        if (otherPlayer != null)
            player = Bukkit.getPlayer(otherPlayer);
        else if (!(actor instanceof CraftBookPlayer))
            throw new CraftBookException("Please provide a player! (-p flag)");
        else
            player = ((BukkitCraftBookPlayer) actor).getPlayer();

        if (player == null)
            throw new CraftBookException("Unknown Player!");

        if (!actor.hasPermission("craftbook.mech.commanditems.give" + (otherPlayer != null ? ".others" : "") + "." + name))
            throw new AuthorizationException();

        CommandItemDefinition def = CommandItems.INSTANCE.getDefinitionByName(name);
        if (def == null)
            throw new CraftBookException("Invalid CommandItem!");

        ItemStack stack = ItemUtil.makeItemValid(def.getItem().clone());
        stack.setAmount(stack.getAmount() * amount);

        if (!player.getInventory().addItem(stack).isEmpty())
            throw new CraftBookException("Failed to add item to inventory!");

        if (!silent)
            actor.print("Gave CommandItem " + ChatColor.BLUE + def.getName() + ChatColor.YELLOW + " to " + player.getName());
    }

    @Command(name = "spawn", desc = "Spawns the item at the coordinates")
    public void spawnItem(Actor actor,
                          @Arg(desc = "The commanditem to give") String name,
                          @Arg(desc = "The location to spawn") Vector3 location,
                          @ArgFlag(name = 'w', desc = "The world") World world,
                          @ArgFlag(name = 'a', desc = "Amount to give", def = "1") int amount,
                          @Switch(name = 's', desc = "Silence output") boolean silent
    ) throws CraftBookException, AuthorizationException {
        if (!actor.hasPermission("craftbook.mech.commanditems.spawn" + name))
            throw new AuthorizationException();

        CommandItemDefinition def = CommandItems.INSTANCE.getDefinitionByName(name);
        if (def == null)
            throw new CraftBookException("Invalid CommandItem!");

        if (world == null && actor instanceof CraftBookPlayer) {
            world = BukkitAdapter.adapt(((CraftBookPlayer) actor).getWorld());
        }

        if (world == null) {
            throw new CraftBookException("You must be a player or specify a valid world to use this command.");
        }

        ItemStack stack = def.getItem().clone();

        stack = ItemUtil.makeItemValid(stack);
        stack.setAmount(stack.getAmount() * amount);

        world.dropItem(new Location(world, location.x(), location.y(), location.z()), stack);

        if (!silent)
            actor.print("Spawned CommandItem " + ChatColor.BLUE + def.getName() + ChatColor.YELLOW + " at " + location.toString() + " in " + world.getName());
    }

    private ConversationFactory conversationFactory;

    @Command(name = "create", aliases = { "add" }, desc = "Create a new CommandItem.")
    @CommandPermissions("craftbook.mech.commanditems.create")
    public void create(CraftBookPlayer player) throws CraftBookException {
        Player bukkitPlayer = ((BukkitCraftBookPlayer) player).getPlayer();
        if (bukkitPlayer.getInventory().getItemInMainHand().getType() == Material.AIR)
            throw new CraftBookException("Invalid Item for CommandItems!");
        Conversation convo = conversationFactory.buildConversation(bukkitPlayer);
        convo.getContext().setSessionData("item", bukkitPlayer.getInventory().getItemInHand());
        List<ItemStack> consumables = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            if (i == bukkitPlayer.getInventory().getHeldItemSlot())
                continue;
            ItemStack stack = bukkitPlayer.getInventory().getItem(i);
            if (!ItemUtil.isStackValid(stack))
                continue;
            consumables.add(stack);
        }
        convo.getContext().setSessionData("consumables", consumables);

        convo.begin();
    }

    public void setupAddCommand(CraftBookPlugin plugin) {
        conversationFactory = new ConversationFactory(plugin).withModality(true).withEscapeSequence("cancel").withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Please enter a unique ID for this CommandItem. (Used in /comitems give) (Type 'cancel' to quit)";
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {

                if (input.trim().length() == 0)
                    return Prompt.END_OF_CONVERSATION;
                context.setSessionData("name", input);
                return new CommandPrompt();
            }
        });
    }

    private static class CommandPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter the commands you wish for this CommandItem to perform (Without the /). Please enter one per message. (Type 'done' to stop entering commands)";
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public Prompt acceptInput(ConversationContext context, final String input) {

            if (input.trim().length() == 0 || input.trim().equalsIgnoreCase("done")) {
                if (context.getSessionData("commands") == null)
                    context.setSessionData("commands", new ArrayList<String>());
                return new RunAsPrompt();
            }

            if (context.getSessionData("commands") == null)
                context.setSessionData("commands", new ArrayList<String>() {{
                    add(input.trim());
                }});
            else {
                ArrayList<String> list = (ArrayList<String>) context.getSessionData("commands");
                list.add(input.trim());
                context.setSessionData("commands", list);
            }
            return new CommandPrompt();
        }
    }

    private static class RunAsPrompt extends FixedSetPrompt {

        public RunAsPrompt() {
            super(EnumUtil.getStringArrayFromEnum(CommandItemDefinition.CommandType.class));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter what you would like the CommandItem to run as.";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            context.setSessionData("run-as", CommandItemDefinition.CommandType.valueOf(input));
            return new EventPrompt();
        }
    }

    private static class EventPrompt extends FixedSetPrompt {

        public EventPrompt() {
            super(EnumUtil.getStringArrayFromEnum(ClickType.class));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter what event you want the CommandItem to be triggered by.";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            context.setSessionData("click-type", ClickType.valueOf(input));
            return new PermissionNodePromp();
        }
    }

    private static class PermissionNodePromp extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter the permission node you wish users to require to use the CommandItem. (Type 'none' for none.)";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            input = input.trim();

            if (input.length() > 0 && !input.equalsIgnoreCase("none"))
                context.setSessionData("permission-node", input);
            else
                context.setSessionData("permission-node", "");

            return new CooldownPromp();
        }
    }

    private static class CooldownPromp extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter the cooldown for using this CommandItem. (Type '0' for none)";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            context.setSessionData("cooldown", input.intValue());

            return new CancelActionPrompt();
        }
    }

    private static class CancelActionPrompt extends BooleanPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Do you wish the action that is being performed to not occur when the CommandItem is used? (Eg, damaging entities)";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {

            context.setSessionData("cancel-action", input);

            return new ConsumeSelfPrompt();
        }
    }

    private static class ConsumeSelfPrompt extends BooleanPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Do you wish the CommandItem to be consumed when used?";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {

            context.setSessionData("consume-self", input);

            return new RequireSneakingPrompt();
        }
    }

    private static class RequireSneakingPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "What sneaking state do you want players to require?";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("require-sneaking", TernaryState.parseTernaryState(input));
            return new DelayPrompt();
        }
    }

    private static class DelayPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter a delay after which an extra set of Commands will be performed. (Useful for turning off CommandItems after a delay). (Type '0' for none)";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            context.setSessionData("delay", input.intValue());

            if (input.intValue() == 0)
                return new KeepOnDeathPrompt();
            else
                return new DelayedCommandPrompt();
        }
    }

    private static class DelayedCommandPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Please enter the commands you wish for this CommandItem to perform after the delay (Without the /). Please enter one per message. (Type 'done' to stop entering commands)";
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public Prompt acceptInput(ConversationContext context, final String input) {

            if (input.trim().length() == 0 || input.trim().equalsIgnoreCase("done"))
                return new KeepOnDeathPrompt();

            if (context.getSessionData("delayed-commands") == null)
                context.setSessionData("delayed-commands", new ArrayList<String>() {{
                    add(input.trim());
                }});
            else {
                ArrayList<String> list = (ArrayList<String>) context.getSessionData("delayed-commands");
                list.add(input.trim());
                context.setSessionData("delayed-commands", list);
            }
            return new DelayedCommandPrompt();
        }
    }

    private static class KeepOnDeathPrompt extends BooleanPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Do you wish the CommandItem to be kept on death?";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {

            context.setSessionData("keep-on-death", input);

            return new CreateItemPrompt();
        }
    }

    private static class CreateItemPrompt extends MessagePrompt {

        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText(ConversationContext context) {

            try {
                String name = (String) context.getSessionData("name");
                ItemStack stack = (ItemStack) context.getSessionData("item");
                List<ItemStack> consumables = (List<ItemStack>) context.getSessionData("consumables");

                List<String> commands = (List<String>) context.getSessionData("commands");
                String permNode = (String) context.getSessionData("permission-node");
                CommandType type = (CommandType) context.getSessionData("run-as");
                ClickType clickType = (ClickType) context.getSessionData("click-type");
                int delay = (Integer) context.getSessionData("delay");
                List<String> delayedCommands = new ArrayList<>();
                if (delay > 0)
                    delayedCommands = (List<String>) context.getSessionData("delayed-commands");
                int cooldown = (Integer) context.getSessionData("cooldown");
                boolean cancelAction = (Boolean) context.getSessionData("cancel-action");
                boolean consumeSelf = (Boolean) context.getSessionData("consume-self");
                TernaryState requireSneaking = (TernaryState) context.getSessionData("require-sneaking");
                boolean keepOnDeath = (Boolean) context.getSessionData("keep-on-death");
                List<CommandItemAction> actions = new ArrayList<>();
                String missingConsumableMessage = "mech.command-items.need";
                String cooldownMessage = "mech.command-items.wait";
                CommandItemDefinition def = new CommandItemDefinition(name, stack, type, clickType, permNode,
                    commands.toArray(new String[commands.size()]), delay, delayedCommands.toArray(new String[delayedCommands.size()]),
                    cooldown, cancelAction, consumables.toArray(new ItemStack[consumables.size()]), consumeSelf, requireSneaking,
                    keepOnDeath, actions.toArray(new CommandItemAction[actions.size()]), missingConsumableMessage, cooldownMessage,
                    false);
                CommandItems.INSTANCE.addDefinition(def);
                CommandItems.INSTANCE.save();
                return ChatColor.YELLOW + "Successfully added CommandItem: " + name;
            } catch (Exception e) {
                e.printStackTrace();
                return ChatColor.RED + "Failed to add CommandItem! See Console for more details!";
            }
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }
    }
}