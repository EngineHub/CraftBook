package com.sk89q.craftbook.bukkit;

import org.bukkit.Material;

public enum IDConversion {

    AIR(0, Material.AIR),
    STONE(1, Material.STONE),
    GRASS(2, Material.GRASS),
    DIRT(3, Material.DIRT),
    COBBLESTONE(4, Material.COBBLESTONE),
    PLANKS(5, Material.WOOD),
    SAPLING(6, Material.SAPLING),
    BEDROCK(7, Material.BEDROCK),
    WATER(8, Material.WATER),
    WATER_STILL(9, Material.STATIONARY_WATER),
    LAVA(10, Material.LAVA),
    LAVA_STILL(11, Material.STATIONARY_LAVA),
    SAND(12, Material.SAND),
    GRAVEL(13, Material.GRAVEL);

    private IDConversion(int id, Material material) {
        this.id = id;
        this.material = material;
    }

    private int id;
    private Material material;

    public Material getMaterial(int i) {

        for(IDConversion idc : values()) {
            if(idc.id == i) return idc.material;
        }

        return Material.STONE;
    }
}