/*
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

package org.enginehub.craftbook.mechanic;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanic.load.LoadComparator;
import org.enginehub.craftbook.mechanic.load.LoadDependency;
import org.enginehub.craftbook.mechanic.load.MechanicDependency;
import org.enginehub.craftbook.mechanic.load.UnsatisfiedLoadDependencyException;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MechanicManager {

    /**
     * List of common mechanics.
     */
    private final List<CraftBookMechanic> loadedMechanics = new ArrayList<>();

    public MechanicManager() {
    }

    public abstract void setup();

    public void shutdown() {
        List.copyOf(loadedMechanics).forEach(this::disableMechanic);
        loadedMechanics.clear();
    }

    /**
     * Gets a list of loaded mechanics.
     *
     * <p>
     * This list is immutable
     * </p>
     *
     * @return A list of loaded mechanics
     */
    public List<CraftBookMechanic> getLoadedMechanics() {
        return List.copyOf(this.loadedMechanics);
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> @Nullable MechanicType<T> getMechanicType(T mechanic) {
        for (MechanicType<? extends CraftBookMechanic> type : MechanicType.REGISTRY.values()) {
            if (type.isInstance(mechanic)) {
                return (MechanicType<T>) type;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> Optional<T> getMechanic(@Nullable MechanicType<T> mechanicType) {
        if (mechanicType == null) {
            return Optional.empty();
        }
        for (CraftBookMechanic loadedMechanic : this.loadedMechanics) {
            if (mechanicType.isInstance(loadedMechanic)) {
                return (Optional<T>) Optional.of(loadedMechanic);
            }
        }
        return (Optional<T>) Optional.<CraftBookMechanic>empty();
    }

    /**
     * Load and enable all mechanics that are enabled in the configuration.
     */
    public void enableMechanics() {
        List<MechanicType<? extends CraftBookMechanic>> mechanicTypes = new ArrayList<>(MechanicType.REGISTRY.values());
        mechanicTypes.sort(new LoadComparator());

        // We explicitly do not filter out unmet plugin dependencies here, so that they can throw an error when enabling.

        for (MechanicType<?> mechanicType : mechanicTypes) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().enabledMechanics.contains(mechanicType.id())) {
                try {
                    enableMechanic(mechanicType);
                } catch (UnsatisfiedLoadDependencyException e) {
                    CraftBook.LOGGER.warn("Failed to load mechanic: " + e.getMechanicType().getName() + ". " + e.getMessage());
                } catch (MechanicInitializationException e) {
                    CraftBook.LOGGER.warn("Failed to load mechanic: " + e.getMechanicType().id() + ". " + e.getMessage());
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
                TextComponent.of(mechanicType.id())
            ));
        }
        try {
            for (LoadDependency dependency : mechanicType.getDependencies()) {
                if (!dependency.isMet() && !dependency.isOptional()) {
                    throw new UnsatisfiedLoadDependencyException(mechanicType, dependency);
                }
            }

            CraftBookMechanic mech;
            try {
                mech = mechanicType.getMechanicClass().getDeclaredConstructor(MechanicType.class).newInstance(mechanicType);
            } catch (NoSuchMethodException e) {
                mech = mechanicType.getMechanicClass().getDeclaredConstructor().newInstance();
            }

            mech.loadConfiguration(new File(CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("mechanics").toFile(), mechanicType.getName() + ".yml"));
            mech.enable();

            loadedMechanics.add(mech);
            enableMechanicPlatformListeners(mech);
        } catch (CraftBookException e) {
            // Re-throw with a wrapper
            throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                "craftbook.mechanisms.enable-failed",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE),
                e.getRichMessage()
            ), e);
        } catch (Throwable t) {
            throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                "craftbook.mechanisms.enable-failed",
                TextComponent.of(mechanicType.getName(), TextColor.WHITE),
                TextComponent.of(t.getMessage())
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
    public boolean disableMechanic(@Nullable CraftBookMechanic mechanic) {
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


    /**
     * Reload the mechanic's configuration.
     *
     * @param mechanic The mechanic.
     * @throws MechanicInitializationException If the mechanic could not be reloaded.
     */
    public void reloadMechanic(CraftBookMechanic mechanic) throws MechanicInitializationException {
        MechanicType<?> mechanicType = mechanic.getMechanicType();
        try {
            mechanic.loadConfiguration(new File(CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("mechanics").toFile(), mechanicType.getName() + ".yml"));
            mechanic.reload();
        } catch (MechanicInitializationException e) {
            // Re-throw
            throw e;
        } catch (Throwable t) {
            throw new MechanicInitializationException(mechanicType, TranslatableComponent.of(
                "craftbook.mechanisms.reload-failed",
                TextComponent.of(mechanicType.id())
            ), t);
        }
    }
}
