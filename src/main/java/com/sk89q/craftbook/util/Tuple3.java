/*
 * Craftbook Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.util;

public class Tuple3<A, B, C> {

    public final A a;
    public final B b;
    public final C c;

    public Tuple3(A a, B b, C c) {

        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {

        return o instanceof Tuple3<?, ?, ?> && equals((Tuple3<?, ?, ?>) o);
    }

    public boolean equals(Tuple3<?, ?, ?> o) {

        return o.a.equals(a) && o.b.equals(b) && o.c.equals(c);
    }

    @Override
    public int hashCode() {
        // Constants correspond to glibc's lcg algorithm parameters
        return (a.hashCode() * 1103515245 + 12345 ^ b.hashCode() * 1103515245 + 12345 ^ c.hashCode() * 1103515245 +
                12345) * 1103515245 + 12345;
    }
}
