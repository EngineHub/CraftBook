package com.sk89q.craftbook.bukkit.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mech.CommandItems;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition.ClickType;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition.CommandType;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;

public class CommandItemCommands {

    public CommandItemCommands(CraftBookPlugin plugin) {
        setupAddCommand(plugin);
    }

    //private CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(aliases = {"give"}, desc = "Gives the player the item.", flags = "p:", usage = "CommandItem Name [-p player]", min = 1, max = 1)
    public void giveItem(CommandContext context, CommandSender sender) throws CommandException {

        Player player = null;

        if(context.hasFlag('p'))
            player = Bukkit.getPlayer(context.getFlag('p'));
        else if(!(sender instanceof Player))
            throw new CommandException("Please provide a player! (-p flag)");
        else
            player = (Player) sender;

        if(player == null)
            throw new CommandException("Unknown Player!");

        if(CommandItems.INSTANCE == null)
            throw new CommandException("CommandItems are not enabled!");

        if(!sender.hasPermission("craftbook.mech.commanditems.give." + context.getString(0)))
            throw new CommandPermissionsException();

        CommandItemDefinition def = CommandItems.INSTANCE.getDefinitionByName(context.getString(0));
        if(def == null)
            throw new CommandException("Invalid CommandItem!");
        if(!((Player) sender).getInventory().addItem(def.getItem()).isEmpty())
            throw new CommandException("Failed to add item to inventory!");
    }

    private ConversationFactory conversationFactory;

    @Command(aliases = {"add", "create"}, desc = "Create a new CommandItem.")
    @CommandPermissions("craftbook.mech.commanditems.create")
    public void addCommandItem(CommandContext context, CommandSender sender) throws CommandException {

        if(!(sender instanceof Player))
            throw new CommandException("Can only add CommandItems as a player!");
        if(((Player) sender).getInventory().getItemInHand() == null)
            throw new CommandException("Invalid Item for CommandItems!");
        Conversation convo = conversationFactory.buildConversation((Conversable) sender);
        convo.getContext().setSessionData("item", ((HumanEntity) sender).getInventory().getItemInHand());
        List<ItemStack> consumables = new ArrayList<ItemStack>();
        for(int i = 0; i <= 8; i++) {
            if(i == ((HumanEntity) sender).getInventory().getHeldItemSlot())
                continue;
            ItemStack stack = ((HumanEntity) sender).getInventory().getItem(i);
            if(!ItemUtil.isStackValid(stack))
                continue;
            consumables.add(stack);
        }
        convo.getContext().setSessionData("consumables", consumables);

        convo.begin();
    }

