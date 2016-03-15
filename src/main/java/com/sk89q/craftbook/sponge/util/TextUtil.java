/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class TextUtil {

    public static Text transform(Text text, Function<Text, TextRepresentable> transformer) {
        checkNotNull(transformer, "transformer");

        List<Text> children = null;
        if (!text.getChildren().isEmpty()) {
            int i = 0;
            for (Text child : text.getChildren()) {
                Text newChild = transform(child, transformer);
                if (child != newChild) {
                    if (children == null) {
                        children = new ArrayList<>(text.getChildren());
                    }

                    children.set(i, newChild);
                }

                i++;
            }
        }

        TextRepresentable newText = checkNotNull(transformer.apply(text), "newText");
        if (text != newText) {
            return newText.toText();
        } else if (children != null) {
            return newText.toText().toBuilder().removeAll().append(children).build();
        }

        return text;
    }
}
