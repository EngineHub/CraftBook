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

import com.sk89q.craftbook.util.Tuple2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Finds all currently loaded chunks in a world.
 *
 * @author Lymia
 */
public class ChunkFinder {

    private ChunkFinder() {

    }

    @SuppressWarnings("unchecked")
    public static Tuple2<Integer, Integer>[] getLoadedChunks(fv world) {

        List<Tuple2<Integer, Integer>> chunkList = new ArrayList<Tuple2<Integer, Integer>>();
        List<me> list = (List<me>) get(get(world, "G"), "f");
        for (me chunk : list.toArray(new me[0])) { if (chunk != null) chunkList.add(getChunkCoords(chunk)); }
        return chunkList.toArray((Tuple2<Integer, Integer>[]) new Tuple2<?, ?>[0]);
    }

    private static Tuple2<Integer, Integer> getChunkCoords(me chunk) {

        return new Tuple2<Integer, Integer>(chunk.j, chunk.k);
    }

    private static Object get(Object o, String field) {

        try {
            return get(o, o.getClass(), field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("NoSuchFieldException thrown. CraftBook likely needs updating", e);
        }
    }

    private static Object get(Object o, Class<?> c, String field) throws NoSuchFieldException {

        if (c == Object.class) throw new NoSuchFieldException(field);
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(o);
        } catch (NoSuchFieldException e) {
            return get(o, c.getSuperclass(), field);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("IllegalArgumentException thrown. CraftBook likely needs updating", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException thrown. CraftBook likely needs updating", e);
        }
    }
}
