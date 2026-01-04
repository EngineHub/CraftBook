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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.BounceBlocks;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

public class BukkitBounceBlocks extends BounceBlocks implements Listener {

    public BukkitBounceBlocks(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getFrom().distanceSquared(event.getTo()) <= 0.0001) {
            // Ignore tiny movements for performance reasons.
            return;
        }

        Block block = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
        if (!Blocks.containsFuzzy(allowedBlocks, BukkitAdapter.adapt(block.getBlockData()))) {
            return;
        }

        if (!event.getPlayer().hasPermission("craftbook.bounceblocks.use")) {
            // Check permissions after the simple calculations, permission lookup is slower.
            // Given this is movement based, we also want to avoid providing feedback to the player.
            return;
        }

        CraftBookPlugin.logDebugMessage("Player jumped on a block that is a bounce block!", "bounce-blocks");

        Block sign = block.getRelative(BlockFace.DOWN);

        if (SignUtil.isSign(sign)) {
            for (Side side : Side.values()) {
                final BukkitChangedSign s = BukkitChangedSign.create(sign, side);

                String signLine1 = PlainTextComponentSerializer.plainText().serialize(s.getLine(1));
                if (signLine1.equals("[Jump]") || signLine1.equals("[Launch]")) {
                    boolean requiresManualJump = signLine1.equals("[Jump]");

                    if (requiresManualJump) {
                        // Sensitivity setting for the jumping, may need tweaking
                        if (!(Math.abs(event.getTo().getY() - event.getFrom().getY()) > sensitivity) || !(event.getFrom().getY() - event.getFrom().getBlockY() < 0.25)) {
                            // Jump blocks require a more significant Y movement to trigger
                            return;
                        }
                    }

                    String signLine2 = PlainTextComponentSerializer.plainText().serialize(s.getLine(2, CraftBookPlugin.inst().wrapPlayer(event.getPlayer())));

                    CraftBookPlugin.logDebugMessage("Jump sign found where player jumped!", "bounce-blocks");

                    double x = 0;
                    double y = 0;
                    double z = 0;
                    boolean straight = signLine2.startsWith("!");

                    String[] bits = RegexUtil.COMMA_PATTERN.split(signLine2.replace("!", ""));
                    if (bits.length == 0) {
                        y = 0.5;
                    } else if (bits.length == 1) {
                        try {
                            y = Double.parseDouble(bits[0]);
                        } catch (NumberFormatException e) {
                            y = 0.5;
                        }
                    } else {
                        x = Double.parseDouble(bits[0]);
                        y = Double.parseDouble(bits[1]);
                        z = Double.parseDouble(bits[2]);
                    }

                    if (!straight) {
                        // Attempt to approximate velocity as basic player movement is client side and not replicated to the server.
                        Vector facingBasis = event.getTo().toVector().subtract(event.getFrom().toVector()).setY(0);

                        if (facingBasis.lengthSquared() <= 0.0001) {
                            // If the player isn't moving, default to their look direction to avoid NaN
                            facingBasis = event.getTo().getDirection().clone().setY(0);
                        }

                        Vector forward = facingBasis.normalize();

                        // Compute the right vector by crossing forward with up
                        Vector up = new Vector(0, 1, 0);
                        Vector right = forward.clone().crossProduct(up).normalize();

                        up = right.clone().crossProduct(forward).normalize();

                        // Convert velocity space to world coordinates
                        Vector worldVel = forward.multiply(x).add(up.multiply(y)).add(right.multiply(z));

                        x = worldVel.getX();
                        z = worldVel.getZ();
                    }

                    event.getPlayer().setVelocity(new Vector(x, y, z));
                    // We can set this to a large negative value to prevent fall damage, it'll reset to 0 when they
                    // next touch a surface. Not really a better way to do this.
                    event.getPlayer().setFallDistance(-1000f);

                    // Some fancy visuals/sounds to go along with the jump
                    event.getPlayer().playSound(block.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);
                    event.getPlayer().spawnParticle(Particle.GUST, block.getLocation().add(0.5, 1.5, 0.5), 1, 0, 0, 0, 0.1);
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[Jump]") && !signLine1.equalsIgnoreCase("[Launch]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.bounceblocks.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                        "craftbook.mechanisms.create-permission",
                        TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        try {
            String signLine2 = PlainTextComponentSerializer.plainText().serialize(event.line(2));
            String[] bits = RegexUtil.COMMA_PATTERN.split(signLine2.replace("!", ""));
            if (bits.length == 1) {
                Double.parseDouble(bits[0]);
            } else if (bits.length == 3) {
                Double.parseDouble(bits[0]);
                Double.parseDouble(bits[1]);
                Double.parseDouble(bits[2]);
            } else if (bits.length != 0) {
                throw new Exception("Invalid number of velocity components");
            }
        } catch (Exception e) {
            player.printError(TranslatableComponent.of("craftbook.bounceblocks.invalid-velocity"));
            SignUtil.cancelSignChange(event);
            return;
        }

        if (signLine1.equalsIgnoreCase("[Jump]")) {
            event.line(1, Component.text("[Jump]"));
        } else if (signLine1.equalsIgnoreCase("[Launch]")) {
            event.line(1, Component.text("[Launch]"));
        }
        player.printInfo(TranslatableComponent.of("craftbook.bounceblocks.create"));
    }
}
