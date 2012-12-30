package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;

/**
 * Teleporter Mechanism. Based off Elevator
 *
 * @author sk89q
 * @author hash
 * @author Me4502
 */
public class Teleporter extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Teleporter> {

        public Factory() {

        }

        /**
         * Explore around the trigger to find a functional elevator; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return an Elevator if we could make a valid one, or null if this looked nothing like an elevator.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be an elevator, but it failed.
         */
        @Override
        public Teleporter detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first

            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                if (!s.getLine(1).equalsIgnoreCase("[Teleporter]")) return null;
                String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
                if (pos.length > 2) return new Teleporter(block);
            } else if (block.getTypeId() == BlockID.STONE_BUTTON || block.getTypeId() == BlockID.WOODEN_BUTTON) {
                Button b = (Button) block.getState().getData();
                Block sign = block.getRelative(b.getAttachedFace()).getRelative(b.getAttachedFace());
                if (sign.getState() instanceof Sign) {
                    Sign s = (Sign) sign.getState();
                    if (!s.getLine(1).equalsIgnoreCase("[Teleporter]")) return null;
                    String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
                    if (pos.length > 2) return new Teleporter(s.getBlock());
                }
            }

            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public Teleporter detect(BlockWorldVector pt, LocalPlayer player,
                                 ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Teleporter]")) return null;

            if (!player.hasPermission("craftbook.mech.teleporter")) throw new InsufficientPermissionsException();

            player.print("mech.teleport.create");
            sign.setLine(1, "[Teleporter]");

            String[] pos = RegexUtil.COLON_PATTERN.split(sign.getLine(2));
            if (!(pos.length > 2)) return null;

            throw new ProcessedMechanismException();
        }
    }

    /**
     * @param trigger if you didn't already check if this is a wall sign with appropriate text,
     *                you're going on Santa's naughty list.
     * @param plugin  the direction (UP or DOWN) in which we're looking for a destination
     *
     * @throws InvalidMechanismException
     */
    private Teleporter(Block trigger) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
    }

    private final Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().teleporterEnabled) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)) && !(event
                .getClickedBlock().getState().getData() instanceof Button))
            return; // wth? our manager is insane.

        LocalPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.teleporter.use")) {
            localPlayer.printError("mech.use-permission");
            return;
        }

        makeItSo(CraftBookPlugin.inst().wrapPlayer(event.getPlayer()));

        event.setCancelled(true);
    }

    private void makeItSo(LocalPlayer player) {
        // start with the block shifted vertically from the player
        // to the destination sign's height (plus one).
        // check if this looks at all like something we're interested in first

        double toX = 0;
        double toY = 0;
        double toZ = 0;

        if (trigger.getState() instanceof Sign) {
            Sign s = (Sign) trigger.getState();
            String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
            if (pos.length > 2) {
                try {
                    toX = Double.parseDouble(pos[0]);
                    toY = Double.parseDouble(pos[1]);
                    toZ = Double.parseDouble(pos[2]);
                } catch (Exception e) {
                    return;
                }
            } else return;
        }

        if (CraftBookPlugin.inst().getConfiguration().teleporterRequireSign) {
            Block location = trigger.getWorld().getBlockAt((int) toX, (int) toY, (int) toZ);
            if (location.getTypeId() != BlockID.WALL_SIGN && location.getTypeId() != BlockID.SIGN_POST) {
                if (location.getTypeId() == BlockID.STONE_BUTTON || location.getTypeId() == BlockID.WOODEN_BUTTON) {
                    Button b = (Button) location.getState().getData();
                    Block sign = location.getRelative(b.getAttachedFace()).getRelative(b.getAttachedFace());
                    if (sign.getState() instanceof Sign) {
                        Sign s = (Sign) sign.getState();
                        if (!s.getLine(1).equalsIgnoreCase("[Teleporter]")) {
                            player.printError("mech.teleport.sign");
                            return;
                        }
                    }
                } else {
                    player.printError("mech.teleport.sign");
                    return;
                }
            }
        }

        Block floor = trigger.getWorld().getBlockAt((int) Math.floor(toX), (int) (Math.floor(toY) + 1),
                (int) Math.floor(toZ));
        // well, unless that's already a ceiling.
        if (!occupiable(floor)) {
            floor = floor.getRelative(BlockFace.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        for (int i = 0; i < 5; i++) {
            if (occupiable(floor)) {
                foundFree++;
            } else {
                break;
            }
            if (floor.getY() == 0x0) {
                break;
            }
            floor = floor.getRelative(BlockFace.DOWN);
        }
        if (foundFree < 2) {
            player.printError("mech.lift.obstruct");
            return;
        }

        // Teleport!
        Location subspaceRift = player.getPosition();
        subspaceRift = subspaceRift.setPosition(new Vector(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5));
        if (player.isInsideVehicle()) {
            subspaceRift = player.getVehicle().getLocation();
            subspaceRift = subspaceRift.setPosition(new Vector(floor.getX() + 0.5, floor.getY() + 2,
                    floor.getZ() + 0.5));
            player.getVehicle().teleport(subspaceRift);
        }
        if (CraftBookPlugin.inst().getConfiguration().teleporterMaxRange > 0)
            if (subspaceRift.getPosition().distanceSq(player.getPosition().getPosition()) >
                    CraftBookPlugin.inst().getConfiguration().teleporterMaxRange * CraftBookPlugin.inst()
                            .getConfiguration().teleporterMaxRange) {
                player.print("mech.teleport.range");
                return;
            }

        player.teleport(subspaceRift);

        player.print("mech.teleport.alert");
    }

    private static boolean occupiable(Block block) {

        return BlockType.canPassThrough(block.getTypeId());
    }
}