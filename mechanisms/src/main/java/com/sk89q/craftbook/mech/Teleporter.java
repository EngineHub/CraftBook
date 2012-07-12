package com.sk89q.craftbook.mech;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * Teleporter Mechanism.
 * 
 * @author sk89q
 * @author hash
 * @author Me4502
 * 
 */
public class Teleporter extends AbstractMechanic{

    public static class Factory extends AbstractMechanicFactory<Teleporter> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        private MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a functional elevator; throw if
         * things look funny.
         * 
         * @param pt
         *            the trigger (should be a signpost)
         * @return an Elevator if we could make a valid one, or null if this
         *         looked nothing like an elevator.
         * @throws InvalidMechanismException
         *             if the area looked like it was intended to be an
         *             elevator, but it failed.
         */
        @Override
        public Teleporter detect(BlockWorldVector pt) throws InvalidMechanismException {
            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first

            if(block.getState() instanceof Sign) {
                Sign s = (Sign)block.getState();
                if(!s.getLine(1).equalsIgnoreCase("[Teleport]")) return null;
                String[] pos = s.getLine(2).split(":");
                if(pos.length > 2)
                    return new Teleporter(block, plugin);
            }

            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException
         */
        @Override
        public Teleporter detect(BlockWorldVector pt, LocalPlayer player, Sign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if(!sign.getLine(1).equalsIgnoreCase("[Teleport]")) return null;

            if (!player.hasPermission("craftbook.mech.teleporter")) {
                throw new InsufficientPermissionsException();
            }

            player.print("mech.teleport.create");
            sign.setLine(1, "[Teleport]");

            String[] pos = sign.getLine(2).split(":");
            if(!(pos.length > 2))
                return null;

            throw new ProcessedMechanismException();
        }
    }
    /**
     * @param trigger
     *            if you didn't already check if this is a wall sign with
     *            appropriate text, you're going on Santa's naughty list.
     * @param dir
     *            the direction (UP or DOWN) in which we're looking for a destination
     * @throws InvalidMechanismException
     */
    private Teleporter(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {
        super();
        this.trigger = trigger;
        this.plugin = plugin;
    }

    private MechanismsPlugin plugin;

    private Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().teleporterSettings.enable) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane. ikr.

        LocalPlayer localPlayer = plugin.wrap(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.teleporter.use")) {
            localPlayer.printError("mech.use-permission");
            return;
        }

        makeItSo(event.getPlayer());

        event.setCancelled(true);
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        /* we only affect players, so we don't care about redstone events */
    }

    private void makeItSo(Player player) {
        // start with the block shifted vertically from the player
        // to the destination sign's height (plus one).
        // check if this looks at all like something we're interested in first

        double toX = 0;
        double toY = 0;
        double toZ = 0;

        if(trigger.getState() instanceof Sign) {
            Sign s = (Sign)trigger.getState();
            String[] pos = s.getLine(2).split(":");
            if(pos.length > 2)
            {
                try {
                    toX = Double.parseDouble(pos[0]);
                    toY = Double.parseDouble(pos[1]);
                    toZ = Double.parseDouble(pos[2]);
                }
                catch(Exception e){
                    return;
                }
            }
            else
                return;
        }

        Block floor = trigger.getWorld().getBlockAt((int)Math.floor(toX),(int) (Math.floor(toY) + 1),(int)Math.floor(toZ));
        // well, unless that's already a ceiling.
        if (!occupiable(floor)) floor = floor.getRelative(BlockFace.DOWN);

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (occupiable(floor)) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            if (floor.getY() == 0x0)        // hit the bottom of the world
                break;
            floor = floor.getRelative(BlockFace.DOWN);
        }
        if (foundFree < 2) {
            player.sendMessage("mech.lift.obstruct");
            return;
        }

        // Teleport!
        Location subspaceRift = player.getLocation().clone();
        subspaceRift.setY(floor.getY() + 1);
        subspaceRift.setX(floor.getX());
        subspaceRift.setZ(floor.getZ());
        if(player.isInsideVehicle()) {
            subspaceRift = player.getVehicle().getLocation().clone();
            subspaceRift.setY(floor.getY() + 2);
            subspaceRift.setX(floor.getX());
            subspaceRift.setZ(floor.getZ());
            player.getVehicle().teleport(subspaceRift);
        }
        player.teleport(subspaceRift);

        player.sendMessage("mech.teleport.alert");
    }

    private static boolean occupiable(Block block) {
        return BlockType.canPassThrough(block.getTypeId());
    }

    @Override
    public void unload() {
        /* we're not persistent */
    }

    @Override
    public boolean isActive() {
        /* we're not persistent */
        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }}
