package com.enderio.modconduits.mods.laserio;

import com.direwolf20.laserio.setup.Registration;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.integration.Integration;
import com.enderio.base.common.init.EIOCapabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class MekansimIntegration implements Integration {

    public static ICapabilityProvider<ItemStack, Void, ResourceFilter> CHEMICAL_FILTER_PROVIDER =
        (stack, v) -> new LaserChemicalFilter(stack);

    @Override
    public void addEventListener(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(this::registerCapEvent);
    }

    @SubscribeEvent
    public void registerCapEvent(RegisterCapabilitiesEvent event) {
        event.registerItem(EIOCapabilities.Filter.ITEM, CHEMICAL_FILTER_PROVIDER, Registration.Card_Chemical.get());
    }
}