    public void setupAddCommand(CraftBookPlugin plugin) {
        conversationFactory = new ConversationFactory(plugin).withModality(true).withEscapeSequence("cancel").withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            @Override
            public String getPromptText (ConversationContext context) {
                return "Please enter a unique ID for this CommandItem. (Used in /comitems give) (Type 'cancel' to quit)";
            }

            @Override
            public Prompt acceptInput (ConversationContext context, String input) {

                if(input.trim().length() == 0)
                    return Prompt.END_OF_CONVERSATION;
                context.setSessionData("name", input);
                return new CommandPrompt();
            }
        });
    }

    private static class CommandPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return "Please enter the commands you wish for this CommandItem to perform (Without the /). Please enter one per message. (Leave blank to stop entering commands)";
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public Prompt acceptInput (ConversationContext context, final String input) {

            if(input.trim().length() == 0)
                return new RunAsPrompt();

            if(context.getSessionData("commands") == null)
                context.setSessionData("commands", new ArrayList<String>(){{add(input.trim());}});
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
            super(EnumUtil.getStringArrayFromEnum(CommandItems.CommandItemDefinition.CommandType.class));
        }

        @Override
        public String getPromptText (ConversationContext context) {
            return "Please enter what you would like the CommandItem to run as.";
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, String input) {

            context.setSessionData("run-as", CommandItems.CommandItemDefinition.CommandType.valueOf(input));
            return new EventPrompt();
        }
    }

    private static class EventPrompt extends FixedSetPrompt {

        public EventPrompt() {
            super(EnumUtil.getStringArrayFromEnum(CommandItems.CommandItemDefinition.ClickType.class));
        }

        @Override
        public String getPromptText (ConversationContext context) {
            return "Please enter what event you want the CommandItem to be triggered by.";
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, String input) {

            context.setSessionData("click-type", CommandItems.CommandItemDefinition.ClickType.valueOf(input));
            return new PermissionNodePromp();
        }
    }

    private static class PermissionNodePromp extends StringPrompt {

        @Override
        public String getPromptText (ConversationContext context) {
            return "Please enter the permission node you wish users to require to use the CommandItem. (Blank for none)";
        }

        @Override
        public Prompt acceptInput (ConversationContext context, String input) {

            input = input.trim();

            if(input.length() > 0)
                context.setSessionData("permission-node", input);

            return new CooldownPromp();
        }
    }

    private static class CooldownPromp extends NumericPrompt {

        @Override
        public String getPromptText (ConversationContext context) {
            return "Please enter the cooldown for using this CommandItem. (Blank for none)";
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {

            if(input.length() == 0)
                return true;
            return super.isInputValid(context, input);
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, Number input) {

            context.setSessionData("cooldown", input.intValue());

            return new CancelActionPrompt();
        }
    }

    private static class CancelActionPrompt extends BooleanPrompt {

        @Override
        public String getPromptText (ConversationContext context) {
            return "Do you wish the action that is being performed to not occur when the CommandItem is used? (Eg, damaging entities)";
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, boolean input) {

            context.setSessionData("cancel-action", input);

            return new ConsumeSelfPrompt();
        }
    }

    private static class ConsumeSelfPrompt extends BooleanPrompt {

        @Override
        public String getPromptText (ConversationContext context) {
            return "Do you wish the CommandItem to be consumed when used?";
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, boolean input) {

            context.setSessionData("consume-self", input);

            return new RequireSneakingPrompt();
        }
    }

    private static class RequireSneakingPrompt extends FixedSetPrompt {

        public RequireSneakingPrompt() {
            super(EnumUtil.getStringArrayFromEnum(TernaryState.class));
        }

        @Override
        public String getPromptText (ConversationContext context) {
            return "What sneaking state do you want players to require?";
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, String input) {

            context.setSessionData("require-sneaking", TernaryState.valueOf(input));
            return new DelayPrompt();
        }
    }

    private static class DelayPrompt extends NumericPrompt {

        @Override
        public String getPromptText (ConversationContext context) {
            return "Please enter a delay after which an extra set of Commands will be performed. (Useful for turning off CommandItems after a delay). (Blank for none)";
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {

            if(input.length() == 0)
                return true;
            return super.isInputValid(context, input);
        }

        @Override
        protected Prompt acceptValidatedInput (ConversationContext context, Number input) {

            context.setSessionData("delay", input.intValue());

            if(input.intValue() == 0)
                return new CreateItemPrompt();
            else
                return new DelayedCommandPrompt();
        }
    }

    private static class DelayedCommandPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return "Please enter the commands you wish for this CommandItem to perform after the delay (Without the /). Please enter one per message. (Leave blank to stop entering commands)";
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public Prompt acceptInput (ConversationContext context, final String input) {

            if(input.trim().length() == 0)
                return new CreateItemPrompt();

            if(context.getSessionData("delayed-commands") == null)
                context.setSessionData("delayed-commands", new ArrayList<String>(){{add(input.trim());}});
            else {
                ArrayList<String> list = (ArrayList<String>) context.getSessionData("delayed-commands");
                list.add(input.trim());
                context.setSessionData("delayed-commands", list);
            }
            return new DelayedCommandPrompt();
        }
    }

    private static class CreateItemPrompt extends MessagePrompt {

        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText (ConversationContext context) {

            try {
                String name = (String) context.getSessionData("name");
                ItemStack stack = (ItemStack) context.getSessionData("item");
                List<ItemStack> consumables = (List<ItemStack>) context.getSessionData("consumables");

                List<String> commands = (List<String>) context.getSessionData("commands");
                String permNode = (String) context.getSessionData("permission-node");
                CommandType type = (CommandType) context.getSessionData("run-as");
                ClickType clickType = (ClickType) context.getSessionData("click-type");
                int delay = (Integer) context.getSessionData("delay");
                List<String> delayedCommands = new ArrayList<String>();
                if(delay > 0)
                    delayedCommands = (List<String>) context.getSessionData("delatyed-commands");
                int cooldown = (Integer) context.getSessionData("cooldown");
                boolean cancelAction = (Boolean) context.getSessionData("cancel-action");
                boolean consumeSelf = (Boolean) context.getSessionData("consume-self");
                TernaryState requireSneaking = (TernaryState) context.getSessionData("require-sneaking");

                CommandItemDefinition def = new CommandItemDefinition(name, stack, type, clickType, permNode, commands.toArray(new String[commands.size()]), delay, delayedCommands.toArray(new String[delayedCommands.size()]), cooldown, cancelAction, consumables.toArray(new ItemStack[consumables.size()]), consumeSelf, requireSneaking);

                CommandItems.INSTANCE.addDefinition(def);
                CommandItems.INSTANCE.save();
                return "Successfully added CommandItem: " + name;
            } catch(Exception e) {
                BukkitUtil.printStacktrace(e);
                return "Failed to add CommandItem! See Console for more details!";
            }
        }

        @Override
        protected Prompt getNextPrompt (ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }
    }
}