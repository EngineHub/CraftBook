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

package com.sk89q.craftbook.mechanic;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableList;
import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.mechanic.exception.MechanicInitializationException;
import com.sk89q.craftbook.mechanic.load.LoadComparator;
import com.sk89q.craftbook.mechanic.load.LoadDependency;
import com.sk89q.craftbook.mechanic.load.MechanicDependency;
import com.sk89q.craftbook.mechanic.load.UnsatisfiedLoadDependencyException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public abstract class MechanicManager {

    /**
     * List of common mechanics.
     */
    private final List<CraftBookMechanic> loadedMechanics = new ArrayList<>();

    public MechanicManager() {
    }

    @Deprecated
    protected static void registerMechanic(String name, Class<? extends CraftBookMechanic> mechanicClass, MechanicCategory category) {
        CraftBook.logger.warn("Legacy mechanic registered: " + name);
        Converter<String, String> camelToSnake = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

        MechanicType<?> mechanicType = MechanicType.Builder
                .create()
                .id(camelToSnake.convert(name))
                .name(name)
                .className(mechanicClass.getName())
                .category(category)
                .build();

        MechanicType.REGISTRY.register(mechanicType.getId(), mechanicType);
    }

    protected void registerMechanic(MechanicType<?> mechanicType) {
        MechanicType.REGISTRY.register(mechanicType.getId(), mechanicType);
    }

    public abstract void setup();

    public void shutdown() {
        ImmutableList.copyOf(loadedMechanics).forEach(this::disableMechanic);
        loadedMechanics.clear();
    }

    /**
     * Gets a list of loaded mechanics.
     *
     * <p>
     *     This list is immutable
     * </p>
     *
     * @return A list of loaded mechanics
     */
    public List<CraftBookMechanic> getLoadedMechanics() {
        return ImmutableList.copyOf(this.loadedMechanics);
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> MechanicType<T> getMechanicType(T mechanic) {
        return (MechanicType<T>) MechanicType.REGISTRY.values().stream().filter(type -> type.isInstance(mechanic)).findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> Optional<T> getMechanic(@Nullable MechanicType<T> mechanicType) {
        if (mechanicType == null) {
            return Optional.empty();
        }
        return (Optional<T>) this.loadedMechanics.stream().filter(mechanicType::isInstance).findAny();
    }

    /**
     * Load and enable all mechanics that are enabled in the configuration.
     */
    public void enableMechanics() {
        List<MechanicType<? extends CraftBookMechanic>> mechanicTypes = new ArrayList<>(MechanicType.REGISTRY.values());
        mechanicTypes.sort(new LoadComparator());

        // We explicitly do not filter out unmet plugin dependencies here, so that they can throw an error when enabling.

        for (MechanicType<?> mechanicType : mechanicTypes) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().enabledMechanics.contains(mechanicType.getId())) {
                try {
                    enableMechanic(mechanicType);
                } catch (UnsatisfiedLoadDependencyException e) {
                    CraftBook.logger.warn("Failed to load mechanic: " + e.getMechanicType().getName() + ". " + e.getMessage());
                } catch (MechanicInitializationException e) {
                    CraftBook.logger.warn("Failed to load mechanic: " + e.getMechanicType().getId() + ". " + e.getMessage());
                    if (e.getCause() != null) {
                        e.getCause().printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isMechanicEnabled(@Nullable MechanicType<?> mechanicType) {
        return getMechanic(mechanicType).isPresent();
    }

    /**
     * Enables the mechanic with the specified type.
     *
     * @param mechanicType The type of the mechanic.
     * @throws MechanicInitializationException If the mechanic could not be enabled.
     */
    public void enableMechanic(MechanicType<?> mechanicType) throws MechanicInitializationException {
        if (isMechanicEnabled(mechanicType)) {
            throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                    "craftbook.mechanisms.already-enabled",
                    TextComponent.of(mechanicType.getId())
            ));
        }
        try {
            for (LoadDependency dependency : mechanicType.getDependencies()) {
                if (!dependency.isMet()) {
                    throw new UnsatisfiedLoadDependencyException(mechanicType, dependency);
                }
            }
            CraftBookMechanic mech = mechanicType.getMechanicClass().getDeclaredConstructor().newInstance();
            mech.loadConfiguration(new File(CraftBook.getInstance().getPlatform().getConfigDir().resolve("mechanics").toFile(), mechanicType.getName() + ".yml"));

            if (!mech.enable()) {
                mech.disable();
                throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                        "craftbook.mechanisms.enable-failed",
                        TextComponent.of(mechanicType.getId())
                ));
            }
            loadedMechanics.add(mech);
            enableMechanicPlatformListeners(mech);
        } catch (MechanicInitializationException e) {
            // Re-throw
            throw e;
        } catch (Throwable t) {
            throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                    "craftbook.mechanisms.enable-failed",
                    TextComponent.of(mechanicType.getId())
            ), t);
        }
    }

    protected abstract void enableMechanicPlatformListeners(CraftBookMechanic mechanic);

    /**
     * Disables the mechanic.
     *
     * @param mechanic The mechanic.
     * @return If the mechanic could be disabled.
     */
    public boolean disableMechanic(CraftBookMechanic mechanic) {
        if (mechanic == null) {
            return false;
        }

        MechanicType<?> mechanicType = getMechanicType(mechanic);

        // Unload any dependent mechanics
        for (CraftBookMechanic enabledMechanic : this.getLoadedMechanics()) {
            for (LoadDependency dependency : getMechanicType(enabledMechanic).getDependencies()) {
                if (dependency instanceof MechanicDependency) {
                    if (((MechanicDependency) dependency).getMechanicType() == mechanicType) {
                        disableMechanic(enabledMechanic);
                        break;
                    }
                }
            }
        }

        mechanic.disable();
        this.loadedMechanics.remove(mechanic);
        disableMechanicPlatformListeners(mechanic);

        return true;
    }

    protected abstract void disableMechanicPlatformListeners(CraftBookMechanic mechanic);
}
