package com.enderio.base.api.travel;

import com.enderio.base.api.registry.EnderIORegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface TravelTarget {
    Codec<TravelTarget> CODEC = EnderIORegistries.TRAVEL_TARGET_SERIALIZERS.byNameCodec()
            .dispatch(TravelTarget::serializer, TravelTargetSerializer::codec);
    StreamCodec<RegistryFriendlyByteBuf, TravelTarget> STREAM_CODEC = ByteBufCodecs
            .registry(EnderIORegistries.Keys.TRAVEL_TARGET_SERIALIZERS)
            .dispatch(TravelTarget::serializer, TravelTargetSerializer::streamCodec);

    BlockPos pos();

    int item2BlockRange();

    int block2BlockRange();

    /**
     * @deprecated No longer used directly, use canTeleportTo and canJumpTo instead. These are more specific to the possible travel types.
     * @return Whether the target can be travelled to.
     */
    @Deprecated(since = "7.0.12-alpha")
    default boolean canTravelTo() {
        return true;
    }

    /**
     * @return Whether the target can be teleported to.
     */
    default boolean canTeleportTo() {
        return canTravelTo();
    }

    /**
     * @return Whether the target can be jumped to like an elevator.
     */
    default boolean canJumpTo() {
        return canTravelTo();
    }

    TravelTargetType<?> type();

    TravelTargetSerializer<?> serializer();
}
