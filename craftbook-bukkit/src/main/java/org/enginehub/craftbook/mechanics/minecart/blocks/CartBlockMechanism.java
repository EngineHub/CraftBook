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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RedstoneUtil;
import org.enginehub.craftbook.util.RedstoneUtil.Power;

import java.util.List;
import java.util.Locale;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their logic at interation
 * time (like
 * non-persistant mechanics, but allowed
 * zero state even in RAM). In order to be effective, configuration loading in MinecartManager must
 * be modified to
 * include an implementer.
 */
public abstract class CartBlockMechanism extends AbstractCraftBookMechanic implements Listener {

    private BaseBlock block;

    public CartBlockMechanism(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    public BaseBlock getBlock() {
        return this.block;
    }

    public void setBlock(BaseBlock block) {
        this.block = block;
    }

    private static final BlockFace[] powerSupplyOptions = new BlockFace[] {
        BlockFace.NORTH, BlockFace.EAST,
        BlockFace.SOUTH, BlockFace.WEST
    };

    /**
     * Determins if a cart mechanism should be enabled.
     *
     * @param blocks The {@link CartMechanismBlocks} that represents the blocks that are being
     *     checked for activity on.
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s
     *     members).
     */
    public static Power isActive(CartMechanismBlocks blocks) {
        boolean isWired = false;
        if (blocks.hasSign()) {
            switch (isActive(blocks.sign())) {
                case ON:
                    return Power.ON;
                case NA:
                default:
                    break;
                case OFF:
                    isWired = true;
            }
        }

        if (blocks.hasBase()) {
            switch (isActive(blocks.base())) {
                case ON:
                    return Power.ON;
                case NA:
                default:
                    break;
                case OFF:
                    isWired = true;
            }
        }

        if (blocks.hasRail()) {
            switch (isActive(blocks.rail())) {
                case ON:
                    return Power.ON;
                case NA:
                default:
                    break;
                case OFF:
                    isWired = true;
            }
        }

        return isWired ? Power.OFF : Power.NA;
    }

    /**
     * Checks if any of the blocks horizonally adjacent to the given block are powered wires.
     *
     * @param block The block to check
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s
     *     members).
     */
    private static Power isActive(Block block) {
        boolean isWired = false;

        for (BlockFace face : powerSupplyOptions) {
            Power p = RedstoneUtil.isPowered(block, face);
            switch (p) {
                case ON:
                    return Power.ON;
                case NA:
                default:
                    break;
                case OFF:
                    isWired = true;
            }
        }

        return isWired ? Power.OFF : Power.NA;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (getApplicableSigns().isEmpty() || !EventUtil.passesFilter(event)) {
            return;
        }

        Block block = event.getBlock();
        Component[] lines = event.lines().toArray(new Component[0]);
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        try {
            boolean found = false;
            String lineFound = null;

            for (String sign : getApplicableSigns()) {
                if (PlainTextComponentSerializer.plainText().serialize(lines[1]).equalsIgnoreCase('[' + sign + ']')) {
                    found = true;
                    lineFound = sign;
                    break;
                }
            }

            if (!found) {
                return;
            }
            if (!verify(BukkitChangedSign.create(event.getBlock(), event.getSide(), lines, player), player)) {
                block.breakNaturally();
                event.setCancelled(true);
                return;
            }

            player.checkPermission("craftbook." + getNodeId() + ".create");
            event.line(1, Component.text('[' + lineFound + ']'));
            player.printInfo(TranslatableComponent.of("craftbook." + getNodeId() + ".create"));
        } catch (AuthorizationException e) {
            player.printError(TranslatableComponent.of(
                "craftbook.mechanisms.create-permission",
                TextComponent.of(getMechanicType().getName())
            ));
            block.breakNaturally();
            event.setCancelled(true);
        }
    }

    public String getDocsUrl(MechanicType<? extends CraftBookMechanic> mechanicType) {
        return CraftBook.getDocsDomain() + "mechanics/minecart/block/" + mechanicType.id() + "/";
    }

    /**
     * Gets the identifier for this mechanic in permission nodes and translation nodes.
     *
     * @return The node ID.
     */
    protected String getNodeId() {
        return getMechanicType().id().replace("_", "").toLowerCase(Locale.ENGLISH);
    }

    public List<String> getApplicableSigns() {
        return List.of();
    }

    public boolean verify(BukkitChangedSign sign, CraftBookPlayer player) {
        return true;
    }
}
