package com.enderio.machines.common.blockentity;

import java.util.HashSet;
import java.util.Set;

import com.enderio.machines.common.conduits.Conduit;
import com.enderio.machines.common.conduits.Conduit.ConduitType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ConduitBundleBlockEntity extends BlockEntity {
    
    Set<Conduit> conduits = new HashSet<>();
    Set<Conduit.ConduitType> types = new HashSet<>();
    Set<Conduit.ConduitType> connected = new HashSet<>();

    public ConduitBundleBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }
    
    public Set<Conduit> getConduits() {
        return conduits;
    }
    
    public void removeConduit(Conduit conduit) {
        conduits.remove(conduit);
        types.remove(conduit.getType());
    }
    
    public void addConduit(Conduit conduit) {
        conduits.add(conduit);
        types.add(conduit.getType());
    }
    
    public Set<ConduitType> getTypes() {
        return types;
    }
    
    

}
