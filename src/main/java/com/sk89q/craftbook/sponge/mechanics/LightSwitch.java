package com.sk89q.craftbook.sponge.mechanics;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(moduleName = "LightSwitch", onEnable="onInitialize", onDisable="onDisable")
public class LightSwitch extends SpongeSignMechanic implements DocumentationProvider {

    private SpongePermissionNode createPermission = new SpongePermissionNode("craftbook.mech.lightswitch", "Allows for creation of the light switch.",
            PermissionDescription.ROLE_STAFF);
    private SpongePermissionNode usePermission = new SpongePermissionNode("craftbook.mech.lightswitch.use", "Allows for creation of the light switch.",
            PermissionDescription.ROLE_USER);

    private ConfigValue<Integer> maxRange = new ConfigValue<>("max-range", "The range that the mechanic searches.", 10);
    private ConfigValue<Integer> maxLights = new ConfigValue<>("max-lights", "The maximum amount of lights the mechanic will toggle.", 20);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        createPermission.register();
        usePermission.register();

        maxRange.load(config);
        maxLights.load(config);
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            if (SignUtil.isSign(location)) {
                Sign sign = (Sign) location.getTileEntity().get();
                if (isMechanicSign(sign)) {
                    if (!player.hasPermission(usePermission.getNode())) {
                        player.sendMessage(USE_PERMISSIONS);
                        return;
                    }

                    Location<World> above = location.getRelative(Direction.UP);
                    if (above.getBlockType() == BlockTypes.TORCH || above.getBlockType() == BlockTypes.REDSTONE_TORCH) {
                        boolean state = above.getBlockType() == BlockTypes.REDSTONE_TORCH;

                        int maxRangeValue = maxRange.getValue();
                        if (!SignUtil.getTextRaw(sign, 2).isEmpty()) {
                            try {
                                maxRangeValue = Integer.valueOf(SignUtil.getTextRaw(sign, 2));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        int maxLightsValue = maxLights.getValue();
                        if (!SignUtil.getTextRaw(sign, 3).isEmpty()) {
                            try {
                                maxLightsValue = Integer.valueOf(SignUtil.getTextRaw(sign, 3));
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        toggleSwitches(location, state, maxRangeValue, maxLightsValue, player);
                        player.sendMessage(Text.of(TextColors.YELLOW, "Toggled lights"));
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "Missing torch above sign"));
                    }
                }
            }
        });
    }

    public void toggleSwitches(Location<World> location, boolean state, int maxRange, int maxLights, Player player) {
        int toggledLights = 0;
        for (int x = -maxRange; x < maxRange; x++) {
            for (int y = -maxRange; y < maxRange; y++) {
                for (int z = -maxRange; z < maxRange; z++) {
                    if (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2) > Math.pow(maxRange, 2))
                        continue;

                    Location<World> offset = location.add(x, y, z);
                    if (offset.getBlockType() == (state ? BlockTypes.REDSTONE_TORCH : BlockTypes.TORCH)) {
                        toggledLights ++;

                        Cause.Builder causeBuilder = Cause.source(CraftBookPlugin.inst());
                        if (player != null)
                            causeBuilder.notifier(player);

                        offset.setBlock((state ? BlockTypes.TORCH : BlockTypes.REDSTONE_TORCH).getDefaultState(), causeBuilder.build());

                        if (toggledLights > maxLights)
                            return;
                    }
                }
            }
        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{
                "[I]",
                "[|]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermission;
    }

    @Override
    public String getPath() {
        return "mechanics/light_switch";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                maxRange,
                maxLights
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermission,
                usePermission
        };
    }
}
