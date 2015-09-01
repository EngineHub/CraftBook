package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.SpongeConfiguration;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashSet;
import java.util.Set;

@Module(moduleName = "Gate", onEnable="onInitialize", onDisable="onDisable")
public class Gate extends SimpleArea {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        searchRadius = SpongeConfiguration.<Integer>getValue(config.getNode("search-radius"), 5, "The maximum area around the sign the gate can search.");
    }

    private int searchRadius = 5;

    @Override
    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if (event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionTypes.USE) return;

        super.onPlayerInteract(event);

        if (event.getBlock().getType() == BlockTypes.FENCE) {

            int x = event.getLocation().getBlockX();
            int y = event.getLocation().getBlockY();
            int z = event.getLocation().getBlockZ();

            for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
                for (int y1 = y - searchRadius; y1 <= y + searchRadius * 2; y1++) {
                    for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                        if (SignUtil.isSign(event.getLocation().getExtent().getLocation(x1, y1, z1))) {

                            Sign sign = (Sign) event.getLocation().getExtent().getLocation(x1, y1, z1).getTileEntity().get();

                            triggerMechanic(event.getLocation().getExtent().getLocation(x1, y1, z1), sign, event.getEntity(), null);
                        }
                    }
                }
            }
        }
    }

    public void findColumns(Location block, Set<GateColumn> columns) {

        int x = block.getBlockX();
        int y = block.getBlockY();
        int z = block.getBlockZ();

        for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
            for (int y1 = y - searchRadius; y1 <= y + searchRadius * 2; y1++) {
                for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                    searchColumn(block.getExtent().getLocation(x1, y1, z1), columns);
                }
            }
        }
    }

    public void searchColumn(Location block, Set<GateColumn> columns) {

        int y = block.getBlockY();

        if (block.getExtent().getBlock(block.getBlockX(), y, block.getBlockZ()).getType() == BlockTypes.FENCE) {

            GateColumn column = new GateColumn(block);

            columns.add(column);

            Location temp = column.topBlock;

            while (temp.getBlockType() == BlockTypes.FENCE || temp.getBlockType() == BlockTypes.AIR) {
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.NORTH)))) searchColumn(temp.getRelative(Direction.NORTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.SOUTH)))) searchColumn(temp.getRelative(Direction.SOUTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.EAST)))) searchColumn(temp.getRelative(Direction.EAST), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.WEST)))) searchColumn(temp.getRelative(Direction.WEST), columns);

                temp = temp.getRelative(Direction.DOWN);
            }
        }
    }

    public void toggleColumn(Location block, boolean on) {

        Direction dir = Direction.DOWN;

        block = block.getRelative(dir);

        if (on) {
            while (block.getBlockType() == BlockTypes.AIR) {
                block.setBlockType(BlockTypes.FENCE);
                block = block.getRelative(dir);
            }
        } else {
            while (block.getBlockType() == BlockTypes.FENCE) {
                block.setBlockType(BlockTypes.AIR);
                block = block.getRelative(dir);
            }
        }
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Human human, Boolean forceState) {

        if (SignUtil.getTextRaw(sign.getData().get(), 1).equals("[Gate]")) {

            Set<GateColumn> columns = new HashSet<>();

            findColumns(block, columns);

            if (columns.size() > 0) {
                Boolean on = forceState;
                for (GateColumn vec : columns) {
                    Location col = vec.getBlock();
                    if (on == null) {
                        on = col.getRelative(Direction.DOWN).getBlockType() != BlockTypes.FENCE;
                    }
                    toggleColumn(col, on);
                }
            } else {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Can't find a gate!").build());
            }
        } else return false;

        return true;
    }

    public static class GateColumn {

        Location topBlock;

        public GateColumn(Extent extent, int x, int y, int z) {
            this(extent.getLocation(x, y, z));
        }

        public GateColumn(Location topBlock) {

            while (topBlock.getBlockType() == BlockTypes.FENCE) {
                topBlock = topBlock.getRelative(Direction.UP);
            }

            topBlock = topBlock.getRelative(Direction.DOWN);

            this.topBlock = topBlock;
        }

        public Location getBlock() {
            return topBlock;
        }

        @Override
        public int hashCode() {
            return topBlock.getExtent().hashCode() + topBlock.getBlockX() + topBlock.getBlockZ();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GateColumn && ((GateColumn) o).topBlock.getX() == topBlock.getX() && ((GateColumn) o).topBlock.getZ() == topBlock.getZ();

        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Gate]"};
    }
}
