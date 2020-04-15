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

package com.sk89q.craftbook.util.compat.nms;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.block.Block;

public class NMSAdapter {

    /**
     * Gets whether an NMS extension is installed.
     *
     * @return If an NMS extension is installed
     */
    public boolean hasNMSExtension() {
        return false;
    }

    /**
     * Get a CraftBook sign-wrapper instance
     *
     * @param block The block
     * @param lines The lines
     * @param player The player (Optional)
     * @return The ChangedSign
     */
    public ChangedSign getChangedSign(Block block, String[] lines, CraftBookPlayer player) {
        if (!SignUtil.isSign(block)) return null;
        return new ChangedSign(block, lines, player);
    }
}
