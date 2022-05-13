package com.enderio.machines.client;

import java.util.List;
import java.util.Optional;

import com.enderio.machines.EIOMachines;
import com.enderio.machines.client.rendering.blockentity.FluidTankBER;
import com.enderio.machines.client.rendering.model.ConduitBakedModel;
import com.enderio.machines.client.rendering.model.IOOverlayBakedModel;
import com.enderio.machines.common.block.ConduitBundleBlock;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.PositionImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MachinesClientSetup {

    @SubscribeEvent
    public static void customModelLoaders(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(EIOMachines.loc("io_overlay"), new IOOverlayBakedModel.Loader());
        ModelLoaderRegistry.registerLoader(EIOMachines.loc("conduit"), new ConduitBakedModel.Loader());
    }

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MachineBlockEntities.FLUID_TANK.get(), FluidTankBER::new);
        event.registerBlockEntityRenderer(MachineBlockEntities.PRESSURIZED_FLUID_TANK.get(), FluidTankBER::new);
    }
    
    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class forgeMachinesClientSetup {
        
        //TODO really messy and can be a lot better
        @SubscribeEvent
        public static void higlightConduit(DrawSelectionEvent.HighlightBlock event) {
            BlockPos blockPos = event.getTarget().getBlockPos();
            if (Minecraft.getInstance().level.getBlockState(blockPos).getBlock() instanceof ConduitBundleBlock) { //get the block
                event.setCanceled(true);
                Vec3 location = event.getTarget().getLocation();
                List<VoxelShape> shapes = List.of(ConduitBundleBlock.SHAPE, ConduitBundleBlock.SHAPE2); //get list dynamically (from BE)
                event.getTarget().getBlockPos();
                for (VoxelShape shape : shapes) {
                    Vec3 camPos = event.getCamera().getPosition();
                    Vec3 subtract = location.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    Optional<Vec3> closestPointTo = shape.closestPointTo(subtract);
                    if (closestPointTo.isPresent() && closestPointTo.get().subtract(subtract).closerThan(new PositionImpl(0, 0, 0), 0.001)) { //point really close to block (can't be zero due to precision, but can be better)
                        renderHitOutline(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()), camPos.x, camPos.y, camPos.z, blockPos, shape);
                    }
                }
            }
        }
    }
    
    private static void renderHitOutline(PoseStack poseStack, VertexConsumer consumer, double camX, double camY, double camZ, BlockPos pos, VoxelShape shape) {
        renderShape(poseStack, consumer, shape, (double)pos.getX() - camX, (double)pos.getY() - camY, (double)pos.getZ() - camZ, 0.0F, 0.0F, 0.0F, 0.4F);
     }
    
    //TODO use at.
    private static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose posestack$pose = poseStack.last();
        shape.forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
           float f = (float)(p_194327_ - p_194324_);
           float f1 = (float)(p_194328_ - p_194325_);
           float f2 = (float)(p_194329_ - p_194326_);
           float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
           f /= f3;
           f1 /= f3;
           f2 /= f3;
           consumer.vertex(posestack$pose.pose(), (float)(p_194324_ + x), (float)(p_194325_ + y), (float)(p_194326_ + z)).color(red, green, blue, alpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
           consumer.vertex(posestack$pose.pose(), (float)(p_194327_ + x), (float)(p_194328_ + y), (float)(p_194329_ + z)).color(red, green, blue, alpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
     }

}
