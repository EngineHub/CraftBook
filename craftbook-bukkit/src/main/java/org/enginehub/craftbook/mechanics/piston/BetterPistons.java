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

package org.enginehub.craftbook.mechanics.piston;

import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;
import java.util.Locale;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;

public class BetterPistons extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Block pistonBlock = SignUtil.getBackBlock(event.getBlock());
        Material pistonBlockType = pistonBlock.getType();

        // Check if this looks at all like something we're interested in first.
        if (pistonBlockType == Material.PISTON || pistonBlockType == Material.STICKY_PISTON) {
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            PistonType type = null;

            for (PistonType testType : PistonType.values()) {
                if (isEnabled(testType) && event.getLine(1).equalsIgnoreCase(testType.getSignText())) {
                    event.setLine(1, testType.getSignText());
                    type = testType;
                    break;
                }
            }

            if (type == null) {
                return;
            }

            if (type == PistonType.BOUNCE) {
                double velocity = 1.0d;
                try {
                    velocity = Double.parseDouble(event.getLine(2));
                } catch (Exception ignored) {
                }
                velocity = Math.min(Math.max(velocity, -maxBounceVelocity), maxBounceVelocity);
                event.setLine(2, String.valueOf(velocity));
            }

            if (!player.hasPermission("craftbook.betterpistons." + type.name().toLowerCase(Locale.ENGLISH) + ".create")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError("mech.create-permission");
                }
                SignUtil.cancelSignChange(event);
                return;
            }

            if (!type.getAllowedBlocks().contains(pistonBlockType)) {
                player.printError(TranslatableComponent.of("craftbook.betterpistons.invalid-piston-block"));
                SignUtil.cancelSignChange(event);
                return;
            }

            if (ProtectionUtil.shouldUseProtection()) {
                Piston pistonData = (Piston) pistonBlock.getBlockData();

                if (type == PistonType.BOUNCE || type == PistonType.CRUSH) {
                    Block off = pistonBlock.getRelative(pistonData.getFacing());
                    if (ProtectionUtil.isBreakingPrevented(event.getPlayer(), off)) {
                        if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                            player.printError("area.use-permission");
                        }
                        SignUtil.cancelSignChange(event);
                        return;
                    }
                } else if (type == PistonType.SUPER_PUSH || type == PistonType.SUPER_STICKY) {
                    int distance = 10;
                    try {
                        distance = Integer.parseInt(RegexUtil.MINUS_PATTERN.split(event.getLine(2))[0]);
                    } catch (Exception ignored) {
                    }

                    distance = Math.min(maxDistance, distance);
                    Block off = pistonBlock;
                    for (int i = 0; i < distance; i++) {
                        off = off.getRelative(pistonData.getFacing());
                        if (ProtectionUtil.isBreakingPrevented(event.getPlayer(), off)) {
                            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                                player.printError("area.use-permission");
                            }
                            SignUtil.cancelSignChange(event);
                            return;
                        }
                    }
                }
            }

            player.printInfo(TranslatableComponent.of("craftbook.betterpistons." + type.name().toLowerCase(Locale.ENGLISH) + ".created"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Piston piston = (Piston) event.getBlock().getBlockData();

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            // Cannot put a sign on this face
            if (face == piston.getFacing()) {
                continue;
            }

            // Check it's actually a sign
            if (!SignUtil.isSign(event.getBlock().getRelative(face))) {
                continue;
            }

            BlockFace facing = SignUtil.getFacing(event.getBlock().getRelative(face));

            // Only care if it faces the piston
            if (face != BlockFace.UP
                && face != BlockFace.DOWN
                && SignUtil.getBackBlock(event.getBlock().getRelative(face)).getBlockKey() != event.getBlock().getBlockKey()) {
                continue;
            }

            Block sign = event.getBlock();
            do {
                sign = sign.getRelative(face);
                ChangedSign signState = CraftBookBukkitUtil.toChangedSign(sign);

                PistonType type = PistonType.getFromSign(signState);

                if (type != null) {
                    switch (type) {
                        case CRUSH -> crush(event.getBlock(), piston);
                        case BOUNCE -> bounce(event.getBlock(), piston, signState);
                        case SUPER_PUSH -> superPush(event.getBlock(), piston, signState);
                    }
                }
            } while (SignUtil.isSign(sign.getRelative(face)) && SignUtil.getFacing(sign) == facing);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        // We only listen to this for sticky
        if (event.getBlock().getType() != Material.STICKY_PISTON) {
            return;
        }

        Piston piston = (Piston) event.getBlock().getBlockData();

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            // Cannot put a sign on this face
            if (face == piston.getFacing()) {
                continue;
            }

            // Check it's actually a sign
            if (!SignUtil.isSign(event.getBlock().getRelative(face))) {
                continue;
            }

            BlockFace facing = SignUtil.getFacing(event.getBlock().getRelative(face));

            // Only care if it faces the piston
            if (face != BlockFace.UP
                && face != BlockFace.DOWN
                && SignUtil.getBackBlock(event.getBlock().getRelative(face)).getBlockKey() != event.getBlock().getBlockKey()) {
                continue;
            }

            Block sign = event.getBlock();
            do {
                sign = sign.getRelative(face);
                ChangedSign signState = CraftBookBukkitUtil.toChangedSign(sign);

                PistonType type = PistonType.getFromSign(signState);

                if (type == PistonType.SUPER_STICKY) {
                    superSticky(event.getBlock(), piston, signState);

                    // Only one type - eject once we've ran it.
                    break;
                }
            } while (SignUtil.isSign(sign.getRelative(face)) && SignUtil.getFacing(sign) == facing);
        }
    }

    public void crush(Block trigger, Piston piston) {
        Block pistonHead = trigger.getRelative(piston.getFacing());

        if (crushInstaKill) {
            BoundingBox box = BoundingBox.of(pistonHead);
            for (Entity ent : trigger.getWorld().getNearbyEntities(box)) {
                EntityUtil.killEntity(ent);
            }
        }

        if (Blocks.containsFuzzy(crushBlockBlacklist, adapt(pistonHead.getBlockData()))) {
            return;
        }

        pistonHead.breakNaturally();
        pistonHead.setType(Material.AIR, false);
    }

    public void bounce(Block trigger, Piston piston, ChangedSign signState) {
        if (piston.getMaterial() == Material.STICKY_PISTON) {
            return;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(signState.getLine(2));
        } catch (Exception ignored) {
            multiplier = 1;
            signState.setLine(2, "1.0");
            signState.update(false);
        }

        multiplier = Math.min(Math.max(multiplier, -maxBounceVelocity), maxBounceVelocity);

        Vector vel = new Vector(piston.getFacing().getModX(), piston.getFacing().getModY(), piston.getFacing().getModZ()).multiply(multiplier);
        Block pistonHead = trigger.getRelative(piston.getFacing());
        Material pistonHeadType = pistonHead.getType();
        BlockData pistonHeadData = pistonHead.getBlockData();

        if (pistonHeadType.isAir()
            || pistonHeadType == Material.MOVING_PISTON
            || pistonHeadType == Material.PISTON_HEAD
            || InventoryUtil.doesBlockHaveInventory(pistonHead)
            || Blocks.containsFuzzy(bounceBlockBlacklist, adapt(pistonHeadData))) {
            for (Entity ent : trigger.getWorld().getNearbyEntities(BoundingBox.of(pistonHead))) {
                ent.setVelocity(ent.getVelocity().add(vel));
            }
        } else {
            FallingBlock fall = trigger.getWorld().spawnFallingBlock(pistonHead.getLocation().add(vel), pistonHeadData);
            pistonHead.setType(Material.AIR);
            fall.setVelocity(vel);
        }
    }

    public void superSticky(final Block trigger, final Piston piston, final ChangedSign signState) {
        if (piston.getMaterial() != Material.STICKY_PISTON) {
            return;
        }

        Block pistonHead = trigger.getRelative(piston.getFacing());
        Material pistonHeadType = pistonHead.getType();

        if (pistonHeadType == Material.PISTON_HEAD || pistonHeadType == Material.MOVING_PISTON) {
            int block = 10;
            int amount = 1;
            try {
                String[] split = RegexUtil.COLON_PATTERN.split(signState.getLine(2));
                block = Integer.parseInt(split[0]);
                if (split.length > 1) {
                    amount = Integer.parseInt(split[1]);
                }
            } catch (Exception ignored) {
                signState.setLine(2, Math.min(maxDistance, 10) + ":1");
                signState.update(false);
            }

            block = Math.min(maxDistance, block);

            final boolean air = signState.getLine(3).equalsIgnoreCase("AIR");

            final int fblock = block;

            for (int run = 0; run < amount; run++) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    for (int x = 3; x <= fblock + 2; x++) {
                        Block to = trigger.getRelative(piston.getFacing(), x - 1);
                        Block from = trigger.getRelative(piston.getFacing(), x);
                        Material fromType = from.getType();

                        if (x >= fblock + 2
                            || fromType.isAir() && !air
                            || fromType == Material.MOVING_PISTON
                            || fromType == Material.PISTON_HEAD
                            || isImmovableBlock(from)) {
                            to.setType(Material.AIR);
                            break;
                        }

                        if (to.getType().isAir()) {
                            for (Entity ent : trigger.getWorld().getNearbyEntities(BoundingBox.of(from))) {
                                ent.teleport(ent.getLocation().subtract(piston.getFacing().getDirection()));
                            }

                            if (copyData(from, to)) {
                                from.setType(Material.AIR);
                            }
                        }
                    }
                }, 3L * (run + 1));
            }
        }
    }

    public void superPush(final Block trigger, final Piston piston, ChangedSign signState) {
        Block pistonHead = trigger.getRelative(piston.getFacing());
        Material pistonHeadType = pistonHead.getType();

        if (pistonHeadType != Material.PISTON_HEAD && pistonHeadType != Material.MOVING_PISTON) {
            int block = 10;
            int amount = 1;
            try {
                String[] split = RegexUtil.COLON_PATTERN.split(signState.getLine(2));
                block = Integer.parseInt(split[0]);
                if (split.length > 1) {
                    amount = Integer.parseInt(split[1]);
                }
            } catch (Exception ignored) {
                signState.setLine(2, Math.min(maxDistance, 10) + ":1");
                signState.update(false);
            }

            block = Math.min(maxDistance, block);

            final int blockCount = block;

            for (int run = 0; run < amount; run++) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    for (int x = blockCount + 2; x >= 1; x--) {
                        Block from = trigger.getRelative(piston.getFacing(), x);
                        Block to = trigger.getRelative(piston.getFacing(), x + 1);
                        Material fromType = from.getType();

                        if (fromType == Material.MOVING_PISTON || fromType == Material.PISTON_HEAD || isImmovableBlock(from)) {
                            continue;
                        }

                        if (to.getType().isAir()) {
                            for (Entity ent : trigger.getWorld().getNearbyEntities(BoundingBox.of(from))) {
                                ent.teleport(ent.getLocation().add(piston.getFacing().getDirection()));
                            }
                            if (copyData(from, to)) {
                                from.setType(Material.AIR);
                            }
                        }
                    }
                }, 3L * run);
            }
        }
    }

    /**
     * Copies a block.
     *
     * @param from The from block
     * @param to The block the data is being moved to
     */
    public static boolean copyData(Block from, Block to) {
        World world = BukkitAdapter.adapt(from.getWorld());
        BaseBlock fromBlock = world.getFullBlock(BlockVector3.at(from.getX(), from.getY(), from.getZ()));
        try {
            return world.setBlock(BlockVector3.at(to.getX(), to.getY(), to.getZ()), fromBlock, SideEffectSet.defaults().with(SideEffect.VALIDATION, SideEffect.State.ON));
        } catch (WorldEditException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isImmovableBlock(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
            return true;
        }

        if (Blocks.containsFuzzy(movementBlacklist, adapt(blockData))) {
            return true;
        }

        return blockData.getMaterial() == Material.MOVING_PISTON;
    }

    /**
     * Checks if the given {@link PistonType} is enabled by the user.
     *
     * @param type The piston type
     * @return Whether it's enabled
     */
    public boolean isEnabled(PistonType type) {
        return switch (type) {
            case CRUSH -> enableCrush;
            case SUPER_STICKY -> enableSuperSticky;
            case BOUNCE -> enableBounce;
            case SUPER_PUSH -> enableSuperPush;
        };
    }

    // Crush
    private boolean enableCrush;
    private boolean crushInstaKill;
    private List<BaseBlock> crushBlockBlacklist;

    // Push / Sticky
    private boolean enableSuperPush;
    private boolean enableSuperSticky;
    private List<BaseBlock> movementBlacklist;
    private int maxDistance;

    // Bounce
    private boolean enableBounce;
    private List<BaseBlock> bounceBlockBlacklist;
    private double maxBounceVelocity;

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
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enable-crush", "Enables the Crush mechanic.");
        enableCrush = config.getBoolean("enable-crush", true);

        config.setComment("crush-kills-mobs", "Causes Crush to kill mobs as well as break blocks. This includes players.");
        crushInstaKill = config.getBoolean("crush-kills-mobs", false);

        config.setComment("crush-block-blacklist", "A list of blocks that the Crush piston cannot break.");
        crushBlockBlacklist = BlockParser.getBlocks(config.getStringList("crush-block-blacklist", getDefaultBlacklist()), true);

        config.setComment("enable-super-sticky", "Enables the SuperSticky mechanic.");
        enableSuperSticky = config.getBoolean("enable-super-sticky", true);

        config.setComment("enable-super-push", "Enables the SuperPush mechanic.");
        enableSuperPush = config.getBoolean("enable-super-push", true);

        config.setComment("movement-blacklist", "A list of blocks that the movement related BetterPistons can not interact with.");
        movementBlacklist = BlockParser.getBlocks(config.getStringList("movement-blacklist", getDefaultBlacklist()), true);

        config.setComment("enable-bounce", "Enables the Bounce mechanic.");
        enableBounce = config.getBoolean("enable-bounce", true);

        config.setComment("bounce-blacklist", "A list of blocks that the Bounce piston can not bounce.");
        bounceBlockBlacklist = BlockParser.getBlocks(config.getStringList("bounce-blacklist", getDefaultBlacklist()), true);

        config.setComment("max-distance", "The maximum distance a BetterPiston can interact with blocks from.");
        maxDistance = config.getInt("max-distance", 12);

        config.setComment("bounce-max-velocity", "The maximum velocity bounce pistons can use.");
        maxBounceVelocity = config.getDouble("bounce-max-velocity", 5.0);
    }
}
