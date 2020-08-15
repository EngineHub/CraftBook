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

import com.google.common.base.Strings;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.enginehub.craftbook.CraftBook;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MechanicListBox extends PaginationBox {

    private static final List<MechanicType<?>> MECHANICS = MechanicType.REGISTRY.values()
        .stream()
        .sorted(Comparator.comparing(MechanicType::getId))
        .collect(Collectors.toList());

    private final Actor actor;

    public MechanicListBox(Actor actor) {
        super("Mechanics", "/cb mech list -p %page%");

        this.actor = actor;
    }

    @Override
    public Component getComponent(int number) {
        MechanicType<?> mechanic = MECHANICS.get(number);
        MechanicManager manager = CraftBook.getInstance().getPlatform().getMechanicManager();

        boolean isEnabled = manager.isMechanicEnabled(mechanic);

        TextComponent.Builder builder = TextComponent.builder()
            .append(TextComponent.of("\u2588", isEnabled ? TextColor.GREEN : TextColor.RED))
            .append(TextComponent.space())
            .append(TextComponent.of(mechanic.getName(), TextColor.WHITE))
            .append(TextComponent.space());

        if (actor.isPlayer()) {
            TextComponent modifyComponent;
            if (isEnabled) {
                modifyComponent = TextComponent
                    .of("[Disable]", TextColor.RED)
                    .hoverEvent(HoverEvent.showText(TranslatableComponent.of("craftbook.mechanisms.click-to-disable")))
                    .clickEvent(ClickEvent.runCommand("/cb mech disable " + mechanic.getId() + " -l " + getCurrentPage()));
            } else {
                modifyComponent = TextComponent
                    .of("[Enable]", TextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(TranslatableComponent.of("craftbook.mechanisms.click-to-enable")))
                    .clickEvent(ClickEvent.runCommand("/cb mech enable " + mechanic.getId() + " -l " + getCurrentPage()));
            }

            int length = FontInfo.getPxLength(mechanic.getName());
            int leftover = 200 - length - 4;
            if (leftover > 0) {
                builder.append(TextComponent.of(Strings.repeat(".", leftover / 2), TextColor.DARK_GRAY));
            }

            builder
                .append(TextComponent.space())
                .append(modifyComponent);
        }

        return builder.build();
    }

    @Override
    public int getComponentsSize() {
        return MECHANICS.size();
    }

    private static final class FontInfo {
        static int getPxLength(char c) {
            switch (c) {
                case 'i':
                case ':':
                    return 1;
                case 'l':
                    return 2;
                case '*':
                case 't':
                    return 3;
                case 'f':
                case 'k':
                    return 4;
                default:
                    return 5;
            }
        }

        static int getPxLength(String string) {
            return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
        }
    }
}
