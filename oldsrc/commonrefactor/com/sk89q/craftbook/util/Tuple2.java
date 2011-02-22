/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.util;

public class Tuple2<A,B> {
    public final A a;
    public final B b;
    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof Tuple2<?,?>)) return false;
        else return equals((Tuple2<?,?>)o);
    }
    public boolean equals(Tuple2<?,?> o) {
        return o.a.equals(a)&&o.b.equals(b);
    }
    public int hashCode() {
        //Constants corraspond to glibc's lcg algorithm parameters
        return ((a.hashCode()*1103515245+12345)^
                (b.hashCode()*1103515245+12345))*1103515245+12345;
    }
}
