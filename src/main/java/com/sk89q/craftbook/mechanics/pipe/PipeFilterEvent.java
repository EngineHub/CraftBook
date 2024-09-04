package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PipeFilterEvent extends PipeEvent {

  private static final HandlerList handlers = new HandlerList();

  private boolean beenSet = false;

  public PipeFilterEvent(Block theBlock, List<ItemStack> items) {
    super(theBlock, items);
  }

  @Override
  public void setItems(List<ItemStack> items) {
    super.setItems(items);
    this.beenSet = true;
  }

  @Override
  public List<ItemStack> getItems() {
    return Collections.unmodifiableList(super.getItems());
  }

  @Override
  public void addItems(List<ItemStack> items) {
    throw new UnsupportedOperationException();
  }

  public boolean hasBeenSet() {
    return this.beenSet;
  }

  @Override
  @Nonnull
  public HandlerList getHandlers() {
    return handlers;
  }

  @Nonnull
  public static HandlerList getHandlerList() {
    return handlers;
  }
}
