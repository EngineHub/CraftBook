package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Material;


/**
 * @author Me4502
 */
public class XPShooter extends ItemShooter {

    public XPShooter(Material[] recipe) {

        super(Material.EXP_BOTTLE, recipe);
    }

    public XPShooter() {

        super(Material.EXP_BOTTLE, new Material[] {
                Material.AIR,            Material.REDSTONE,   Material.AIR,
                Material.REDSTONE,   Material.GLASS_BOTTLE,    Material.REDSTONE,
                Material.AIR,            Material.REDSTONE,   Material.AIR
        });
    }
}