package com.chillzone.zenith;

import com.chillzone.zenith.command.ZenithCommands;
import net.fabricmc.api.ModInitializer;

public final class ZenithMod implements ModInitializer {
    public static final String MOD_ID = "chillzonezenith";

    @Override
    public void onInitialize() {
        ZenithCommands.register();

        // NEXT PHASE:
        // ZenithItems.register();
        // ZenithBlocks.register();
        // ZenithLootHooks.register();
        // ZenithRecipes.register();
    }
}
