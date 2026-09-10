package com.chillzone.zenith.item;

import com.chillzone.zenith.ZenithMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.function.Function;

public final class ZenithItems {
    private ZenithItems() {}

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, name)
        );
    }

    private static Item register(
            String name,
            Function<Item.Properties, Item> factory,
            Item.Properties properties
    ) {
        ResourceKey<Item> key = key(name);
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    private static Item material(String name) {
        return register(name, Item::new, new Item.Properties());
    }

    private static Item sword(String name) {
        return register(
                name,
                Item::new,
                new Item.Properties().sword(ToolMaterial.NETHERITE, 1.0F, -2.4F)
        );
    }

    // Ender
    public static final Item ENDER_ESSENCE = material("ender_essence");
    public static final Item SHULKER_ESSENCE = material("shulker_essence");
    public static final Item ENDER_BLADE = sword("ender_blade");
    public static final Item SHULKER_BLADE = sword("shulker_blade");
    public static final Item ENDER_DRAGON_BLADE = sword("ender_dragon_blade");

    // Ravager
    public static final Item RAVAGER_HORN = material("ravager_horn");
    public static final Item RAVAGER_HEART = material("ravager_heart");
    public static final Item MANSION_KEY = material("mansion_key");
    public static final Item SWORD_OF_UNDYING = sword("sword_of_undying");
    public static final Item MANSION_BLADE = sword("mansion_blade");
    public static final Item RAVAGER_BLADE = sword("ravager_blade");

    // Guardian
    public static final Item GUARDIAN_SCALE = material("guardian_scale");
    public static final Item ELDER_GUARDIAN_CORE = material("elder_guardian_core");
    public static final Item PRISMARINE_BLADE = sword("prismarine_blade");
    public static final Item SPONGE_BLADE = sword("sponge_blade");
    public static final Item ELDER_TIDE_BLADE = sword("elder_tide_blade");
    public static final Item ELDER_GUARDIAN_BLADE = sword("elder_guardian_blade");

    // Warden
    public static final Item WARDEN_HEART = material("warden_heart");
    public static final Item ECHO_BLADE = sword("echo_blade");
    public static final Item WARDENS_WRATH = sword("wardens_wrath");
    public static final Item WARDEN_BLADE = sword("warden_blade");

    // Wither
    public static final Item BLAZING_CORE = material("blazing_core");
    public static final Item WITHERED_FRAGMENT = material("withered_fragment");
    public static final Item PIGLIN_SIGIL = material("piglin_sigil");
    public static final Item BRUTES_EMBLEM = material("brutes_emblem");
    public static final Item GHAST_ESSENCE = material("ghast_essence");
    public static final Item BLADE_OF_FIRE = sword("blade_of_fire");
    public static final Item GOLDEN_DESIRE = sword("golden_desire");
    public static final Item GHOST_BLADE = sword("ghost_blade");
    public static final Item WITHER_BLADE = sword("wither_blade");

    // Final
    public static final Item ZENITH_BLADE = sword("zenith_blade");

    public static void initialize() {
        // Items are registered by the static fields above.
        // Creative-tab insertion is intentionally omitted in this build because
        // Fabric's item-group API changed in Minecraft 26.2.
        //
        // Use /zenith give <item> or vanilla /give for testing.
    }
}
