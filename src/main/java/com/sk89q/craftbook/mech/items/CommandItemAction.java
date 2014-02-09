package com.sk89q.craftbook.mech.items;

import org.bukkit.event.Event;

import com.sk89q.craftbook.util.RegexUtil;

/**
 * An action that can be performed by a {@link CommandItemDefinition}
 */
public class CommandItemAction {

    protected ActionType type;
    protected String value;
    protected ActionRunStage stage;

    public CommandItemAction(ActionType type, String value, ActionRunStage stage) {
        this.type = type;
        this.value = value;
        this.stage = stage;
    }

    /**
     * The type of action this {@link CommandItemAction} is.
     */
    public static enum ActionType {

        SETVAR;
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
     * @return If this is a 'BEFORE' {@link ActionRunStage}, returning false causes the {@link CommandItemDefinition} to not run.
     */
    public boolean runAction(CommandItemDefinition definition, Event event) {

        switch(type) {
            case SETVAR:
                String[] parts = RegexUtil.EQUALS_PATTERN.split(value,2);
                String[] varBits = RegexUtil.PIPE_PATTERN.split(parts[0],2);
                return true;
            default:
                return true;
        }
    }
}