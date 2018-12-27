package com.sk89q.craftbook.mechanics;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.sk89q.craftbook.BaseTestCase;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({TreeLopper.class,BlockBreakEvent.class})
public class TreeLopperTest extends BaseTestCase {

    private TreeLopper lopper;

    @Test
    public void testOnBlockBreak() {

        setup();

        if(lopper == null)
            lopper = new TreeLopper();

        World world = mock(World.class);

        ItemStack axe = mock(ItemStack.class);
        when(axe.getType()).thenReturn(Material.DIAMOND_AXE);
        when(axe.getAmount()).thenReturn(1);
        when(axe.hasItemMeta()).thenReturn(false);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInMainHand()).thenReturn(axe);

        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getItemInHand()).thenReturn(axe);
        when(player.getInventory()).thenReturn(inventory);

        final Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.OAK_LOG);
        when(block.getLocation()).thenReturn(new Location(world, 64,64,64));

        lopper.enabledBlocks = new ArrayList<>();
        lopper.enabledBlocks.add(BlockTypes.OAK_LOG.getDefaultState().toBaseBlock());

        lopper.enabledItems = new ArrayList<>();
        lopper.enabledItems.add(ItemTypes.DIAMOND_AXE);

        getConfig().showPermissionMessages = true;

        when(CraftBookPlugin.inst().hasPermission(Matchers.any(), Matchers.anyString())).thenReturn(false);

        final BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getBlock()).thenReturn(block);

        lopper.onBlockBreak(event);

        Mockito.verify(player, Mockito.times(1)).sendMessage(ChatColor.RED + "mech.use-permission");

        when(CraftBookPlugin.inst().hasPermission(Matchers.any(), Matchers.anyString())).thenReturn(true);

        when(block.getRelative(Matchers.any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            BlockFace face = (BlockFace) args[0];

            block.getLocation().setX(block.getLocation().getX() + face.getModX());
            block.getLocation().setY(block.getLocation().getY() + face.getModY());
            block.getLocation().setZ(block.getLocation().getZ() + face.getModZ());
            return block;
        });

        lopper.onBlockBreak(event);

        Mockito.verify(block).breakNaturally(player.getItemInHand());
    }
}