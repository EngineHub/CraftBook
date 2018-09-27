package com.sk89q.craftbook.mechanics.items;

import com.sk89q.craftbook.mechanics.items.CommandItemAction.ActionRunStage;
import com.sk89q.craftbook.mechanics.items.CommandItemAction.ActionType;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommandItemDefinition {

    protected String name;
    protected ItemStack stack;
    protected String permNode;
    protected CommandType type;
    protected ClickType clickType;
    protected boolean fakeCommand;

    protected String[] commands;

    protected String[] delayedCommands;
    protected int delay;
    protected int cooldown;
    protected boolean cancelAction;

    protected ItemStack[] consumables;
    protected boolean consumeSelf;

    protected TernaryState requireSneaking;

    protected boolean keepOnDeath;

    protected String missingConsumableMessage;
    protected String cooldownMessage;

    protected CommandItemAction[] actions;

    public ItemStack getItem() {

        return stack;
    }

    public String getName() {

        return name;
    }

    public CommandItemDefinition(String name, ItemStack stack, CommandType type, ClickType clickType, String permNode, String[] commands, int
            delay, String[] delayedCommands, int cooldown, boolean cancelAction, ItemStack[] consumables, boolean consumeSelf, TernaryState
            requireSneaking, boolean keepOnDeath, CommandItemAction[] actions, String missingConsumableMessage, String cooldownMessage, boolean fakeCommand) {

        this.name = name;
        this.stack = stack;
        this.type = type;
        this.permNode = permNode;
        this.commands = commands;
        this.delay = delay;
        this.delayedCommands = delayedCommands;
        this.cooldown = cooldown;
        this.clickType = clickType;
        this.cancelAction = cancelAction;
        this.consumables = consumables;
        this.consumeSelf = consumeSelf;
        this.requireSneaking = requireSneaking;
        this.keepOnDeath = keepOnDeath;
        this.actions = actions;
        this.missingConsumableMessage = missingConsumableMessage;
        this.cooldownMessage = cooldownMessage;
        this.fakeCommand = fakeCommand;
    }

    public static CommandItemDefinition load(YAMLProcessor config, String path) {

        String name = RegexUtil.PERIOD_PATTERN.split(path)[1];
        ItemStack stack = ItemSyntax.getItem(config.getString(path + ".item"));
        List<String> commands = config.getStringList(path + ".commands", new ArrayList<>());
        String permNode = config.getString(path + ".permission-node", "");
        CommandType type = CommandType.valueOf(config.getString(path + ".run-as", "PLAYER").toUpperCase(Locale.ENGLISH));
        ClickType clickType = ClickType.valueOf(config.getString(path + ".click-type", "CLICK_RIGHT").toUpperCase(Locale.ENGLISH));
        int delay = config.getInt(path + ".delay", 0);
        List<String> delayedCommands = new ArrayList<>();
        if(delay > 0)
            delayedCommands = config.getStringList(path + ".delayed-commands", new ArrayList<>());
        int cooldown = config.getInt(path + ".cooldown", 0);
        boolean cancelAction = config.getBoolean(path + ".cancel-action", true);

        List<ItemStack> consumables = new ArrayList<>();

        try {
            for(String s : config.getStringList(path + ".consumed-items", new ArrayList<>()))
                consumables.add(ItemUtil.makeItemValid(ItemSyntax.getItem(s)));
        } catch(Exception ignored){}

        boolean consumeSelf = config.getBoolean(path + ".consume-self", false);
        TernaryState requireSneaking = TernaryState.getFromString(config.getString(path + ".require-sneaking-state", "either"));

        boolean keepOnDeath = config.getBoolean(path + ".keep-on-death", false);
        boolean fakeCommand = config.getBoolean(path + ".fake-command-compatibility", false);

        List<CommandItemAction> actionList = new ArrayList<>();

        if(config.getKeys(path + ".actions") != null)
            for(String ac : config.getKeys(path + ".actions")) {

                ActionType acType = ActionType.valueOf(config.getString(path + ".actions." + ac + ".type"));
                String acValue = config.getString(path + ".actions." + ac + ".value");
                ActionRunStage acStage = ActionRunStage.valueOf(config.getString(path + ".actions." + ac + ".run-stage"));

                actionList.add(new CommandItemAction(ac, acType, acValue, acStage));
            }

        String missingConsumableMessage = config.getString(path + ".consumable-message", "mech.command-items.need");
        String cooldownMessage = config.getString(path + ".cooldown-message", "mech.command-items.wait");

        return new CommandItemDefinition(name, stack, type, clickType, permNode, commands.toArray(new String[commands.size()]), delay,
                delayedCommands.toArray(new String[delayedCommands.size()]), cooldown, cancelAction, consumables.toArray(new ItemStack[consumables
                .size()]), consumeSelf, requireSneaking, keepOnDeath, actionList.toArray(new CommandItemAction[actionList.size()]), missingConsumableMessage, cooldownMessage, fakeCommand);
    }

    public void save(YAMLProcessor config, String path) {

        config.setProperty(path + ".item", ItemSyntax.getStringFromItem(stack));
        config.setProperty(path + ".commands", commands);
        config.setProperty(path + ".permission-node", permNode);
        config.setProperty(path + ".run-as", type.name());
        config.setProperty(path + ".click-type", clickType.name());
        config.setProperty(path + ".delay", delay);
        if(delay > 0)
            config.setProperty(path + ".delayed-commands", delayedCommands);
        config.setProperty(path + ".cooldown", cooldown);
        config.setProperty(path + ".cancel-action", cancelAction);

        List<String> consumables = new ArrayList<>();
        for(ItemStack s : this.consumables)
            consumables.add(ItemSyntax.getStringFromItem(s));
        config.setProperty(path + ".consumed-items", consumables);
        config.setProperty(path + ".consume-self", consumeSelf);
        config.setProperty(path + ".require-sneaking-state", requireSneaking.name());
        config.setProperty(path + ".keep-on-death", keepOnDeath);
        config.setProperty(path + ".fake-command-compatibility", fakeCommand);

        config.addNode(path + ".actions");
        for(CommandItemAction ac : actions) {
            config.addNode(path + ".actions." + ac.name);
            config.setProperty(path + ".actions." + ac.name + ".type", ac.type.name());
            config.setProperty(path + ".actions." + ac.name + ".value", ac.value);
            config.setProperty(path + ".actions." + ac.name + ".run-stage", ac.stage.name());
        }

        config.setProperty(path + ".consumable-message", missingConsumableMessage);
        config.setProperty(path + ".cooldown-message", cooldownMessage);
    }

    enum CommandType {
        PLAYER,CONSOLE,SUPERUSER
    }
}