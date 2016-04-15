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

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

public final class SpongePermissionNode extends PermissionNode {

    private PermissionDescription permissionDescription;

    public SpongePermissionNode(String node, String description, String defaultRole) {
        super(node, description, defaultRole);
    }

    public PermissionDescription getPermissionDescription() {
        return this.permissionDescription;
    }

    @Override
    public void register() {
        ProviderRegistration<PermissionService> provider = Sponge.getServiceManager().getRegistration(PermissionService.class).orElse(null);
        if(provider == null) {
            CraftBookAPI.<CraftBookPlugin>inst().getLogger().warn("Missing Permissions Provider. Permissions will not work as expected!");
            return;
        }

        PermissionDescription.Builder permissionBuilder = provider.getProvider().newDescriptionBuilder(CraftBookPlugin.inst()).get();
        permissionDescription = permissionBuilder.id(getNode()).description(Text.of(getDescription())).assign(getDefaultRole(), true).register();
    }

    public boolean hasPermission(Subject subject) {
        return subject.hasPermission(getNode());
    }
}
