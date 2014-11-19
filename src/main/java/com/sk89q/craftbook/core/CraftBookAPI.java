package com.sk89q.craftbook.core;

public abstract class CraftBookAPI {

	public static CraftBookAPI instance;

	public <T extends CraftBookAPI> T inst() {

		return (T) instance;
	}
}