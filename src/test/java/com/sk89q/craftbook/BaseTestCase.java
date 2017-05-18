package com.sk89q.craftbook;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.mockito.Matchers;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.core.LanguageManager;

public class BaseTestCase {

    BukkitConfiguration config;

    public void setup() {

        if(config == null)
            config = mock(BukkitConfiguration.class);

        if(CraftBookPlugin.inst() == null) {
            CraftBookPlugin plugin = mock(CraftBookPlugin.class);
            when(plugin.getConfiguration()).thenReturn(getConfig());
            when(plugin.wrapPlayer(Matchers.any())).thenCallRealMethod();
            CraftBookPlugin.setInstance(plugin);
        }

        if(CraftBookPlugin.inst().getLanguageManager() == null) {
            LanguageManager manager = mock(LanguageManager.class);
            when(manager.getString(Matchers.anyString(), Matchers.anyString())).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            });
            when(CraftBookPlugin.inst().getLanguageManager()).thenReturn(manager);
        }
    }

    public BukkitConfiguration getConfig() {
        return config;
    }
}