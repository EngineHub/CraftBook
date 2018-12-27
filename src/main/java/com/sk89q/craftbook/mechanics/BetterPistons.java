package com.sk89q.craftbook.mechanics;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BetterPistons extends AbstractCraftBookMechanic {

    protected static BetterPistons instance;

    @Override
    public boolean enable() {
        instance = this;
        return true;
    }

    /**
     * Check to see if the sign is a valid and enabled sign
     *
     * @param sign The sign to check
     * @return the type of piston created
     */
    private static Types checkSign(Block sign) {
        Types type = null;

        if (SignUtil.isSign(sign)) {
            ChangedSign s = CraftBookBukkitUtil.toChangedSign(sign);
            switch (s.getLine(1)) {
                case "[Crush]":
                    type = Types.CRUSH;
                    break;
                case "[SuperSticky]":
                    type = Types.SUPERSTICKY;
                    break;
                case "[Bounce]":
                    type = Types.BOUNCE;
                    break;
                case "[SuperPush]":
                    type = Types.SUPERPUSH;
                    break;
            }
        }

        return type != null && Types.isEnabled(type) ? type : null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        Block block = SignUtil.getBackBlock(event.getBlock());
        Types type = null;
        // check if this looks at all like something we're interested in first
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {

            if (event.getLine(1).equalsIgnoreCase("[Crush]") && Types.isEnabled(Types.CRUSH)) {
                event.setLine(1, "[Crush]");
                type = Types.CRUSH;
            } else if (event.getLine(1).equalsIgnoreCase("[SuperSticky]") && Types.isEnabled(Types.SUPERSTICKY)) {
                event.setLine(1, "[SuperSticky]");
                type = Types.SUPERSTICKY;
            } else if (event.getLine(1).equalsIgnoreCase("[Bounce]") && Types.isEnabled(Types.BOUNCE)) {
                event.setLine(1, "[Bounce]");
                type = Types.BOUNCE;
                double velocity = 1.0d;
                try {
                    velocity = Double.parseDouble(event.getLine(2));
                } catch (Exception e) {}
                velocity = Math.min(Math.max(velocity, -pistonBounceMaxVelocity), pistonBounceMaxVelocity);
                event.setLine(2, String.valueOf(velocity));
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

            if(ProtectionUtil.shouldUseProtection()) {
                if (type == Types.BOUNCE || type == Types.CRUSH) {
                    Piston pis = (Piston) block.getBlockData();
                    Block off = block.getRelative(pis.getFacing());
                    if (!ProtectionUtil.canBuild(event.getPlayer(), off, false)) {
                        if (CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                            player.printError("area.use-permission");
                        SignUtil.cancelSign(event);
                        return;
                    }
                } else if (type == Types.SUPERPUSH || type == Types.SUPERSTICKY) {
                    Piston pis = (Piston) block.getBlockData();

                    int distance = 10;
                    try {
                        distance = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(event.getLine(2))[0]);
                    } catch (Exception ignored) {
                    }

                    distance = Math.min(pistonMaxDistance, distance);
                    Block off = block;
                    for (int i = 0; i < distance; i++) {
                        off = off.getRelative(pis.getFacing());
                        if (!ProtectionUtil.canBuild(event.getPlayer(), off, false)) {
                            if (CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                                player.printError("area.use-permission");
                            SignUtil.cancelSign(event);
                            return;
                        }
                    }
                }
            }

            player.print("mech.pistons." + type.name().toLowerCase(Locale.ENGLISH) + ".created");
        }
    }

    private static final double movemod = 1.0;

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if (event.getBlock().getType() != Material.PISTON && event.getBlock().getType() != Material.STICKY_PISTON) return;

        Set<Tuple2<Types, Block>> types = new HashSet<>();

        // check if this looks at all like something we're interested in first
        Piston piston = (Piston) event.getBlock().getBlockData();
        Block sign;
        Types type;

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            if (face == piston.getFacing())
                continue;
            sign = event.getBlock().getRelative(face);
            if(face != BlockFace.UP && face != BlockFace.DOWN && !SignUtil.getBackBlock(sign).getLocation().equals(event.getBlock().getLocation()))
                continue;
            type = checkSign(sign);
            if(type != null)
                types.add(new Tuple2<>(type, sign));
            if (type != null && SignUtil.isSign(sign.getRelative(face)) && SignUtil.getFacing(sign.getRelative(face)) == SignUtil.getFacing(sign)) {
                sign = sign.getRelative(face);
                type = checkSign(sign);
                if(type != null)
                    types.add(new Tuple2<>(type, sign));
            }
        }

        for(Tuple2<Types, Block> tups : types) {
            ChangedSign signState = CraftBookBukkitUtil.toChangedSign(tups.b);

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

    public void crush(Block trigger, Piston piston, ChangedSign signState) {

        //piston.setPowered(false);

        if (pistonsCrusherInstaKill) {
            for (Entity ent : trigger.getRelative(piston.getFacing()).getChunk().getEntities()) {
                if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing()))) {
                    EntityUtil.killEntity(ent);
                }
            }
        }

        if (Blocks.containsFuzzy(pistonsCrusherBlacklist, BukkitAdapter.adapt(trigger.getRelative(piston.getFacing()).getBlockData()))) {
            return;
        }

        trigger.getRelative(piston.getFacing()).breakNaturally();
        trigger.getRelative(piston.getFacing()).setType(Material.AIR, false);
    }

    public void bounce(Block trigger, Piston piston, ChangedSign signState) {

        if (piston.getMaterial() == Material.STICKY_PISTON) return;

        double mult;
        try {
            mult = Double.parseDouble(signState.getLine(2));
        } catch (Exception e) {
            mult = 1;
        }

        mult = Math.min(Math.max(mult, -pistonBounceMaxVelocity), pistonBounceMaxVelocity);

        Vector vel = new Vector(piston.getFacing().getModX(), piston.getFacing().getModY(), piston.getFacing().getModZ()).multiply(mult);
        if (trigger.getRelative(piston.getFacing()).getType() == Material.AIR || trigger.getRelative(piston.getFacing()).getState() != null
                && InventoryUtil.doesBlockHaveInventory(trigger.getRelative(piston.getFacing()))
                || trigger.getRelative(piston.getFacing()).getType() == Material.MOVING_PISTON
                || trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_HEAD
                || Blocks.containsFuzzy(pistonsBounceBlacklist, BukkitAdapter.adapt(trigger.getRelative(piston.getFacing()).getBlockData()))) {
            for (Entity ent : trigger.getRelative(piston.getFacing()).getChunk().getEntities()) {
                if (EntityUtil.isEntityInBlock(ent, trigger.getRelative(piston.getFacing()))) {
                    ent.setVelocity(ent.getVelocity().add(vel));
                }
            }
        } else {
            FallingBlock fall = trigger.getWorld().spawnFallingBlock(trigger.getRelative(piston.getFacing()).getLocation().add(vel), trigger.getRelative(piston.getFacing()).getBlockData());
            trigger.getRelative(piston.getFacing()).setType(Material.AIR);
            fall.setVelocity(vel);
        }
    }

    public void superSticky(final Block trigger, final Piston piston, final ChangedSign signState) {

        if (piston.getMaterial() != Material.STICKY_PISTON) return;

        if (trigger.getRelative(piston.getFacing()).getType() == Material.PISTON_HEAD || trigger.getRelative(piston.getFacing()).getType() == Material.MOVING_PISTON) {

            int block = 10;
            int amount = 1;
            try {
                block = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[0]);
                if (RegexUtil.MINUS_PATTERN.split(signState.getLine(2)).length > 1) {
                    amount = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[1]);
                }
            } catch (Exception ignored) {
            }

            block = Math.min(pistonMaxDistance, block);

            final boolean air = signState.getLine(3).equalsIgnoreCase("AIR");

            final int fblock = block;

            for (int p = 0; p < amount; p++) {
                final int fp = p;

                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    for (int x = 1; x <= fblock + 2; x++) {
                        int i = x;
                        if (x == 1 && !InventoryUtil.doesBlockHaveInventory(trigger.getRelative(piston.getFacing(), i)) && fp == 0) {
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
                }, 3L * (p + 1));
            }
        }
    }

    public void superPush(final Block trigger, final Piston piston, ChangedSign signState) {
        if (trigger.getRelative(piston.getFacing()).getType() != Material.PISTON_HEAD && trigger.getRelative(piston.getFacing()).getType() != Material.MOVING_PISTON) {
            int block = 10;
            int amount = 1;
            try {
                block = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[0]);
                if (RegexUtil.MINUS_PATTERN.split(signState.getLine(2)).length > 1) {
                    amount = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(signState.getLine(2))[1]);
                }
            } catch (Exception ignored) {
            }

            block = Math.min(pistonMaxDistance, block);

            final int fblock = block;

            for (int p = 0; p < amount; p++) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    for (int x = fblock + 2; x >= 1; x--) {
                        Block offset = trigger.getRelative(piston.getFacing(), x);
                        Block next = trigger.getRelative(piston.getFacing(), x + 1);
                        if (trigger.equals(offset) || offset.getType() == Material.MOVING_PISTON || offset.getType() == Material.PISTON_HEAD || !canPistonPushBlock(offset))
                            continue;
                        if (next.getType() == Material.AIR) {
                            for (Entity ent : next.getChunk().getEntities()) {
                                if (EntityUtil.isEntityInBlock(ent, offset)) {
                                    ent.teleport(ent.getLocation().add(piston.getFacing().getModX() * movemod, piston.getFacing().getModY() * movemod, piston.getFacing().getModZ() * movemod));
                                }
                            }
                            if(copyData(offset, next))
                                offset.setType(Material.AIR);
                        }
                    }
                }, 3L * (p + 1));
            }
        }
    }

    /**
     * Used for moving a block to elsewhere.
     *
     * @param from The from block.
     * @param to   The block the data is being moved to.
     */
    public static boolean copyData(Block from, Block to) {

        BlockState toState = to.getState();
        BlockState fromState = from.getState();

        if (fromState instanceof DoubleChest || toState instanceof DoubleChest) {
            return false;
        }

        BlockData oldBlock = from.getBlockData();

        ItemStack[] oldInventory = null;
        if (fromState instanceof InventoryHolder) {
            oldInventory = ((InventoryHolder) fromState).getInventory().getContents().clone();
            ((InventoryHolder) fromState).getInventory().clear();
            //fromState.update();
            from.setType(Material.AIR);
        }
        to.setBlockData(oldBlock);
        if (Tag.BUTTONS.isTagged(to.getType())) {
            Powerable powerable = (Powerable) to.getBlockData();
            if (powerable.isPowered()) {
                powerable.setPowered(false);
            }
        }

        if (toState instanceof Sign) {
            for (int i = 0; i < 4; i++) {
                ((Sign) toState).setLine(i, ((Sign) fromState).getLine(i));
            }
            toState.update();
        } else if (toState instanceof InventoryHolder) {
            ((InventoryHolder) toState).getInventory().setContents(oldInventory);
            //toState.update(true);
        }

        return true;
    }

    private boolean canPistonPushBlock(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Chest && ((Chest) blockData).getType() != Chest.Type.SINGLE) return false;

        if (Blocks.containsFuzzy(pistonsMovementBlacklist, BukkitAdapter.adapt(block.getBlockData()))) {
            return false;
        }

        return block.getType() != Material.MOVING_PISTON;
    }

    public enum Types {

        CRUSH, SUPERSTICKY, BOUNCE, SUPERPUSH;

        public static boolean isEnabled(Types type) {
            switch (type) {
                case CRUSH:
                    return instance.pistonsCrusher;
                case SUPERSTICKY:
                    return instance.pistonsSuperSticky;
                case BOUNCE:
                    return instance.pistonsBounce;
                case SUPERPUSH:
                    return instance.pistonsSuperPush;
                default:
                    return false;
            }
        }
    }

    private int pistonMaxDistance;
    private boolean pistonsCrusher;
    private boolean pistonsCrusherInstaKill;
    private List<BaseBlock> pistonsCrusherBlacklist;
    private boolean pistonsSuperPush;
    private boolean pistonsSuperSticky;
    private List<BaseBlock> pistonsMovementBlacklist;
    private boolean pistonsBounce;
    private List<BaseBlock> pistonsBounceBlacklist;
    private double pistonBounceMaxVelocity;

    public static List<String> getDefaultBlacklist() {
        return Lists.newArrayList(
                BlockTypes.OBSIDIAN.getId(),
                BlockTypes.BEDROCK.getId(),
                BlockTypes.NETHER_PORTAL.getId(),
                BlockTypes.END_PORTAL.getId(),
                BlockTypes.END_PORTAL_FRAME.getId(),
                BlockTypes.END_GATEWAY.getId()
        );
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "crushers", "Enables BetterPistons Crusher Mechanic.");
        pistonsCrusher = config.getBoolean(path + "crushers", true);

        config.setComment(path + "crushers-kill-mobs", "Causes crushers to kill mobs as well as break blocks. This includes players.");
        pistonsCrusherInstaKill = config.getBoolean(path + "crushers-kill-mobs", false);

        config.setComment(path + "crusher-blacklist", "A list of blocks that the Crusher piston can not break.");
        pistonsCrusherBlacklist = BlockSyntax.getBlocks(config.getStringList(path + "crusher-blacklist", getDefaultBlacklist()), true);

        config.setComment(path + "super-sticky", "Enables BetterPistons SuperSticky Mechanic.");
        pistonsSuperSticky = config.getBoolean(path + "super-sticky", true);

        config.setComment(path + "super-push", "Enables BetterPistons SuperPush Mechanic.");
        pistonsSuperPush = config.getBoolean(path + "super-push", true);

        config.setComment(path + "movement-blacklist", "A list of blocks that the movement related BetterPistons can not interact with.");
        pistonsMovementBlacklist = BlockSyntax.getBlocks(config.getStringList(path + "movement-blacklist", getDefaultBlacklist()), true);

        config.setComment(path + "bounce", "Enables BetterPistons Bounce Mechanic.");
        pistonsBounce = config.getBoolean(path + "bounce", true);

        config.setComment(path + "bounce-blacklist", "A list of blocks that the Bounce piston can not bounce.");
        pistonsBounceBlacklist = BlockSyntax.getBlocks(config.getStringList(path + "bounce-blacklist", getDefaultBlacklist()), true);

        config.setComment(path + "max-distance", "The maximum distance a BetterPiston can interact with blocks from.");
        pistonMaxDistance = config.getInt(path + "max-distance", 12);

        config.setComment(path + "bounce-max-velocity", "The maximum velocity bounce pistons can provide.");
        pistonBounceMaxVelocity = config.getDouble(path + "bounce-max-velocity", 5.0);
    }
}