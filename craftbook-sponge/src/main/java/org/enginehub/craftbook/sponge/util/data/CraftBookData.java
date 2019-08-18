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
package org.enginehub.craftbook.sponge.util.data;

import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import org.enginehub.craftbook.sponge.util.data.builder.BlockBagDataManipulatorBuilder;
import org.enginehub.craftbook.sponge.util.data.builder.EmbeddedBlockBagDataBuilder;
import org.enginehub.craftbook.sponge.util.data.builder.ICDataManipulatorBuilder;
import org.enginehub.craftbook.sponge.util.data.builder.KeyLockDataBuilder;
import org.enginehub.craftbook.sponge.util.data.builder.LastPowerDataManipulatorBuilder;
import org.enginehub.craftbook.sponge.util.data.builder.NamespaceDataBuilder;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableBlockBagData;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableEmbeddedBlockBagData;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableICData;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableKeyLockData;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableLastPowerData;
import org.enginehub.craftbook.sponge.util.data.immutable.ImmutableNamespaceData;
import org.enginehub.craftbook.sponge.util.data.mutable.BlockBagData;
import org.enginehub.craftbook.sponge.util.data.mutable.EmbeddedBlockBagData;
import org.enginehub.craftbook.sponge.util.data.mutable.ICData;
import org.enginehub.craftbook.sponge.util.data.mutable.KeyLockData;
import org.enginehub.craftbook.sponge.util.data.mutable.LastPowerData;
import org.enginehub.craftbook.sponge.util.data.mutable.NamespaceData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;

public class CraftBookData {

    public static void registerData() {
        // This registers the keys.
        new CraftBookKeys();

        // Generic Data
        DataRegistration<LastPowerData, ImmutableLastPowerData> lastPowerData =
                DataRegistration.<LastPowerData, ImmutableLastPowerData>builder()
                        .dataClass(LastPowerData.class)
                        .immutableClass(ImmutableLastPowerData.class)
                        .builder(new LastPowerDataManipulatorBuilder())
                        .manipulatorId("last_power")
                        .dataName("LastPower")
                        .buildAndRegister(CraftBookPlugin.spongeInst().container);

        Sponge.getDataManager().registerLegacyManipulatorIds("LastPowerData", lastPowerData);

        // IC Data
        DataRegistration<ICData, ImmutableICData> icData =
                DataRegistration.<ICData, ImmutableICData>builder()
                        .dataClass(ICData.class)
                        .immutableClass(ImmutableICData.class)
                        .builder(new ICDataManipulatorBuilder())
                        .manipulatorId("ic")
                        .dataName("IC")
                        .buildAndRegister(CraftBookPlugin.spongeInst().container);

        Sponge.getDataManager().registerLegacyManipulatorIds("ICData", icData);

        // Area Data
        DataRegistration<NamespaceData, ImmutableNamespaceData> namespaceData =
                DataRegistration.<NamespaceData, ImmutableNamespaceData>builder()
                        .dataClass(NamespaceData.class)
                        .immutableClass(ImmutableNamespaceData.class)
                        .builder(new NamespaceDataBuilder())
                        .manipulatorId("namespace")
                        .dataName("Namespace")
                        .buildAndRegister(CraftBookPlugin.spongeInst().container);

        Sponge.getDataManager().registerLegacyManipulatorIds("NamespaceData", namespaceData);

        // BlockBag Data
        Sponge.getDataManager().registerBuilder(EmbeddedBlockBag.class, new EmbeddedBlockBag.EmbeddedBlockBagBuilder());

        DataRegistration<EmbeddedBlockBagData, ImmutableEmbeddedBlockBagData> embeddedBlockBagData =
                DataRegistration.<EmbeddedBlockBagData, ImmutableEmbeddedBlockBagData>builder()
                        .dataClass(EmbeddedBlockBagData.class)
                        .immutableClass(ImmutableEmbeddedBlockBagData.class)
                        .builder(new EmbeddedBlockBagDataBuilder())
                        .manipulatorId("embedded_blockbag")
                        .dataName("EmbeddedBlockBag")
                        .buildAndRegister(CraftBookPlugin.spongeInst().container);

        Sponge.getDataManager().registerLegacyManipulatorIds("com.sk89q.craftbook.sponge.mechanics.blockbags.data.EmbeddedBlockBagData", embeddedBlockBagData);

        DataRegistration<BlockBagData, ImmutableBlockBagData> blockBagData =
                DataRegistration.<BlockBagData, ImmutableBlockBagData>builder()
                        .dataClass(BlockBagData.class)
                        .immutableClass(ImmutableBlockBagData.class)
                        .builder(new BlockBagDataManipulatorBuilder())
                        .manipulatorId("blockbag")
                        .dataName("BlockBag")
                        .buildAndRegister(CraftBookPlugin.spongeInst().container);

        Sponge.getDataManager().registerLegacyManipulatorIds("com.sk89q.craftbook.sponge.mechanics.blockbags.data.BlockBagData", blockBagData);

        // Hidden Switch Data
        DataRegistration.<BlockBagData, ImmutableBlockBagData>builder()
                .dataClass(KeyLockData.class)
                .immutableClass(ImmutableKeyLockData.class)
                .builder(new KeyLockDataBuilder())
                .manipulatorId("key_lock")
                .dataName("KeyLock")
                .buildAndRegister(CraftBookPlugin.spongeInst().container);
    }
}
