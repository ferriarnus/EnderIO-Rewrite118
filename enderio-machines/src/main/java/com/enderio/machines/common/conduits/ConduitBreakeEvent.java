package com.enderio.machines.common.conduits;

import java.util.Optional;

import com.enderio.machines.EIOMachines;
import com.enderio.machines.common.block.ConduitBundleBlock;
import com.enderio.machines.common.blockentity.ConduitBundleBlockEntity;

import net.minecraft.core.PositionImpl;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = EIOMachines.MODID, bus = Bus.FORGE)
public class ConduitBreakeEvent {
    
    @SubscribeEvent
    static void breake(BlockEvent.BreakEvent event) {
        if (event.getWorld().getBlockEntity(event.getPos()) instanceof ConduitBundleBlockEntity conduitBE) {
            if (conduitBE.getTypes().size() < 1) {
                return;
            }
            event.setCanceled(true);
            HitResult pick = event.getPlayer().pick(event.getPlayer().getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue(), 1.0F, false);
            for (Conduit type: conduitBE.getConduits()) {
                Vec3 subtract = pick.getLocation().subtract(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
                Optional<Vec3> closestPointTo = ConduitBundleBlock.getShape(type.getType().pos()).closestPointTo(subtract);
                if (closestPointTo.isPresent() && closestPointTo.get().subtract(subtract).closerThan(new PositionImpl(0, 0, 0), 0.001)) {
                    conduitBE.removeConduit(type);
                    conduitBE.cachedShape();
                    return;
                }
            }
        }
    }

}
