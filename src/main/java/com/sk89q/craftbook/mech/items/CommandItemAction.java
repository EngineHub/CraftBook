package com.sk89q.craftbook.mech.items;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.sk89q.craftbook.common.variables.VariableManager;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * An action that can be performed by a {@link CommandItemDefinition}
 */
public class CommandItemAction {

    protected String name;
    protected ActionType type;
    protected String value;
    protected ActionRunStage stage;

    public CommandItemAction(String name, ActionType type, String value, ActionRunStage stage) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.stage = stage;
    }

    /**
     * The type of action this {@link CommandItemAction} is.
     */
    public static enum ActionType {

        SETVAR,
        ISVAR;
    }

    /**
     * Defines when this {@link CommandItemAction} should run.
     * BEFORE should generally be for checks, whereas AFTER should generally be for changing things.
     */
    public static enum ActionRunStage {

        BEFORE, AFTER;
    }

    /**
     * Runs the action defined by this {@link CommandItemAction}
     * 
     * @param definition The {@link CommandItemDefinition} that is calling this action.
     * @param event The {@link Event} that the {@link CommandItemDefinition} was triggered by.
     * @param player The {@link Player} that the {@link CommandItemDefinition} was triggered by.
     * 
     * @return If this is a 'BEFORE' {@link ActionRunStage}, returning false causes the {@link CommandItemDefinition} to not run.
     */
    public boolean runAction(CommandItemDefinition definition, Event event, Player player) {

        switch(type) {
            case SETVAR:
                String[] svarParts = RegexUtil.EQUALS_PATTERN.split(value,2);
                String snamespace = VariableManager.instance.getNamespace(svarParts[0]);
                String svar = VariableManager.instance.getVariableName(svarParts[0]);
                VariableManager.instance.setVariable(svar, snamespace, CommandItems.INSTANCE.parseLine(svarParts[1], event, player));
                return true;
            case ISVAR:
                String[] isparts = RegexUtil.EQUALS_PATTERN.split(value,2);
                String isnamespace = VariableManager.instance.getNamespace(isparts[0]);
                String isvar = VariableManager.instance.getVariableName(isparts[0]);
                return VariableManager.instance.getVariable(isvar, isnamespace).equals(CommandItems.INSTANCE.parseLine(isparts[1], event, player));
            default:
                return true;
        }
    }
}