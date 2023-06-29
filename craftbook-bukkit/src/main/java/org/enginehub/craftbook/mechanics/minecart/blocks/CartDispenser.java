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

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.minecart.RailUtil;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

/**
 * <p>
 * Collects carts when carts pass over an unpowered chest marked as a dispenser; dispenses carts
 * when power flips on
 * if no cart is already above the
 * dispenser. The dispenser does not impart velocity to dispensed carts, so it is typical to simply
 * place the track
 * above the dispenser so as to be at
 * an angle.
 * </p>
 * <p>
 * Carts of all types (including powered and storage carts) will be collected by this mechanism,
 * but only regular minecarts are dispensed. This may be
 * changed in a future version.
 * </p>
 * <p>
 * Dispenser signs which contain "inf" on the third line will collect carts without storing them and
 * create carts
 * without requiring them from
 * inventory.
 * </p>
 * <p>
 * Note that this is not an exact reimplementation of the mechanics from hmod; this is something new
 * and more
 * consistent with other modern cart
 * mechanisms.
 * </p>
 *
 * @author hash
 */
public class CartDispenser extends CartBlockMechanism {

    private final static List<String> SIGNS = ImmutableList.of("Dispenser");

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {
        boolean inf = "inf".equalsIgnoreCase(sign.getLine(2));

        if (inf && !player.hasPermission("craftbook.minecartdispenser.infinite")) {
            player.printError(TranslatableComponent.of("craftbook.minecartdispenser.infinite-permissions"));
            return false;
        }

        return super.verify(sign, player);
    }

    @EventHandler
    public void onCartImpact(CartBlockImpactEvent event) {
        performMechanic(event.getMinecart(), event.getBlocks());
    }

    @EventHandler
    public void onRedstoneImpact(CartBlockRedstoneEvent event) {
        performMechanic(event.getMinecart(), event.getBlocks());
    }

    private void performMechanic(Minecart cart, CartMechanismBlocks blocks) {
        if (!blocks.matches(getBlock()) || !blocks.matches("dispenser")) {
            return;
        }

        ChangedSign sign = blocks.getChangedSign();

        Power pow = isActive(blocks);
        EntityType type = parseMinecartType(sign.getLine(0));
        boolean inf = "inf".equalsIgnoreCase(sign.getLine(2));
        boolean canCollect = cart != null && !sign.getLine(3).toLowerCase(Locale.ENGLISH).contains("dispense");
        boolean canDispense = cart == null && !sign.getLine(3).toLowerCase(Locale.ENGLISH).contains("collect");

        if (!canCollect && !canDispense) {
            // We can't do either, don't bother trying.
            return;
        }

        if (inf) {
            tryAction(blocks, pow, cart, null, type, canCollect, canDispense);
        } else {
            for (BlockInventoryHolder container : RailUtil.getNearbyInventoryBlocks(blocks)) {
                Inventory inv = container.getInventory();
                if (tryAction(blocks, pow, cart, inv, type, canCollect, canDispense)) {
                    // If the action succeeded, stop.
                    return;
                }
            }
        }
    }

    private boolean tryAction(CartMechanismBlocks blocks, Power pow, Minecart cart, Inventory inv, EntityType type, boolean canCollect, boolean canDispense) {
        if (canDispense) {
            switch (pow) {
                case OFF: // power going off doesn't eat a cart unless the cart moves.
                case NA:
                    return false;
                case ON:
                    dispense(blocks, inv, type);
                    return true;
            }
        } else if (canCollect) {
            switch (pow) {
                case ON: // there's already a cart moving on the dispenser so don't spam.
                    return false;
                case OFF:
                case NA:
                    collect(cart, inv);
                    return true;
            }
        }

        return false;
    }

    /**
     * @param cart the cart to be destroyed/collected
     * @param inv the inventory to place a cart item in, or null if we don't care.
     */
    private void collect(Minecart cart, Inventory inv) {
        if (cart == null || cart.isDead()) {
            return;
        }

        if (inv != null) {
            // Add it to the inventory before we kill it, so we can reliably access the type
            inv.addItem(new ItemStack(cart.getMinecartMaterial(), 1));
        }

        cart.eject();
        cart.remove();
    }

    /**
     * @param blocks The CMB instance of this run
     * @param inv the inventory to remove a cart item from, or null if we don't care.
     */
    private void dispense(CartMechanismBlocks blocks, Inventory inv, EntityType type) {
        Location location = blocks.rail().getLocation().toCenterLocation();

        if (checkForCarts && EntityUtil.isEntityOfTypeInBlock(location.getBlock(), type)) {
            return;
        }

        if (inv != null) {
            var cartMaterial = switch (type) {
                case MINECART_CHEST -> Material.CHEST_MINECART;
                case MINECART_FURNACE -> Material.FURNACE_MINECART;
                case MINECART_HOPPER -> Material.HOPPER_MINECART;
                case MINECART_COMMAND -> Material.COMMAND_BLOCK_MINECART;
                case MINECART_TNT -> Material.TNT_MINECART;
                default -> Material.MINECART;
            };
            if (!inv.contains(cartMaterial)) {
                return;
            }
            inv.removeItem(new ItemStack(cartMaterial, 1));
        }

        Minecart cart = (Minecart) blocks.rail().getWorld().spawn(location, type.getEntityClass());

        if (propelCart) {
            BlockFace dir = SignUtil.getBack(blocks.sign());
            cart.setVelocity(dir.getDirection());
        }
    }

    @Nullable
    private EntityType parseMinecartType(String text) {
        return switch (text.toLowerCase(Locale.ENGLISH)) {
            case "hopper" -> EntityType.MINECART_HOPPER;
            case "tnt" -> EntityType.MINECART_TNT;
            case "powered" -> EntityType.MINECART_FURNACE;
            case "storage" -> EntityType.MINECART_CHEST;
            case "minecart", "rideable" -> EntityType.MINECART;
            case "command" -> EntityType.MINECART_COMMAND;
            case "mob" -> EntityType.MINECART_MOB_SPAWNER;
            default -> {
                EntityType type = Registry.ENTITY_TYPE.get(NamespacedKey.fromString(text.toLowerCase(Locale.ENGLISH)));
                if (type == null || type.getEntityClass() == null || !Minecart.class.isAssignableFrom(type.getEntityClass())) {
                    yield null;
                }
                yield type;
            }
        };
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    private boolean checkForCarts;
    private boolean propelCart;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the dispenser mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.EMERALD_ORE.getId()), true));

        config.setComment("check-for-carts", "If true, the dispenser will not dispense a cart if there is already one in the dispenser's block.");
        checkForCarts = config.getBoolean("check-for-carts", true);

        config.setComment("propel-cart", "Propels carts when they are dispensed.");
        propelCart = config.getBoolean("propel-cart", true);
    }
}
