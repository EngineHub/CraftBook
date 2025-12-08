package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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

    parseLineItems(sign.getLine(2), includeFilters);
    parseLineItems(sign.getLine(3), excludeFilters);

    return new PipeSign(includeFilters, excludeFilters);
  }

  private static void parseLineItems(String line, List<ItemStack> output) {
    for(String token : RegexUtil.COMMA_PATTERN.split(line)) {
      // TODO: This can throw on malformed input - wrap it in a catch-block and log, including the location
      ItemStack item = ItemSyntax.getItem(token.trim());

      if (item != null)
        output.add(item);
    }
  }
}
