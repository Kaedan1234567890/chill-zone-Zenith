package com.chillzone.zenith;

import com.chillzone.zenith.block.ZenithBlocks;
import com.chillzone.zenith.command.ZenithCommands;
import com.chillzone.zenith.drop.ZenithMobDrops;
import com.chillzone.zenith.item.ZenithItems;
import com.chillzone.zenith.item.ZenithCreativeTabExtras;
import com.chillzone.zenith.item.ZenithAbilityScheduler;
import net.fabricmc.api.ModInitializer;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;

public final class ZenithMod implements ModInitializer {
    public static final String MOD_ID = "chillzonezenith";

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ZenithItems.initialize();
        ZenithBlocks.initialize();
        ZenithCreativeTabExtras.initialize();
        ZenithAbilityScheduler.initialize();
        ZenithCommands.register();
        ZenithMobDrops.initialize();
    }
}
