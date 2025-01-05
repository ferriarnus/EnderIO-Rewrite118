package com.enderio.machines.common.blocks.farming_station;

import com.enderio.base.api.attachment.StoredEntityData;
import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.farm.FarmInteraction;
import com.enderio.base.api.farm.FarmTask;
import com.enderio.base.api.farm.FarmTaskManager;
import com.enderio.base.api.farm.FarmingStation;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIODataComponents;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.attachment.ActionRange;
import com.enderio.machines.common.attachment.FluidTankUser;
import com.enderio.machines.common.attachment.RangedActor;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.base.inventory.MultiSlotAccess;
import com.enderio.machines.common.blocks.base.inventory.SingleSlotAccess;
import com.enderio.machines.common.blocks.base.state.MachineState;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineDataComponents;
import com.enderio.machines.common.io.fluid.FluidItemInteractive;
import com.enderio.machines.common.io.fluid.MachineFluidHandler;
import com.enderio.machines.common.io.fluid.MachineFluidTank;
import com.enderio.machines.common.io.fluid.MachineTankLayout;
import com.enderio.machines.common.io.fluid.TankAccess;
import com.enderio.machines.common.souldata.FarmSoul;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.ticket.AABBTicket;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


;

public class FarmingStationBlockEntity extends PoweredMachineBlockEntity implements RangedActor, FarmingStation, FluidTankUser, FluidItemInteractive {
    public static final String CONSUMED = "Consumed";
    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.FARM_CAPACITY);
    private static final QuadraticScalable ENERGY_USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.FARM_USAGE);

    private static final ActionRange DEFAULT_RANGE = new ActionRange(5, false);

    public static final SingleSlotAccess AXE = new SingleSlotAccess();
    public static final SingleSlotAccess HOE = new SingleSlotAccess();
    public static final SingleSlotAccess SHEAR = new SingleSlotAccess();
    public static final SingleSlotAccess NE = new SingleSlotAccess();
    public static final SingleSlotAccess SE = new SingleSlotAccess();
    public static final SingleSlotAccess SW = new SingleSlotAccess();
    public static final SingleSlotAccess NW = new SingleSlotAccess();
    public static final MultiSlotAccess BONEMEAL = new MultiSlotAccess();
    public static final MultiSlotAccess OUTPUT = new MultiSlotAccess();

    private static final TankAccess TANK = new TankAccess();
    private static final int CAPACITY = 1000;

    //TODO One fake player for all? Or one for each machine?
    public static final FakePlayer FARM_PLAYER = new FakePlayer(
        ServerLifecycleHooks.getCurrentServer().overworld(), new GameProfile(UUID.fromString("7b2621b4-83fb-11ee-b962-0242ac120002"), "enderio:farm"));
    private List<BlockPos> positions;
    private int currentIndex = 0;
    private int consumed = 0;
    @Nullable
    private FarmTask currentTask = null;
    private final MachineFluidHandler fluidHandler;
    @Nullable
    private AABBTicket ticket;

    private StoredEntityData entityData = StoredEntityData.EMPTY;
    @Nullable
    private FarmSoul.SoulData soulData;
    private static boolean reload = false;
    private boolean reloadCache = !reload;

    private ActionRange actionRange = DEFAULT_RANGE;


    public FarmingStationBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.FARMING_STATION.get(), worldPosition, blockState, true, CapacitorSupport.REQUIRED, EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE);
        fluidHandler = createFluidHandler();
    }

    @Override
    public int getMaxRange() {
        return 5;
    }

    @Override
    public ActionRange getActionRange() {
        return actionRange;
    }

    @Override
    public void setActionRange(ActionRange actionRange) {
        this.actionRange = actionRange.clamp(0, getMaxRange());
        updateLocations();
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected @Nullable MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .capacitor()
            .inputSlot((i,s) -> s.is(ItemTags.AXES))
            .slotAccess(AXE)
            .inputSlot((i,s) -> s.is(ItemTags.HOES))
            .slotAccess(HOE)
            .inputSlot((i,s) -> s.is(Tags.Items.TOOLS_SHEAR))
            .slotAccess(SHEAR)
            .inputSlot()
            .slotAccess(NE)
            .inputSlot()
            .slotAccess(SE)
            .inputSlot()
            .slotAccess(SW)
            .inputSlot()
            .slotAccess(NW)
            .inputSlot(2, (integer, stack) -> stack.is(EIOTags.Items.FERTILIZERS))
            .slotAccess(BONEMEAL)
            .outputSlot(6)
            .slotAccess(OUTPUT)
            .build();
    }

    @Override
    public void serverTick() {
        if (reloadCache != reload && entityData != StoredEntityData.EMPTY && entityData.entityType().isPresent()) {
            Optional<FarmSoul.SoulData> op = FarmSoul.FARM.matches(entityData.entityType().get());
            op.ifPresent(data -> soulData = data);
            reloadCache = reload;
        }
        if (canAct()) {
            doFarmTask();
        }
        updateMachineState(MachineState.IDLE, currentTask == null);

        super.serverTick();
    }

    @Override
    public void clientTick() {
        if (level.isClientSide && level instanceof ClientLevel clientLevel) {
            getActionRange().addClientParticle(clientLevel, getParticleLocation(), MachinesConfig.CLIENT.BLOCKS.DRAIN_RANGE_COLOR.get());
        }

        super.clientTick();
    }

    private void doFarmTask() {
        int stop = Math.min(currentIndex + getRange(), positions.size());
        while (currentIndex < stop) {
            BlockPos soil = positions.get(currentIndex);
            if (currentTask != null) {
                if (currentTask.farm(soil, this) != FarmInteraction.POWERED) {
                    currentTask = null; //Task is done or no longer valid
                }
                break;
            }
            //Look for a new task
            for (FarmTask task: FarmTaskManager.getTasks()) {
                FarmInteraction interaction = task.farm(soil, this);
                if (interaction == FarmInteraction.POWERED) { //new task found
                    currentTask = task;
                    break;
                }
                if (interaction == FarmInteraction.FINISHED) {//Task found and already done
                    currentTask = null;
                    break;
                }
            }
            //task found
            if (currentTask != null) {
                break;
            }
            currentIndex++;
        }

        //All positions have been checked, restart
        if (stop == positions.size()) {
            currentIndex = 0;
        }
    }

    //TODO check if the coords actually are these direction
    public SingleSlotAccess getSeedForPos(BlockPos soil) {
        if (soil.getX() >= getBlockPos().getX() && soil.getZ() > getBlockPos().getZ()){
            return SW;
        }
        if (soil.getX() > getBlockPos().getX() && soil.getZ() <= getBlockPos().getZ()){
            return NW;
        }
        if (soil.getX() <= getBlockPos().getX() && soil.getZ() < getBlockPos().getZ()){
            return SE;
        }
        if (soil.getX() < getBlockPos().getX() && soil.getZ() >= getBlockPos().getZ()){
            return NE;
        }
        return NW;
    }

    @Override
    public boolean isActive() {
        if (!canAct()) {
            return false;
        }
        //Check tool
        return currentTask != null;
    }

    public BlockPos getParticleLocation() {
        return worldPosition.below();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLocations();
        onTankContentsChanged(); //Refresh the ticket
    }

    private void updateLocations() {
        positions = new ArrayList<>();
        currentIndex = 0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-getRange(),-1, -getRange()), worldPosition.offset(getRange(),-1,getRange()))) {
            positions.add(pos.immutable()); //Need to make it immutable
        }
    }

    public boolean handleDrops(BlockState plant, BlockPos pos, BlockPos soil, BlockEntity blockEntity, ItemStack stack) {
        ItemStack dummy = stack.copy();
        if (soulData != null) {
            var enchantmentsRecipe = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var fortuneEnchantment = enchantmentsRecipe.getOrThrow(Enchantments.FORTUNE);
            dummy.enchant(fortuneEnchantment, dummy.getEnchantmentLevel(fortuneEnchantment) + soulData.seeds());
        }
        List<ItemStack> drops = Block.getDrops(plant, (ServerLevel) this.level, pos, blockEntity, getPlayer(), dummy);
        return collectDrops(drops, soil);
    }

    //TODO handle inv full
    public boolean collectDrops(List<ItemStack> drops, @Nullable BlockPos soil) {
        ArrayList<ItemStack> list = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (soil != null) {
                ItemStack seeds = getSeedForPos(soil).getItemStack(this);
                if (seeds.isEmpty()) {
                    if (drop.getItem() instanceof BlockItem || drop.getItem() instanceof SpecialPlantable) { //Collect potential seeds
                        getSeedForPos(soil).setStackInSlot(this, drop);
                        continue;
                    }
                }
                if (ItemStack.isSameItem(drop, seeds)) {
                    int leftOver = seeds.getMaxStackSize() - seeds.getCount();
                    if (drop.getCount() > leftOver) {
                        seeds.setCount(seeds.getMaxStackSize());
                        drop.shrink(leftOver);
                    } else {
                        seeds.setCount(seeds.getCount() + drop.getCount());
                        drop.setCount(0);
                        continue;
                    }
                }
            }
            ItemStack temp = drop.copy();
            list.add(temp);
            for (int i = 0; i < 6; i++) {
                ItemStack leftOver = OUTPUT.get(i).insertItem(this, temp, true);
                if (leftOver.isEmpty()) {
                    temp.setCount(0);
                    break;
                } else {
                    temp.setCount(leftOver.getCount());
                }
            }
        }
        boolean empty = list.stream().filter(d -> !d.isEmpty()).findAny().isEmpty();
        if (empty) {
            for (ItemStack drop : drops) {
                for (int i = 0; i < 6; i++) {
                    ItemStack leftOver = OUTPUT.get(i).insertItem(this, drop.copy(), false);
                    if (leftOver.isEmpty()) {
                        drop.setCount(0);
                        break;
                    } else {
                        drop.setCount(leftOver.getCount());
                    }
                }
            }
        }
        updateMachineState(MachineState.FULL_OUTPUT, !empty);
        return empty;
    }

    public int getConsumedPower() {
        return consumed;
    }

    @Override
    public void addConsumedPower(int power) {
        if (power > 0) {
            power = soulData == null ? power : (int) (power * soulData.power());
        }
        consumed += power;
    }

    public boolean consumeBonemeal() {
        boolean consumed = false;
        for (int i = 0; i < 2; i++) {
            ItemStack itemStack = BONEMEAL.get(i).getItemStack(this);
            if (!itemStack.isEmpty()) {
                if (soulData == null || level.random.nextFloat() < soulData.bonemeal()) {
                    itemStack.shrink(1);
                }
                consumed = true;
                break;
            }
        }
        return consumed;
    }

    @Override
    public ItemStack getSeedsForPos(BlockPos pos) {
        return getSeedForPos(pos).getItemStack(this);
    }

    @Override
    public ItemStack getAxe() {
        return AXE.getItemStack(this);
    }

    @Override
    public ItemStack getHoe() {
        return HOE.getItemStack(this);
    }

    @Override
    public ItemStack getShears() {
        return SHEAR.getItemStack(this);
    }

    @Override
    public FakePlayer getPlayer() {
        return FARM_PLAYER;
    }

    @Override
    public int consumeEnergy(int energy, boolean simulate) {
        return getEnergyStorage().consumeEnergy(energy, simulate);
    }

    @Override
    public MachineTankLayout getTankLayout() {
        return new MachineTankLayout.Builder().tank(TANK, CAPACITY, f -> f.is(FluidTags.WATER)).build();
    }

    @Override
    public MachineFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public MachineFluidHandler createFluidHandler() {
        return new MachineFluidHandler(this, getTankLayout()) {
            @Override
            protected void onContentsChanged(int slot) {
                onTankContentsChanged();
                setChanged();
                super.onContentsChanged(slot);
                updateMachineState(MachineState.EMPTY_TANK, TANK.getFluidAmount(this) <= 0);
            }
        };
    }

    public MachineFluidTank getFluidTank() {
        return TANK.getTank(this);
    }

    private void onTankContentsChanged() {
        if (level.isClientSide) {
            return;
        }
        if (TANK.getTank(this).getFluidAmount() == TANK.getTank(this).getCapacity()) {
            if (ticket != null) {
                ticket.invalidate();
            }
            this.ticket = FarmlandWaterManager.addAABBTicket(this.level, new AABB(this.worldPosition).inflate(getRange()));
        } else {
            if (ticket != null) {
                ticket.invalidate();
                ticket = null;
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (ticket != null) {
            ticket.invalidate();
            ticket = null;
        }
    }

    public Optional<ResourceLocation> getEntityType() {
        return entityData.entityType();
    }

    public void setEntityType(ResourceLocation entityType) {
        entityData = StoredEntityData.of(entityType);
    }

    @SubscribeEvent
    static void onReload(RecipesUpdatedEvent event) {
        reload = !reload;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FarmingStationMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.saveAdditional(tag, lookupProvider);
        tag.putInt(CONSUMED, consumed);
        saveTank(lookupProvider, tag);
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);

        if (!actionRange.equals(DEFAULT_RANGE)) {
            tag.put(MachineNBTKeys.ACTION_RANGE, actionRange.save(registries));
        }

        tag.put(MachineNBTKeys.ENTITY_STORAGE, entityData.saveOptional(registries));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(tag, lookupProvider);
        consumed = tag.getInt(CONSUMED);
        loadTank(lookupProvider, tag);

        if (tag.contains(MachineNBTKeys.ACTION_RANGE)) {
            actionRange = ActionRange.parse(lookupProvider,
                Objects.requireNonNull(tag.get(MachineNBTKeys.ACTION_RANGE)));
        } else {
            actionRange = DEFAULT_RANGE;
        }

        entityData = StoredEntityData.parseOptional(lookupProvider, tag.getCompound(MachineNBTKeys.ENTITY_STORAGE));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);

        SimpleFluidContent storedFluid = components.get(EIODataComponents.ITEM_FLUID_CONTENT);
        if (storedFluid != null) {
            var tank = TANK.getTank(this);
            tank.setFluid(storedFluid.copy());

        }

        var actionRange = components.get(MachineDataComponents.ACTION_RANGE);
        if (actionRange != null) {
            this.actionRange = actionRange;
        }

        entityData = components.getOrDefault(EIODataComponents.STORED_ENTITY, StoredEntityData.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        var tank = TANK.getTank(this);
        if (!tank.isEmpty()) {
            components.set(EIODataComponents.ITEM_FLUID_CONTENT, SimpleFluidContent.copyOf(tank.getFluid()));

        }

        // Only if unchanged.
        if (!actionRange.equals(DEFAULT_RANGE)) {
            components.set(MachineDataComponents.ACTION_RANGE, actionRange);
        }

        if (entityData.hasEntity()) {
            components.set(EIODataComponents.STORED_ENTITY, entityData);
        }
    }
}
