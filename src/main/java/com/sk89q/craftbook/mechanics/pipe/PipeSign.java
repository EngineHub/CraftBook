package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class PipeSign {

  private static final List<ItemStack> EMPTY_LIST = new ArrayList<>();

  public static final PipeSign NO_SIGN = new PipeSign(EMPTY_LIST, EMPTY_LIST);

  public final List<ItemStack> includeFilters;
  public final List<ItemStack> excludeFilters;

  private PipeSign(List<ItemStack> includeFilters, List<ItemStack> excludeFilters) {
    this.includeFilters = Collections.unmodifiableList(includeFilters);
    this.excludeFilters = Collections.unmodifiableList(excludeFilters);
  }

  public static PipeSign fromSign(Sign sign) {
    List<ItemStack> includeFilters = new ArrayList<>();
    List<ItemStack> excludeFilters = new ArrayList<>();

    parseLineItems(sign, 2, includeFilters);
    parseLineItems(sign, 3, excludeFilters);

    return new PipeSign(includeFilters, excludeFilters);
  }

  private static void parseLineItems(Sign sign, int lineId, List<ItemStack> output) {
    for(String token : RegexUtil.COMMA_PATTERN.split(sign.getLine(lineId))) {
      token = token.trim();

      if (token.isEmpty())
        continue;

      ItemStack item;

      try {
        item = ItemSyntax.getItem(token);
      } catch (Throwable e) {
        String position = sign.getX() + "," + sign.getY() + "," + sign.getZ() + "@" + sign.getWorld().getName();
        CraftBookPlugin.logger().log(Level.WARNING, "Could not parse \"" + token + "\" as a valid item in line " + (lineId + 1) + " on sign at " + position, e);
        continue;
      }

      if (item != null)
        output.add(item);
    }
  }
}
