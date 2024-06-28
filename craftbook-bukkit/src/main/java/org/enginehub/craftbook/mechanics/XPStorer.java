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

package org.enginehub.craftbook.mechanics;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.st.BukkitSelfTriggerManager;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.TernaryState;
import org.enginehub.craftbook.util.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.util.events.SelfTriggerThinkEvent;

import java.util.ArrayList;
import java.util.List;

public class XPStorer extends AbstractCraftBookMechanic {

    private final NamespacedKey xpQuantityKey = new NamespacedKey("craftbook", "xp_quantity");

    public XPStorer(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    private ItemStack createStack(int bottles) {
        ItemStack stack = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.min(bottles, 64));
        if (bottleXpOverride >= 0) {
            ItemMeta meta = stack.getItemMeta();
            meta.getPersistentDataContainer().set(xpQuantityKey, PersistentDataType.INTEGER, bottleXpOverride);
            // TODO Make this translatable when possible.
            meta.lore(List.of(net.kyori.adventure.text.Component.text("Stored XP: " + bottleXpOverride)));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!EventUtil.passesFilter(event)
            || (!allowOffHand && event.getHand() != EquipmentSlot.HAND)
            || event.getPlayer().getLevel() < 1) {
            return;
        }

        if (!block.equalsFuzzy(BukkitAdapter.adapt(event.getClickedBlock().getBlockData()))) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!allowSneaking.doesPass(player.isSneaking())) {
            return;
        }

        int maxBottleCount = Integer.MAX_VALUE;

        if (requireBottle) {
            if (event.getItem() == null || event.getItem().getType() != Material.GLASS_BOTTLE) {
                if (event.getHand() == EquipmentSlot.HAND
                    && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.GLASS_BOTTLE
                    && (!allowOffHand || event.getPlayer().getInventory().getItemInOffHand().getType() != Material.GLASS_BOTTLE)) {
                    // Only show this for the main-hand event
                    player.printError(TranslatableComponent.of("craftbook.xpstorer.require-bottle"));
                }
                return;
            }

            maxBottleCount = event.getItem().getAmount();
        }

        if (!player.hasPermission("craftbook.xpstorer.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        int xp = 0;

        float pcnt = event.getPlayer().getExp();
        int level = event.getPlayer().getLevel();

        event.getPlayer().setExp(0);
        xp += (int) (event.getPlayer().getExpToLevel() * pcnt);

        while (event.getPlayer().getLevel() > 0) {
            event.getPlayer().setLevel(event.getPlayer().getLevel() - 1);
            xp += event.getPlayer().getExpToLevel();
        }

        event.getPlayer().setLevel(level);
        event.getPlayer().setExp(pcnt);

        if (xp < bottleXpRequirement) {
            player.printError(TranslatableComponent.of("craftbook.xpstorer.insufficient-xp"));
            return;
        }

        int bottleCount = (int) Math.min(maxBottleCount, Math.floor(xp / (double) bottleXpRequirement));

        if (requireBottle) {
            event.getItem().subtract(bottleCount);
        }

        int tempBottles = bottleCount;

        while (tempBottles > 0) {
            ItemStack bottles = createStack(tempBottles);
            event.getClickedBlock().getWorld().dropItemNaturally(LocationUtil.getBlockCentreTop(event.getClickedBlock()), bottles);
            tempBottles -= 64;
        }

        event.getPlayer().setLevel(0);
        event.getPlayer().setExp(0);

        int remainingXP = xp - bottleCount * bottleXpRequirement;
        event.getPlayer().giveExp(remainingXP, false);

        player.printInfo(TranslatableComponent.of("craftbook.xpstorer.success"));
        event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        // We only expect signs for radius mode.
        if (!radiusMode || !EventUtil.passesFilter(event)) {
            return;
        }

        Block baseBlock = SignUtil.getBackBlock(event.getBlock());

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[XP]")
            || !block.equalsFuzzy(BukkitAdapter.adapt(baseBlock.getBlockData()))) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.xpstorer.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.create-permission");
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        int signRadius = maxRadius;
        try {
            signRadius = Math.max(maxRadius, Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(event.line(2))));
        } catch (Exception ignored) {
        }

        event.line(1, Component.text("[XP]"));
        event.line(2, Component.text(signRadius));
        player.printInfo(TranslatableComponent.of("craftbook.xpstorer.create"));

