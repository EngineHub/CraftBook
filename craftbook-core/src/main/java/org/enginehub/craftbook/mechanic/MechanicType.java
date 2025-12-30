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

import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.craftbook.mechanic.load.LoadDependency;
import org.enginehub.craftbook.mechanic.load.LoadPriority;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class MechanicType<T extends CraftBookMechanic> implements Keyed {

    public static final Registry<MechanicType<? extends CraftBookMechanic>> REGISTRY = new Registry<>("mechanic type", "craftbook:mechanic_type");

    private final String id;
    private final String name;
    private final String className;
    private final @Nullable Component description;
    private final MechanicCategory category;
    private final LoadPriority loadPriority;
    private final List<LoadDependency> dependencies;

    private MechanicType(String id,
                         String name,
                         @Nullable Component description,
                         String className,
                         MechanicCategory category,
                         LoadPriority loadPriority,
                         List<LoadDependency> dependencies) {
        this.id = checkNotNull(id);
        this.name = checkNotNull(name);
        this.description = description;
        this.className = checkNotNull(className);
        this.category = checkNotNull(category);
        this.loadPriority = checkNotNull(loadPriority);
        this.dependencies = List.copyOf(checkNotNull(dependencies));
    }

    @Override
    public String id() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public @Nullable Component getDescription() {
        return this.description;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getMechanicClass() throws ReflectiveOperationException {
        return (Class<T>) Class.forName(this.className);
    }

    public MechanicCategory getCategory() {
        return this.category;
    }

    public LoadPriority getLoadPriority() {
        return this.loadPriority;
    }

    public List<LoadDependency> getDependencies() {
        return this.dependencies;
    }

    public boolean isInstance(CraftBookMechanic mechanic) {
        return mechanic.getMechanicType().id.equals(this.id);
    }

    public static class Builder<T extends CraftBookMechanic> {

        private @Nullable String id;
        private @Nullable String name;
        private @Nullable Component description;
        private @Nullable String className;
        private @Nullable MechanicCategory mechanicCategory;
        private LoadPriority loadPriority = LoadPriority.NORMAL;

        private final List<LoadDependency> dependencies = new ArrayList<>();

        public Builder<T> id(String id) {
            this.id = id;
            return this;
        }

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> description(Component description) {
            this.description = description;
            return this;
        }

        public Builder<T> className(String className) {
            this.className = className;
            return this;
        }

        public Builder<T> category(MechanicCategory mechanicCategory) {
            this.mechanicCategory = mechanicCategory;
            return this;
        }

        public Builder<T> loadPriority(LoadPriority loadPriority) {
            this.loadPriority = loadPriority;
            return this;
        }

        public Builder<T> dependsOn(LoadDependency loadDependency) {
            this.dependencies.add(loadDependency);
            return this;
        }

        public MechanicType<T> buildAndRegister() {
            checkNotNull(id, "ID must be provided");
            checkNotNull(name, "Name must be provided");
            checkNotNull(className, "Class name must be provided");
            checkNotNull(mechanicCategory, "Mechanic category must be provided");

            MechanicType<T> mechanicType = new MechanicType<>(
                this.id,
                this.name,
                this.description,
                this.className,
                this.mechanicCategory,
                this.loadPriority,
                this.dependencies
            );

            MechanicType.REGISTRY.register(this.id, mechanicType);

            return mechanicType;
        }

        public static <T extends CraftBookMechanic> Builder<T> create() {
            return new Builder<>();
        }
    }
}
