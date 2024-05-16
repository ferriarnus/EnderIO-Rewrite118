package com.enderio.conduits.common.types.item;

import com.enderio.api.conduit.ConduitDataSerializer;
import com.enderio.api.conduit.ExtendedConduitData;
import com.enderio.conduits.common.init.EIOConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemExtendedData implements ExtendedConduitData<ItemExtendedData> {

    public Map<Direction, ItemSidedData> itemSidedData;

    public ItemExtendedData() {
        itemSidedData = new EnumMap<>(Direction.class);
    }

    public ItemExtendedData(Map<Direction, ItemSidedData> itemSidedData) {
        this.itemSidedData = new HashMap<>(itemSidedData);
    }

    @Override
    public void applyGuiChanges(ItemExtendedData guiData) {
        for (Direction direction : Direction.values()) {
            compute(direction).applyGuiChanges(guiData.get(direction));
        }
    }

    @Override
    public ConduitDataSerializer<ItemExtendedData> serializer() {
        return EIOConduitTypes.Serializers.ITEM.get();
    }

    public ItemSidedData get(Direction direction) {
        return Objects.requireNonNull(itemSidedData.getOrDefault(direction, ItemSidedData.EMPTY));
    }

    public ItemSidedData compute(Direction direction) {
        return itemSidedData.computeIfAbsent(direction, dir -> new ItemSidedData());
    }

    public static class ItemSidedData {

        public static Codec<ItemSidedData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.BOOL.fieldOf("is_round_robin").forGetter(i -> i.isRoundRobin),
                Codec.INT.fieldOf("rotating_index").forGetter(i -> i.rotatingIndex),
                Codec.BOOL.fieldOf("is_self_feed").forGetter(i -> i.isSelfFeed),
                Codec.INT.fieldOf("priority").forGetter(i -> i.priority)
            ).apply(instance, ItemSidedData::new)
        );

        public static ItemSidedData EMPTY = new ItemSidedData(false, 0, false, 0);

        public boolean isRoundRobin = false;
        public int rotatingIndex = 0;
        public boolean isSelfFeed = false;
        public int priority = 0;

        public ItemSidedData() {
        }

        public ItemSidedData(boolean isRoundRobin, int rotatingIndex, boolean isSelfFeed, int priority) {
            this.isRoundRobin = isRoundRobin;
            this.rotatingIndex = rotatingIndex;
            this.isSelfFeed = isSelfFeed;
            this.priority = priority;
        }

        private void applyGuiChanges(ItemSidedData guiChanges) {
            this.isRoundRobin = guiChanges.isRoundRobin;
            this.isSelfFeed = guiChanges.isSelfFeed;
            this.priority = guiChanges.priority;
        }
    }

    public static class Serializer implements ConduitDataSerializer<ItemExtendedData> {
        public static MapCodec<ItemExtendedData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.unboundedMap(Direction.CODEC, ItemSidedData.CODEC)
                    .fieldOf("item_sided_data").forGetter(i -> i.itemSidedData)
            ).apply(instance, ItemExtendedData::new)
        );

        @Override
        public MapCodec<ItemExtendedData> codec() {
            return CODEC;
        }
    }
}
