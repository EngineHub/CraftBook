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
package org.enginehub.craftbook.sponge.mechanics.blockbags;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.util.CraftBookException;
import org.enginehub.craftbook.util.PermissionNode;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.enginehub.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.service.permission.PermissionDescription;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Module(id = "blockbag", name = "BlockBag", onEnable="onInitialize", onDisable="onDisable")
public class BlockBagManager extends SpongeMechanic implements DocumentationProvider {

    public SpongePermissionNode adminPermissions = new SpongePermissionNode("craftbook.blockbag.admin",
            "Allows usage of admin block bags.", PermissionDescription.ROLE_ADMIN);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private Set<BlockBag> blockBags = new HashSet<>();

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        adminPermissions.register();
    }

    public long getUnusedId() {
        long id = ThreadLocalRandom.current().nextLong();
        while(getBlockBag(id) != null) {
            id = ThreadLocalRandom.current().nextLong();
        }
        return id;
    }

    public IdentifiableBlockBag getBlockBag(long id) {
        if (id == -1) {
            return AdminBlockBag.INSTANCE;
        }
        return blockBags.stream().filter(blockBag -> blockBag instanceof IdentifiableBlockBag)
                .map(blockBag -> (IdentifiableBlockBag) blockBag)
                .filter(bag -> bag.getId() == id)
                .findFirst().orElse(null);
    }

    public void addBlockBag(BlockBag blockBag) {
        blockBags.add(blockBag);
    }

    @Override
    public String getPath() {
        return "mechanics/block_bags";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                adminPermissions
        };
    }
}
