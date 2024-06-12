package com.enderio.conduits.common.init;

import com.enderio.EnderIO;
import com.enderio.api.conduit.ConduitApi;
import com.enderio.api.conduit.ConduitType;
import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOCreativeTabs;
import com.enderio.conduits.common.components.FluidSpeedUpgrade;
import com.enderio.conduits.common.components.ItemSpeedUpgrade;
import com.enderio.conduits.common.conduit.upgrade.SpeedUpgradeItem;
import com.enderio.conduits.common.redstone.DoubleRedstoneChannel;
import com.enderio.conduits.common.redstone.RedstoneCountFilter;
import com.enderio.conduits.common.redstone.RedstoneFilterItem;
import com.enderio.conduits.common.redstone.RedstoneTLatchFilter;
import com.enderio.conduits.common.redstone.RedstoneTimerFilter;
import com.enderio.regilite.holder.RegiliteItem;
import com.enderio.regilite.registry.ItemRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class ConduitItems {
    private static final ItemRegistry ITEM_REGISTRY = EnderIO.getRegilite().itemRegistry();

    public static final RegiliteItem<Item> ENERGY = createConduitItem(EIOConduitTypes.Types.ENERGY, "energy");
    public static final RegiliteItem<Item> FLUID = createConduitItem(EIOConduitTypes.Types.FLUID, "fluid");
    public static final RegiliteItem<Item> PRESSURIZED_FLUID = createConduitItem(EIOConduitTypes.Types.FLUID2, "pressurized_fluid");
    public static final RegiliteItem<Item> ENDER_FLUID = createConduitItem(EIOConduitTypes.Types.FLUID3, "ender_fluid");
    public static final RegiliteItem<Item> REDSTONE = createConduitItem(EIOConduitTypes.Types.REDSTONE, "redstone");
    public static final RegiliteItem<Item> ITEM = createConduitItem(EIOConduitTypes.Types.ITEM, "item");

    public static final RegiliteItem<SpeedUpgradeItem> ITEM_SPEED_UPGRADE = ITEM_REGISTRY.registerItem("item_speed_upgrade", properties ->
            new SpeedUpgradeItem(properties.component(ConduitComponents.ITEM_SPEED_UPGRADE, new ItemSpeedUpgrade(2)))).setTab(EIOCreativeTabs.CONDUITS)
        .addCapability(ConduitCapabilities.ConduitUpgrade.ITEM, SpeedUpgradeItem.ITEM_SPEED_UPGRADE_PROVIDER);

    public static final RegiliteItem<SpeedUpgradeItem> FLUID_SPEED_UPGRADE = ITEM_REGISTRY.registerItem("fluid_speed_upgrade", properties ->
            new SpeedUpgradeItem(properties.component(ConduitComponents.FLUID_SPEED_UPGRADE, new FluidSpeedUpgrade(2))))
        .setTab(EIOCreativeTabs.CONDUITS)
        .addCapability(ConduitCapabilities.ConduitUpgrade.ITEM, SpeedUpgradeItem.FLUID_SPEED_UPGRADE_PROVIDER);

    public static final RegiliteItem<RedstoneFilterItem> AND_FILTER = createRedstoneFilter("redstone_and_filter", ConduitComponents.REDSTONE_AND_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.AND_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> COUNT_FILTER = createRedstoneFilter("redstone_counting_filter", ConduitComponents.REDSTONE_COUNT_FILTER,
        RedstoneCountFilter.INSTANCE, RedstoneFilterItem.COUNT_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> NAND_FILTER = createRedstoneFilter("redstone_nand_filter", ConduitComponents.REDSTONE_NAND_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.NAND_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> NOR_FILTER = createRedstoneFilter("redstone_nor_filter", ConduitComponents.REDSTONE_NOR_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.NOR_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> NOT_FILTER = createRedstoneFilter("redstone_not_filter", ConduitComponents.REDSTONE_NOT_FILTER,
        Unit.INSTANCE, RedstoneFilterItem.NOT_FILTER_PROVIDER, null);
    public static final RegiliteItem<RedstoneFilterItem> OR_FILTER = createRedstoneFilter("redstone_or_filter", ConduitComponents.REDSTONE_OR_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.OR_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> SENSOR_FILTER = createRedstoneFilter("redstone_sensor_filter", ConduitComponents.REDSTONE_SENSOR_FILTER,
        Unit.INSTANCE, RedstoneFilterItem.SENSOR_FILTER_PROVIDER, null);
    public static final RegiliteItem<RedstoneFilterItem> TIMER_FILTER = createRedstoneFilter("redstone_timer_filter", ConduitComponents.REDSTONE_TIMER_FILTER,
        RedstoneTimerFilter.INSTANCE, RedstoneFilterItem.TIMER_FILTER_PROVIDER, ConduitMenus.REDSTONE_TIMER_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> TLATCH_FILTER = createRedstoneFilter("redstone_toggle_filter", ConduitComponents.REDSTONE_TLATCH_FILTER,
        RedstoneTLatchFilter.INSTANCE, RedstoneFilterItem.TLATCH_FILTER_PROVIDER, null);
    public static final RegiliteItem<RedstoneFilterItem> XNOR_FILTER = createRedstoneFilter("redstone_xnor_filter", ConduitComponents.REDSTONE_XNOR_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.XNOR_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);
    public static final RegiliteItem<RedstoneFilterItem> XOR_FILTER = createRedstoneFilter("redstone_xor_filter", ConduitComponents.REDSTONE_XOR_FILTER,
        DoubleRedstoneChannel.INSTANCE, RedstoneFilterItem.XOR_FILTER_PROVIDER, ConduitMenus.REDSTONE_DOUBLE_CHANNEL_FILTER::get);

    private static RegiliteItem<Item> createConduitItem(Supplier<? extends ConduitType<?>> type, String itemName) {
        return ITEM_REGISTRY
            .registerItem(itemName + "_conduit",
                p -> ConduitApi.INSTANCE.createConduitItem(type, p))
            .setTab(EIOCreativeTabs.CONDUITS)
            .setModelProvider((prov, ctx) -> prov
                .withExistingParent(itemName+"_conduit", EnderIO.loc("item/conduit"))
                .texture("0", type.get().getItemTexture()));
    }

    public static <T> RegiliteItem<RedstoneFilterItem> createRedstoneFilter(String name, DeferredHolder<DataComponentType<?>, DataComponentType<T>> type, T defaultValue, ICapabilityProvider<ItemStack, Void, ResourceFilter> provider, Supplier<MenuType<?>> menu) {
        return ITEM_REGISTRY
            .registerItem(name, properties -> new RedstoneFilterItem(properties.component(type, defaultValue), menu))
            .setTab(EIOCreativeTabs.CONDUITS)
            .addCapability(EIOCapabilities.Filter.ITEM, provider);
    }

    public static void register(IEventBus bus) {
        ITEM_REGISTRY.register(bus);
    }
}
