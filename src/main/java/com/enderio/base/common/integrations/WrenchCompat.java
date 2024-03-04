package com.enderio.base.common.integrations;

import com.enderio.base.common.blockentity.IWrenchable;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.tag.EIOTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber
public class WrenchCompat {
    @SubscribeEvent
    public static void onRightClickBlock(RightClickBlock event) {
        ItemStack itemInHand = event.getEntity().getItemInHand(event.getHand());
        // @formatter:off
        if (itemInHand.is(EIOTags.Items.WRENCH)
            && !itemInHand.is(EIOItems.YETA_WRENCH.get())
            && event.getLevel().getBlockEntity(event.getPos()) instanceof IWrenchable) {
            // @formatter:on
            event.setUseBlock(Event.Result.ALLOW);
            event.setUseItem(Event.Result.DENY);
        }
    }
}
