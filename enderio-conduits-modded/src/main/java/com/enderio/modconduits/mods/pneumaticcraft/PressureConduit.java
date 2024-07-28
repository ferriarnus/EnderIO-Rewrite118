package com.enderio.modconduits.mods.pneumaticcraft;

import com.enderio.base.api.io.IOMode;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.ticker.ConduitTicker;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PressureConduit(ResourceLocation texture, Component description, PressureTier tier) implements Conduit<PressureConduit> {

    public static final MapCodec<PressureConduit> CODEC = RecordCodecBuilder.mapCodec(
        builder -> builder
            .group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(o -> o.texture),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(o -> o.description),
                Codec.INT.fieldOf("tier").forGetter(pressureConduit -> {
                    if (pressureConduit.tier == PressureTier.TIER_ONE) {
                        return 1;
                    }
                    if (pressureConduit.tier == PressureTier.TIER_ONE_HALF) {
                        return 2;
                    }
                    if (pressureConduit.tier == PressureTier.TIER_TWO) {
                        return 3;
                    }
                    return 0;
                })
            ).apply(builder, PressureConduit::of)
    );

    private static PressureConduit of(ResourceLocation resourceLocation, Component component, int i) {
        PressureTier tier = PressureTier.TIER_ONE;
        if (i == 0) {
            throw new IllegalArgumentException("Unknown pressure tier for conduit: " + resourceLocation);
        }
        if (i == 1) {
            tier = PressureTier.TIER_ONE;
        }
        if (i == 2) {
            tier = PressureTier.TIER_ONE_HALF;
        }
        if (i == 3) {
            tier = PressureTier.TIER_TWO;
        }
        return new PressureConduit(resourceLocation, component, tier);
    }

    private static final ConduitMenuData MENU_DATA = new ConduitMenuData.Simple(false, false, false, true, true, true);

    @Override
    public ConduitType<PressureConduit> type() {
        return PneumaticModule.Types.PRESSURE.get();
    }

    @Override
    public ConduitTicker<PressureConduit> getTicker() {
        return PressureTicker.INSTANCE;
    }

    @Override
    public ConduitMenuData getMenuData() {
        return MENU_DATA;
    }

    @Override
    public <K> @Nullable K proxyCapability(BlockCapability<K, Direction> capability, ConduitNode node, Level level, BlockPos pos, @Nullable Direction direction,
        ConduitNode.@Nullable IOState state) {
        if (capability == PNCCapabilities.AIR_HANDLER_MACHINE && (state == null || state.isExtract())) {
            //noinspection unchecked
            return (K) new PressureConduitStorage(node, tier);
        }
        return null;
    }

    @Override
    public int compareTo(@NotNull PressureConduit o) {
        if (tier().getCriticalPressure() < o.tier().getCriticalPressure()) {
            return -1;
        } else if (tier().getCriticalPressure() > o.tier().getCriticalPressure()) {
            return 1;
        }

        return 0;
    }
}
