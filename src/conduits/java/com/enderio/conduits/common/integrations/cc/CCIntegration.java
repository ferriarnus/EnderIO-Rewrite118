package com.enderio.conduits.common.integrations.cc;

import com.enderio.api.conduit.IExtendedConduitData;
import com.enderio.api.integration.Integration;
import com.enderio.api.misc.ColorControl;
import com.enderio.conduits.common.blockentity.ConduitBlockEntity;
import com.enderio.conduits.common.blockentity.connection.DynamicConnectionState;
import com.enderio.conduits.common.blockentity.connection.IConnectionState;
import com.enderio.conduits.common.init.EnderConduitTypes;
import com.enderio.conduits.common.types.RedstoneExtendedData;
import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;

public class CCIntegration implements Integration {

    @Override
    public void addEventListener(IEventBus modEventBus, IEventBus forgeEventBus) {
        ComputerCraftAPI.registerBundledRedstoneProvider(this::getBundledRedstoneOutput);
    }

    private int getBundledRedstoneOutput(Level world, BlockPos pos, Direction side) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ConduitBlockEntity conduit) {
            IConnectionState connectionState = conduit.getBundle().getConnection(side).getConnectionState(EnderConduitTypes.REDSTONE.get());
            if (connectionState instanceof DynamicConnectionState dyn && dyn.isInsert()) {
                IExtendedConduitData<?> extendedConduitData = conduit.getBundle().getNodeFor(EnderConduitTypes.REDSTONE.get()).getExtendedConduitData();
                if (extendedConduitData instanceof RedstoneExtendedData redstone) {
                    int out = 0;
                    for (ColorControl control: ColorControl.values()) {
                        out |= (redstone.isActive(control) ? 1 : 0) << (ColorControl.values().length - control.ordinal());
                    }
                    return out;
                }
            }
        }
        return -1;
    }
}
