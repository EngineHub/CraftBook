package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

//TODO finish this.
public class BetterPistons extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<BetterPistons> {

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
        public BetterPistons detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            if(block.getTypeId() == BlockID.PISTON_BASE || block.getTypeId() == BlockID.PISTON_STICKY_BASE) {

                PistonBaseMaterial piston = (PistonBaseMaterial) block.getState().getData();
                Block sign = block.getRelative(piston.getFacing().getOppositeFace());
                Types type = null;
                signCheck: {
                    for(BlockFace face : BlockFace.values()) {
                        if(face == piston.getFacing())
                            continue;
                        sign = block.getRelative(face);
                        type = checkSign(sign);
                        if(type != null)
                            break signCheck;
                    }
                }

                if(type == null)
                    return null;
                return new BetterPistons(block, sign, type);
            }

            return null;
        }

        public Types checkSign(Block sign) {

            Types type = null;

            if(sign.getState() instanceof Sign) {

                Sign s = (Sign) sign.getState();
                if(s.getLine(1).equalsIgnoreCase("[Crush]") && CraftBookPlugin.inst().getConfiguration().pistonsCrusher) {
                    s.setLine(1, "[Crush]");
                    s.update(true);
                    type = Types.CRUSH;
                }
                if(s.getLine(1).equalsIgnoreCase("[SuperSticky]") && CraftBookPlugin.inst().getConfiguration().pistonsSuperSticky) {
                    s.setLine(1, "[SuperSticky]");
                    s.update(true);
                    type = Types.SUPERSTICKY;
                }
                if(s.getLine(1).equalsIgnoreCase("[Bounce]") && CraftBookPlugin.inst().getConfiguration().pistonsBounce) {
                    s.setLine(1, "[Bounce]");
                    s.update(true);
                    type = Types.BOUNCE;
                }
            }

            return type;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public BetterPistons detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            Block block = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock());
            Types type = null;
            // check if this looks at all like something we're interested in first
            if(block.getTypeId() == BlockID.PISTON_BASE || block.getTypeId() == BlockID.PISTON_STICKY_BASE) {

                type = checkSign(BukkitUtil.toSign(sign).getBlock());

                if(type == null)
                    return null;

                player.checkPermission("craftbook.mech.pistons." + type.name().toLowerCase());

                player.print("mech.pistons." + type.name().toLowerCase() + ".created");

                throw new ProcessedMechanismException();
            }

            return null;
        }
    }

    /**
     * @param The piston triggering.
     * @param The type of piston mechanic this is.
     * 
     * @throws InvalidMechanismException
     */
    private BetterPistons(Block trigger, Block sign, Types type) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
        this.sign = sign;
        this.type = type;
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(event.getBlock().getTypeId() != trigger.getTypeId()) //Make sure same type (Lazy checks)
            return;

        if(type == Types.CRUSH && event.getNewCurrent() > event.getOldCurrent()) {
            PistonBaseMaterial piston = (PistonBaseMaterial) trigger.getState().getData();
            if(piston.isSticky())
                return;
            piston.setPowered(false);
            trigger.getRelative(piston.getFacing()).breakNaturally();
            trigger.getRelative(piston.getFacing()).setTypeId(0, false);
        } else if(type == Types.BOUNCE && event.getNewCurrent() > event.getOldCurrent()) {
            PistonBaseMaterial piston = (PistonBaseMaterial) trigger.getState().getData();
            if(piston.isSticky())
                return;

            double mult;
            try {
                mult = Double.parseDouble(((Sign) sign.getState()).getLine(2));
            }
            catch(Exception e){
                mult = 1;
            }

            Vector vel = new Vector(piston.getFacing().getModX()*mult, piston.getFacing().getModY()*mult, piston.getFacing().getModZ()*mult);
            if(trigger.getRelative(piston.getFacing()).getTypeId() == 0 || trigger.getRelative(piston.getFacing()).getTypeId() == BlockID.PISTON_MOVING_PIECE || trigger.getRelative(piston.getFacing()).getTypeId() == BlockID.PISTON_EXTENSION) {
                for(Entity ent : trigger.getChunk().getEntities()) {

                    if(ent.getLocation().distanceSquared(trigger.getRelative(piston.getFacing()).getLocation()) < 2) {
                        ent.setVelocity(vel);
                    }
                }
            } else {
                FallingBlock fall = trigger.getWorld().spawnFallingBlock(trigger.getRelative(piston.getFacing()).getLocation().add(vel), trigger.getRelative(piston.getFacing()).getTypeId(), trigger.getRelative(piston.getFacing()).getData());
                trigger.getRelative(piston.getFacing()).setTypeId(0);
                fall.setVelocity(vel);
            }
        } else if (type == Types.SUPERSTICKY && event.getNewCurrent() < event.getOldCurrent()) {
            final PistonBaseMaterial piston = (PistonBaseMaterial) trigger.getState().getData();
            if(trigger.getRelative(piston.getFacing()).getTypeId() == BlockID.PISTON_EXTENSION || trigger.getRelative(piston.getFacing()).getTypeId() == BlockID.PISTON_MOVING_PIECE) {

                int block;
                try {
                    block = Integer.parseInt(((Sign) sign.getState()).getLine(2));
                }
                catch(Exception e){
                    block = 10;
                }

                if(block > 10)
                    block = 10;

                final int fblock = block;

                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run () {
                        for(int x = 2; x <= fblock+2; x++) {
                            final int i = x;
                            if(x >= fblock+2 || trigger.getRelative(piston.getFacing(), i+1).getTypeId() == BlockID.PISTON_MOVING_PIECE || trigger.getRelative(piston.getFacing(), i+1).getTypeId() == 0 || trigger.getRelative(piston.getFacing(), i+1).getState() != null && trigger.getRelative(piston.getFacing(), i+1).getState() instanceof InventoryHolder || trigger.getRelative(piston.getFacing(), i+1).getState().getData() instanceof PistonBaseMaterial && ((PistonBaseMaterial) trigger.getRelative(piston.getFacing(), i+1).getState().getData()).isPowered()) {
                                trigger.getRelative(piston.getFacing(), i).setTypeId(0);
                                break;
                            }
                            trigger.getRelative(piston.getFacing(), i).setTypeIdAndData(trigger.getRelative(piston.getFacing(), i+1).getTypeId(), trigger.getRelative(piston.getFacing(), i+1).getData(), true);
                            //trigger.getRelative(piston.getFacing(), i+1).setTypeId(0);
                        }
                    }

                }, 2L);
            }
        }
    }

    private final Block trigger;
    private final Block sign;
    private final Types type;

    private enum Types {

        CRUSH, SUPERSTICKY, BOUNCE;
    }
}