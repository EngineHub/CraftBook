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
