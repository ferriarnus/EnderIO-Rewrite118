package com.enderio.modconduits.mods.pneumaticcraft;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.ConduitNetwork;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class PressureTicker implements IOAwareConduitTicker<PressureConduit> {

    public static final PressureTicker INSTANCE = new PressureTicker();

    public PressureTicker() {

    }

    @Override
    public void tickGraph(ServerLevel level, PressureConduit conduit, List<ConduitNode> loadedNodes, ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {
        // Reset insertion cap
        PressureConduitNetworkContext context = graph.getContext(PneumaticModule.ContextSerializers.PRESSURE.get());
        if (context != null) {
            context.setPressureInsertedThisTick(0);
        }

        IOAwareConduitTicker.super.tickGraph(level, conduit, loadedNodes, graph, coloredRedstoneProvider);
    }

    @Override
    public void tickColoredGraph(ServerLevel level, PressureConduit conduit, List<Connection> inserts, List<Connection> extracts, DyeColor color,
        ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        PressureConduitNetworkContext context = graph.getContext(PneumaticModule.ContextSerializers.PRESSURE.get());
        if (context == null) {
            return;
        }

        int totalVolume = graph.getNodes().size() * context.getVolume();
        int totalAir = context.getAirStored();

        List<IAirHandlerMachine> storagesForInsert = new ArrayList<>();
        for (var insert : inserts) {
            IAirHandlerMachine capability = level.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, insert.move(), insert.direction().getOpposite());
            if (capability != null) {
                storagesForInsert.add(capability);
                totalVolume += capability.getVolume();
                totalAir += capability.getAir();
            }
        }

        for (var insert : extracts) {
            IAirHandlerMachine capability = level.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, insert.move(), insert.direction().getOpposite());
            if (capability != null) {
                storagesForInsert.add(capability);
                totalVolume += capability.getVolume();
                totalAir += capability.getAir();
            }
        }

        // Revert overflow.
        if (storagesForInsert.size() <= context.getRotatingIndex()) {
            context.setRotatingIndex(0);
        }

        int startingRotatingIndex = context.getRotatingIndex();
        for (int i = startingRotatingIndex; i < startingRotatingIndex + storagesForInsert.size(); i++) {
            int insertIndex = i % storagesForInsert.size();

            IAirHandlerMachine insertHandler = storagesForInsert.get(insertIndex);
            context.setRotatingIndex(insertIndex + 1);

            int totalMachineAir = (int) ((long) totalAir * insertHandler.getVolume() / totalVolume);
            int air = Math.max(0, totalMachineAir - insertHandler.getAir());
            insertHandler.addAir(air);
            context.setAirStored(context.getAirStored() - air);
        }
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        if (level.getBlockEntity(conduitPos.relative(direction)) instanceof ConduitBundleBlockEntity) {
            return false;
        }

        IAirHandlerMachine capability = level.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, conduitPos.relative(direction), direction.getOpposite());
        return capability != null;
    }
}
