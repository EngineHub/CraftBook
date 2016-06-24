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
package com.sk89q.craftbook.sponge.util.locale;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;
import org.spongepowered.api.text.translation.locale.Locales;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Locale;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Function;

public class TranslationsManager {

    //A few commonly used translated texts.
    public static TranslatableText USE_PERMISSIONS;

    private static Function<Locale, ResourceBundle> resourceBundleFunction;

    private static LoadingCache<Locale, ResourceBundle> resourceBundleCache = CacheBuilder.newBuilder().build(new ResourceBundleLoader());

    public static void initialize() {
        resourceBundleFunction = locale -> Optional.of(resourceBundleCache.getUnchecked(locale)).orElse(resourceBundleCache.getUnchecked(Locales.EN_US));

        USE_PERMISSIONS = TranslatableText.builder(new ResourceBundleTranslation("mechanic.use-permissions", resourceBundleFunction)).color(TextColors.RED).build();
    }

    public static Function<Locale, ResourceBundle> getResourceBundleFunction() {
        return resourceBundleFunction;
    }

    private static class ResourceBundleLoader extends CacheLoader<Locale, ResourceBundle> {
        @Override
        @NonnullByDefault
        public ResourceBundle load(Locale key) throws Exception {
            return PropertyResourceBundle.getBundle("strings/strings", key);
        }
    }
}
