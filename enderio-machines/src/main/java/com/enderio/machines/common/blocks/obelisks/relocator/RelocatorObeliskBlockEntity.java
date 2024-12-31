package com.enderio.machines.common.blocks.obelisks.relocator;

import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.filter.EntityFilter;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.obelisk.ObeliskAreaManager;
import com.enderio.machines.common.obelisk.RelocatorObeliskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.jetbrains.annotations.Nullable;

public class RelocatorObeliskBlockEntity extends ObeliskBlockEntity<RelocatorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.RELOCATOR_CAPACITY);
    private static final QuadraticScalable ENERGY_USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.RELOCATOR_USAGE);

    public RelocatorObeliskBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.RELOCATOR_OBELISK.get(), worldPosition, blockState, false, CapacitorSupport.REQUIRED,
                EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE);
    }

    @Override
    protected @Nullable ObeliskAreaManager<RelocatorObeliskBlockEntity> getAreaManager(ServerLevel level) {
        return RelocatorObeliskManager.getManager(level);
    }

    @Override
    public @Nullable MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
                .inputSlot((integer,
                        itemStack) -> itemStack.getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter)
                .slotAccess(FILTER)
                .capacitor()
                .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player pPlayer) {
        return new RelocatorObeliskMenu(containerId, playerInventory, this);
    }

    @Override
    public int getMaxRange() {
        return 32;
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.RELOCATOR_RANGE_COLOR.get();
    }

    public boolean handleSpawnEvent(FinalizeSpawnEvent event) {
        if (FILTER.getItemStack(this).getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter entityFilter) {
            if (!entityFilter.test(event.getEntity())) {
                return false;
            }
        }

        if (isActive() && getAABB().contains(event.getX(), event.getY(), event.getZ())) {
            int cost = ENERGY_USAGE.base().get(); // TODO scale on entity and range? The issue is that it needs the
                                                  // energy "now" and can't wait for it like other machines
            int energy = getEnergyStorage().consumeEnergy(cost, true);
            if (energy == cost) {
                RandomSource randomsource = level.getRandom(); // TODO proper checks for valid spawn?
                double x = getBlockPos().getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * 5 + 0.5D;
                double y = getBlockPos().getY() + randomsource.nextInt(3) - 1;
                double z = getBlockPos().getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * 5 + 0.5D;
                EntityTeleportEvent telEvent = new EntityTeleportEvent(event.getEntity(), x, y, z);
                if (!NeoForge.EVENT_BUS.post(telEvent).isCanceled()) {
                    event.getEntity().teleportTo(x, y, z);
                    getEnergyStorage().consumeEnergy(cost, false);
                    return true;
                }
            }
        }

        return false;
    }
}
