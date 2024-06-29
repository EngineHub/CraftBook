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

package org.enginehub.craftbook.mechanics.dispenser;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.Registry;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.dispenser.recipe.Cannon;
import org.enginehub.craftbook.mechanics.dispenser.recipe.DispenserRecipe;
import org.enginehub.craftbook.mechanics.dispenser.recipe.EntityMover;
import org.enginehub.craftbook.mechanics.dispenser.recipe.FireArrows;
import org.enginehub.craftbook.mechanics.dispenser.recipe.ItemShooter;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.jspecify.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class DispenserRecipes extends AbstractCraftBookMechanic {

    public static final Registry<DispenserRecipe> REGISTRY = new NamespacedRegistry<>("dispenser recipe", "craftbook");

    public DispenserRecipes(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        if (xpShooterEnable) {
            addRecipe(new ItemShooter(
                "craftbook:xp_shooter",
                Material.EXPERIENCE_BOTTLE,
                new Material[] {
                    Material.AIR, Material.REDSTONE, Material.AIR,
                    Material.REDSTONE, Material.GLASS_BOTTLE, Material.REDSTONE,
                    Material.AIR, Material.REDSTONE, Material.AIR
                }
            ));
        }
        if (snowShooterEnable) {
            addRecipe(new ItemShooter(
                "craftbook:snow_shooter",
                Material.SNOWBALL,
                new Material[] {
                    Material.AIR, Material.SNOW_BLOCK, Material.AIR,
                    Material.SNOW_BLOCK, Material.POTION, Material.SNOW_BLOCK,
                    Material.AIR, Material.SNOW_BLOCK, Material.AIR
                }
            ));
        }
        if (fireArrowsEnable) {
            addRecipe(new FireArrows(
                "craftbook:fire_arrows",
                new Material[] {
                    Material.AIR, Material.FIRE_CHARGE, Material.AIR,
                    Material.FIRE_CHARGE, Material.ARROW, Material.FIRE_CHARGE,
                    Material.AIR, Material.FIRE_CHARGE, Material.AIR
                }
            ));
        }
        if (fanEnable) {
            addRecipe(new EntityMover(
                "craftbook:fan",
                10,
                new Material[] {
                    Material.COBWEB, Material.OAK_LEAVES, Material.COBWEB,
                    Material.OAK_LEAVES, Material.PISTON, Material.OAK_LEAVES,
                    Material.COBWEB, Material.OAK_LEAVES, Material.COBWEB
                }
            ));
        }
        if (vacuumEnable) {
            addRecipe(new EntityMover(
                "craftbook:vacuum",
                -5,
                new Material[] {
                    Material.COBWEB, Material.OAK_LEAVES, Material.COBWEB,
                    Material.OAK_LEAVES, Material.STICKY_PISTON, Material.OAK_LEAVES,
                    Material.COBWEB, Material.OAK_LEAVES, Material.COBWEB
                }
            ));
        }
        if (cannonEnable) {
            addRecipe(new Cannon(
                "craftbook:cannon",
                new Material[] {
                    Material.FIRE_CHARGE, Material.GUNPOWDER, Material.FIRE_CHARGE,
                    Material.GUNPOWDER, Material.TNT, Material.GUNPOWDER,
                    Material.FIRE_CHARGE, Material.GUNPOWDER, Material.FIRE_CHARGE
                }
            ));
        }
    }

    @Override
    public void disable() {
        // Clear the existing registry.
        REGISTRY.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDispense(BlockPreDispenseEvent event) {
        if (!EventUtil.passesFilter(event) || event.getBlock().getType() != Material.DISPENSER) {
            return;
        }

        Block block = event.getBlock();
        Dispenser dispenser = (Dispenser) block.getState(false);
        Directional dispenserData = (Directional) block.getBlockData();
        BlockFace direction = dispenserData.getFacing();

        Inventory inventory = dispenser.getInventory();
        ItemStack[] stacks = inventory.getContents();

        for (DispenserRecipe r : REGISTRY.values()) {
            Material[] recipe = r.getRecipe();
            if (checkRecipe(stacks, recipe)) {
                r.apply(block, event.getItemStack(), direction);
                for (int i = 0; i < stacks.length; i++) {
                    if (recipe[i] != Material.AIR) {
                        stacks[i] = ItemUtil.getUsedItem(stacks[i]);
                    }
                }
                inventory.setContents(stacks);

                block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                block.getWorld().playEffect(block.getLocation().toCenterLocation(), Effect.SMOKE, direction);
                event.setCancelled(true);
                return;
            }
        }
    }

    private static boolean checkRecipe(@Nullable ItemStack[] stacks, Material[] recipe) {
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            Material id = stack == null ? Material.AIR : stack.getType();
            if (recipe[i] != id) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds a dispenser recipe.
     *
     * @param recipe the recipe to add
     */
    public void addRecipe(DispenserRecipe recipe) {
        checkNotNull(recipe, "Recipe must not be null");
        REGISTRY.register(recipe.id(), recipe);
    }

    private boolean cannonEnable;
    private boolean fanEnable;
    private boolean vacuumEnable;
    private boolean fireArrowsEnable;
    private boolean snowShooterEnable;
    private boolean xpShooterEnable;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("cannon-enable", "Enables Cannon Dispenser Recipe.");
        cannonEnable = config.getBoolean("cannon-enable", true);

        config.setComment("fan-enable", "Enables Fan Dispenser Recipe.");
        fanEnable = config.getBoolean("fan-enable", true);

        config.setComment("vacuum-enable", "Enables Vacuum Dispenser Recipe.");
        vacuumEnable = config.getBoolean("vacuum-enable", true);

        config.setComment("fire-arrows-enable", "Enables Fire Arrows Dispenser Recipe.");
        fireArrowsEnable = config.getBoolean("fire-arrows-enable", true);

        config.setComment("snow-shooter-enable", "Enables Snow Shooter Dispenser Recipe.");
        snowShooterEnable = config.getBoolean("snow-shooter-enable", true);

        config.setComment("xp-shooter-enable", "Enables XP Shooter Dispenser Recipe.");
        xpShooterEnable = config.getBoolean("xp-shooter-enable", true);
    }
}
