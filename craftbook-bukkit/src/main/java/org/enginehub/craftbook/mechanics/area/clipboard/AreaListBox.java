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

package org.enginehub.craftbook.mechanics.area.clipboard;

import com.google.common.base.Strings;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.nio.file.Path;
import java.util.List;

public class AreaListBox extends PaginationBox {

    private final Actor actor;
    private final List<Path> areaList;
    private final String namespace;
    private final boolean showAll;

    public AreaListBox(Actor actor, List<Path> areaList, String namespace, boolean showAll) {
        super(
            showAll ? "Toggle Areas" : (namespace.isBlank() ? "Toggle Areas (" + actor.getName() + ")" : "Toggle Areas (" + namespace + ")"),
            showAll ? "/area list -a -p %page%" : (namespace.isBlank() ? "/area list -p %page%" : "/area list -p %page% -n " + namespace)
        );

        this.actor = actor;
        this.areaList = areaList;
        this.namespace = namespace;
        this.showAll = showAll;
    }

    @Override
    public Component getComponent(int number) {
        Path area = areaList.get(number);

        String filename = area.getFileName().toString();
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }

        TextComponent areaName = TextComponent.of(filename, TextColor.WHITE);
        if (actor.isPlayer()) {
            areaName = areaName.hoverEvent(HoverEvent.showText(TextComponent.of(area.toString())));
        }

        TextComponent.Builder builder = TextComponent.builder().append(areaName).append(TextComponent.space());

        if (actor.isPlayer()) {
            TextComponent copyComponent = TextComponent
                    .of("[C]", TextColor.YELLOW)
                    .hoverEvent(HoverEvent.showText(TranslatableComponent.of("craftbook.togglearea.copy-name-to-clipboard")))
                    .clickEvent(ClickEvent.copyToClipboard(filename));

            int length = FontInfo.getPxLength(filename);
            int leftover = 200 - length - 4;
            if (leftover > 0) {
                builder.append(TextComponent.of(Strings.repeat(".", leftover / 2), TextColor.DARK_GRAY));
            }

            builder
                .append(TextComponent.space())
                .append(copyComponent);

            if (!namespace.isBlank() || !showAll) {
                TextComponent deleteComponent = TextComponent
                    .of("[D]", TextColor.RED)
                    .hoverEvent(HoverEvent.showText(TranslatableComponent.of("craftbook.togglearea.delete-area")))
                    .clickEvent(ClickEvent.runCommand("/area delete " + filename + (namespace.isBlank() ? "" : " -n " + namespace)));

                builder.append(TextComponent.space())
                    .append(deleteComponent);
            }
        }

        return builder.build();
    }

    @Override
    public int getComponentsSize() {
        return areaList.size();
    }

    private static final class FontInfo {
        static int getPxLength(char c) {
            return switch (c) {
                case 'i', ':' -> 1;
                case 'l' -> 2;
                case '*', 't' -> 3;
                case 'f', 'k' -> 4;
                default -> 5;
            };
        }

        static int getPxLength(String string) {
            return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
        }
    }
}
