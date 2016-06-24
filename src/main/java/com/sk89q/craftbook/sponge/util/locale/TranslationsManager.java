package com.sk89q.craftbook.sponge.util.locale;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.spongepowered.api.text.translation.locale.Locales;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Locale;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Function;

public class TranslationsManager {

    private Function<Locale, ResourceBundle> resourceBundleFunction;

    private LoadingCache<Locale, ResourceBundle> resourceBundleCache = CacheBuilder.newBuilder().build(new ResourceBundleLoader());

    public TranslationsManager() {
        resourceBundleFunction = locale -> Optional.of(resourceBundleCache.getUnchecked(locale)).orElse(resourceBundleCache.getUnchecked(Locales.EN_US));
    }

    public Function<Locale, ResourceBundle> getResourceBundleFunction() {
        return this.resourceBundleFunction;
    }

    private static class ResourceBundleLoader extends CacheLoader<Locale, ResourceBundle> {
        @Override
        @NonnullByDefault
        public ResourceBundle load(Locale key) throws Exception {
            return PropertyResourceBundle.getBundle("strings/strings", key);
        }
    }
}