        // TODO Improve ST manager to not require bukkit stuff
        ((BukkitSelfTriggerManager) CraftBook.getInstance().getPlatform().getSelfTriggerManager()).registerSelfTrigger(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(SelfTriggerPingEvent event) {
        if (!radiusMode || !EventUtil.passesFilter(event)) {
            return;
        }

        Block baseBlock = SignUtil.getBackBlock(event.getBlock());

        if (!SignUtil.isSign(event.getBlock()) || !block.equalsFuzzy(BukkitAdapter.adapt(baseBlock.getBlockData()))) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState(false);

        for (Side side : Side.values()) {
            if (!sign.getSide(side).getLine(1).equals("[XP]")) {
                return;
            }

            event.setHandled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThink(SelfTriggerThinkEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign bukkitSign = (Sign) event.getBlock().getState(false);
        ChangedSign sign = null;

        for (Side side : Side.values()) {
            String line1 = PlainTextComponentSerializer.plainText().serialize(bukkitSign.line(1));
            if (!line1.equals("[XP]")) {
                continue;
            }
            sign = ChangedSign.create(event.getBlock(), side, bukkitSign.lines().toArray(new Component[0]), null);
            break;
        }
        if (sign == null) {
            // None found
            return;
        }

        event.setHandled(true);

        int signRadius = maxRadius;
        try {
            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            signRadius = Math.max(maxRadius, Integer.parseInt(line2));
        } catch (Exception ignored) {
            sign.setLine(2, Component.text(signRadius));
            sign.update(false);
        }

        int xp = 0;
        Block baseBlock = SignUtil.getBackBlock(event.getBlock());

        List<ExperienceOrb> orbs = new ArrayList<>();

        for (Entity entity : baseBlock.getLocation().getNearbyEntities(signRadius, signRadius, signRadius)) {
            if (entity instanceof ExperienceOrb orb && entity.getTicksLived() > 20) {
                xp += orb.getExperience();
                orbs.add(orb);
            }
        }

        int max = Integer.MAX_VALUE;
        Inventory inventory = null;

        if (requireBottle && InventoryUtil.doesBlockHaveInventory(baseBlock.getRelative(BlockFace.UP))) {
            inventory = ((InventoryHolder) baseBlock.getRelative(BlockFace.UP).getState(false)).getInventory();
            max = 0;
            for (ItemStack stack : inventory.getContents()) {
                if (ItemUtil.isStackValid(stack) && stack.getType() == Material.GLASS_BOTTLE) {
                    max += stack.getAmount();
                }
            }
        } else if (requireBottle) {
            return;
        }

        int bottleCount = (int) Math.min(max, Math.floor(xp / (double) bottleXpRequirement));
        int tempBottles = bottleCount;

        while (tempBottles > 0) {
            ItemStack bottles = createStack(tempBottles);
            if (inventory != null) {
                for (ItemStack leftover : inventory.addItem(bottles).values()) {
                    event.getBlock().getWorld().dropItemNaturally(LocationUtil.getBlockCentreTop(baseBlock), leftover);
                }
            } else {
                event.getBlock().getWorld().dropItemNaturally(LocationUtil.getBlockCentreTop(baseBlock), bottles);
            }

            tempBottles -= 64;
        }

        if (requireBottle && inventory != null) {
            var leftovers = inventory.removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleCount));
            for (ItemStack stack : leftovers.values()) {
                int amount = stack.getAmount();
                for (int i = 0; i < inventory.getContents().length; i++) {
                    if (amount <= 0) {
                        break;
                    }
                    ItemStack content = inventory.getContents()[i];
                    if (ItemUtil.isStackValid(content) && content.getType() == stack.getType()) {
                        content.subtract(amount);
                        amount = Math.max(0, amount - content.getAmount());
                    }
                }
            }
        }

        int remainingXP = xp - bottleCount * bottleXpRequirement;
        for (ExperienceOrb orb : orbs) {
            if (remainingXP > 0) {
                orb.setExperience(Math.min(5, remainingXP));
                remainingXP -= 5;
            } else {
                orb.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThrow(PlayerLaunchProjectileEvent event) {
        if (bottleXpOverride < 0
            || !(event.getProjectile() instanceof ThrownExpBottle)
            || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getItemStack().getType() != Material.EXPERIENCE_BOTTLE || !event.getItemStack().hasItemMeta()) {
            return;
        }

        ItemMeta meta = event.getItemStack().getItemMeta();
        if (!meta.getPersistentDataContainer().has(xpQuantityKey, PersistentDataType.INTEGER)) {
            return;
        }

        event.getProjectile().getPersistentDataContainer().set(
            xpQuantityKey,
            PersistentDataType.INTEGER,
            meta.getPersistentDataContainer().get(xpQuantityKey, PersistentDataType.INTEGER)
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExpBreak(ExpBottleEvent event) {
        if (bottleXpOverride < 0
            || !EventUtil.passesFilter(event)) {
            return;
        }

        ThrownExpBottle bottle = event.getEntity();
        if (!bottle.getPersistentDataContainer().has(xpQuantityKey, PersistentDataType.INTEGER)) {
            return;
        }

        event.setExperience(bottle.getPersistentDataContainer().get(xpQuantityKey, PersistentDataType.INTEGER));
    }

    private boolean requireBottle;
    private boolean allowOffHand;
    private int bottleXpRequirement;
    private int bottleXpOverride;
    private BaseBlock block;
    private TernaryState allowSneaking;
    private boolean radiusMode;
    private int maxRadius;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("require-bottle", "Requires the player to be holding a glass bottle to use.");
        requireBottle = config.getBoolean("require-bottle", true);

        config.setComment("allow-offhand", "Allows XP bottles in the off hand to work.");
        allowOffHand = config.getBoolean("allow-offhand", true);

        config.setComment("bottle-xp-requirement", "Sets the amount of XP points required per each bottle.");
        bottleXpRequirement = config.getInt("bottle-xp-requirement", 16);

        config.setComment("bottle-xp-override", "Set the amount of XP points that each bottle provides on usage (-1 to use MC behaviour).");
        bottleXpOverride = config.getInt("bottle-xp-override", -1);

        config.setComment("block", "The block that is an XP Storer.");
        block = BlockParser.getBlock(config.getString("block", BlockTypes.SPAWNER.id()), true);

        config.setComment("allow-sneaking", "Sets how the player must be sneaking in order to use the XP Storer.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.FALSE.toString()));

        config.setComment("radius-mode", "Allows XP Storer mechanics with a sign attached to work in a radius.");
        radiusMode = config.getBoolean("radius-mode", false);

        config.setComment("max-radius", "The max radius when using radius-mode.");
        maxRadius = config.getInt("max-radius", 5);
    }
}
