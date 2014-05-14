package com.sk89q.craftbook;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.bukkit.entity.Player;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.LanguageManager;

public class BaseTestCase {

    BukkitConfiguration config;

    public void setup() {

        if(config == null)
            config = mock(BukkitConfiguration.class);

        if(CraftBookPlugin.inst() == null) {
            CraftBookPlugin plugin = mock(CraftBookPlugin.class);
            when(plugin.getConfiguration()).thenReturn(getConfig());
            when(plugin.wrapPlayer(Matchers.<Player>any())).thenCallRealMethod();
            CraftBookPlugin.setInstance(plugin);
        }

        if(CraftBookPlugin.inst().getLanguageManager() == null) {
            LanguageManager manager = mock(LanguageManager.class);
            when(manager.getString(Matchers.anyString(), Matchers.anyString())).thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    return (String) args[0];
                }
            });
            when(CraftBookPlugin.inst().getLanguageManager()).thenReturn(manager);
        }
    }

    public BukkitConfiguration getConfig() {
        return config;
    }
}