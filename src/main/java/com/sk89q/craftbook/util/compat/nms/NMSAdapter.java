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
