/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.st.SpongeSelfTriggerManager;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.recipe.smelting.SmeltingResult;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Module(id = "cookingpot", name = "CookingPot", onEnable="onInitialize", onDisable="onDisable")
public class CookingPot extends SpongeSignMechanic implements SelfTriggeringMechanic, DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.cookingpot", "Allows the user to create Cooking Pots", PermissionDescription.ROLE_USER);
    private SpongePermissionNode refuelPermissions = new SpongePermissionNode("craftbook.cookingpot.refuel", "Allows the user to refuel Cooking Pots", PermissionDescription.ROLE_USER);

    private ConfigValue<Boolean> requireFuel = new ConfigValue<>("require-fuel", "Causes the cooking pot to require fuel to cook. Otherwise fuel speeds up cooking.", true);
    private ConfigValue<Boolean> redstoneFuel = new ConfigValue<>("redstone-fuel", "Allows for a pulsing redstone signal to be used as a fuel source.", false);
    private ConfigValue<Boolean> superFast = new ConfigValue<>("super-fast", "Removes the cap for 5 fuel to be used per tick, making the cooking pot faster.", false);
    private ConfigValue<Boolean> cookFoodOnly = new ConfigValue<>("food-only", "Caused the cooking pot to only cook food. Food is defined as anything that gives hunger points.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        createPermissions.register();
        refuelPermissions.register();

        requireFuel.load(config);
        redstoneFuel.load(config);
        superFast.load(config);
        cookFoodOnly.load(config);

        super.onInitialize();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable Player player) {
        ((SpongeSelfTriggerManager) CraftBookPlugin.inst().getSelfTriggerManager().get()).register(this, location);

        lines.set(2, Text.of("0"));
        lines.set(3, requireFuel.getValue() ? Text.of("0") : Text.of("1"));

        return true;
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (isValid(location)) {
                location.getTileEntity().map(sign -> (Sign) sign).ifPresent(sign -> {
                    ItemStack itemStack = player.getItemInHand(event.getHandType()).filter(stack -> FuelSource.canCookWith(stack.getType())).orElse(null);
                    if (itemStack != null) {
                        if (refuelPermissions.hasPermission(player)) {
                            int value = FuelSource.getFuelValue(itemStack.getType());
                            increaseMultiplier(sign, value);

                            if (itemStack.getQuantity() > 1) {
                                itemStack.setQuantity(itemStack.getQuantity() - 1);
                                player.setItemInHand(event.getHandType(), itemStack);
                            } else {
                                if (itemStack.getType() == ItemTypes.LAVA_BUCKET) {
                                    player.setItemInHand(event.getHandType(), ItemStack.of(ItemTypes.BUCKET, 1));
                                } else {
                                    player.setItemInHand(event.getHandType(), null);
                                }
                            }
                        } else {
                            player.sendMessage(Text.of("You don't have permission to refuel this mechanic!"));
                        }
                    } else {
                        Location<?> chestBlock = SignUtil.getBackBlock(location).add(0, 2, 0);
                        chestBlock.getTileEntity().filter(tileEntity -> tileEntity instanceof Chest).map(tileEntity -> (Chest) tileEntity).ifPresent(chest ->
                                player.openInventory(chest.getInventory()));
                    }
                });
            }
        });
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        if(!redstoneFuel.getValue())
            return;

        if(!SignUtil.isSign(source.getLocation())) return;
        Location<World> block = source.getLocation();
        Sign sign = (Sign) block.getTileEntity().get();

        if (isMechanicSign(sign)) {
            Player player = event.getCause().first(Player.class).orElse(null);
            if(player != null) {
                if(!refuelPermissions.hasPermission(player)) {
                    player.sendMessage(Text.of("You don't have permission to refuel this mechanic!"));
                    return;
                }
            }

            int newPower = BlockUtil.getBlockPowerLevel(block).orElse(0);
            int oldPower = block.get(CraftBookKeys.LAST_POWER).orElse(0);

            if (newPower != oldPower) {
                if (newPower > oldPower) {
                    increaseMultiplier(sign, newPower - oldPower);
                }
                block.offer(new LastPowerData(newPower));
            }
        }
    }

    @Override
    public void onThink(Location<World> location) {
        if (!isValid(location)) {
            ((SpongeSelfTriggerManager) CraftBookPlugin.spongeInst().getSelfTriggerManager().get()).unregister(this, location);
            return;
        }

        int lastTick = 0, oldTick;

        Sign sign = location.getTileEntity().map(tile -> (Sign) tile).get();

        try {
            lastTick = Math.max(0, Integer.parseInt(SignUtil.getTextRaw(sign, 2)));
        } catch (Exception e) {
            List<Text> lines = sign.lines().get();
            lines.set(2, Text.of(0));
            sign.offer(Keys.SIGN_LINES, lines);
        }
        oldTick = lastTick;

        Location<?> baseBlock = SignUtil.getBackBlock(location);
        Location<?> chestBlock = baseBlock.add(0, 2, 0);

        if (chestBlock.getBlockType() == BlockTypes.CHEST || chestBlock.getBlockType() == BlockTypes.TRAPPED_CHEST) {
            Location<?> fireBlock = baseBlock.add(0, 1, 0);
            if (fireBlock.getBlockType() == BlockTypes.FIRE) {
                Chest chest = chestBlock.getTileEntity().map(tile -> (Chest) tile).get();
                Inventory inventory = chest.getInventory();

                if (inventory.totalItems() > 0) {
                    if (lastTick < 500) {
                        int multiplier = getMultiplier(sign);
                        if (superFast.getValue()) {
                            lastTick += multiplier;
                        } else {
                            lastTick += Math.min(multiplier, 5);
                        }
                        if (multiplier > 0) {
                            setMultiplier(sign, multiplier - 1);
                        }
                    }
                    if (lastTick >= 50) {
                        for (Slot slot : inventory.<Slot>slots()) {
                            ItemStack item = slot.peek().orElse(null);
                            if (item != null) {
                                Optional<SmeltingResult> resultOptional = Sponge.getRegistry().getSmeltingRecipeRegistry().getResult(item.createSnapshot());
                                if (resultOptional.isPresent()) {
                                    SmeltingResult result = resultOptional.get();
                                    if (cookFoodOnly.getValue() && !result.getResult().getProperty(FoodRestorationProperty.class).isPresent()) {
                                        continue;
                                    }
                                    if (inventory.offer(result.getResult().createStack()).getRejectedItems().isEmpty()) {
                                        inventory.queryAny(item).poll(1);
                                        lastTick -= 50;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (oldTick != lastTick) {
            List<Text> lines = sign.lines().get();
            lines.set(2, Text.of(lastTick));
            sign.offer(Keys.SIGN_LINES, lines);
        }
    }

    private void setMultiplier(Sign sign, int amount) {
        if(!requireFuel.getValue())
            amount = Math.max(amount, 1);
        List<Text> lines = sign.lines().get();
        lines.set(3, Text.of(amount));
        sign.offer(Keys.SIGN_LINES, lines);
    }

    private void increaseMultiplier(Sign sign, int amount) {
        setMultiplier(sign, getMultiplier(sign) + amount);
    }

    private int getMultiplier(Sign sign) {
        int multiplier;
        try {
            multiplier = Integer.parseInt(SignUtil.getTextRaw(sign, 3));
        } catch (Exception e) {
            multiplier = requireFuel.getValue() ? 0 : 1;
            setMultiplier(sign, multiplier);
        }
        if (multiplier <= 0 && !requireFuel.getValue()) return 1;
        return Math.max(0, multiplier);
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Cook]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public String getPath() {
        return "mechanics/cooking_pot";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermissions,
                refuelPermissions
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                requireFuel,
                redstoneFuel,
                superFast,
                cookFoodOnly
        };
    }

    private enum FuelSource {
        COAL(ItemTypes.COAL, 40), COALBLOCK(ItemTypes.COAL_BLOCK, 360), LAVA(ItemTypes.LAVA_BUCKET, 6000), BLAZE(ItemTypes.BLAZE_ROD, 500),
        BLAZEDUST(ItemTypes.BLAZE_POWDER, 250), SNOWBALL(ItemTypes.SNOWBALL, -40), SNOW(ItemTypes.SNOW, -100), ICE(ItemTypes.ICE, -1000);

        private ItemType item;
        private int fuelValue;

        FuelSource(ItemType item, int fuelValue) {
            this.item = item;
            this.fuelValue = fuelValue;
        }

        public static boolean canCookWith(ItemType item) {
            for (FuelSource fuelSource : values()) {
                if (fuelSource.item == item) {
                    return true;
                }
            }
            return false;
        }

        public static int getFuelValue(ItemType item) {
            for (FuelSource fuelSource : values()) {
                if (fuelSource.item == item) {
                    return fuelSource.fuelValue;
                }
            }
            return 0;
        }
    }
}
