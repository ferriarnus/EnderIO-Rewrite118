package com.enderio.conduits.common.types.fluid;

import com.enderio.EnderIO;
import com.enderio.api.conduit.ConduitDataSerializer;
import com.enderio.api.conduit.ExtendedConduitData;
import com.enderio.conduits.common.init.EIOConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidExtendedData implements ExtendedConduitData<FluidExtendedData> {

    public final boolean isMultiFluid;

    @Nullable
    Fluid lockedFluid = null;
    boolean shouldReset = false;

    public FluidExtendedData(boolean isMultiFluid) {
        this.isMultiFluid = isMultiFluid;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public FluidExtendedData(boolean isMultiFluid, Optional<Fluid> fluid) {
        this.isMultiFluid = isMultiFluid;
        this.lockedFluid = isMultiFluid
            ? fluid.orElse(null)
            : null;
    }

    @Override
    public void applyGuiChanges(FluidExtendedData guiData) {
        this.shouldReset = guiData.shouldReset;
    }

    @Override
    public ConduitDataSerializer<FluidExtendedData> serializer() {
        return EIOConduitTypes.Serializers.FLUID.get();
    }

    @Override
    public void onConnectTo(FluidExtendedData otherData) {
        if (lockedFluid != null) {
            if (otherData.lockedFluid != null && lockedFluid != otherData.lockedFluid) {
                EnderIO.LOGGER.warn("incompatible fluid conduits merged");
            }
            otherData.setLockedFluid(lockedFluid);
        } else if (otherData.lockedFluid != null) {
            setLockedFluid(otherData.lockedFluid);
        }
    }

    @Override
    public boolean canConnectTo(FluidExtendedData otherData) {
        return lockedFluid == null || otherData.lockedFluid == null || lockedFluid == otherData.lockedFluid;
    }

    private void setLockedFluid(@Nullable Fluid lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    public static class Serializer implements ConduitDataSerializer<FluidExtendedData> {
        public static MapCodec<FluidExtendedData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.BOOL.fieldOf("is_multi_fluid").forGetter(i -> i.isMultiFluid),
                BuiltInRegistries.FLUID.byNameCodec()
                    .optionalFieldOf("locked_fluid")
                    .forGetter(i -> Optional.ofNullable(i.lockedFluid))
            ).apply(instance, FluidExtendedData::new)
        );

        @Override
        public MapCodec<FluidExtendedData> codec() {
            return CODEC;
        }
    }
}
