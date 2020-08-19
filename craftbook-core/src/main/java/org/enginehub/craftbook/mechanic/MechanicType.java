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

package org.enginehub.craftbook.mechanic;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.craftbook.mechanic.load.LoadDependency;
import org.enginehub.craftbook.mechanic.load.LoadPriority;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class MechanicType<T extends CraftBookMechanic> implements Keyed {

    public static final Registry<MechanicType<? extends CraftBookMechanic>> REGISTRY = new Registry<>("mechanic type");

    private final String id;
    private final String name;
    private final String className;
    private final Component description;
    private final MechanicCategory category;
    private final LoadPriority loadPriority;
    private final List<LoadDependency> dependencies;

    private MechanicType(String id,
                         String name,
                         Component description,
                         String className,
                         MechanicCategory category,
                         LoadPriority loadPriority,
                         List<LoadDependency> dependencies) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.className = className;
        this.category = category;
        this.loadPriority = loadPriority;
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Component getDescription() {
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
        return mechanic.getClass().getName().equals(this.className);
    }

    public static class Builder<T extends CraftBookMechanic> {

        private String id;
        private String name;
        private Component description;
        private String className;
        private MechanicCategory mechanicCategory;
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
