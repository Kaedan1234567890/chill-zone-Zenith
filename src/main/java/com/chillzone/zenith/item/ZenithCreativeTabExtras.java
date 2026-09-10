package com.chillzone.zenith.item;

import com.chillzone.zenith.block.ZenithBlocks;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

public final class ZenithCreativeTabExtras {
    private ZenithCreativeTabExtras() {}

    public static void initialize() {
        CreativeModeTabEvents.modifyEntries(ZenithItems.ZENITH_CREATIVE_TAB_KEY)
                .register(output -> {
                    output.accept(ZenithBlocks.ENDER_CRAFTING_TABLE);
                    output.accept(ZenithBlocks.RAVAGER_CRAFTING_TABLE);
                    output.accept(ZenithBlocks.GUARDIAN_CRAFTING_TABLE);
                    output.accept(ZenithBlocks.WARDEN_CRAFTING_TABLE);
                    output.accept(ZenithBlocks.WITHER_CRAFTING_TABLE);
                    output.accept(ZenithBlocks.ZENITH_CRAFTING_TABLE);
                });
    }
}
