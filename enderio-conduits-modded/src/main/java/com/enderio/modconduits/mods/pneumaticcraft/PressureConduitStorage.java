package com.enderio.modconduits.mods.pneumaticcraft;

import com.enderio.conduits.api.ConduitNode;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PressureConduitStorage implements IAirHandlerMachine {

    private final PressureTier tier;
    private final ConduitNode node;
    private int air;

    public PressureConduitStorage(ConduitNode node, PressureTier tier) {
        this.node = node;
        this.tier = tier;
    }

    @Override
    public float getPressure() {
        return (float) getAir() / (float) getVolume();
    }

    @Override
    public float getDangerPressure() {
        return tier.getDangerPressure();
    }

    @Override
    public float getCriticalPressure() {
        return tier.getCriticalPressure();
    }

    public void setPressure(float pressure) {
        this.addAir((int)(pressure * (float)this.getVolume()) - this.getAir());
    }

    @Override
    public void setVolumeUpgrades(int i) {

    }

    @Override
    public void enableSafetyVenting(FloatPredicate floatPredicate, Direction direction) {

    }

    @Override
    public void disableSafetyVenting() {

    }

    @Override
    public void tick(BlockEntity blockEntity) {

    }

    @Override
    public void setSideLeaking(@Nullable Direction direction) {

    }

    @Nullable
    @Override
    public Direction getSideLeaking() {
        return null;
    }

    @Override
    public List<Connection> getConnectedAirHandlers(BlockEntity blockEntity) {
        List<Connection> connections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            var cap = blockEntity.getLevel().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, blockEntity.getBlockPos().relative(direction), direction.getOpposite());
            if (cap != null) {
                connections.add(new ConnectedAirHandler(direction, cap));
            }
        }
        return connections;
    }

    @Override
    public void setConnectableFaces(Collection<Direction> collection) {

    }

    @Override
    public Tag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }

    @Override
    public void addPendingAir(int i) {
        node.getParentGraph().getOrCreateContext(PneumaticModule.ContextSerializers.PRESSURE.get()).setPressureInsertedThisTick(i);
    }

    @Override
    public int getAir() {
        return air;
    }

    @Override
    public void addAir(int i) {
        this.air += i;
    }

    @Override
    public int getBaseVolume() {
        return node.getParentGraph().getOrCreateContext(PneumaticModule.ContextSerializers.PRESSURE.get()).getVolume();
    }

    @Override
    public void setBaseVolume(int i) {
        node.getParentGraph().getOrCreateContext(PneumaticModule.ContextSerializers.PRESSURE.get()).setVolume(i);
    }

    @Override
    public int getVolume() {
        return node.getParentGraph().getOrCreateContext(PneumaticModule.ContextSerializers.PRESSURE.get()).getVolume() * node.getParentGraph().getNodes().size();
    }

    @Override
    public float maxPressure() {
        return tier.getCriticalPressure();
    }

    @Override
    public void printManometerMessage(Player player, List<Component> list) {

    }

    private static class ConnectedAirHandler implements IAirHandlerMachine.Connection {
        @Nullable
        final Direction direction;
        final IAirHandlerMachine airHandler;
        int maxDispersion;
        int toDisperse;

        ConnectedAirHandler(@Nullable Direction direction, IAirHandlerMachine airHandler) {
            this.direction = direction;
            this.airHandler = airHandler;
        }

        @Nullable
        public Direction getDirection() {
            return this.direction;
        }

        public int getMaxDispersion() {
            return this.maxDispersion;
        }

        public void setMaxDispersion(int maxDispersion) {
            this.maxDispersion = maxDispersion;
        }

        public int getDispersedAir() {
            return this.toDisperse;
        }

        public void setAirToDisperse(int toDisperse) {
            this.toDisperse = toDisperse;
        }

        public IAirHandlerMachine getAirHandler() {
            return this.airHandler;
        }
    }
}
