package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.PistonBaseMaterial;

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
                    type = checkSign(sign);
                    if(type != null)
                        break signCheck;
                    sign = block.getRelative(SignUtil.getClockWise(piston.getFacing().getOppositeFace()));
                    type = checkSign(sign);
                    if(type != null)
                        break signCheck;
                    sign = block.getRelative(SignUtil.getCounterClockWise(piston.getFacing().getOppositeFace()));
                    type = checkSign(sign);
                    if(type != null)
                        break signCheck;
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
            PistonBaseMaterial piston = (PistonBaseMaterial) event.getBlock().getState().getData();
            event.getBlock().getRelative(piston.getFacing()).breakNaturally();
        }
    }

    private final Block trigger;
    private final Block sign;
    private final Types type;

    private enum Types {

        CRUSH;
    }
}