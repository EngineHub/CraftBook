package com.sk89q.craftbook.mech.cauldron;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Silthus
 */
public class CauldronItemStack implements Comparable<CauldronItemStack> {

	public static Collection<CauldronItemStack> convert(Collection<Item> stacks) {
		Map<String, Integer> items = new HashMap<String, Integer>();
		for (Item item : stacks) {
			ItemStack stack = item.getItemStack();
			String name = stack.getType() + ":" + stack.getDurability();
			if (items.containsKey(name)) {
				items.put(name, items.get(name) + stack.getAmount());
			} else {
				items.put(name, stack.getAmount());
			}
		}
		Set<CauldronItemStack> stackSet = new LinkedHashSet<CauldronItemStack>();
		// merge the amounts and stacks
		for (String stack : items.keySet()) {
			String[] split = stack.split(":");
			stackSet.add(new CauldronItemStack(Material.getMaterial(split[0]), Short.parseShort(split[1]), items.get(stack)));
		}
		return stackSet;
	}

	private Material material;
	private short data;
	private int amount;

	public CauldronItemStack(Material material, short data, int amount) {
		this.material = material;
		this.data = data;
		this.amount = amount;
	}

	public CauldronItemStack(Material material, int amount) {
		this.material = material;
		this.amount = amount;
	}

	public CauldronItemStack(Material material, short data) {
		this(material, data, 0);
	}

	public CauldronItemStack(Material material) {
		this(material, 0);
	}

	public Material getMaterial() {
		return material;
	}

	public short getData() {
		return data;
	}

	public int getAmount() {
		return amount;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public void setData(short data) {
		this.data = data;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public ItemStack getItemStack() {
		return new ItemStack(material, amount, data);
	}

	public CauldronItemStack add(CauldronItemStack stack) {
		if (stack.equals(this)) {
			this.amount += stack.getAmount();
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CauldronItemStack) {
			CauldronItemStack stack = (CauldronItemStack) obj;
			return isSameType(stack) && stack.getAmount() == getAmount();
		}
		return false;
	}

	public boolean isSameType(CauldronItemStack stack) {
		if (data == -1 || stack.getData() == -1) {
			return stack.getMaterial() == getMaterial();
		}
		return stack.getMaterial() == getMaterial() &&
				stack.getData() == getData();
	}

	@Override
	public int compareTo(CauldronItemStack stack) {
		if (getAmount() > stack.getAmount()) return 1;
		if (getAmount() == stack.getAmount()) return 0;
		return -1;
	}
}
