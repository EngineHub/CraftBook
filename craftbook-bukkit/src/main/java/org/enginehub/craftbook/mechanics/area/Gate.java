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

package org.enginehub.craftbook.mechanics.area;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.events.SignClickEvent;
import org.enginehub.craftbook.bukkit.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ConfigUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed or open, a nearby fence
 * will be found,
 * the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides up to a certain number
 * of fences. To the
 * fences that it gets to, it will
 * iterate over the blocks below to open or close the gate.
 */
public class Gate extends StoredBlockMechanic {

    public Gate(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Toggles the gate closest to a location.
     *
     * @param block The base block
     * @param close null to toggle, true to close, false to open
     * @return true if a gate was found and blocks were changed; false otherwise.
     * @throws InvalidMechanismException if something went wrong with the gate
     */
    public boolean toggleGates(Block block, BukkitChangedSign sign, @Nullable Boolean close) throws InvalidMechanismException {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        boolean foundGate = false;

        Set<GateColumn> visitedColumns = new HashSet<>();

        Material type = getOrSetStoredType(sign.getBlock());

        // Toggle nearby gates
        for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
            for (int y1 = y - searchRadius; y1 <= y + searchRadius * 2; y1++) {
                for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {
                    if (recurseColumn(sign, block.getWorld().getBlockAt(x1, y1, z1), type, visitedColumns, close)) {
                        foundGate = true;
                    }
                }
            }
        }

        return foundGate && !visitedColumns.isEmpty();
    }

    /**
     * Toggles one column of gate.
     *
     * @param sign The sign block.
     * @param block A part of the column.
     * @param expectedType The expected type of this gate.
     * @param visitedColumns Previously visited columns.
     * @param close Should close or open.
     * @return true if a gate column was found and blocks were changed; false otherwise.
     */
    private boolean recurseColumn(BukkitChangedSign sign, Block block, Material expectedType, Set<GateColumn> visitedColumns, @Nullable Boolean close) throws InvalidMechanismException {
        if (columnLimit >= 0 && visitedColumns.size() > columnLimit || block.getType() != expectedType) {
            return false;
        }

        CraftBookPlugin.logDebugMessage("Found a possible gate column at " + block.getX() + ':' + block.getY() + ':' + block.getZ(), "gates.search");

        int x = block.getX();
        int z = block.getZ();

        GateColumn column = new GateColumn(block, expectedType);

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (block.getWorld().getBlockAt(x, column.getStartingY() + 1, z).getType().isAir()) {
            return false;
        }

        if (visitedColumns.contains(column)) {
            return false;
        }

        visitedColumns.add(column);

        if (close == null) {
            close = block.getWorld().getBlockAt(x, column.getStartingY() - 1, z).getType() != expectedType;
        }

        CraftBookPlugin.logDebugMessage("Valid column at " + block.getX() + ':' + block.getY() + ':' + block.getZ() + " is being " + (close ? "closed" : "opened"), "gates.search");
        CraftBookPlugin.logDebugMessage("Column Top: " + column.getStartingY() + " End: " + column.getEndingY(), "gates.search");

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        return toggleColumn(sign, column, close, visitedColumns);
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     *
     * @param sign The sign block.
     * @param column The gate column
     * @param close To open or close.
     * @param visitedColumns Previously searched columns.
     */
    private boolean toggleColumn(@Nullable BukkitChangedSign sign, GateColumn column, boolean close, Set<GateColumn> visitedColumns) throws InvalidMechanismException {
        Block block = column.getBlock();
        Material expectedType = column.getExpectedType();

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        BlockData item;
        if (close) {
            item = column.getStartingPoint().getBlockData();
            if (item.getMaterial() != expectedType) {
                return false;
            }
        } else {
            item = Material.AIR.createBlockData();
        }

        CraftBookPlugin.logDebugMessage("Setting column at " + block.getX() + ':' + block.getY() + ':' + block.getZ() + " to " + item, "gates.search");

        if (sign == null) {
            CraftBookPlugin.logDebugMessage("Invalid Sign!", "gates.search");
            return false;
        }

        Sign otherSign = null;

        Block ot = SignUtil.getNextSign(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(1)), 4);
        if (ot != null) {
            otherSign = (Sign) ot.getState(false);
        }

        for (BlockVector3 bl : column.getRegion()) {
            Block blo = BukkitAdapter.adapt(block.getWorld(), bl).getBlock();
            Material bloType = blo.getType();

            if (!BlockUtil.isBlockReplacable(bloType) && bloType != expectedType) {
                break;
            }

            if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction) {
                int blockCount = getStoredBlockCounts(sign.getSign(), otherSign);
                if (!close || blockCount > 0) {
                    boolean transactionSuccess = false;
                    if (!close && bloType == expectedType) {
                        transactionSuccess = addToStoredBlockCount(sign.getSign(), 1);
                    } else if (close && item.getMaterial() != bloType) {
                        transactionSuccess = takeFromStoredBlockCounts(1, sign.getSign(), otherSign);
                    }

                    if (transactionSuccess) {
                        blo.setBlockData(item, true);
                    }
                } else if (item.getMaterial() == expectedType) {
                    throw new InvalidMechanismException(TranslatableComponent.of("craftbook.gate.not-enough-blocks"));
                }
            } else {
                blo.setBlockData(item, true);
            }

            CraftBookPlugin.logDebugMessage("Set block " + bl.x() + ':' + bl.y() + ':' + bl.z() + " to " + item, "gates.search");

            recurseColumn(sign, blo.getRelative(1, 0, 0), expectedType, visitedColumns, close);
            recurseColumn(sign, blo.getRelative(-1, 0, 0), expectedType, visitedColumns, close);
            recurseColumn(sign, blo.getRelative(0, 0, 1), expectedType, visitedColumns, close);
            recurseColumn(sign, blo.getRelative(0, 0, -1), expectedType, visitedColumns, close);
        }

