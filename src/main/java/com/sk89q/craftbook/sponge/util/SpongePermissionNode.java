package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

public class SpongePermissionNode extends PermissionNode {

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
            System.out.println("Warning! Missing Permissions Provider. Permissions will not work as expected!");
            return;
        }

        PermissionDescription.Builder permissionBuilder = provider.getProvider().newDescriptionBuilder(CraftBookPlugin.inst()).get();
        permissionDescription = permissionBuilder.id(getNode()).description(Text.of(getDescription())).assign(getDefaultRole(), true).register();
    }

    public boolean hasPermission(Subject subject) {
        return subject.hasPermission(getNode());
    }
}
