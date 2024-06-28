/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.headdrops;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.enginehub.craftbook.CraftBook;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkinData {

    protected static final String HEAD_NAME = "cb-headdrops";
    private static final UUID DEFAULT_UUID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

    protected static PlayerProfile createProfile(String texture) {
        PlayerProfile profile = Bukkit.createProfile(DEFAULT_UUID, HEAD_NAME);
        profile.setProperty(new ProfileProperty("textures", texture));

        return profile;
    }

    public static void addDefaultSkinData(Map<EntityType, PlayerProfile> skinMap) {
        skinMap.put(EntityType.BAT, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzMjgxNTA3NSwKICAicHJvZmlsZUlkIiA6ICIzMzlkNTI1NjFjZDg0Yjc5YmJkOTU5ODc0ZDI1YmE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJib3p6b2JyYWluIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1YjkzNmJjMzEzNTljYmU3N2YyZjFmMDY0NmMxZjE2OGQ1YmVkZWNhYzNjYjQ2ZmYzMWNlYWFmZjQ5ZDE2YjgiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.BLAZE, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNTc1NTA1MSwKICAicHJvZmlsZUlkIiA6ICI0YzM4ZWQxMTU5NmE0ZmQ0YWIxZDI2ZjM4NmMxY2JhYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQmxhemUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDA2ZTM0MmY5MGVjNTM4YWFhMTU1MmIyMjRmMjY0YTA0MDg0MDkwMmUxMjZkOTFlY2U2MTM5YWE1YjNjN2NjMyIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.CAVE_SPIDER, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNTc5NjE4NSwKICAicHJvZmlsZUlkIiA6ICJjYWIyODc3MWYwY2Q0ZmU3YjEyOTAyYzY5ZWJhNzlhNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQ2F2ZVNwaWRlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83N2IwNzA2M2E2ODc0ZmEzZTIyNTQ4ZTAyMDYyYmQ3MzNjMjU4ODU5Mjk4MDk2MjQxODBhZWJiODUxNTU3ZjZhIgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.CHICKEN, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNTgyOTk3OCwKICAicHJvZmlsZUlkIiA6ICI5MmRlYWZhOTQzMDc0MmQ5YjAwMzg4NjAxNTk4ZDZjMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQ2hpY2tlbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MTZiOGU5ODM4OWM1NDFiYjM2NDUzODUwYmNiZDFmN2JjNWE1N2RhNjJkY2M1MDUwNjA0MDk3MzdlYzViNzJhIgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.COW, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNTg2NzI1NiwKICAicHJvZmlsZUlkIiA6ICJmMTU5YjI3NGMyMmU0MzQwYjdjMTUyYWJkZTE0NzcxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQ293IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QwZTRlNmZiZjVmM2RjZjk0NDIyYTFmMzE5NDQ4ZjE1MjM2OWQxNzlkYmZiY2RmMDBlNWJmZTg0OTVmYTk3NyIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.DONKEY, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNTkyOTIwMCwKICAicHJvZmlsZUlkIiA6ICI1NDY0ZGQ2NzVhNDY0MjM5ODYyZmIyN2I0OTQ0NDQyYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJEb25rZXkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE2ZWYwNzdkNTA2YjEwNzdiMjY3ZWJhZDUyYzRjZDQ3ZWZiYmQxN2I3NjBlMzZmMWUyYWViZDVkNjQzOGIxMSIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.ELDER_GUARDIAN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM3OTc0ODJhMTRiZmNiODc3MjU3Y2IyY2ZmMWI2ZTZhOGI4NDEzMzM2ZmZiNGMyOWE2MTM5Mjc4YjQzNmIifX19"
        ));
        skinMap.put(EntityType.ENDERMAN, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjE3MzAyNSwKICAicHJvZmlsZUlkIiA6ICI0MGZmYjM3MjEyZjY0Njc4YjNmMjIxNzZiZjU2ZGQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfRW5kZXJtYW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIwOWEzNzUyNTEwZTkxNGIwYmRjOTA5NmIzOTJiYjM1OWY3YThlOGE5NTY2YTAyZTdmNjZmYWZmOGQ2Zjg5ZSIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.ENDERMITE, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjIyOTUzNCwKICAicHJvZmlsZUlkIiA6ICIzZGY2YTA1MGI5M2U0ZDhiOGZhNGI1MjI4YTc5N2I4NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfRW5kZXJtaXRlIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3MzAxMjdlM2FjNzY3NzEyMjQyMmRmMDAyOGQ5ZTczNjhiZDE1NzczOGM4YzNjZGRlY2M1MDJlODk2YmUwMWMiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.EVOKER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQzMzMyMmUyY2NiZDljNTVlZjQxZDk2ZjM4ZGJjNjY2YzgwMzA0NWIyNDM5MWFjOTM5MWRjY2FkN2NkIn19fQ=="
        ));
        skinMap.put(EntityType.GHAST, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjMzMzc5MCwKICAicHJvZmlsZUlkIiA6ICIwNjMwODVhNjc5N2Y0Nzg1YmUxYTIxY2Q3NTgwZjc1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfR2hhc3QiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE0ZTQyZWIxNWEwODgxM2E2YTZmNjFmMTBhYTI4ODAxOWZhMGZhZTEwNmEyOTUzZGRiNDZmNzdlZTJkNzdmIgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.GUARDIAN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBiZjM0YTcxZTc3MTViNmJhNTJkNWRkMWJhZTVjYjg1Zjc3M2RjOWIwZDQ1N2I0YmZjNWY5ZGQzY2M3Yzk0In19fQ=="
        ));
        skinMap.put(EntityType.HORSE, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjQxODAzMiwKICAicHJvZmlsZUlkIiA6ICIxYjkwZWRjZjM5M2Q0ZTkzYTBkNmNmNzM3ZGM4MDk5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJnYXZlcnRvc28iLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBiZGMwNTA0ODhhNTA4MjhlY2Y3YTE0ZTVkMzM0ZWIwNjc5ZjFmNGE3ZjFjYjg5MDRiMDMyM2EyODE4NjQyMiIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.IRON_GOLEM, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjQ1MTk2MiwKICAicHJvZmlsZUlkIiA6ICI3NTdmOTBiMjIzNDQ0YjhkOGRhYzgyNDIzMmUyY2VjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfR29sZW0iLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM2Y2Q3MjAyYzM0ZTc4ZjMwNzMwOTAzNDlmN2Q5NzNiMjg4YWY1ZTViNzMzNGRkMjQ5MDEwYjNmMjcwNzhmOSIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.MAGMA_CUBE, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjQ4NTk3MSwKICAicHJvZmlsZUlkIiA6ICIwOTcyYmRkMTRiODY0OWZiOWVjY2EzNTNmODQ5MWE1MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTGF2YVNsaW1lIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5MGQ2MWU4Y2U5NTExYTBhMmI1ZWEyNzQyY2IxZWYzNjEzMTM4MGVkNDEyOWUxYjE2M2NlOGZmMDAwZGU4ZWEiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.MOOSHROOM, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjU0NDM5MCwKICAicHJvZmlsZUlkIiA6ICJhNDY4MTdkNjczYzU0ZjNmYjcxMmFmNmIzZmY0N2I5NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTXVzaHJvb21Db3ciLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTIzY2ZjNTU4MjQ1NGZjZjk5MDZmODQxZmRhMmNjNmFlODk2Y2Y0NTU4MjFjNGFkYTE5OThkZTcwODc3Y2M4NiIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.OCELOT, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjU3MTQ0NSwKICAicHJvZmlsZUlkIiA6ICIxYmVlOWRmNTRmNzE0MmEyYmY1MmQ5Nzk3MGQzZmVhMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfT2NlbG90IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzExOGI2Yjc5NzgzMzY4ZGZlMDA0Mjk4NTExMGRhMzY2ZjljNzg4YjQ1MDk3YTNlYTZkMGQ5YTc1M2U5ZjQyYzYiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.PARROT, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjYxMDU4NCwKICAicHJvZmlsZUlkIiA6ICIzZDg4YzQxMWM3ZTE0MGY5YjFmN2ZiZTRiN2FlZjRhMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUGFycm90IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2N2E5ODRjZGQ1MzkxYzY3ZWJiZjMzMzE2YWJmZWMxOGNhNWJlMDIzMzVkY2I4YTk1M2YyYWM1OTAyNDYwYWIiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.PIG, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjY1MDUwOSwKICAicHJvZmlsZUlkIiA6ICI4YjU3MDc4YmYxYmQ0NWRmODNjNGQ4OGQxNjc2OGZiZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUGlnIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E1NjJhMzdiODcxZjk2NGJmYzNlMTMxMWVhNjcyYWFhMDM5ODRhNWRjNDcyMTU0YTM0ZGMyNWFmMTU3ZTM4MmIiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.ZOMBIFIED_PIGLIN, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjY4Nzg1MiwKICAicHJvZmlsZUlkIiA6ICIxOGEyYmI1MDMzNGE0MDg0OTE4NDJjMzgwMjUxYTI0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUGlnWm9tYmllIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkxNmQxNjdjNTc0NGVkMTRlYmMwMmY0NDdmMzI2MTQwNTkzNjJiN2QyZWNiODA4ZmYwNjE2NWQyYzM0M2JlZjIiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.POLAR_BEAR, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1ZDYwYTRkNzBlYzEzNmE2NTg1MDdjZTgyZTM0NDNjZGFhMzk1OGQ3ZmNhM2Q5Mzc2NTE3YzdkYjRlNjk1ZCJ9fX0="
        ));
        skinMap.put(EntityType.RABBIT, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjc2MjkxNywKICAicHJvZmlsZUlkIiA6ICJmYmVjMTFkNDgwYTc0YzFjOWRlMzQxMzZhMTZmMWRlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUmFiYml0IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY1ZmExZWM5ODVmMWRlNDMwYWQyOGNkNTkyMzM3ZWU1ZWUyNjFhY2NkMjdiODIxNjJkMTc4ZDgzODFlY2FkYjIiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.SHEEP, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjc5MTk5OCwKICAicHJvZmlsZUlkIiA6ICJkZmFhZDU1MTRlN2U0NWExYTZmN2M2ZmM1ZWM4MjNhYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU2hlZXAiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhMzhjY2Y0MTdlOTljYTlkNDdlZWIxNWE4YTMwZWRiMTUwN2FhNTJiNjc4YzIyMGM3MTdjNDc0YWE2ZmUzZSIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.SHULKER, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjgzODczMiwKICAicHJvZmlsZUlkIiA6ICIxNjBmN2Q4YWM2YjA0ZmM4ODkyNTllOWQ2YzljNTdkNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU2h1bGtlciIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MDgwZGE5ZDJmODljM2M0MDgyMTdiMGMyMjEzMDk4NTRiM2QzNTEwZDIzOGViMDZkMzVmYzFhNTQxYmI0MGQ4IgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.SILVERFISH, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ=="
        ));
        skinMap.put(EntityType.SLIME, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjg5MDg0MSwKICAicHJvZmlsZUlkIiA6ICI4NzBhYmE5MzQwZTg0OGIzODljNTMyZWNlMDBkNjYzMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU2xpbWUiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODZjMjdiMDEzZjFiZjMzNDQ4NjllODFlNWM2MTAwMjdiYzQ1ZWM1Yjc5NTE0ZmRjOTZlMDFkZjFiN2UzYTM4NyIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.SNOW_GOLEM, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjk0MzIzMCwKICAicHJvZmlsZUlkIiA6ICIyMTdmOWU1ZTYwMWY0YTNkODc5YmUxMGQzMGUzZTU5YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU25vd0dvbGVtIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2YxOWMwYmY5Y2VjYWU5YzdkM2VlMDUxMjViMGE5MmU1YmE0OGU5MDY4MGU1OTk3MDUzY2I1YzA3ZDI4YTBkOWMiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.SPIDER, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNjk3Mzg5MywKICAicHJvZmlsZUlkIiA6ICI1YWQ1NWYzNDQxYjY0YmQyOWMzMjE4OTgzYzYzNTkzNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3BpZGVyIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y2MWE0OTU0MWE4MzZhYThmNGY3NmUwZDRjYjJmZjA0ODg4YzYyZjk0MTFlYTEwY2JhY2YxZjJhNTQ0MjQyNDAiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.STRAY, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzhkZGY3NmU1NTVkZDVjNGFhOGEwYTVmYzU4NDUyMGNkNjNkNDg5YzI1M2RlOTY5ZjdmMjJmODVhOWEyZDU2In19fQ=="
        ));
        skinMap.put(EntityType.SQUID, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzEyNTc2NiwKICAicHJvZmlsZUlkIiA6ICI3MmU2NDY4M2UzMTM0YzM2YTQwOGM2NmI2NGU5NGFmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3F1aWQiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU4OTEwMWQ1Y2M3NGFhNDU4MDIxYTA2MGY2Mjg5YTUxYTM1YTdkMzRkOGNhZGRmYzNjZGYzYjJjOWEwNzFhIgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.WITCH, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzE2MjQzOSwKICAicHJvZmlsZUlkIiA6ICJmZWY4NWM0OTJmZGY0N2Y4OTEzMjU1MjA0NjI0MzIyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfV2l0Y2giLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBhNDY3OTliNmNlNGI3ZTI5YThkZWY5ZjU0ZjMwY2M3MDI1ZTk2MzIxNjI1ZjJhYjQwYTlkNzBiODQzNmIyMSIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.WITHER, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzE5NDMwMCwKICAicHJvZmlsZUlkIiA6ICIzOWFmNjg0NDY4MDk0ZDJmOGJhNDdlOTJkMDg3YmUxOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfV2l0aGVyIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VhMTVkMDVkMGE4Njg5NjhjNjY4YjVkODZjNDA4MzA1M2ViZTlmZGVhOWNlNGFhMTMxNTAzNWQzYmE4ODQzZCIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.WOLF, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzIyOTg3MywKICAicHJvZmlsZUlkIiA6ICI4ZDJkMWQ2ZDgwMzQ0Yzg5YmQ4NjgwOWEzMWZkNTE5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfV29sZiIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82OTZhNTM3YTgxM2ExYzE3YWI0YzkwZDIxMmI4MGQwM2IxYmFmMWUzY2YxNzJmNWZmZTAyYWZkMDUzMmZjY2VmIgogICAgfQogIH0KfQ=="
        ));
        skinMap.put(EntityType.VEX, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzI3NjAwOSwKICAicHJvZmlsZUlkIiA6ICJmNWYyMDk5NzIxN2Y0NDI2OGFiOWM2ZGI2Y2NlMDIzZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfVmV4IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ2Y2I0OTRlN2U0ZGNiM2U3YjMyODZjZGE5MDk3YjM4Y2M5MGZjN2NmYWQ1ODM2ODE4M2IwYjg2YjBlNmI0N2YiCiAgICB9CiAgfQp9"
        ));
        skinMap.put(EntityType.VILLAGER, createProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNDIzNzMwMjc2MCwKICAicHJvZmlsZUlkIiA6ICJiZDQ4MjczOTc2N2M0NWRjYTFmOGMzM2M0MDUzMDk1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfVmlsbGFnZXIiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjRiZDgzMjgxM2FjMzhlNjg2NDg5MzhkN2EzMmY2YmEyOTgwMWFhZjMxNzQwNDM2N2YyMTRiNzhiNGQ0NzU0YyIKICAgIH0KICB9Cn0="
        ));
        skinMap.put(EntityType.HUSK, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY5Yjk3MzRkMGU3YmYwNjBmZWRjNmJmN2ZlYzY0ZTFmN2FkNmZjODBiMGZkODQ0MWFkMGM3NTA4Yzg1MGQ3MyJ9fX0="
        ));
        skinMap.put(EntityType.ZOMBIE_VILLAGER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQ5YTQ2Mjc1ZGVjMGMyNDdkZjk4NmRmYjRiMzUxZDI4OWYwMjQyYjVmY2Q2MjBkYWFlMTEzNzI1NzIwYzdjOSJ9fX0="
        ));
        skinMap.put(EntityType.SKELETON_HORSE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlZmZjZTM1MTMyYzg2ZmY3MmJjYWU3N2RmYmIxZDIyNTg3ZTk0ZGYzY2JjMjU3MGVkMTdjZjg5NzNhIn19fQ=="
        ));
        skinMap.put(EntityType.ZOMBIE_HORSE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIyOTUwZjJkM2VmZGRiMThkZTg2ZjhmNTVhYzUxOGRjZTczZjEyYTZlMGY4NjM2ZDU1MWQ4ZWI0ODBjZWVjIn19fQ=="
        ));
        skinMap.put(EntityType.MULE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA0ODZhNzQyZTdkZGEwYmFlNjFjZTJmNTVmYTEzNTI3ZjFjM2IzMzRjNTdjMDM0YmI0Y2YxMzJmYjVmNWYifX19"
        ));
        skinMap.put(EntityType.VINDICATOR, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGY2ZmI4OWQxYzYzMWJkN2U3OWZlMTg1YmExYTY3MDU0MjVmNWMzMWE1ZmY2MjY1MjFlMzk1ZDRhNmY3ZTIifX19"
        ));
        skinMap.put(EntityType.ILLUSIONER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEyNTEyZTdkMDE2YTIzNDNhN2JmZjFhNGNkMTUzNTdhYjg1MTU3OWYxMzg5YmQ0ZTNhMjRjYmViODhiIn19fQ=="
        ));
        skinMap.put(EntityType.GIANT, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZmYzg1NGJiODRjZjRiNzY5NzI5Nzk3M2UwMmI3OWJjMTA2OTg0NjBiNTFhNjM5YzYwZTVlNDE3NzM0ZTExIn19fQ=="
        ));
        skinMap.put(EntityType.LLAMA, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJiMWVjZmY3N2ZmZTNiNTAzYzMwYTU0OGViMjNhMWEwOGZhMjZmZDY3Y2RmZjM4OTg1NWQ3NDkyMTM2OCJ9fX0="
        ));
        skinMap.put(EntityType.TURTLE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ=="
        ));
        skinMap.put(EntityType.PHANTOM, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U5NTE1M2VjMjMyODRiMjgzZjAwZDE5ZDI5NzU2ZjI0NDMxM2EwNjFiNzBhYzAzYjk3ZDIzNmVlNTdiZDk4MiJ9fX0="
        ));
        skinMap.put(EntityType.COD, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MmQ3ZGQ2YWFkZjM1Zjg2ZGEyN2ZiNjNkYTRlZGRhMjExZGY5NmQyODI5ZjY5MTQ2MmE0ZmIxY2FiMCJ9fX0="
        ));
        skinMap.put(EntityType.SALMON, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlYjIxYTI1ZTQ2ODA2Y2U4NTM3ZmJkNjY2ODI4MWNmMTc2Y2VhZmU5NWFmOTBlOTRhNWZkODQ5MjQ4NzgifX19"
        ));
        skinMap.put(EntityType.PUFFERFISH, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxNTI4NzZiYzNhOTZkZDJhMjI5OTI0NWVkYjNiZWVmNjQ3YzhhNTZhYzg4NTNhNjg3YzNlN2I1ZDhiYiJ9fX0="
        ));
        skinMap.put(EntityType.TROPICAL_FISH, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkZDVlNmFkZGI1NmFjYmM2OTRlYTRiYTU5MjNiMWIyNTY4ODE3OGZlZmZhNzIyOTAyOTllMjUwNWM5NzI4MSJ9fX0="
        ));
        skinMap.put(EntityType.DROWNED, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0ZGY3OWM0OTEwNGIxOThjZGFkNmQ5OWZkMGQwYmNmMTUzMWM5MmQ0YWI2MjY5ZTQwYjdkM2NiYmI4ZTk4YyJ9fX0="
        ));
        skinMap.put(EntityType.DOLPHIN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ=="
        ));
        skinMap.put(EntityType.CAT, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZkMTBjOGU3NWY2NzM5OGM0NzU4N2QyNWZjMTQ2ZjMxMWMwNTNjYzVkMGFlYWI4NzkwYmNlMzZlZTg4ZjVmOCJ9fX0="
        ));
        skinMap.put(EntityType.PANDA, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhMDk2ZWVhNTA2MzAxYmVhNmQ0YjE3ZWUxNjA1NjI1YTZmNTA4MmM3MWY3NGE2MzljYzk0MDQzOWY0NzE2NiJ9fX0="
        ));
        skinMap.put(EntityType.PILLAGER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFlZTZiYjM3Y2JmYzkyYjBkODZkYjVhZGE0NzkwYzY0ZmY0NDY4ZDY4Yjg0OTQyZmRlMDQ0MDVlOGVmNTMzMyJ9fX0="
        ));
        skinMap.put(EntityType.RAVAGER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2QyMGJmNTJlYzM5MGEwNzk5Mjk5MTg0ZmM2NzhiZjg0Y2Y3MzJiYjFiZDc4ZmQxYzRiNDQxODU4ZjAyMzVhOCJ9fX0="
        ));
        skinMap.put(EntityType.TRADER_LLAMA, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU0ZDhhMGJjMTVmMjM5OTIxZWZkOGJlMzQ4MGJhNzdhOThlZTdkOWNlMDA3MjhjMGQ3MzNmMGEyZDYxNGQxNiJ9fX0="
        ));
        skinMap.put(EntityType.WANDERING_TRADER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzc5YTgyMjkwZDdhYmUxZWZhYWJiYzcwNzEwZmYyZWMwMmRkMzRhZGUzODZiYzAwYzkzMGM0NjFjZjkzMiJ9fX0="
        ));
        skinMap.put(EntityType.FOX, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRhMDM0NzQzNjQzNGViMTNkNTM3YjllYjZiNDViNmVmNGM1YTc4Zjg2ZTkxODYzZWY2MWQyYjhhNTNiODIifX19"
        ));
        skinMap.put(EntityType.BEE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQxNjU4ZmQ0OTRmOGUyMjkwZGI0MWRjNGQxMWQ0NjdjMjU5NjFlZGNhNjMzMTdlOGY5OTcxZWIyOGE0N2NjNSJ9fX0="
        ));
        skinMap.put(EntityType.HOGLIN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiOWJjMGYwMWRiZDc2MmEwOGQ5ZTc3YzA4MDY5ZWQ3Yzk1MzY0YWEzMGNhMTA3MjIwODU2MWI3MzBlOGQ3NSJ9fX0="
        ));
        skinMap.put(EntityType.STRIDER, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMThhOWFkZjc4MGVjN2RkNDYyNWM5YzA3NzkwNTJlNmExNWE0NTE4NjY2MjM1MTFlNGM4MmU5NjU1NzE0YjNjMSJ9fX0="
        ));
        skinMap.put(EntityType.ZOGLIN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE5YjdiNWU5ZmZkNGUyMmI4OTBhYjc3OGI0Nzk1YjY2MmZhZmYyYjQ5NzhiZjgxNTU3NGU0OGIwZTUyYjMwMSJ9fX0="
        ));
        skinMap.put(EntityType.PIGLIN_BRUTE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzMDBlOTAyNzM0OWM0OTA3NDk3NDM4YmFjMjllM2E0Yzg3YTg0OGM1MGIzNGMyMTI0MjcyN2I1N2Y0ZTFjZiJ9fX0="
        ));
        skinMap.put(EntityType.AXOLOTL, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTIxNDllYjhhNDg5ZDhiMDM1OGQ5ODk1NjBjZDI3MDRiMjU2NGFjYjkxY2JmYjZkMDE0NmYzNWNjMDRhM2ZmIn19fQ=="
        ));
        skinMap.put(EntityType.GLOW_SQUID, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDVjOTk5ZGQxMmRkMWM4NjZmZGQwZWU5NGEzOTczNTMzNDI4Y2Q3MmQ5Mjk2YzYyNzI0ZjQyOTM2NWRhOGVlYiJ9fX0="
        ));
        skinMap.put(EntityType.GOAT, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU3YTBkNTM4ZmEwOGE3YWZmZTMxMjkwMzQ2ODg2MTcyMGY5ZmEzNGU4NmQ0NGI4OWRjZWM1NjM5MjY1ZjAzIn19fQ=="
        ));
        skinMap.put(EntityType.ALLAY, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTUwMjk0YTE3NDczMTBmMTA0MTI0YzYzNzNjYzYzOWI3MTJiYWE1N2I3ZDkyNjI5N2I2NDUxODhiN2JiOWFiOSJ9fX0="
        ));
        skinMap.put(EntityType.FROG, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjNjZTZmOTk5OGVkMmRhNzU3ZDFlNjM3MmYwNGVmYTIwZTU3ZGZjMTdjM2EwNjQ3ODY1N2JiZGY1MWMyZjJhMiJ9fX0="
        ));
        skinMap.put(EntityType.TADPOLE, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIzZWJmMjZiN2E0NDFlMTBhODZmYjVjMmE1ZjNiNTE5MjU4YTVjNWRkZGQ2YTFhNzU1NDlmNTE3MzMyODE1YiJ9fX0="
        ));
        skinMap.put(EntityType.WARDEN, createProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNmYWJkZGRlZDQ0ZDBmYTE2YTQ4NmRkNTQwODY5Nzc5ZWY0MmY5YzNkODgyMDA1MDllYTAwMmM5ZWYxZWQ0MCJ9fX0="
        ));

        // TODO minecraft:camel
    }

    private static final Set<EntityType> IGNORED_ENTITIES = Set.of(
        EntityType.PLAYER, EntityType.ZOMBIE, EntityType.CREEPER,
        EntityType.SKELETON, EntityType.WITHER_SKELETON,
        EntityType.ARMOR_STAND, EntityType.ENDER_DRAGON, EntityType.PIGLIN
    );

    @SuppressWarnings("unused")
    private static void printMissingSkins(Map<EntityType, PlayerProfile> skinMap) {
        String missingText = Stream.of(EntityType.values())
            .filter(type -> type.getName() != null && type.isAlive() && !IGNORED_ENTITIES.contains(type))
            .filter(type -> !skinMap.containsKey(type))
            .map(EntityType::getKey)
            .map(NamespacedKey::toString)
            .collect(Collectors.joining(", "));

        if (!missingText.isEmpty()) {
            CraftBook.LOGGER.debug(missingText);
        }
    }
}