        recurseColumn(sign, column.getStartingPoint().getRelative(1, 0, 0), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(-1, 0, 0), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(0, 0, 1), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(0, 0, -1), expectedType, visitedColumns, close);

        recurseColumn(sign, column.getStartingPoint().getRelative(1, 1, 0), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(-1, 1, 0), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(0, 1, 1), expectedType, visitedColumns, close);
        recurseColumn(sign, column.getStartingPoint().getRelative(0, 1, -1), expectedType, visitedColumns, close);
        return true;
    }

    /**
     * Raised when a block is right clicked.
     *
     * @param event SignClickEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (!EventUtil.passesFilter(event) || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null) {
            return;
        }

        if (!isApplicableSign(event.getSign().getSign())) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.gate.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        try {
            if (CraftBook.getInstance().getPlatform().getConfiguration().safeDestruction) {
                Material heldItemType = event.getPlayer().getInventory().getItem(event.getHand()).getType();
                if (heldItemType != Material.AIR) {
                    Material gateType = getOrSetStoredType(event.getClickedBlock());
                    if (gateType == heldItemType) {
                        if (!player.hasPermission("craftbook.gate.restock")) {
                            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                                player.printError(TranslatableComponent.of("craftbook.gate.restock-permissions"));
                            }
                            return;
                        }

                        int heldAmount = event.getPlayer().getInventory().getItem(event.getHand()).getAmount();

                        int amount = 1;
                        if (event.getPlayer().isSneaking() && heldAmount >= 5) {
                            amount = 5;
                        }

                        addToStoredBlockCount(event.getSign().getSign(), amount);

                        if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
                            event.getPlayer().getInventory().getItem(event.getHand()).subtract(amount);
                        }

                        player.printInfo(TranslatableComponent.of("craftbook.gate.restock"));
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            event.setCancelled(true);

            if (toggleGates(event.getClickedBlock(), event.getSign(), null)) {
                player.printInfo(TranslatableComponent.of("craftbook.gate.toggle"));
            } else {
                player.printError(TranslatableComponent.of("craftbook.gate.not-found"));
            }
        } catch (InvalidMechanismException e) {
            player.printError(e.getRichMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {
        if (!allowRedstone || event.isMinor() || !EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign bukkitSign = (Sign) event.getBlock().getState(false);
        if (!isApplicableSign(bukkitSign)) {
            return;
        }

        Side side = bukkitSign.getInteractableSideFor(event.getSource().getLocation());
        BukkitChangedSign sign = BukkitChangedSign.create(bukkitSign, side);

        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(),
            () -> {
                try {
                    toggleGates(event.getBlock(), sign, event.getNewCurrent() > 0);
                } catch (InvalidMechanismException ignored) {
                    // ignore these, there's no one to send them to.
                }
            }, 2);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[Gate]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.gate.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        event.line(1, Component.text("[Gate]"));
        player.printInfo(TranslatableComponent.of("craftbook.gate.create"));
    }

    @Override
    public Block getBlockBase(Block sign) throws InvalidMechanismException {
        Block gateBlock = null;

        if (sign != null) {
            Material expectedType = getStoredType((Sign) sign.getState(false));

            Deque<Block> enumerationQueue = new ArrayDeque<>();
            Set<Block> visited = new HashSet<>();

            enumerationQueue.add(sign);
            visited.add(sign);

            enumerationLoop: while (!enumerationQueue.isEmpty()) {
                Block currentOrigin = enumerationQueue.poll();

                if (Math.abs(currentOrigin.getX() - sign.getX()) >= searchRadius) {
                    continue;
                }

                if (Math.abs(currentOrigin.getY() - sign.getY()) >= searchRadius * 2) {
                    continue;
                }

                if (Math.abs(currentOrigin.getZ() - sign.getZ()) >= searchRadius) {
                    continue;
                }

                for (var face : LocationUtil.getDirectFaces()) {
                    Block neighbor = currentOrigin.getRelative(face);

                    if (!visited.add(neighbor)) {
                        continue;
                    }

                    BlockData neighborData = neighbor.getBlockData();

                    if (expectedType != null) {
                        // If we have an expected type set, require that one to be used.
                        if (neighborData.getMaterial() == expectedType) {
                            gateBlock = neighbor;
                            break enumerationLoop;
                        }
                    } else if (Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(neighborData))) {
                        gateBlock = neighbor;
                        break enumerationLoop;
                    }

                    enumerationQueue.add(neighbor);
                }
            }
        }

        if (gateBlock == null) {
            throw new InvalidMechanismException(TranslatableComponent.of("craftbook.gate.unusable-material"));
        }

        return gateBlock;
    }

    @Override
    public boolean isApplicableSign(String line) {
        return line.equalsIgnoreCase("[Gate]");
    }

    protected class GateColumn {
        private final Block block;
        private final Material expectedType;

        private int minY = Integer.MIN_VALUE;
        private int maxY = Integer.MAX_VALUE;

        public GateColumn(Block block, Material expectedType) {
            this.block = block;
            this.expectedType = expectedType;
        }

        public Block getStartingPoint() {
            return block.getWorld().getBlockAt(block.getX(), getStartingY(), block.getZ());
        }

        public Block getEndingPoint() {
            return block.getWorld().getBlockAt(block.getX(), getEndingY(), block.getZ());
        }

        public int getStartingY() {
            if (maxY == Integer.MAX_VALUE) {
                int remainingColumnHeight = columnHeight;
                int max = Math.min(block.getWorld().getMaxHeight() - 1, block.getY() + remainingColumnHeight);
                for (int y1 = block.getY() + 1; y1 <= max; y1++) {
                    if (remainingColumnHeight <= 0) {
                        break;
                    }
                    Material currentBlockType = block.getWorld().getBlockAt(block.getX(), y1, block.getZ()).getType();
                    if (currentBlockType == expectedType) {
                        maxY = y1;
                        remainingColumnHeight--;
                    } else {
                        break;
                    }
                }

                if (maxY == Integer.MAX_VALUE) {
                    maxY = block.getY();
                }
            }

            return maxY;
        }

        public int getEndingY() {
            if (minY == Integer.MIN_VALUE) {
                int remainingColumnHeight = columnHeight;
                int min = Math.max(block.getWorld().getMinHeight(), block.getY() - remainingColumnHeight);
                for (int y = block.getY(); y >= min; y--) {
                    if (remainingColumnHeight <= 0) {
                        break;
                    }
                    Material currentBlockType = block.getWorld().getBlockAt(block.getX(), y, block.getZ()).getType();
                    if (BlockUtil.isBlockReplacable(currentBlockType) || currentBlockType == expectedType) {
                        minY = y;
                        remainingColumnHeight--;
                    } else {
                        break;
                    }
                }
                if (minY == Integer.MIN_VALUE) {
                    minY = block.getY();
                }
            }

            return minY;
        }

        public int getX() {
            return this.block.getX();
        }

        public int getZ() {
            return this.block.getZ();
        }

        public Block getBlock() {
            return this.block;
        }

        public Material getExpectedType() {
            return this.expectedType;
        }

        public CuboidRegion getRegion() {
            return new CuboidRegion(
                BukkitAdapter.adapt(getStartingPoint().getRelative(0, -1, 0).getLocation()).toVector().toBlockPoint(),
                BukkitAdapter.adapt(getEndingPoint().getLocation()).toVector().toBlockPoint()
            );
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GateColumn && ((GateColumn) o).getX() == getX() && ((GateColumn) o).getZ() == getZ() && block.getWorld().getName().equals(((GateColumn) o).block.getWorld().getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getX(), getZ());
        }
    }

    private boolean allowRedstone;
    private int columnLimit;
    private List<BaseBlock> blocks;
    private int columnHeight;
    private int searchRadius;

    public List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.addAll(ConfigUtil.getIdsFromCategory(BlockCategories.FENCES));
        materials.add(BlockTypes.IRON_BARS.id());
        materials.add(BlockTypes.GLASS_PANE.id());
        return materials;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allows the gate mechanic to be toggled via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);

        config.setComment("max-columns", "The maximum number of columns that a gate can toggle. -1 for no limit.");
        columnLimit = config.getInt("max-columns", 14);

        config.setComment("blocks", "The list of blocks that a gate can use.");
        blocks = BlockParser.getBlocks(config.getStringList("blocks", getDefaultBlocks().stream().sorted(String::compareToIgnoreCase).toList()), true);

        config.setComment("max-column-height", "The max height of a column.");
        columnHeight = config.getInt("max-column-height", 12);

        config.setComment("search-radius", "The radius around the sign the gate checks for fences in. Note: This is doubled upwards.");
        searchRadius = config.getInt("search-radius", 3);
    }
}
