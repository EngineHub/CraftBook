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

package org.enginehub.craftbook.internal.util;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.enginehub.craftbook.util.TextUtil;
import org.enginehub.piston.util.TextHelper;

import java.util.Locale;
import java.util.stream.Collectors;

public class ComponentRstRenderer {

    public static String reduceToRst(Component component) {
        Component formatted = TextUtil.format(component, Locale.US);
        return formatAsRst(formatted, null);
    }

    private static String formatAsRst(Component component, String currentDeco) {
        if (component instanceof TextComponent) {
            StringBuilder content = new StringBuilder(((TextComponent) component).content());

            String deco = null;

            if (isSuggestingAsCommand(component, content.toString())) {
                deco = "``";
            }
            if (component.decorations().stream().anyMatch(de -> de == TextDecoration.BOLD)) {
                deco = "**";
            } else if (component.decorations().stream().anyMatch(de -> de == TextDecoration.ITALIC)) {
                deco = "*";
            }

            final String finalDeco = deco;
            component.children().stream().map(comp -> formatAsRst(comp, finalDeco)).forEach(content::append);

            if (deco != null) {
                if (currentDeco != null) {
                    throw new RuntimeException("Nested decorations are hell in RST. \n" +
                        "Existing: " + currentDeco + "; New: " + deco + "\n" +
                        "Offender: " + TextHelper.reduceToText(component));
                }
                rstDeco(content, deco);
            }

            return content.toString();
        } else if (component instanceof TranslatableComponent) {
            StringBuilder content = new StringBuilder(((TranslatableComponent) component).key());
            component.children().stream().map(comp -> formatAsRst(comp, currentDeco)).forEach(content::append);
            return content.toString();
        } else {
            return component.children().stream().map(comp -> formatAsRst(comp, currentDeco)).collect(Collectors.joining());
        }
    }

    private static boolean isSuggestingAsCommand(Component component, String text) {
        ClickEvent ce = component.clickEvent();
        if (ce != null && (ce.action() == ClickEvent.Action.RUN_COMMAND || ce.action() == ClickEvent.Action.SUGGEST_COMMAND)) {
            return ce.value().equals(text);
        }
        {
            return false;
        }
    }

    private static int firstNonWhitespace(StringBuilder builder) {
        for (int i = 0; i < builder.length(); i++) {
            if (!Character.isWhitespace(builder.charAt(i))) {
                return i;
            }
        }
        return 0;
    }

    private static int lastNonWhitespace(StringBuilder builder) {
        for (int i = builder.length(); i > 0; i--) {
            if (!Character.isWhitespace(builder.charAt(i))) {
                return i;
            }
        }
        return builder.length();
    }

    private static void rstDeco(StringBuilder builder, String deco) {
        builder.ensureCapacity(builder.length() + deco.length() * 2);
        int insertionPoint = firstNonWhitespace(builder);
        builder.insert(insertionPoint, deco);
        int endInsertionPoint = lastNonWhitespace(builder);
        builder.insert(endInsertionPoint, deco);
    }
}
