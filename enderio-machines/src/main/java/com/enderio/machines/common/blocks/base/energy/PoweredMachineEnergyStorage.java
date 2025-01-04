package com.enderio.machines.common.blocks.base.energy;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class PoweredMachineEnergyStorage implements IEnergyStorage, INBTSerializable<IntTag>, IMachineEnergyStorage {

    private final PoweredMachineBlockEntity machine;

    private int energyStored;

    public PoweredMachineEnergyStorage(PoweredMachineBlockEntity machine) {
        this.machine = machine;
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(int energyStored) {
        this.energyStored = energyStored;
        machine.setChanged();
    }

    @Override
    public int getMaxEnergyStored() {
        return machine.getMaxEnergyStored();
    }

    public final int addEnergy(int energyToAdd) {
        return addEnergy(energyToAdd, false);
    }

    public final int addEnergy(int energyToAdd, boolean simulate) {
        int energyBefore = energyStored;
        int energyAfter = Math.min(energyBefore + energyToAdd, getMaxEnergyStored());

        if (!simulate) {
            setEnergyStored(energyAfter);
        }

        return energyAfter - energyBefore;
    }

    public final int consumeEnergy(int energyToConsume) {
        return consumeEnergy(energyToConsume, false);
    }

    // TODO: Make final once Primitive Alloy Smelter is rewritten.
    public int consumeEnergy(int energyToConsume, boolean simulate) {
        // Capped consumption rate
        energyToConsume = Math.min(energyToConsume, machine.getMaxEnergyUse());

        int energyExtracted = Math.min(energyStored, energyToConsume);

        if (!simulate) {
            setEnergyStored(energyStored - energyExtracted);
        }

        return energyExtracted;
    }

    @Nullable
    public IEnergyStorage getSided(Direction side) {
        if (!machine.energyIOMode().canInput() && !machine.energyIOMode().canOutput()) {
            return null;
        }

        if (!machine.getIOMode(side).canConnect()) {
            return null;
        }

        return new SidedAccess(this, side);
    }

    // region IMachineEnergyStorage Implementation (Legacy Interop)

    @Override
    public int takeEnergy(int energy) {
        return extractEnergy(energy, false);
    }

    @Override
    public int getMaxEnergyUse() {
        return machine.getMaxEnergyUse();
    }

    @Override
    public IOConfigurable getConfig() {
        return machine;
    }

    @Override
    public EnergyIOMode getIOMode() {
        return machine.energyIOMode();
    }

    // endregion

    // region Directionless Access

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || maxReceive <= 0) {
            return 0;
        }

        if (MachinesConfig.COMMON.ENERGY.THROTTLE_ENERGY_INPUT.get()) {
            maxReceive = Math.min(machine.getMaxEnergyUse() * 2, maxReceive);
        }

        int energyReceived = Mth.clamp(getMaxEnergyStored() - energyStored, 0, maxReceive);
        if (!simulate) {
            setEnergyStored(energyStored + energyReceived);
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract() || maxExtract <= 0) {
            return 0;
        }

        int energyExtracted = Math.min(energyStored, maxExtract);

        if (!simulate) {
            setEnergyStored(energyStored - energyExtracted);
        }

        return energyExtracted;
    }

    @Override
    public boolean canExtract() {
        return machine.energyIOMode().canOutput();
    }

    @Override
    public boolean canReceive() {
        return machine.energyIOMode().canInput();
    }

    @Override
    public IntTag serializeNBT(HolderLookup.Provider provider) {
        return IntTag.valueOf(energyStored);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, IntTag intTag) {
        energyStored = intTag.getAsInt();
    }

    // endregion

    public record SidedAccess(PoweredMachineEnergyStorage wrapped, Direction side) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive <= 0) {
                return 0;
            }

            return wrapped.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract <= 0) {
                return 0;
            }

            return wrapped.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return wrapped.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return wrapped.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            if (!wrapped.machine.energyIOMode().canOutput()) {
                return false;
            }

            if (side != null && wrapped.machine.energyIOMode().respectIOConfig()
                    && !wrapped.machine.getIOMode(side).canOutput()) {
                return false;
            }

            return wrapped.canExtract();
        }

        @Override
        public boolean canReceive() {
            if (!wrapped.machine.energyIOMode().canInput()) {
                return false;
            }

            if (side != null && wrapped.machine.energyIOMode().respectIOConfig()
                    && !wrapped.machine.getIOMode(side).canInput()) {
                return false;
            }

            return wrapped.canReceive();
        }
    }
}
