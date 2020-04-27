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

package com.sk89q.craftbook.core.mechanic;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.craftbook.CraftBookMechanic;

public class MechanicRegistration<T extends CraftBookMechanic> {

    private final String name;
    private final String className;
    private final MechanicCategory category;

    private MechanicRegistration(String name, String className, MechanicCategory category) {
        this.name = name;
        this.className = className;
        this.category = category;
    }

    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    public T create() throws ReflectiveOperationException {
        Class<T> clazz = (Class<T>) Class.forName(this.className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    public MechanicCategory getCategory() {
        return this.category;
    }

    public boolean matches(CraftBookMechanic mechanic) {
        return mechanic.getClass().getName().equals(this.className);
    }

    public static class Builder<T extends CraftBookMechanic> {

        private String name;
        private String className;
        private MechanicCategory mechanicCategory;

        public Builder<T> name(String name) {
            this.name = name;
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

        public MechanicRegistration<T> build() {
            checkNotNull(name, "Name must be provided");
            checkNotNull(className, "Class name must be provided");
            checkNotNull(mechanicCategory, "Mechanic category must be provided");

            return new MechanicRegistration<>(this.name, this.className, this.mechanicCategory);
        }

        public static <T extends CraftBookMechanic> Builder<T> create() {
            return new Builder<>();
        }
    }
}
