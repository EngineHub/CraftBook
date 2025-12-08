package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum TubeColor {
  NONE(null, null),
  TRANSPARENT(Material.GLASS, Material.GLASS_PANE),
  WHITE(Material.WHITE_STAINED_GLASS, Material.WHITE_STAINED_GLASS_PANE),
  ORANGE(Material.ORANGE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS_PANE),
  MAGENTA(Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS_PANE),
  LIGHT_BLUE(Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS_PANE),
  YELLOW(Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE),
  LIME(Material.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS_PANE),
  PINK(Material.PINK_STAINED_GLASS, Material.PINK_STAINED_GLASS_PANE),
  GRAY(Material.GRAY_STAINED_GLASS, Material.GRAY_STAINED_GLASS_PANE),
  LIGHT_GRAY(Material.LIGHT_GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS_PANE),
  CYAN(Material.CYAN_STAINED_GLASS, Material.CYAN_STAINED_GLASS_PANE),
  PURPLE(Material.PURPLE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS_PANE),
  BLUE(Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS_PANE),
  BROWN(Material.BROWN_STAINED_GLASS, Material.BROWN_STAINED_GLASS_PANE),
  GREEN(Material.GREEN_STAINED_GLASS, Material.GREEN_STAINED_GLASS_PANE),
  RED(Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE),
  BLACK(Material.BLACK_STAINED_GLASS, Material.BLACK_STAINED_GLASS_PANE),
  TINTED(Material.TINTED_GLASS, null),
  ;

  public record TypeAwareTubeColor(TubeColor color, boolean isPane) {}

  private static final TubeColor[] VALUES = values();
  private static final TypeAwareTubeColor AWARE_NONE = new TypeAwareTubeColor(NONE, false);

  private static final TypeAwareTubeColor[] tubeColorByMaterialOrdinal;
  private static final int tubeColorByMaterialOrdinalOffset;

  static {
    var materials = Material.values();

    int lowestIndex = -1;
    int highestIndex = -1;

    List<TypeAwareTubeColor> mappedColors = new ArrayList<>();

    for (var index = 0; index < materials.length; ++index) {
      var color = awareColorFromMaterialOrNull(materials[index]);

      if (lowestIndex < 0) {
        if (color != null) {
          lowestIndex = index;
          highestIndex = index;
          mappedColors.add(color);
          continue;
        }
        continue;
      }

      if (color != null)
        highestIndex = index;
      else
        color = AWARE_NONE;

      mappedColors.add(color);
    }

    if (lowestIndex < 0)
      throw new IllegalStateException("Did not encounter a single tube-color!");

    tubeColorByMaterialOrdinal = new TypeAwareTubeColor[highestIndex - lowestIndex + 1];
    tubeColorByMaterialOrdinalOffset = -lowestIndex;

    for (var i = 0; i < tubeColorByMaterialOrdinal.length; ++i)
      tubeColorByMaterialOrdinal[i] = mappedColors.get(i);
  }

  private final @Nullable Material blockType;
  private final @Nullable Material paneType;

  TubeColor(@Nullable Material blockType, @Nullable Material paneType) {
    this.blockType = blockType;
    this.paneType = paneType;
  }

  public static TypeAwareTubeColor fromMaterial(Material material) {
    var index = material.ordinal() + tubeColorByMaterialOrdinalOffset;

    if (index < 0 || index >= tubeColorByMaterialOrdinal.length)
      return AWARE_NONE;

    return tubeColorByMaterialOrdinal[index];
  }

  private static @Nullable TypeAwareTubeColor awareColorFromMaterialOrNull(Material material) {
    for (var tubeColor : VALUES) {
      if (tubeColor.paneType == material)
        return new TypeAwareTubeColor(tubeColor, true);

      if (tubeColor.blockType == material)
        return new TypeAwareTubeColor(tubeColor, false);
    }

    return null;
  }
}
