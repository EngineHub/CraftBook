package com.sk89q.craftbook.mech;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ProtectionUtil;

public class HeadDrops extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().headDropsEnabled) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(CraftBookPlugin.inst().getConfiguration().headDropsPlayerKillOnly && event.getEntity().getKiller() == null) return;
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

        double chance = Math.min(1, CraftBookPlugin.inst().getConfiguration().headDropsDropRate);
        if(CraftBookPlugin.inst().getConfiguration().headDropsCustomDropRate.containsKey(typeName))
            chance = Math.min(1, CraftBookPlugin.inst().getConfiguration().headDropsCustomDropRate.get(typeName));

        if(event.getEntity().getKiller() != null && event.getEntity().getKiller().getItemInHand() != null && event.getEntity().getKiller().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
            chance = Math.min(1, chance + CraftBookPlugin.inst().getConfiguration().headDropsLootingRateModifier * event.getEntity().getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));

        if(CraftBookPlugin.inst().getRandom().nextDouble() > chance)
            return;

        ItemStack toDrop = null;

        switch(event.getEntityType()) {

            case PLAYER:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsPlayers)
                    return;
                String playerName = ((Player) event.getEntity()).getName();
                toDrop = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                SkullMeta meta = (SkullMeta) toDrop.getItemMeta();
                meta.setOwner(playerName);
                meta.setDisplayName(ChatColor.RESET + playerName + "'s Head");
                toDrop.setItemMeta(meta);
                break;
            case ZOMBIE:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                toDrop = new ItemStack(Material.SKULL_ITEM, 1, (short)2);
                break;
            case CREEPER:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                toDrop = new ItemStack(Material.SKULL_ITEM, 1, (short)4);
                break;
            case SKELETON:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                if(((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER && !CraftBookPlugin.inst().getConfiguration().headDropsDropOverrideNatural)
                    return;
                toDrop = new ItemStack(Material.SKULL_ITEM, 1, (short) (((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER ? 1 : 0));
                break;
            default:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                MobSkullType type = MobSkullType.getFromEntityType(event.getEntityType());
                String mobName = null;
                if(type != null)
                    mobName = type.getPlayerName();
                if(CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.containsKey(typeName))
                    mobName = CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.get(typeName);
                if(mobName == null || mobName.isEmpty())
                    break;
                toDrop = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                ItemMeta metaD = toDrop.getItemMeta();
                if(metaD instanceof SkullMeta) {
                    SkullMeta itemMeta = (SkullMeta) metaD;
                    itemMeta.setDisplayName(ChatColor.RESET + typeName + " Head");
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(event.getClickedBlock().getType() == Material.SKULL) {

            Skull skull = (Skull)event.getClickedBlock().getState();
            if(skull == null || !skull.hasOwner())
                return;

            LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            if(CraftBookPlugin.inst().getConfiguration().headDropsShowNameClick && MobSkullType.getEntityType(skull.getOwner()) == null) {
                player.printRaw(ChatColor.YELLOW + player.translate("mech.headdrops.click-message") + " " + skull.getOwner());
            } else if (MobSkullType.getEntityType(skull.getOwner()) != null) {
                skull.setOwner(MobSkullType.getFromEntityType(MobSkullType.getEntityType(skull.getOwner())).getPlayerName());
                skull.update();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().headDropsEnabled) return;
        if(!CraftBookPlugin.inst().getConfiguration().headDropsMiningDrops) return;
        if(EventUtil.shouldIgnoreEvent(event))
            return;
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(event.getBlock().getType() == Material.SKULL) {

            Skull skull = (Skull)event.getBlock().getState();
            if(!skull.hasOwner())
                return;
            String playerName = ChatColor.stripColor(skull.getOwner());
            LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            EntityType type = MobSkullType.getEntityType(playerName);

            ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setOwner(playerName);

            if(type != null && !CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                return;
            if(type == null && !CraftBookPlugin.inst().getConfiguration().headDropsPlayers)
                return;

            if(!event.getPlayer().hasPermission("craftbook.mech.headdrops.break")) {
                player.printError("mech.headdrops.break-permission");
                return;
            }

            if(type != null)
                meta.setDisplayName(ChatColor.RESET + type.getName().replace("_", " ") + " Head");
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

    private enum MobSkullType {

        //Official
        BLAZE("MHF_Blaze", "Blaze_Head"),
        CAVE_SPIDER("MHF_CaveSpider"),
        CHICKEN("MHF_Chicken", "scraftbrothers1"),
        COW("MHF_Cow", "VerifiedBernard", "CarlosTheCow"),
        ENDERMAN("MHF_Enderman", "Violit"),
        GHAST("MHF_Ghast", "_QuBra_"),
        MAGMA_CUBE("MHF_LavaSlime"),
        MUSHROOM_COW("MHF_MushroomCow", "Mooshroom_Stew"),
        PIG("MHF_Pig", "XlexerX"),
        PIG_ZOMBIE("MHF_PigZombie", "ManBearPigZombie", "scraftbrothers5"),
        SHEEP("MHF_Sheep", "SGT_KICYORASS", "Eagle_Peak"),
        SLIME("MHF_Slime", "HappyHappyMan"),
        SPIDER("MHF_Spider", "Kelevra_V"),
        VILLAGER("MHF_Villager", "Villager", "Kuvase", "scraftbrothers9"),
        IRON_GOLEM("MHF_Golem", "zippie007"),
        SQUID("MHF_Squid", "squidette8"),
        OCELOT("MHF_Ocelot", "scraftbrothers3"),

        //Unofficial/Community
        BAT("coolwhip101", "bozzobrain"),
        ENDER_DRAGON("KingEndermen", "KingEnderman"),
        SILVERFISH("AlexVMiner"),
        SNOWMAN("scraftbrothers2", "Koebasti"),
        HORSE("gavertoso"),
        WOLF("Budwolf"),
        WITCH("scrafbrothers4");

        MobSkullType(String playerName, String ... oldNames) {

            this.playerName = playerName;
            this.oldNames = new HashSet<String>(Arrays.asList(oldNames));
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
                if(type.getPlayerName().equalsIgnoreCase(name) || type.isOldName(name) || name.equalsIgnoreCase(CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.get(EntityType.valueOf(type.name()).getName().toUpperCase())))
                    return EntityType.valueOf(type.name());

            return null;
        }
    }
}