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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemCategories;
import com.sk89q.worldedit.world.item.ItemType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ConfigUtil;
import org.enginehub.craftbook.util.ItemParser;
import org.enginehub.craftbook.util.TernaryState;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeLopper extends AbstractCraftBookMechanic {
    public TreeLopper(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected List<BaseBlock> enabledBlocks;
    protected List<ItemType> enabledItems;
    protected int maxSearchSize;
    protected boolean allowDiagonals;
    protected boolean placeSaplings;
    protected boolean breakLeaves;
    protected boolean singleDamageAxe;
    protected boolean leavesDamageAxe;
    protected TernaryState allowSneaking;

    private List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.addAll(ConfigUtil.getIdsFromCategory(BlockCategories.OVERWORLD_NATURAL_LOGS));
        materials.add(BlockTypes.CRIMSON_STEM.id());
        materials.add(BlockTypes.WARPED_STEM.id());
        return materials;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enabled-blocks", "A list of enabled log blocks. This list can only contain logs, but can be modified to include more logs (for mod support).");
        enabledBlocks = BlockParser.getBlocks(config.getStringList("enabled-blocks", getDefaultBlocks().stream().sorted(String::compareToIgnoreCase).toList()), true);

        config.setComment("tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = ItemParser.getItems(config.getStringList("tool-list", ConfigUtil.getIdsFromCategory(ItemCategories.AXES)), true).stream().map(BaseItem::getType).toList();

        config.setComment("max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt("max-size", 75);

        config.setComment("allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        allowDiagonals = config.getBoolean("allow-diagonals", false);

        config.setComment("place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        placeSaplings = config.getBoolean("place-saplings", false);

        config.setComment("break-leaves", "If enabled, TreeLopper will break leaves connected to the tree.");
        breakLeaves = config.getBoolean("break-leaves", true);

        config.setComment("leaves-damage-axe", "Whether the leaves will also damage the axe when single-damage-axe is false and break-leaves is true.");
        leavesDamageAxe = config.getBoolean("leaves-damage-axe", false);

        config.setComment("single-damage-axe", "Only remove one damage from the axe, regardless of the amount of blocks removed.");
        singleDamageAxe = config.getBoolean("single-damage-axe", false);

        config.setComment("allow-sneaking", "Sets how the player must be sneaking in order to use the Tree Lopper.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.NONE.toString()));
    }
}
