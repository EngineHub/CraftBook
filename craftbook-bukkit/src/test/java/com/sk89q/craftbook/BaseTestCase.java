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