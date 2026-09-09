package com.chillzone.zenith.progression;

import com.chillzone.zenith.ZenithMod;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Persistent server/world-wide activation state for all Zenith branches.
 *
 * Minecraft 26.2 uses Codec + SavedDataType rather than the old
 * CompoundTag save/load override API.
 */
public final class ZenithProgressionState extends SavedData {
    private static final Codec<ZenithProgressionState> CODEC =
            Codec.INT.xmap(
                    ZenithProgressionState::new,
                    ZenithProgressionState::toMask
            );

    private static final SavedDataType<ZenithProgressionState> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            ZenithMod.MOD_ID,
                            "progression_state"
                    ),
                    ZenithProgressionState::new,
                    CODEC,
                    null
            );

    private final EnumMap<ZenithCategory, Boolean> enabled =
            new EnumMap<>(ZenithCategory.class);

    public ZenithProgressionState() {
        for (ZenithCategory category : ZenithCategory.values()) {
            enabled.put(category, false);
        }
    }

    private ZenithProgressionState(int mask) {
        this();

        ZenithCategory[] categories = ZenithCategory.values();
        for (int i = 0; i < categories.length; i++) {
            enabled.put(categories[i], (mask & (1 << i)) != 0);
        }
    }

    private int toMask() {
        int mask = 0;
        ZenithCategory[] categories = ZenithCategory.values();

        for (int i = 0; i < categories.length; i++) {
            if (isEnabled(categories[i])) {
                mask |= (1 << i);
            }
        }

        return mask;
    }

    public boolean isEnabled(ZenithCategory category) {
        return enabled.getOrDefault(category, false);
    }

    public void setEnabled(ZenithCategory category, boolean value) {
        enabled.put(category, value);
        setDirty();
    }

    public void setAll(boolean value) {
        for (ZenithCategory category : ZenithCategory.values()) {
            enabled.put(category, value);
        }
        setDirty();
    }

    public Map<ZenithCategory, Boolean> snapshot() {
        return Map.copyOf(enabled);
    }

    public static ZenithProgressionState get(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        if (level == null) {
            // This should not happen during normal command execution, but keeps
            // the helper safe during unusual server startup/shutdown states.
            return new ZenithProgressionState();
        }

        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
