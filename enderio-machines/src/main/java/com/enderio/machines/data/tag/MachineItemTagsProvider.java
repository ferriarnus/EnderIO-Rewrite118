package com.enderio.machines.data.tag;

import com.enderio.base.api.EnderIO;
import com.enderio.machines.common.tag.MachineTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MachineItemTagsProvider extends ItemTagsProvider {

    private static final Map<TagKey<Item>, List<Item>> tags = new HashMap<>();

    public MachineItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, EnderIO.NAMESPACE, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MachineTags.ItemTags.EXPLOSIVES, Items.TNT, Items.FIREWORK_STAR, Items.FIREWORK_ROCKET, Items.FIRE_CHARGE);
        tag(MachineTags.ItemTags.NATURAL_LIGHTS, Items.GLOWSTONE_DUST, Items.GLOWSTONE, Items.SEA_LANTERN, Items.SEA_PICKLE, Items.GLOW_LICHEN, Items.GLOW_BERRIES, Items.GLOW_INK_SAC);
        tag(MachineTags.ItemTags.SUNFLOWER, Items.SUNFLOWER);
        tag(MachineTags.ItemTags.BLAZE_POWDER, Items.BLAZE_POWDER);

        tags.forEach((key, list) -> {
                var holder = tag(key);
                list.forEach(holder::add);
            }
        );
    }

    // helpers for tags. since tags can be added by external files, this prevents duplicates.
    public static void tag(TagKey<Item> tag, Item item) {
       var items = tags.computeIfAbsent(tag, t -> new ArrayList<>());
       if(!items.contains(item))
           items.add(item);
    }

    public static void tag(TagKey<Item> tag, Item ... itemList) {
        var items = tags.computeIfAbsent(tag, t -> new ArrayList<>());
        for (Item item: itemList) {
            if(!items.contains(item))
                items.add(item);
        }
    }
}
