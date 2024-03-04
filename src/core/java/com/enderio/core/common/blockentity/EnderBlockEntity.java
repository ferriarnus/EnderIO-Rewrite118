package com.enderio.core.common.blockentity;

import com.enderio.api.UseOnly;
import com.enderio.core.common.network.C2SDataSlotChange;
import com.enderio.core.common.network.NetworkUtil;
import com.enderio.core.common.network.S2CDataSlotUpdate;
import com.enderio.core.common.network.slot.NetworkDataSlot;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base block entity class for EnderIO.
 * Handles data slot syncing and capability providers.
 */
public class EnderBlockEntity extends BlockEntity {

    public static final String DATA = "Data";
    public static final String INDEX = "Index";
    private final List<NetworkDataSlot<?>> dataSlots = new ArrayList<>();
    private final List<Runnable> afterDataSync = new ArrayList<>();

    private final Map<BlockCapability<?, ?>, EnumMap<Direction, BlockCapabilityCache<?, ?>>> selfCapabilities = new HashMap<>();
    private final Map<BlockCapability<?, ?>, EnumMap<Direction, BlockCapabilityCache<?, ?>>> neighbourCapabilities = new HashMap<>();

    public EnderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    // region Ticking

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, EnderBlockEntity blockEntity) {
        if (level.isClientSide) {
            blockEntity.clientTick();
        } else {
            blockEntity.serverTick();
        }
    }

    /**
     * Perform server-side ticking
     */
    public void serverTick() {
        // Perform syncing.
        if (level != null && !level.isClientSide) {
            sync();
            level.blockEntityChanged(worldPosition);
        }
    }

    /**
     * Perform client side ticking.
     */
    public void clientTick() {

    }

    // endregion

    // region Sync

    /**
     * This is the initial packet sent to a client loading the block (or when it is placed).
     */
    @Override
    public CompoundTag getUpdateTag() {
        ListTag dataList = new ListTag();
        for (int i = 0; i < dataSlots.size(); i++) {
            var slot = dataSlots.get(i);
            var nbt = slot.serializeNBT(true);
            if (nbt == null) {
                continue;
            }

            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt(INDEX, i);
            slotTag.put(DATA, nbt);

            dataList.add(slotTag);
        }

        CompoundTag data = new CompoundTag();
        data.put(DATA, dataList);
        return data;
    }

    /**
     * This is the client handling the tag above.
     * @param syncData The {@link CompoundTag} sent from {@link BlockEntity#getUpdateTag()}
     */
    @Override
    public void handleUpdateTag(CompoundTag syncData) {
        if (syncData.contains(DATA, Tag.TAG_LIST)) {
            ListTag dataList = syncData.getList(DATA, Tag.TAG_COMPOUND);

            for (Tag dataEntry : dataList) {
                if (dataEntry instanceof CompoundTag slotData) {
                    int slotIdx = slotData.getInt(INDEX);
                    dataSlots.get(slotIdx).fromNBT(slotData.get(DATA));
                }
            }

            for (Runnable task : afterDataSync) {
                task.run();
            }
        }
    }

    @Nullable
    private FriendlyByteBuf createBufferSlotUpdate() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        int amount = 0;
        for (int i = 0; i < dataSlots.size(); i++) {
            NetworkDataSlot<?> networkDataSlot = dataSlots.get(i);
            if (networkDataSlot.needsUpdate()) {
                amount ++;
                buf.writeInt(i);
                networkDataSlot.writeBuffer(buf);
            }
        }
        if (amount == 0) {
            return null;
        }
        FriendlyByteBuf result = new FriendlyByteBuf(Unpooled.buffer()); //Use 2 buffers to be able to write the amount of data
        result.writeInt(amount);
        result.writeBytes(buf.copy());
        return result;
    }

    public <T extends NetworkDataSlot<?>> T addDataSlot(T slot) {
        dataSlots.add(slot);
        return slot;
    }

    public void addAfterSyncRunnable(Runnable runnable) {
        afterDataSync.add(runnable);
    }

    /**
     * Fire this when you change the value of a {@link NetworkDataSlot} on the client side.
     */
    @UseOnly(LogicalSide.CLIENT)
    public <T> void clientUpdateSlot(@Nullable NetworkDataSlot<T> slot, T value) {
        if (slot == null) {
            return;
        }

        if (dataSlots.contains(slot)) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(dataSlots.indexOf(slot));
            slot.toBuffer(buf, value);
            NetworkUtil.sendToServer(new C2SDataSlotChange(getBlockPos(), buf.array()));
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    /**
     * Sync the BlockEntity to all tracking players. Don't call this if you don't know what you do
     */
    @UseOnly(LogicalSide.SERVER)
    public void sync() {
        var syncData = createBufferSlotUpdate();
        if (syncData != null) {
            NetworkUtil.sendToAllTracking(new S2CDataSlotUpdate(getBlockPos(), syncData.array()), level, getBlockPos());
        }
    }

    @UseOnly(LogicalSide.CLIENT)
    public void clientHandleBufferSync(FriendlyByteBuf buf) {
        for (int amount = buf.readInt(); amount > 0; amount--) {
            int index = buf.readInt();
            dataSlots.get(index).fromBuffer(buf);
        }

        for (Runnable task : afterDataSync) {
            task.run();
        }
    }

    @UseOnly(LogicalSide.SERVER)
    public void serverHandleBufferChange(FriendlyByteBuf buf) {
        int index = -1;
        try {
            index = buf.readInt();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid buffer was passed over the network to the server.");
        }
        dataSlots.get(index).fromBuffer(buf);
        dataSlots.get(index).updateServerCallback();
    }

    // endregion

    // region Neighboring Capabilities

    // TODO: NEO-PORT: We might want handling for Void contexts.
    //                 However cannot have two methods with same method name and different context type params :(

    @Nullable
    protected <T> T getSelfCapability(BlockCapability<T, Direction> capability, Direction side) {
        if (level == null) {
            return null;
        }

        if (!selfCapabilities.containsKey(capability)) {
            // We've not seen this capability before, time to register it!
            selfCapabilities.put(capability, new EnumMap<>(Direction.class));

            for (Direction direction : Direction.values()) {
                populateSelfCachesFor(direction, capability);
            }
        }

        if (!selfCapabilities.get(capability).containsKey(side)) {
            return null;
        }

        //noinspection unchecked
        return (T) selfCapabilities.get(capability).get(side).getCapability();
    }

    private void populateSelfCachesFor(Direction direction, BlockCapability<?, Direction> capability) {
        if (level instanceof ServerLevel serverLevel) {
            selfCapabilities.get(capability).put(direction, BlockCapabilityCache.create(capability, serverLevel, getBlockPos(), direction));
        }
    }

    @Nullable
    protected <T> T getNeighbouringCapability(BlockCapability<T, Direction> capability, Direction side) {
        if (level == null) {
            return null;
        }

        if (!neighbourCapabilities.containsKey(capability)) {
            // We've not seen this capability before, time to register it!
            neighbourCapabilities.put(capability, new EnumMap<>(Direction.class));

            for (Direction direction : Direction.values()) {
                populateNeighbourCachesFor(direction, capability);
            }
        }

        if (!neighbourCapabilities.get(capability).containsKey(side)) {
            return null;
        }

        //noinspection unchecked
        return (T) neighbourCapabilities.get(capability).get(side).getCapability();
    }

    private void populateNeighbourCachesFor(Direction direction, BlockCapability<?, Direction> capability) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos neighbourPos = getBlockPos().relative(direction);
            neighbourCapabilities.get(capability).put(direction, BlockCapabilityCache.create(capability, serverLevel, neighbourPos, direction.getOpposite()));
        }
    }

    // endregion
}
