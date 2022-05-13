package com.enderio.machines.common.blockentity;

import java.util.HashSet;
import java.util.Set;

import com.enderio.machines.common.block.ConduitBundleBlock;
import com.enderio.machines.common.conduits.Conduit;
import com.enderio.machines.common.conduits.Conduit.ConduitType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConduitBundleBlockEntity extends BlockEntity {
    
    Set<Conduit> conduits = new HashSet<>();
    Set<Conduit.ConduitType> types = new HashSet<>();
    Set<Conduit.ConduitType> connected = new HashSet<>();
    private VoxelShape shape;

    public ConduitBundleBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
        this.conduits = new HashSet<>(Set.of(new Conduit(null, new ConduitType("power", new Vec3(8D, 8D, 8D))), new Conduit(null, new ConduitType("items", new Vec3(8D, 12D, 8D)))));
        this.types = new HashSet<>(Set.of(new ConduitType("power", new Vec3(8D, 8D, 8D)), new ConduitType("items", new Vec3(8D, 12D, 8D))));
        cachedShape();
    }
    
    public Set<Conduit> getConduits() {
        return Set.copyOf(conduits);
    }
    
    public void removeConduit(Conduit conduit) {
        conduits.remove(conduit);
        types.remove(conduit.getType());
        cachedShape();
    }
    
    public void addConduit(Conduit conduit) {
        conduits.add(conduit);
        types.add(conduit.getType());
        cachedShape();
        
    }
    
    public Set<ConduitType> getTypes() {
        return types;
    }
    
    public void cachedShape() {
        VoxelShape shape = Shapes.empty();
        for (Conduit.ConduitType conduit:getTypes()) {
            shape = Shapes.or(shape, ConduitBundleBlock.getShape(conduit.pos()));
        }
        this.shape = shape;
    }
    
    public VoxelShape getShape() {
        return shape;
    }
    
    

}
