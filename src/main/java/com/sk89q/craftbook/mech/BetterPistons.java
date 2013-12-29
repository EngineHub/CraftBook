package com.sk89q.craftbook.mech;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class BetterPistons extends AbstractCraftBookMechanic {

    /**
     * Check to see if the sign is a valid and enabled sign
     *
     * @param sign The sign to check
     * @return the type of piston created
     */
    public Types checkSign(Block sign) {

        Types type = null;

        if (SignUtil.isSign(sign)) {

            ChangedSign s = BukkitUtil.toChangedSign(sign);
            if (s.getLine(1).equals("[Crush]") && Types.isEnabled(Types.CRUSH))
                type = Types.CRUSH;
            else if (s.getLine(1).equals("[SuperSticky]") && Types.isEnabled(Types.SUPERSTICKY))
                type = Types.SUPERSTICKY;
            else if (s.getLine(1).equals("[Bounce]") && Types.isEnabled(Types.BOUNCE))
                type = Types.BOUNCE;
            else if (s.getLine(1).equals("[SuperPush]") && Types.isEnabled(Types.SUPERPUSH))
                type = Types.SUPERPUSH;
        }

        return type;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        Block block = SignUtil.getBackBlock(event.getBlock());
        Types type = null;
        // check if this looks at all like something we're interested in first
        if (block.getType() == Material.PISTON_BASE || block.getType() == Material.PISTON_STICKY_BASE) {

            if (event.getLine(1).equalsIgnoreCase("[Crush]") && Types.isEnabled(Types.CRUSH)) {
                event.setLine(1, "[Crush]");
                type = Types.CRUSH;
            } else if (event.getLine(1).equalsIgnoreCase("[SuperSticky]") && Types.isEnabled(Types.SUPERSTICKY)) {
                event.setLine(1, "[SuperSticky]");
                type = Types.SUPERSTICKY;
            } else if (event.getLine(1).equalsIgnoreCase("[Bounce]") && Types.isEnabled(Types.BOUNCE)) {
                event.setLine(1, "[Bounce]");
                type = Types.BOUNCE;
            } else if (event.getLine(1).equalsIgnoreCase("[SuperPush]") && Types.isEnabled(Types.SUPERPUSH)) {
                event.setLine(1, "[SuperPush]");
                type = Types.SUPERPUSH;
            }

            if (type == null) return;

            if(!player.hasPermission("craftbook.mech.pistons." + type.name().toLowerCase(Locale.ENGLISH))) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.printError("mech.create-permission");
                SignUtil.cancelSign(event);
                return;
            }

            player.print("mech.pistons." + type.name().toLowerCase(Locale.ENGLISH) + ".created");
        }
    }

    private final double movemod = 1.0;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (event.getBlock().getType() != Material.PISTON_BASE && event.getBlock().getType() != Material.PISTON_STICKY_BASE) return;

        Set<Tuple2<Types, Block>> types = new HashSet<Tuple2<Types, Block>>();

        // check if this looks at all like something we're interested in first
        PistonBaseMaterial piston = (PistonBaseMaterial) event.getBlock().getState().getData();
        Block sign = event.getBlock().getRelative(piston.getFacing().getOppositeFace());
        Types type = null;

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            if (face == piston.getFacing())
                continue;
            sign = event.getBlock().getRelative(face);
            if(face != BlockFace.UP && face != BlockFace.DOWN && !SignUtil.getBackBlock(sign).getLocation().equals(event.getBlock().getLocation()))
                continue;
            type = checkSign(sign);
            if(!types.contains(new Tuple2<Types, Block>(type, sign)) && type != null)
                types.add(new Tuple2<Types, Block>(type, sign));
            if (type != null && SignUtil.isSign(sign.getRelative(face)) && SignUtil.getFacing(sign.getRelative(face)) == SignUtil.getFacing(sign)) {
                sign = sign.getRelative(face);
                type = checkSign(sign);
                if(!types.contains(new Tuple2<Types, Block>(type, sign)) && type != null)
                    types.add(new Tuple2<Types, Block>(type, sign));
            }
        }

        for(Tuple2<Types, Block> tups : types) {
            ChangedSign signState = BukkitUtil.toChangedSign(tups.b);

            switch (tups.a) {
                case CRUSH:
                    if (event.getNewCurrent() > event.getOldCurrent()) {
                        crush(event.getBlock(), piston, signState);
                    }
                    break;
                case BOUNCE:
                    if (event.getNewCurrent() > event.getOldCurrent()) {
                        bounce(event.getBlock(), piston, signState);
                    }
                    break;
                case SUPERSTICKY:
                    if (event.getNewCurrent() < event.getOldCurrent()) {
                        superSticky(event.getBlock(), piston, signState);
                    }
                    break;
                case SUPERPUSH:
                    if (event.getNewCurrent() > event.getOldCurrent()) {
                        superPush(event.getBlock(), piston, signState);
                    }
                    break;
            }
        }
    }

    public void crush(Block trigger, PistonBaseMaterial piston, ChangedSign signState) {

        //piston.setPowered(false);

        if (CraftBookPlugin.inst().getConfiguration().pistonsCrusherInstaKill) {
            for (Entity ent : trigger.getRelative(piston.getFacing()).getChunk().getEntities()) {
                if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing()))) {
                    EntityUtil.killEntity(ent);
                }
            }
        }

        if (CraftBookPlugin.inst().getConfiguration().pistonsCrusherBlacklist.contains(new ItemInfo(trigger.getRelative(piston.getFacing())))) {
            return;
        }
        trigger.getRelative(piston.getFacing()).breakNaturally();
        trigger.getRelative(piston.getFacing()).setTypeId(0, false);
    }

    public void bounce(Block trigger, PistonBaseMaterial piston, ChangedSign signState) {

        if (piston.isSticky()) return;

        double mult;
        try {
            mult = Double.parseDouble(signState.getLine(2));
        } catch (Exception e) {
            mult = 1;
        }

        Vector vel = new Vector(piston.getFacing().getModX(), piston.getFacing().getModY(), piston.getFacing().getModZ()).multiply(mult);
        if (trigger.getRelative(piston.getFacing()).getType() == Material.AIR || trigger.getRelative(piston.getFacing()).getState() != null && trigger.getRelative(piston.getFacing()).getState() instanceof InventoryHolder || trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_MOVING_PIECE || trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_EXTENSION || CraftBookPlugin.inst().getConfiguration().pistonsBounceBlacklist.contains(new ItemInfo(trigger.getRelative(piston.getFacing())))) {
            for (Entity ent : trigger.getRelative(piston.getFacing()).getChunk().getEntities()) {
                if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing()))) {
                    ent.setVelocity(ent.getVelocity().add(vel));
                }
            }
        } else {
            FallingBlock fall = trigger.getWorld().spawnFallingBlock(trigger.getRelative(piston.getFacing()).getLocation().add(vel), trigger.getRelative(piston.getFacing()).getTypeId(), trigger.getRelative(piston.getFacing()).getData());
            trigger.getRelative(piston.getFacing()).setType(Material.AIR);
            fall.setVelocity(vel);
        }
    }

    public void superSticky(final Block trigger, final PistonBaseMaterial piston, final ChangedSign signState) {

        if (!piston.isSticky()) return;

        if (trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_EXTENSION || trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_MOVING_PIECE) {

            int block = 10;
            int amount = 1;
            try {
                block = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[0]);
                if (RegexUtil.MINUS_PATTERN.split(signState.getLine(2)).length > 1) {
                    amount = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[1]);
                }
            } catch (Exception ignored) {
            }

            final boolean air = signState.getLine(3).equalsIgnoreCase("AIR");

            block = Math.min(CraftBookPlugin.inst().getConfiguration().pistonMaxDistance, block);

            final int fblock = block;

            for (int p = 0; p < amount; p++) {
                final int fp = p;

                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run() {
                        for (int x = 1; x <= fblock + 2; x++) {
                            int i = x;
                            if (x == 1 && !(trigger.getRelative(piston.getFacing(), i).getState() instanceof InventoryHolder) && fp == 0) {
                                x = i = 2;
                            }
                            if (x >= fblock + 2 || trigger.getRelative(piston.getFacing(), i + 1).getType() == Material.AIR && !air || !canPistonPushBlock(trigger.getRelative(piston.getFacing(), i + 1))) {
                                trigger.getRelative(piston.getFacing(), i).setType(Material.AIR);
                                break;
                            }
                            for (Entity ent : trigger.getRelative(piston.getFacing(), i).getChunk().getEntities()) {

                                if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing(), i))) {
                                    ent.teleport(ent.getLocation().subtract(piston.getFacing().getModX() * movemod, piston.getFacing().getModY() * movemod, piston.getFacing().getModZ() * movemod));
                                }
                            }
                            copyData(trigger.getRelative(piston.getFacing(), i + 1), trigger.getRelative(piston.getFacing(), i));
                        }
                    }
                }, 2L * (p + 1));
            }
        }
    }

    public void superPush(final Block trigger, final PistonBaseMaterial piston, ChangedSign signState) {

        if (trigger.getRelative(piston.getFacing()).getType() != Material.PISTON_EXTENSION && trigger.getRelative(piston.getFacing()).getType() !=Material.PISTON_MOVING_PIECE) {

            int block = 10;
            int amount = 1;
            try {
                block = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[0]);
                if (RegexUtil.MINUS_PATTERN.split(signState.getLine(2)).length > 1) {
                    amount = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[1]);
                }
            } catch (Exception ignored) {
            }

            block = Math.min(CraftBookPlugin.inst().getConfiguration().pistonMaxDistance, block);

            final int fblock = block;

            for (int p = 0; p < amount; p++) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run() {
                        for (int x = fblock + 2; x >= 1; x--) {
                            final int i = x;
                            if (trigger.equals(trigger.getRelative(piston.getFacing(), i)) || trigger.getRelative(piston.getFacing(), i).getType() == Material.PISTON_MOVING_PIECE || trigger.getRelative(piston.getFacing(), i).getType() == Material.PISTON_EXTENSION || !canPistonPushBlock(trigger.getRelative(piston.getFacing(), i)))
                                continue;
                            if (trigger.getRelative(piston.getFacing(), i + 1).getType() == Material.AIR) {
                                for (Entity ent : trigger.getRelative(piston.getFacing(), i + 1).getChunk().getEntities()) {

                                    if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing(), i + 1))) {
                                        ent.teleport(ent.getLocation().add(piston.getFacing().getModX() * movemod, piston.getFacing().getModY() * movemod, piston.getFacing().getModZ() * movemod));
                                    }
                                }
                                if(copyData(trigger.getRelative(piston.getFacing(), i), trigger.getRelative(piston.getFacing(), i + 1)))
                                    trigger.getRelative(piston.getFacing(), i).setType(Material.AIR);
                            }
                        }
                    }
                }, 2L * (p + 1));
            }
        }
    }

    /**
     * Used for moving a block to elsewhere.
     *
     * @param from The from block.
     * @param to   The block the data is being moved to.
     */
    public boolean copyData(Block from, Block to) {

        BlockState toState = to.getState();
        BlockState fromState = from.getState();

        if (fromState instanceof DoubleChest || toState instanceof DoubleChest) return false;

        int type = from.getTypeId();
        byte data = from.getData();

        ItemStack[] oldInventory = null;
        if (fromState instanceof InventoryHolder) {
            oldInventory = ((InventoryHolder) fromState).getInventory().getContents().clone();
            ((InventoryHolder) fromState).getInventory().clear();
            fromState.update();
            from.setTypeId(0);
        }
        to.setTypeIdAndData(type, data, true);
        if (to.getType() == Material.STONE_BUTTON || to.getType() == Material.WOOD_BUTTON) {
            if ((to.getData() & 0x8) == 0x8) {
                to.setData((byte) (to.getData() ^ 0x8));
            }
        }

        if (toState instanceof Sign) {
            for (int i = 0; i < 4; i++) {
                ((Sign) toState).setLine(i, ((Sign) fromState).getLine(i));
            }
            toState.update();
        } else if (toState instanceof InventoryHolder) {
            ((InventoryHolder) toState).getInventory().setContents(oldInventory);
            toState.update(true);
        }

        return true;
    }

    public boolean canPistonPushBlock(Block block) {

        if (block.getState() instanceof DoubleChest) return false;

        if(CraftBookPlugin.inst().getConfiguration().pistonsMovementBlacklist.contains(new ItemInfo(block)))
            return false;

        switch (block.getType()) {

            case PISTON_MOVING_PIECE:
                return false;
            default:
                return true;
        }
    }

    public static enum Types {

        CRUSH, SUPERSTICKY, BOUNCE, SUPERPUSH;

        public static boolean isEnabled(Types type) {

            if (!CraftBookPlugin.inst().getConfiguration().pistonsEnabled) return false;
            switch (type) {
                case CRUSH:
                    return CraftBookPlugin.inst().getConfiguration().pistonsCrusher;
                case SUPERSTICKY:
                    return CraftBookPlugin.inst().getConfiguration().pistonsSuperSticky;
                case BOUNCE:
                    return CraftBookPlugin.inst().getConfiguration().pistonsBounce;
                case SUPERPUSH:
                    return CraftBookPlugin.inst().getConfiguration().pistonsSuperPush;
                default:
                    return CraftBookPlugin.inst().getConfiguration().pistonsEnabled;
            }
        }
    }
}