package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Material;

/**
 * @author Me4502
 */
public class SnowShooter extends ItemShooter {

    public SnowShooter(Material[] recipe) {

        super(Material.SNOW_BALL, recipe);
    }

    public SnowShooter() {

        super(Material.SNOW_BALL, new Material[] {
                Material.AIR,            Material.SNOW_BLOCK,     Material.AIR,
                Material.SNOW_BLOCK,     Material.POTION,          Material.SNOW_BLOCK,
                Material.AIR,            Material.SNOW_BLOCK,     Material.AIR
        });
    }
}