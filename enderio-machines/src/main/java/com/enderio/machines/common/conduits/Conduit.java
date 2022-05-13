package com.enderio.machines.common.conduits;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class Conduit {
    
    public static Set<ConduitType> TYPES = Set.of(new ConduitType("power", new Vec3(8D, 8D, 8D)), new ConduitType("items", new Vec3(8D, 12D, 8D)));
    private ConduitType type;
    private ResourceLocation texture;
    
    public Conduit(ResourceLocation texture,ConduitType type) {
        this.type = type;
    }
    
    public ConduitType getType() {
        return type;
    }
    
    public void tick() {
        
    }
    
    public ResourceLocation getTexture() {
        return this.texture;
    }

    public record ConduitType(String name, Vec3 pos) {};
}
