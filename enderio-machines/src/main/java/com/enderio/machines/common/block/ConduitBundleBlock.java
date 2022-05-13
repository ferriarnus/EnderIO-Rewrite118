package com.enderio.machines.common.block;

import com.enderio.machines.common.blockentity.ConduitBundleBlockEntity;
import com.enderio.machines.common.conduits.Conduit;
import com.enderio.machines.common.init.MachineBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConduitBundleBlock extends Block implements EntityBlock{
    
    public static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    public static final VoxelShape SHAPE2 = Block.box(6.0D, 10.0D, 6.0D, 10.0D, 14.0D, 10.0D);

    public ConduitBundleBlock(Properties p_49795_) {
        super(p_49795_);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = SHAPE;
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBE) {
            shape = Shapes.empty();
            for (Conduit.ConduitType conduit:conduitBE.getTypes()) {
                Shapes.or(shape, getShape(conduit.pos()));
            }
        }
        return shape.optimize();
    }
    
    public static VoxelShape getShape(Vec3 pos) {
        return Block.box(pos.x-2.0D, pos.y-2.0D, pos.z-2.0D, pos.x+2.0D, pos.y+2.0D, pos.z+2.0D);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // TODO Auto-generated method stub
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // TODO Auto-generated method stub
        super.onPlace(state, level, pos, oldState, isMoving);
    }
    

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return MachineBlockEntities.CONDUIT.create(pos, state);
    }
    
}
