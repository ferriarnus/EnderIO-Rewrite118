package com.enderio.machines.client.rendering.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import com.enderio.machines.EIOMachines;
import com.enderio.machines.common.conduits.Conduit;
import com.enderio.machines.common.conduits.Conduit.ConduitType;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class ConduitBakedModel implements IDynamicBakedModel{
    
    private final ModelState modelState;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final Map<ModelKey, List<BakedQuad>> quadCache = new HashMap<>();
    private ItemOverrides overrides;
    private ItemTransforms cameraTransforms;
    
    public ConduitBakedModel(ModelState modelState, Function<Material, TextureAtlasSprite> spriteGetter,
            ItemOverrides overrides, ItemTransforms cameraTransforms) {
        this.modelState = modelState;
        this.spriteGetter = spriteGetter;
        this.overrides = overrides;
        this.cameraTransforms = cameraTransforms;
        generateQuadCache();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
        RenderType layer = MinecraftForgeClient.getRenderType();
        if (side != null || (layer != null && !layer.equals(RenderType.solid()))) {
            return Collections.emptyList();
        }
        List<String> types = new ArrayList<>();
        types.add("power");
        types.add("item");
        List<String> connected = new ArrayList<>();
        connected.add("power");
        ModelKey key = new ModelKey(types, connected, modelState);
        return quadCache.get(key);
    }
    
    private void generateQuadCache() {
        //TODO Collect all conduits and generate cache
        Set<Set<ConduitType>> combinations = Sets.combinations(Conduit.TYPES, Conduit.TYPES.size());
        for (Set<ConduitType> combination: combinations) {
            List<String> types = new ArrayList<>();
            for (Conduit.ConduitType type: combination) {
                types.add(type.name());
            }
            List<String> connected = new ArrayList<>();
            connected.add("power");
            quadCache.put(new ModelKey(types, connected, modelState), generateQuadsBundle(types, connected));
        }

    }
    
    private List<BakedQuad> generateQuadsBundle(List<String> types, List<String> connected) {
        //TODO Collect single conduit model based on types and connected
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
        for (String conduit: types) {
            quads.addAll(generateQuadsConduit(conduit));
        }
        return quads;
    }
    
    private List<BakedQuad> generateQuadsConduit(String conduit) {
        //TODO get position and texture from conduit
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
        
        TextureAtlasSprite texture = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(new ResourceLocation(EIOMachines.MODID, "block/simple_machine_top"))); //get from conduit
        
        for (Direction dir : Direction.values()) {
           Vec3[] vec = ModelRenderUtil.createQuadVerts(dir, 6f/16f, 10f/16f, 10f/16f); //get offsets from conduit
           quads.add(ModelRenderUtil.createQuad(vec, texture));
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return spriteGetter.apply(ForgeHooksClient.getBlockMaterial(new ResourceLocation(EIOMachines.MODID, "block/simple_machine_top"))); //get from conduit;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }
    
    public record ModelKey(List<String> types, List<String> connected, ModelState modelState) { }
    
    public static class Loader implements IModelLoader<Loader.ConduitModelGeometry>{

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {

        }

        @Override
        public ConduitModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            return new ConduitModelGeometry();
        }
        
        public static class ConduitModelGeometry implements IModelGeometry<ConduitModelGeometry> {

            @Override
            public BakedModel bake(IModelConfiguration owner, ModelBakery bakery,
                    Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides,
                    ResourceLocation modelLocation) {
                return new ConduitBakedModel(modelTransform, spriteGetter, overrides, owner.getCameraTransforms());
            }

            @Override
            public Collection<Material> getTextures(IModelConfiguration owner,
                    Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
                //TODO collect all textures?
                return Collections.singleton(ForgeHooksClient.getBlockMaterial(new ResourceLocation(EIOMachines.MODID, "block/simple_machine_top")));
            }
            
        }

    }

}
