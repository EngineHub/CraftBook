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

package com.sk89q.craftbook.mechanics;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

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
public class TreeLopperTest {

    private TreeLopper lopper;

    @Test
    public void testOnBlockBreak() {
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