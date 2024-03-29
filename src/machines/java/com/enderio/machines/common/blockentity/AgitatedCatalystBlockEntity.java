package com.enderio.machines.common.blockentity;

import com.enderio.api.io.IOMode;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.base.common.util.ExperienceUtil;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import com.enderio.machines.common.attachment.IFluidTankUser;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.io.FixedIOConfig;
import com.enderio.machines.common.io.IOConfig;
import com.enderio.machines.common.io.fluid.MachineFluidHandler;
import com.enderio.machines.common.io.fluid.MachineFluidTank;
import com.enderio.machines.common.io.fluid.MachineTankLayout;
import com.enderio.machines.common.io.fluid.TankAccess;
import com.google.gson.internal.LinkedTreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class AgitatedCatalystBlockEntity extends EnderBlockEntity implements IFluidTankUser {

    private final SculkSpreader sculkSpreader;
    private final MachineFluidHandler fluidHandler;
    private static final TankAccess TANK = new TankAccess();

    public AgitatedCatalystBlockEntity(BlockPos worldPosition, BlockState blockState) {
        this(MachineBlockEntities.AGITATED_CATALYST.get(), worldPosition, blockState);
    }

    public AgitatedCatalystBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
        this.sculkSpreader = SculkSpreader.createLevelSpreader();
        fluidHandler = createFluidHandler();
    }

    private void bloom() {
        if (this.getLevel() instanceof ServerLevel pLevel) {
            BlockPos pPos = getBlockPos();
            BlockState pState = getBlockState();
            pLevel.setBlock(pPos, pState.setValue(SculkCatalystBlock.PULSE, Boolean.valueOf(true)), 3);
            pLevel.scheduleTick(pPos, pState.getBlock(), 8);
            pLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                (double)pPos.getX() + 0.5,
                (double)pPos.getY() + 1.15,
                (double)pPos.getZ() + 0.5,
                2,
                0.2,
                0.0,
                0.2,
                0.0
            );
            pLevel.playSound(null, pPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + pLevel.random.nextFloat() * 0.4F);
        }
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, AgitatedCatalystBlockEntity pAgitatedCatalystBlockEntity) {
        pAgitatedCatalystBlockEntity.sculkSpreader.updateCursors(pLevel, pPos, pLevel.getRandom(), true);
    }

    @Override
    public MachineTankLayout getTankLayout() {
        return MachineTankLayout.builder().tank(TANK, 1000, f -> f.getFluid().is(EIOTags.Fluids.EXPERIENCE)).build();
    }

    @Override
    public MachineFluidHandler createFluidHandler() {
        return new MachineFluidHandler(new FixedIOConfig(IOMode.PULL), getTankLayout()) {
            @Override
            protected void onContentsChanged(int slot) {
                int amount = TANK.drain(this, 1000, FluidAction.SIMULATE).getAmount();
                int exp = amount / ExperienceUtil.EXP_TO_FLUID;
                sculkSpreader.addCursors(getBlockPos(), TANK.drain(this, exp * ExperienceUtil.EXP_TO_FLUID, FluidAction.EXECUTE).getAmount());
                if (getLevel() instanceof ServerLevel level) {
                    bloom();
                }
                setChanged();
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                // Convert into XP Juice
                if (TANK.isFluidValid(this, resource)) {
                    var currentFluid = TANK.getFluid(this).getFluid();
                    if (currentFluid == Fluids.EMPTY || resource.getFluid().isSame(currentFluid)) {
                        return super.fill(resource, action);
                    } else {
                        return super.fill(new FluidStack(currentFluid, resource.getAmount()), action);
                    }
                }

                // Non-XP is not allowed.
                return 0;
            }
        };
    }

    @Override
    public MachineFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public MachineFluidTank getFluidTank() {
        return TANK.getTank(this);
    }
}
