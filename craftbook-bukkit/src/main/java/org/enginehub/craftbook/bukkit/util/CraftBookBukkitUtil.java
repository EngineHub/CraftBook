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

package org.enginehub.craftbook.bukkit.util;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.enginehub.craftbook.ChangedSign;

public final class CraftBookBukkitUtil {

    private CraftBookBukkitUtil() {
    }

    public static Sign toSign(ChangedSign sign) {
        try {
            if (sign.hasChanged()) sign.update(false);
            return sign.getSign();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static BlockVector3 toVector(BlockFace face) {
        return BlockVector3.at(face.getModX(), face.getModY(), face.getModZ());
    }

}
