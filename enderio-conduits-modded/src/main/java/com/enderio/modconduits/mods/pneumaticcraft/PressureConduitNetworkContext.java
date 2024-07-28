package com.enderio.modconduits.mods.pneumaticcraft;

import com.enderio.conduits.api.ConduitNetworkContext;
import com.enderio.conduits.api.ConduitNetworkContextType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PressureConduitNetworkContext implements ConduitNetworkContext<PressureConduitNetworkContext> {

    public static final Codec<PressureConduitNetworkContext> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Codec.INT.fieldOf("air_stored").forGetter(i -> i.airStored),
            Codec.INT.fieldOf("volume").forGetter(i -> i.volume),
            Codec.INT.fieldOf("rotating_index").forGetter(i -> i.rotatingIndex)
        ).apply(builder, PressureConduitNetworkContext::new)
    );

    private int airStored = 0;
    private int pressureInsertedThisTick = 0;
    private int rotatingIndex = 0;
    private int volume = 1000;

    public PressureConduitNetworkContext() {

    }

    public PressureConduitNetworkContext(int airStored) {
        this.airStored = airStored;
    }

    public PressureConduitNetworkContext(int airStored, int volume, int rotatingIndex) {
        this.airStored = airStored;
        this.volume = volume;
        this.rotatingIndex = rotatingIndex;
    }

    public int getAirStored() {
        return airStored;
    }

    public void setAirStored(int airStored) {
        this.airStored = airStored;
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int i) {
        this.volume = i;
    }

    public int getPressureInsertedThisTick() {
        return pressureInsertedThisTick;
    }

    public void setPressureInsertedThisTick(int pressureInsertedThisTick) {
        this.pressureInsertedThisTick = pressureInsertedThisTick;
    }

    public int getRotatingIndex() {
        return rotatingIndex;
    }

    public void setRotatingIndex(int rotatingIndex) {
        this.rotatingIndex = rotatingIndex;
    }

    @Override
    public PressureConduitNetworkContext mergeWith(PressureConduitNetworkContext other) {
        return new PressureConduitNetworkContext(this.airStored + other.airStored);
    }

    @Override
    public PressureConduitNetworkContext copy() {
        return new PressureConduitNetworkContext(this.airStored);
    }

    @Override
    public ConduitNetworkContextType<PressureConduitNetworkContext> type() {
        return PneumaticModule.ContextSerializers.PRESSURE.get();
    }
}
