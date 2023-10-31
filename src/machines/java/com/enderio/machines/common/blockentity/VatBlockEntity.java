package com.enderio.machines.common.blockentity;

import com.enderio.machines.common.blockentity.base.MachineBlockEntity;
import com.enderio.machines.common.blockentity.task.CraftingMachineTask;
import com.enderio.machines.common.blockentity.task.host.CraftingMachineTaskHost;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.io.item.MachineInventory;
import com.enderio.machines.common.io.item.MultiSlotAccess;
import com.enderio.machines.common.io.item.SingleSlotAccess;
import com.enderio.machines.common.recipe.vat.VatFluidConversionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VatBlockEntity extends MachineBlockEntity {

    public static final SingleSlotAccess INPUT = new SingleSlotAccess();
    public static int process;
    private final CraftingMachineTaskHost craftingTaskHost;

    public VatBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);

        craftingTaskHost = new CraftingMachineTaskHost<>(this, () -> true, MachineRecipes.VAT_FLUID.type().get(),
            new VatFluidConversionRecipe.Container(getInventoryNN(), getFluidTankNN()), this::createTask);
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (canAct()) {
            craftingTaskHost.tick();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        craftingTaskHost.onLevelReady();
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        craftingTaskHost.newTaskAvailable();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }

    protected VatBlockEntity.VatCraftingMachineTask createTask(Level level, VatFluidConversionRecipe.Container container, @Nullable VatFluidConversionRecipe recipe) {
        return new VatBlockEntity.VatCraftingMachineTask(level, getInventoryNN(), container, null, recipe);
    }

    protected static class VatCraftingMachineTask extends CraftingMachineTask<VatFluidConversionRecipe, VatFluidConversionRecipe.Container> {

        public VatCraftingMachineTask(@NotNull Level level, MachineInventory inventory, VatFluidConversionRecipe.Container container, MultiSlotAccess outputSlots,
            @Nullable VatFluidConversionRecipe recipe) {
            super(level, inventory, container, outputSlots, recipe);
        }

        @Override
        protected void consumeInputs(VatFluidConversionRecipe recipe) {
            container.getItem(0).shrink(1);
            container.getItem(1).shrink(1);
        }

        @Override
        protected int makeProgress(int remainingProgress) {
            return 1;
        }

        @Override
        protected int getProgressRequired(VatFluidConversionRecipe recipe) {
            return recipe.getTicks();
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        craftingTaskHost.load(pTag);
        process = pTag.getInt("PROCESS");
        super.load(pTag);
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        craftingTaskHost.save(pTag);
        pTag.putInt("PROCESS", process);
        super.saveAdditional(pTag);
    }
}
