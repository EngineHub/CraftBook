package com.sk89q.craftbook.vehicles.cart;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockRedstoneEvent;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * <p>
 * Collects carts when carts pass over an unpowered chest marked as a dispenser; dispenses carts when power flips on
 * if no cart is already above the
 * dispenser. The dispenser does not impart velocity to dispensed carts, so it is typical to simply place the track
 * above the dispenser so as to be at
 * an angle.
 * </p>
 * <p>
 * Carts of all types (including powered and storage carts) will be collected by this mechanism,
 * but only regular minecarts are dispensed. This may be
 * changed in a future version.
 * </p>
 * <p>
 * Dispenser signs which contain "inf" on the third line will collect carts without storing them and create carts
 * without requiring them from
 * inventory.
 * </p>
 * <p>
 * Note that this is not an exact reimplementation of the mechanics from hmod; this is something new and more
 * consistent with other modern cart
 * mechanisms.
 * </p>
 *
 * @author hash
 */
public class CartDispenser extends CartBlockMechanism {

    public CartDispenser (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onCartImpact(CartBlockImpactEvent event) {

        performMechanic(event.getMinecart(), event.getBlocks());
    }

    @EventHandler
    public void onRedstoneImpact(CartBlockRedstoneEvent event) {

        performMechanic(event.getMinecart(), event.getBlocks());
    }

    public void performMechanic(Minecart cart, CartMechanismBlocks blocks) {

        // validate
        if (!blocks.matches(getMaterial())) return;
        if (!blocks.matches("dispenser")) return;

        // detect intentions
        Power pow = isActive(blocks);
        boolean inf = "inf".equalsIgnoreCase(blocks.getSign().getLine(2));

        if(inf) {

            CartType type = CartType.fromString(blocks.getSign().getLine(0));

            // go
            if (cart == null) {
                switch (pow) {
                    case ON:
                        if(!blocks.getSign().getLine(3).toLowerCase(Locale.ENGLISH).contains("collect"))
                            dispense(blocks, null, type);
                        return;
                    case OFF: // power going off doesn't eat a cart unless the cart moves.
                    case NA:
                }
            } else {
                switch (pow) {
                    case ON: // there's already a cart moving on the dispenser so don't spam.
                        return;
                    case OFF:
                    case NA:
                        if(!blocks.getSign().getLine(3).toLowerCase(Locale.ENGLISH).contains("dispense"))
                            collect(cart, null);
                        return;
                }
            }
        } else {
            for (Chest c : RailUtil.getNearbyChests(blocks.base)) {
                Inventory inv = c.getInventory();

                CartType type = CartType.fromString(blocks.getSign().getLine(0));

                // go
                if (cart == null) {
                    switch (pow) {
                        case ON:
                            if(!blocks.getSign().getLine(3).toLowerCase(Locale.ENGLISH).contains("collect"))
                                dispense(blocks, inv, type);
                            return;
                        case OFF: // power going off doesn't eat a cart unless the cart moves.
                        case NA:
                    }
                } else {
                    switch (pow) {
                        case ON: // there's already a cart moving on the dispenser so don't spam.
                            return;
                        case OFF:
                        case NA:
                            if(!blocks.getSign().getLine(3).toLowerCase(Locale.ENGLISH).contains("dispense"))
                                collect(cart, inv);
                            return;
                    }
                }
            }
        }
    }

    /**
     * @param cart the cart to be destroyed/collected
     * @param inv  the inventory to place a cart item in, or null if we don't care.
     */
    private void collect(Minecart cart, Inventory inv) {

        if (cart == null || cart.isDead()) return;
        cart.eject();
        cart.setDamage(9000);
        cart.remove();
        if (inv != null) {
            int cartType = ItemType.MINECART.getID();
            if (cart instanceof StorageMinecart) {
                cartType = ItemType.STORAGE_MINECART.getID();
            } else if (cart instanceof PoweredMinecart) {
                cartType = ItemType.POWERED_MINECART.getID();
            } else if (cart instanceof ExplosiveMinecart) {
                cartType = ItemType.TNT_MINECART.getID();
            } else if (cart instanceof HopperMinecart) {
                cartType = ItemType.HOPPER_MINECART.getID();
            }
            inv.addItem(new ItemStack(cartType, 1));
        }
    }

    /**
     * @param blocks nuff said
     * @param inv    the inventory to remove a cart item from, or null if we don't care.
     */
    private void dispense(CartMechanismBlocks blocks, Inventory inv, CartType type) {

        Location location = BukkitUtil.center(blocks.rail.getLocation());

        if(CraftBookPlugin.inst().getConfiguration().minecartDispenserLegacy) {
            BlockFace direction =  SignUtil.getFront(blocks.sign).getOppositeFace();
            location = blocks.rail.getRelative(direction).getLocation();
        }

        if(CraftBookPlugin.inst().getConfiguration().minecartDispenserAntiSpam && EntityUtil.isEntityOfTypeInBlock(location.getBlock(), EntityType.MINECART))
            return;

        if (inv != null) {
            if (type.equals(CartType.Minecart)) {
                if (!inv.contains(ItemType.MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.MINECART.getID(), 1));
            } else if (type.equals(CartType.StorageMinecart)) {
                if (!inv.contains(ItemType.STORAGE_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.STORAGE_MINECART.getID(), 1));
            } else if (type.equals(CartType.PoweredMinecart)) {
                if (!inv.contains(ItemType.POWERED_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.POWERED_MINECART.getID(), 1));
            } else if (type.equals(CartType.TNTMinecart)) {
                if (!inv.contains(ItemType.TNT_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.TNT_MINECART.getID(), 1));
            } else if (type.equals(CartType.HopperMinecart)) {
                if (!inv.contains(ItemType.HOPPER_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.HOPPER_MINECART.getID(), 1));
            }
        }
        blocks.rail.getWorld().spawn(location, type.toClass());
    }

    public enum CartType {
        Minecart("Minecart", Minecart.class), StorageMinecart("Storage", StorageMinecart.class),
        PoweredMinecart("Powered", PoweredMinecart.class), TNTMinecart("TNT", ExplosiveMinecart.class), HopperMinecart("Hopper", org.bukkit.entity.minecart.HopperMinecart.class);

        private final Class<? extends Minecart> cl;
        private final String name;

        private CartType(String name, Class<? extends Minecart> cl) {

            this.name = name;
            this.cl = cl;
        }

        public static CartType fromString(String s) {

            for (CartType ct : CartType.values()) {
                if (ct == null) {
                    continue;
                }
                if (ct.name.equalsIgnoreCase(s)) return ct;
            }
            return Minecart; // Default to minecarts
        }

        public Class<? extends Minecart> toClass() {

            return cl;
        }
    }

    @Override
    public String getName() {

        return "Dispenser";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Dispenser"};
    }
}