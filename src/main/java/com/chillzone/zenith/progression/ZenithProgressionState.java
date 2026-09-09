package com.chillzone.zenith.progression;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;
import java.util.Map;

public final class ZenithProgressionState extends SavedData {
    private static final String DATA_NAME = "chillzone_zenith_progression";

    private final EnumMap<ZenithCategory, Boolean> enabled =
            new EnumMap<>(ZenithCategory.class);

    public ZenithProgressionState() {
        for (ZenithCategory category : ZenithCategory.values()) {
            enabled.put(category, false);
        }
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (ZenithCategory category : ZenithCategory.values()) {
            tag.putBoolean(category.id(), isEnabled(category));
        }
        return tag;
    }

    public static ZenithProgressionState load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        ZenithProgressionState state = new ZenithProgressionState();

        for (ZenithCategory category : ZenithCategory.values()) {
            if (tag.contains(category.id())) {
                state.enabled.put(category, tag.getBoolean(category.id()));
            }
        }

        return state;
    }

    public static ZenithProgressionState get(MinecraftServer server) {
        // Uses the overworld data storage so the state is server/world-wide.
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(
                        ZenithProgressionState::new,
                        ZenithProgressionState::load,
                        null
                ),
                DATA_NAME
        );
    }
}
