package com.enderio.base.common.item.darksteel.upgrades;

import com.enderio.api.capability.IDarkSteelUpgrade;
import com.enderio.base.common.lang.EIOLang;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

public class SpoonUpgrade implements IDarkSteelUpgrade {

    public static final String NAME = DarkSteelUpgradeRegistry.UPGRADE_PREFIX + "spoon";

    public static SpoonUpgrade create() {
        return new SpoonUpgrade();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Component getDisplayName() {
        return EIOLang.DS_UPGRADE_SPOON;
    }

    @Override
    public Collection<Component> getDescription() {
        return List.of(EIOLang.DS_UPGRADE_SPOON_DESCRIPTION);
    }
}
