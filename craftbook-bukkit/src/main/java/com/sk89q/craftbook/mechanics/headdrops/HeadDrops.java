/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics.headdrops;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.MechanicCommandRegistrar;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.area.AreaCommands;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class HeadDrops extends AbstractCraftBookMechanic {

    protected static HeadDrops instance;

    @Override
    public boolean enable() {

        instance = this;

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
                "headdrops",
                Lists.newArrayList(),
                "CraftBook HeadDrops Commands",
                HeadDropsCommands::register
        );

        return true;
    }

    @Override
    public void disable() {
        super.disable();

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("headdrops");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(playerKillsOnly && event.getEntity().getKiller() == null) return;
        if(event.getEntityType() == null) return;

        if(event.getEntity().getKiller() != null && !event.getEntity().getKiller().hasPermission("craftbook.mech.headdrops.kill"))
            return;

        String typeName = event.getEntityType().getName();
        if (typeName == null && event.getEntityType() == EntityType.PLAYER)
            typeName = "PLAYER";
        else if (typeName == null)
            return; //Invalid type.
        else
            typeName = typeName.toUpperCase();

        double chance = Math.min(1, dropRate);
        if(customDropRates.containsKey(typeName))
            chance = Math.min(1, customDropRates.get(typeName));

        if(event.getEntity().getKiller() != null && event.getEntity().getKiller().getItemInHand() != null && event.getEntity().getKiller().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
            chance = Math.min(1, chance + rateModifier * event.getEntity().getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));

        if(ThreadLocalRandom.current().nextDouble() > chance)
            return;

        ItemStack toDrop = null;

        switch(event.getEntityType()) {

            case PLAYER:
                if(!enablePlayers)
                    return;
                String playerName = event.getEntity().getName();
                if (ignoredNames.contains(playerName)) {
                    return;
                }
                toDrop = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta meta = (SkullMeta) toDrop.getItemMeta();
                meta.setOwner(playerName);
                meta.setDisplayName(ChatColor.RESET + playerName + "'s Head");
                toDrop.setItemMeta(meta);
                break;
            case ZOMBIE:
                if(!enableMobs)
                    return;
                toDrop = new ItemStack(Material.ZOMBIE_HEAD, 1);
                break;
            case CREEPER:
                if(!enableMobs)
                    return;
                toDrop = new ItemStack(Material.CREEPER_HEAD, 1);
                break;
            case SKELETON:
                if(!enableMobs)
                    return;
                toDrop = new ItemStack(Material.SKELETON_SKULL, 1);
                break;
            case WITHER_SKELETON:
                if (!enableMobs || !overrideNatural)
                    return;
                toDrop = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
                break;
            case ENDER_DRAGON:
                if(!enableMobs)
                    return;
                toDrop = new ItemStack(Material.DRAGON_HEAD, 1);
                break;
            default:
                if(!enableMobs)
                    return;
                MobSkullType type = MobSkullType.getFromEntityType(event.getEntityType());
                String mobName = null;
                if(type != null)
                    mobName = type.getPlayerName();
                if(customSkins.containsKey(typeName))
                    mobName = customSkins.get(typeName);
                if(mobName == null || mobName.isEmpty())
                    break;
                toDrop = new ItemStack(Material.PLAYER_HEAD, 1);
                ItemMeta metaD = toDrop.getItemMeta();
                if(metaD instanceof SkullMeta) {
                    SkullMeta itemMeta = (SkullMeta) metaD;
                    itemMeta.setDisplayName(ChatColor.RESET + WordUtils.capitalize(typeName.replace("_", " ")) + " Head");
                    itemMeta.setOwner(mobName);
                    toDrop.setItemMeta(itemMeta);
                } else
                    CraftBookPlugin.logger().warning("Bukkit has failed to set a HeadDrop item to a head!");
                break;
        }

        if(ItemUtil.isStackValid(toDrop)) {
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), toDrop);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(!EventUtil.passesFilter(event) || event.getHand() != EquipmentSlot.HAND) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(event.getClickedBlock().getType() == Material.PLAYER_HEAD || event.getClickedBlock().getType() == Material.PLAYER_WALL_HEAD) {

            Skull skull = (Skull)event.getClickedBlock().getState();
            if(skull == null || !skull.hasOwner())
                return;

            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            if(showNameClick && MobSkullType.getEntityType(skull.getOwner()) == null && skull.getOwner() != null) {
                player.printRaw(ChatColor.YELLOW + player.translate("mech.headdrops.click-message") + ' ' + skull.getOwner());
            } else if (MobSkullType.getEntityType(skull.getOwner()) != null) {
                skull.setOwner(MobSkullType.getFromEntityType(MobSkullType.getEntityType(skull.getOwner())).getPlayerName());
                skull.update();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!miningDrops) return;
        if(!EventUtil.passesFilter(event))
            return;
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(event.getBlock().getType() == Material.PLAYER_HEAD || event.getBlock().getType() == Material.PLAYER_WALL_HEAD) {

            Skull skull = (Skull)event.getBlock().getState();
            if(!skull.hasOwner())
                return;
            String playerName = ChatColor.stripColor(skull.getOwner());
            if (playerName == null || ignoredNames.contains(playerName)) {
                return;
            }
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            EntityType type = MobSkullType.getEntityType(playerName);

            ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setOwner(playerName);

            if(type != null && !enableMobs)
                return;
            if(type == null && !enablePlayers)
                return;

            if(!event.getPlayer().hasPermission("craftbook.mech.headdrops.break")) {
                player.printError("mech.headdrops.break-permission");
                return;
            }

            if(type != null)
                meta.setDisplayName(ChatColor.RESET + WordUtils.capitalize(type.getName().replace("_", " ")) + " Head");
            else
                meta.setDisplayName(ChatColor.RESET + playerName + "'s Head");

            stack.setItemMeta(meta);

            if(!ProtectionUtil.canBuild(event.getPlayer(), event.getBlock(), false))
                return;

            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
        }
    }

    protected enum MobSkullType {

        //Official
        BAT("bozzobrain", "coolwhip101"),
        BLAZE("MHF_Blaze", "Blaze_Head"),
        CAVE_SPIDER("MHF_CaveSpider"),
        CHICKEN("MHF_Chicken", "scraftbrothers1"),
        COW("MHF_Cow", "VerifiedBernard", "CarlosTheCow"),
        DONKEY("Donkey"),
        ELDER_GUARDIAN("ElderGuardian"),
        ENDERMAN("MHF_Enderman", "Violit"),
        ENDERMITE("MHF_Endermite"),
        ENDER_DRAGON("MHF_EnderDragon"),
        EVOKER("MFH_Evoker"),
        GHAST("MHF_Ghast", "_QuBra_"),
        GUARDIAN("MHF_Guardian", "Guardian"),
        HORSE("gavertoso"),
        IRON_GOLEM("MHF_Golem", "zippie007"),
        MAGMA_CUBE("MHF_LavaSlime"),
        MUSHROOM_COW("MHF_MushroomCow", "Mooshroom_Stew"),
        OCELOT("MHF_Ocelot", "scraftbrothers3"),
        PARROT("MHF_Parrot"),
        PIG("MHF_Pig", "XlexerX"),
        PIG_ZOMBIE("MHF_PigZombie", "ManBearPigZombie", "scraftbrothers5"),
        POLAR_BEAR("Polar_Bear", "ice_bear", "_DmacK_"),
        RABBIT("MHF_Rabbit", "rabbit2077"),
        SHEEP("MHF_Sheep", "SGT_KICYORASS", "Eagle_Peak"),
        SHULKER("MHF_Shulker"),
        //SILVERFISH("Xzomag", "AlexVMiner"),
        SLIME("MHF_Slime", "HappyHappyMan"),
        SNOWMAN("MHF_SnowGolem", "Koebasti", "scraftbrothers2"),
        SPIDER("MHF_Spider", "Kelevra_V"),
        STRAY("MHF_Stray"),
        SQUID("MHF_Squid", "squidette8"),
        WITCH("MHF_Witch", "scrafbrothers4"),
        WITHER("MHF_Wither"),
        WOLF("MHF_Wolf", "Budwolf"),
        VEX("MHF_Vex"),
        VILLAGER("MHF_Villager", "Villager", "Kuvase", "scraftbrothers9");

        //Unofficial/Community

        MobSkullType(String playerName, String ... oldNames) {

            this.playerName = playerName;
            this.oldNames = new HashSet<>(Arrays.asList(oldNames));
        }

        private String playerName;
        private Set<String> oldNames;

        public String getPlayerName() {

            return playerName;
        }

        public boolean isOldName(String name) {

            return oldNames.contains(name);
        }

        public static MobSkullType getFromEntityType(EntityType entType) {

            try {
                return MobSkullType.valueOf(entType.name());
            } catch(Exception e){
                return null;
            }
        }

        public static EntityType getEntityType(String name) {

            if (name == null)
                return null;

            for(MobSkullType type : values())
                if(type.playerName.equalsIgnoreCase(name) || type.isOldName(name) || name.equalsIgnoreCase(instance.customSkins.get(EntityType.valueOf(type.name()).getName().toUpperCase())))
                    return EntityType.valueOf(type.name());

            return null;
        }
    }

    private boolean enableMobs;
    private boolean enablePlayers;
    private boolean playerKillsOnly;
    private boolean miningDrops;
    private boolean overrideNatural;
    private double dropRate;
    private double rateModifier;
    private boolean showNameClick;
    private HashMap<String, Double> customDropRates;
    private HashMap<String, String> customSkins;
    private List<String> ignoredNames;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("drop-mob-heads", "Allow the Head Drops mechanic to drop mob heads.");
        enableMobs = config.getBoolean("drop-mob-heads", true);

        config.setComment("drop-player-heads", "Allow the Head Drops mechanic to drop player heads.");
        enablePlayers = config.getBoolean("drop-player-heads", true);

        config.setComment("require-player-killed", "Only drop heads when killed by a player. Otherwise they will drop heads on any death.");
        playerKillsOnly = config.getBoolean("require-player-killed", true);

        config.setComment("drop-head-when-mined", "When enabled, heads keep their current skin when mined and are dropped accordingly.");
        miningDrops = config.getBoolean("drop-head-when-mined", true);

        config.setComment("override-natural-head-drops", "Override natural head drops, this will cause natural head drops to use the chances provided by CraftBook. (Eg, Wither Skeleton Heads)");
        overrideNatural = config.getBoolean("override-natural-head-drops", false);

        config.setComment("drop-rate", "A value between 1 and 0 which dictates the global chance of heads being dropped. This can be overridden per-entity type.");
        dropRate = config.getDouble("drop-rate", 0.05);

        config.setComment("looting-rate-modifier", "This amount is added to the chance for every looting level on an item. Eg, a chance of 0.05(5%) and a looting mod of 0.05(5%) on a looting 3 sword, would give a 0.20 chance (20%).");
        rateModifier = config.getDouble("looting-rate-modifier", 0.05);

        config.setComment("show-name-right-click", "When enabled, right clicking a placed head will say the owner of the head's skin.");
        showNameClick = config.getBoolean("show-name-right-click", true);

        customDropRates = new HashMap<>();
        if(config.getKeys("drop-rates") != null) {
            for(String key : config.getKeys("drop-rates"))
                customDropRates.put(key.toUpperCase(), config.getDouble("drop-rates." + key));
        } else
            config.addNode("drop-rates");
        customSkins = new HashMap<>();
        if(config.getKeys("custom-mob-skins") != null) {
            for(String key : config.getKeys("custom-mob-skins"))
                customSkins.put(key.toUpperCase(), config.getString("custom-mob-skins." + key));
        } else
            config.addNode("custom-mob-skins");

        config.setComment("ignored-names", "List of usernames to ignore when the head is touched.");
        ignoredNames = config.getStringList("ignored-names", Lists.newArrayList("cscorelib"));
    }
}