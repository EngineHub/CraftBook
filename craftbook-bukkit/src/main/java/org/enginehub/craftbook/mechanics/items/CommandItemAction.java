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

package org.enginehub.craftbook.mechanics.items;

import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.gates.variables.NumericModifier.MathFunction;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;
import org.enginehub.craftbook.util.RegexUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * An action that can be performed by a {@link CommandItemDefinition}
 */
public class CommandItemAction {

    protected String name;
    protected ActionType type;
    protected String value;
    ActionRunStage stage;

    public CommandItemAction(String name, ActionType type, String value, ActionRunStage stage) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.stage = stage;
    }

    /**
     * The type of action this {@link CommandItemAction} is.
     */
    enum ActionType {

        SETVAR,
        MATHVAR,
        ISVAR,
        GREATERVAR,
        LESSVAR
    }

    /**
     * Defines when this {@link CommandItemAction} should run.
     * BEFORE should generally be for checks, whereas AFTER should generally be for changing things.
     */
    enum ActionRunStage {

        BEFORE, AFTER
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
    public boolean runAction(CommandItemDefinition definition, Event event, Player player) throws VariableException {

        String newVal = CommandItems.parseLine(value, event, player);
        CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(player);

        switch(type) {
            case SETVAR:
                String[] svarParts = RegexUtil.EQUALS_PATTERN.split(newVal,2);
                VariableKey sVariableKey = VariableKey.fromString(svarParts[0], localPlayer);
                VariableManager.instance.setVariable(sVariableKey, svarParts[1]);
                return true;
            case MATHVAR:
                String[] mvarParts = RegexUtil.EQUALS_PATTERN.split(newVal,2);
                VariableKey mVariableKey = VariableKey.fromString(mvarParts[0], localPlayer);

                String[] mathFunctionParts = RegexUtil.COLON_PATTERN.split(mvarParts[1], 2);
                MathFunction func = MathFunction.parseFunction(mathFunctionParts[0]);

                String cur = VariableManager.instance.getVariable(mVariableKey);
                if(cur == null || cur.isEmpty()) cur = "0";

                double currentValue = Double.parseDouble(cur);
                double amount = Double.parseDouble(mathFunctionParts[1]);

                currentValue = func.parseNumber(currentValue, amount);

                String val = String.valueOf(currentValue);
                if (val.endsWith(".0"))
                    val = StringUtils.replace(val, ".0", "");

                VariableManager.instance.setVariable(mVariableKey, val);
                return true;
            case ISVAR: {
                String[] isparts = RegexUtil.EQUALS_PATTERN.split(newVal, 2);
                VariableKey isVariableKey = VariableKey.fromString(isparts[0], localPlayer);
                return VariableManager.instance.getVariable(isVariableKey).equals(isparts[1]);
            }
            case GREATERVAR: {
                String[] isparts = RegexUtil.GREATER_THAN_PATTERN.split(newVal, 2);
                VariableKey isVariableKey = VariableKey.fromString(isparts[0], localPlayer);
                double variable = 0;
                double test = 0;
                try {
                    variable = Double.parseDouble(VariableManager.instance.getVariable(isVariableKey));
                    test = Double.parseDouble(isparts[1]);
                } catch(NumberFormatException e) {
                    CraftBook.logger.warn("Variable " + isparts[0] + " is not a number!");
                }
                return variable > test;
            }
            case LESSVAR: {
                String[] isparts = RegexUtil.LESS_THAN_PATTERN.split(newVal, 2);
                VariableKey isVariableKey = VariableKey.fromString(isparts[0], localPlayer);
                double variable = 0;
                double test = 0;
                try {
                    variable = Double.parseDouble(VariableManager.instance.getVariable(isVariableKey));
                    test = Double.parseDouble(isparts[1]);
                } catch(NumberFormatException e) {
                    CraftBook.logger.warn("Variable " + isparts[0] + " is not a number!");
                }
                return variable < test;
            }
            default:
                return true;
        }
    }
}