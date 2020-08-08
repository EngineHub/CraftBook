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

package org.enginehub.craftbook.bukkit.util;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.util.SignUtil;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public final class CraftBookBukkitUtil {

    private CraftBookBukkitUtil() {
    }

    public static ChangedSign toChangedSign(Block sign) {
        return toChangedSign(sign, null, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines) {
        return toChangedSign(block, lines, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines, CraftBookPlayer player) {
        if (!SignUtil.isSign(block)) return null;
        return new ChangedSign(block, lines, player);
    }

    public static Sign toSign(ChangedSign sign) {
        try {
            if (sign.hasChanged()) sign.update(false);
            return sign.getSign();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static BlockVector3 toVector(Block block) {
        return BlockVector3.at(block.getX(), block.getY(), block.getZ());
    }

    public static BlockVector3 toVector(BlockFace face) {
        return BlockVector3.at(face.getModX(), face.getModY(), face.getModZ());
    }

}
