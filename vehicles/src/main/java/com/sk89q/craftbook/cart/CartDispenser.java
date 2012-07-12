package com.sk89q.craftbook.cart;

import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * <p>
 * Collects carts when carts pass over an unpowered chest marked as a dispenser;
 * dispenses carts when power flips on if no cart is already above the
 * dispenser. The dispenser does not impart velocity to dispensed carts, so it
 * is typical to simply place the track above the dispenser so as to be at an
 * angle.
 * </p>
 * 
 * <p>
 * Carts of all types (including powered and storage carts) will be collected by
 * this mechanism, but only regular minecarts are dispensed. This may be changed
 * in a future version.
 * </p>
 * 
 * <p>
 * Dispenser signs which contain "inf" on the third line will collect carts
 * without storing them and create carts without requiring them from inventory.
 * </p>
 * 
 * <p>
 * Note that this is not an exact reimplementation of the mechanics from hmod;
 * this is something new and more consistent with other modern cart mechanisms.
 * </p>
 * 
 * @author hash
 * 
 */
public class CartDispenser extends CartMechanism {
    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // care?
        if (minor) return;

        // validate
        if (!blocks.matches("dispenser")) return;

        // detect intentions
        Power pow = isActive(blocks.rail, blocks.base, blocks.sign);
        boolean inf = "inf".equalsIgnoreCase(blocks.getSign().getLine(2));
        Inventory inv = inf ? null : ((Chest)blocks.base.getState()).getInventory();

        CartType type = CartType.fromString(blocks.getSign().getLine(3));

        // go
        if (cart == null)
            switch (pow) {
            case ON:
                dispense(blocks, inv, type);
                return;
            case OFF:       // power going off doesn't eat a cart unless the cart moves.
            case NA:
                return;
            }
        else
            switch (pow) {
            case ON:            // there's already a cart moving on the dispenser so don't spam.
                return;
            case OFF:
            case NA:
                collect(cart, inv);
                return;
            }
    }

    /**
     * @param cart the cart to be destroyed/collected
     * @param inv the inventory to place a cart item in, or null if we don't care.
     */
    private void collect(Minecart cart, Inventory inv) {
        cart.eject();
        cart.setDamage(9000);
        cart.remove();
        if (inv != null) {
            int cartType = ItemType.MINECART.getID();
            if (cart instanceof StorageMinecart) cartType = ItemType.STORAGE_MINECART.getID();
            else if (cart instanceof PoweredMinecart) cartType = ItemType.POWERED_MINECART.getID();
            inv.addItem(new ItemStack(cartType, 1));
        }
    }

    /**
     * @param blocks nuff said
     * @param inv the inventory to place a cart item in, or null if we don't care.
     */
    @SuppressWarnings("unchecked")
    private void dispense(CartMechanismBlocks blocks, Inventory inv, CartType type) {
        if (inv != null) {
            if(type.equals(CartType.Minecart)) {
                if (!inv.contains(ItemType.MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.MINECART.getID(), 1));
            }
            else if(type.equals(CartType.StorageMinecart)) {
                if (!inv.contains(ItemType.STORAGE_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.STORAGE_MINECART.getID(), 1));
            }
            else if(type.equals(CartType.PoweredMinecart)) {
                if (!inv.contains(ItemType.POWERED_MINECART.getID())) return;
                inv.removeItem(new ItemStack(ItemType.POWERED_MINECART.getID(), 1));
            }
        }
        blocks.rail.getWorld().spawn(BukkitUtil.center(blocks.rail.getLocation()), type.toClass());
    }

    public enum CartType {
        Minecart("Minecart", org.bukkit.entity.Minecart.class),
        StorageMinecart("Storage", org.bukkit.entity.StorageMinecart.class),
        PoweredMinecart("Powered", org.bukkit.entity.PoweredMinecart.class);

        private Class<?> cl;
        private String name;

        private CartType(String name, Class<?> cl) {
            this.name = name;
            this.cl = cl;
        }

        public static CartType fromString(String s) {
            for(CartType ct : CartType.values())
            {
                if(ct == null) continue;
                if(ct.name.equalsIgnoreCase(s))
                    return ct;
            }
            return Minecart; //Default to minecarts
        }
        @SuppressWarnings("rawtypes")
        public Class toClass() {
            return cl;
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }
}