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
package org.enginehub.craftbook.sponge.util;

import org.enginehub.craftbook.util.PermissionNode;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
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
            CraftBookPlugin.spongeInst().getLogger().warn("Missing Permissions Provider. Permissions will not work as expected!");
            return;
        }

        PermissionDescription.Builder permissionBuilder = provider.getProvider().newDescriptionBuilder(CraftBookPlugin.inst());
        permissionDescription = permissionBuilder.id(getNode()).description(Text.of(getDescription())).assign(getDefaultRole(), true).register();
    }

    public boolean hasPermission(Subject subject) {
        return subject.hasPermission(getNode());
    }
}
