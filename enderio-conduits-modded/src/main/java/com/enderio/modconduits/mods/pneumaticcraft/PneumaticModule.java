package com.enderio.modconduits.mods.pneumaticcraft;

import com.enderio.EnderIOBase;
import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitDataType;
import com.enderio.conduits.api.ConduitNetworkContextType;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitNetworkContext;
import com.enderio.modconduits.ConduitModule;
import com.enderio.modconduits.ModdedConduits;
import com.enderio.modconduits.mods.mekanism.ChemicalConduit;
import com.enderio.modconduits.mods.mekanism.MekanismModule;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PneumaticModule implements ConduitModule {

    public static final PneumaticModule INSTANCE = new PneumaticModule();

    private static final ModLoadedCondition CONDITION = new ModLoadedCondition("pneumaticcraft");

    public static class Types {

        private static final DeferredRegister<ConduitType<?>> CONDUIT_TYPES = DeferredRegister.create(EnderIOConduitsRegistries.CONDUIT_TYPE,
            ModdedConduits.REGISTRY_NAMESPACE);

        public static final Supplier<ConduitType<PressureConduit>> PRESSURE = CONDUIT_TYPES.register("pressure", () -> ConduitType.builder(PressureConduit.CODEC)
            .exposeCapability(PNCCapabilities.AIR_HANDLER_MACHINE).build());
    }

    public static class ContextSerializers {

        public static final DeferredRegister<ConduitNetworkContextType<?>> CONDUIT_NETWORK_CONTEXT_TYPES = DeferredRegister.create(
            EnderIOConduitsRegistries.CONDUIT_NETWORK_CONTEXT_TYPE, ModdedConduits.REGISTRY_NAMESPACE);

        public static final Supplier<ConduitNetworkContextType<PressureConduitNetworkContext>> PRESSURE = CONDUIT_NETWORK_CONTEXT_TYPES.register("pressure",
            () -> new ConduitNetworkContextType<>(PressureConduitNetworkContext.CODEC, PressureConduitNetworkContext::new));
    }

    public static final ResourceKey<Conduit<?>> PRESSURE = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT, EnderIOBase.loc("pressure"));

    public static final Component LANG_PRESSURE_CONDUIT = addTranslation("item", EnderIOBase.loc("conduit.pressure"), "Pressurised Conduit");

    private static MutableComponent addTranslation(String prefix, ResourceLocation id, String translation) {
        return ModdedConduits.REGILITE.addTranslation(prefix, id, translation);
    }

    @Override
    public void register(IEventBus modEventBus) {
        ContextSerializers.CONDUIT_NETWORK_CONTEXT_TYPES.register(modEventBus);
        Types.CONDUIT_TYPES.register(modEventBus);
    }

    @Override
    public void bootstrapConduits(BootstrapContext<Conduit<?>> context) {
        context.register(PRESSURE, new PressureConduit(EnderIOBase.loc("block/conduit/pressure"), LANG_PRESSURE_CONDUIT, PressureTier.TIER_ONE));

    }

    @Override
    public void buildConduitConditions(BiConsumer<ResourceKey<?>, ICondition> conditions) {
        conditions.accept(PRESSURE, CONDITION);
    }

    @Override
    public void buildRecipes(HolderLookup.Provider lookupProvider, RecipeOutput recipeOutput) {

    }
}
