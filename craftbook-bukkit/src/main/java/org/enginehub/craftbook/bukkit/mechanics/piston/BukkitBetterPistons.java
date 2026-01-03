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

package org.enginehub.craftbook.bukkit.mechanics.piston;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.piston.BetterPistons;
import org.enginehub.craftbook.mechanics.piston.PistonType;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.Locale;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;

public class BukkitBetterPistons extends BetterPistons implements Listener {

    public BukkitBetterPistons(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Block pistonBlock = SignUtil.findNonSignBackBlock(event.getBlock());
        BlockType pistonBlockType = BukkitAdapter.asBlockType(pistonBlock.getType());

        // Check if this looks at all like something we're interested in first.
        if (pistonBlockType == BlockTypes.PISTON || pistonBlockType == BlockTypes.STICKY_PISTON) {
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            PistonType type = null;

            String line1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
            for (PistonType testType : PistonType.values()) {
                if (isEnabled(testType) && line1.equalsIgnoreCase(testType.getSignText())) {
                    event.line(1, Component.text(testType.getSignText()));
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
                event.line(2, Component.text(velocity));
            }

            if (!player.hasPermission("craftbook.betterpistons." + type.name().toLowerCase(Locale.ENGLISH) + ".create")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError(TranslatableComponent.of(
                        "craftbook.mechanisms.create-permission",
                        TextComponent.of(getMechanicType().getName())
                    ));
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
                    if (!ProtectionUtil.canBreak(event.getPlayer(), off)) {
                        if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                            player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
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
                        if (!ProtectionUtil.canBreak(event.getPlayer(), off)) {
                            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
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

            BlockFace facing = SignUtil.getBack(event.getBlock().getRelative(face));

            // Only care if it faces the piston
            if (face != BlockFace.UP
                && face != BlockFace.DOWN
                && !SignUtil.getBackBlock(event.getBlock().getRelative(face)).equals(event.getBlock())) {
                continue;
            }

            Block sign = event.getBlock();
            do {
                sign = sign.getRelative(face);
                Sign bukkitSign = (Sign) sign.getState(false);

                for (Side side : Side.values()) {
                    PistonType type = PistonType.getFromSign(bukkitSign.getSide(side).getLine(1));
                    if (type != null) {
                        ChangedSign signState = ChangedSign.create(bukkitSign, side);
                        switch (type) {
                            case CRUSH -> crush(event.getBlock(), piston);
                            case BOUNCE -> bounce(event.getBlock(), piston, signState);
                            case SUPER_PUSH -> superPush(event.getBlock(), piston, signState);
                            default -> {
                            }
                        }
                    }
                }
            } while (SignUtil.isSign(sign.getRelative(face)) && SignUtil.getBack(sign) == facing);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        // We only listen to this for sticky
        if (!event.isSticky()) {
            return;
        }

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            // Cannot put a sign on this face
            if (face == event.getDirection()) {
                continue;
            }

            // Check it's actually a sign
            if (!SignUtil.isSign(event.getBlock().getRelative(face))) {
                continue;
            }

            BlockFace facing = SignUtil.getBack(event.getBlock().getRelative(face));

            // Only care if it faces the piston
            if (face != BlockFace.UP
                && face != BlockFace.DOWN
                && !SignUtil.getBackBlock(event.getBlock().getRelative(face)).equals(event.getBlock())) {
                continue;
            }

            Block sign = event.getBlock();
            do {
                sign = sign.getRelative(face);
                Sign bukkitSign = (Sign) sign.getState(false);

                for (Side side : Side.values()) {
                    PistonType type = PistonType.getFromSign(bukkitSign.getSide(side).getLine(1));
                    if (type == PistonType.SUPER_STICKY) {
                        superSticky(event.getBlock(), event.getDirection(), ChangedSign.create(bukkitSign, side));

                        // Only one type - eject once we've ran it.
                        break;
                    }
                }


            } while (SignUtil.isSign(sign.getRelative(face)) && SignUtil.getBack(sign) == facing);
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
            String line2 = PlainTextComponentSerializer.plainText().serialize(signState.getLine(2));
            multiplier = Double.parseDouble(line2);
        } catch (Exception ignored) {
            multiplier = 1;
            signState.setLine(2, Component.text("1.0"));
            signState.update(false);
        }

        multiplier = Math.min(Math.max(multiplier, -maxBounceVelocity), maxBounceVelocity);

        Vector vel = new Vector(piston.getFacing().getModX(), piston.getFacing().getModY(), piston.getFacing().getModZ()).multiply(multiplier);
        Block pistonHead = trigger.getRelative(piston.getFacing());
        BlockData pistonHeadData = pistonHead.getBlockData();
        Material pistonHeadType = pistonHeadData.getMaterial();

        if (pistonHeadType.isAir()
            || pistonHeadType == Material.MOVING_PISTON
            || pistonHeadType == Material.PISTON_HEAD
            || InventoryUtil.doesBlockHaveInventory(pistonHead)
            || Blocks.containsFuzzy(bounceBlockBlacklist, adapt(pistonHeadData))) {
            for (Entity ent : trigger.getWorld().getNearbyEntities(BoundingBox.of(pistonHead))) {
                ent.setVelocity(ent.getVelocity().add(vel));
            }
        } else {
            FallingBlock fall = trigger.getWorld().spawn(pistonHead.getLocation().add(vel), FallingBlock.class, fallingBlock -> {
                fallingBlock.setBlockData(pistonHeadData);
            });
            pistonHead.setType(Material.AIR);
            fall.setVelocity(vel);
        }
    }

    public void superSticky(final Block trigger, final BlockFace facing, final ChangedSign signState) {
        Block pistonHead = trigger.getRelative(facing);
        Material pistonHeadType = pistonHead.getType();

        if (pistonHeadType == Material.PISTON_HEAD || pistonHeadType == Material.MOVING_PISTON) {
            int block = 10;
            int amount = 1;
            try {
                String line2 = PlainTextComponentSerializer.plainText().serialize(signState.getLine(2));
                String[] split = RegexUtil.COLON_PATTERN.split(line2);
                block = Integer.parseInt(split[0]);
                if (split.length > 1) {
                    amount = Integer.parseInt(split[1]);
                }
            } catch (Exception ignored) {
                signState.setLine(2, Component.text(Math.min(maxDistance, 10) + ":1"));
                signState.update(false);
            }

            block = Math.min(maxDistance, block);

            String line3 = PlainTextComponentSerializer.plainText().serialize(signState.getLine(3));
            final boolean air = line3.equalsIgnoreCase("AIR");

            final int fblock = block;

            for (int run = 0; run < amount; run++) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    for (int x = 3; x <= fblock + 2; x++) {
                        Block to = trigger.getRelative(facing, x - 1);
                        Block from = trigger.getRelative(facing, x);
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
                            BoundingBox fromBounds = BoundingBox.of(from);
                            for (Entity ent : trigger.getWorld().getNearbyEntities(fromBounds)) {
                                Location dest = ent.getLocation().subtract(facing.getDirection());
                                if (ent instanceof Player player) {
                                    player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN,
                                        TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z,
                                        TeleportFlag.Relative.VELOCITY_ROTATION
                                    );
                                } else {
                                    ent.teleport(dest);
                                }
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
                String line2 = PlainTextComponentSerializer.plainText().serialize(signState.getLine(2));
                String[] split = RegexUtil.COLON_PATTERN.split(line2);
                block = Integer.parseInt(split[0]);
                if (split.length > 1) {
                    amount = Integer.parseInt(split[1]);
                }
            } catch (Exception ignored) {
                signState.setLine(2, Component.text(Math.min(maxDistance, 10) + ":1"));
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
                            BoundingBox fromBounds = BoundingBox.of(from);
                            for (Entity ent : trigger.getWorld().getNearbyEntities(fromBounds)) {
                                Location dest = ent.getLocation().add(piston.getFacing().getDirection());
                                if (ent instanceof Player player) {
                                    player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN,
                                        TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z,
                                        TeleportFlag.Relative.VELOCITY_ROTATION
                                    );
                                } else {
                                    ent.teleport(dest);
                                }
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
}
