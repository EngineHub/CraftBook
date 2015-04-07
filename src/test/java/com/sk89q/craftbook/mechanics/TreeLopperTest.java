package com.sk89q.craftbook.mechanics;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sk89q.craftbook.BaseTestCase;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.worldedit.blocks.ItemID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TreeLopper.class,BlockBreakEvent.class})
public class TreeLopperTest extends BaseTestCase {

    TreeLopper lopper;

    @Test
    public void testOnBlockBreak() {

        setup();

        if(lopper == null)
            lopper = new TreeLopper();

        World world = mock(World.class);

        ItemStack axe = mock(ItemStack.class);
        when(axe.getType()).thenReturn(Material.DIAMOND_AXE);
        when(axe.getTypeId()).thenReturn(ItemID.DIAMOND_AXE);
        when(axe.getAmount()).thenReturn(1);
        when(axe.hasItemMeta()).thenReturn(false);

        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getItemInHand()).thenReturn(axe);

        final Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.LOG);
        when(block.getData()).thenReturn((byte) 0);
        when(block.getLocation()).thenReturn(new Location(world, 64,64,64));

        lopper.enabledBlocks = new ArrayList<ItemInfo>();
        lopper.enabledBlocks.add(new ItemInfo(Material.LOG, 0));

        lopper.enabledItems = new ArrayList<ItemInfo>();
        lopper.enabledItems.add(new ItemInfo(Material.DIAMOND_AXE, -1));

        getConfig().showPermissionMessages = true;

        when(CraftBookPlugin.inst().hasPermission(Matchers.<CommandSender>any(), Matchers.anyString())).thenReturn(false);

        final BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getBlock()).thenReturn(block);

        lopper.onBlockBreak(event);

        Mockito.verify(player, Mockito.times(1)).sendMessage(ChatColor.RED + "mech.use-permission");

        when(CraftBookPlugin.inst().hasPermission(Matchers.<CommandSender>any(), Matchers.anyString())).thenReturn(true);

        when(block.getRelative(Matchers.<BlockFace>any())).thenAnswer(new Answer<Block>() {
            @Override
            public Block answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BlockFace face = (BlockFace) args[0];

                block.getLocation().setX(block.getLocation().getX() + face.getModX());
                block.getLocation().setY(block.getLocation().getY() + face.getModY());
                block.getLocation().setZ(block.getLocation().getZ() + face.getModZ());
                return block;
            }
        });

        lopper.onBlockBreak(event);

        Mockito.verify(block).breakNaturally(player.getItemInHand());
    }
}