package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PipeSign {

  private static final Set<ItemStack> EMPTY_SET = new HashSet<>();

  public static final PipeSign NO_SIGN = new PipeSign(EMPTY_SET, EMPTY_SET);

  public final Set<ItemStack> filters;
  public final Set<ItemStack> exceptions;

  private PipeSign(Set<ItemStack> filters, Set<ItemStack> exceptions) {
    this.filters = Collections.unmodifiableSet(filters);
    this.exceptions = Collections.unmodifiableSet(exceptions);
  }

  public static PipeSign fromSign(Sign sign) {
    HashSet<ItemStack> filters = new HashSet<>();
    HashSet<ItemStack> exceptions = new HashSet<>();

    parseLineItems(sign.getLine(2), filters);
    parseLineItems(sign.getLine(3), exceptions);

    return new PipeSign(filters, exceptions);
  }

  private static void parseLineItems(String line, Set<ItemStack> output) {
    for(String token : RegexUtil.COMMA_PATTERN.split(line)) {
      // TODO: This can throw on malformed input - wrap it in a catch-block and log, including the location
      ItemStack item = ItemSyntax.getItem(token.trim());

      if (item != null)
        output.add(item);
    }
  }
}
