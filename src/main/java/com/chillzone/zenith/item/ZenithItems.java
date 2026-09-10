package com.chillzone.zenith.item;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.progression.ZenithCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
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

    private static Item sword(
            String id,
            String displayName,
            ChatFormatting color,
            ZenithAbility ability,
            ZenithCategory category,
            boolean uniqueBoss,
            String description
    ) {
        Item.Properties properties = new Item.Properties()
                .sword(ToolMaterial.NETHERITE, 1.0F, -2.4F)
                .component(
                        DataComponents.ITEM_NAME,
                        Component.literal(displayName).withStyle(color)
                )
                .component(
                        DataComponents.LORE,
                        new ItemLore(List.of(
                                Component.literal(description).withStyle(ChatFormatting.GRAY),
                                Component.literal("Right Click: " + abilityName(ability))
                                        .withStyle(ChatFormatting.AQUA),
                                Component.literal("Cooldown: " + ability.cooldownSeconds() + "s")
                                        .withStyle(ChatFormatting.DARK_GRAY)
                        ))
                );

        return register(
                id,
                settings -> new AbilitySwordItem(settings, ability, category, uniqueBoss),
                properties
        );
    }

    private static String abilityName(ZenithAbility ability) {
        return switch (ability) {
            case ENDER_STEP -> "Ender Step";
            case SHULKER_SHOT -> "Shulker Shot";
            case DRAGON_WARP -> "Dragon Warp";
            case LAST_STAND -> "Last Stand";
            case VEX_CALL -> "Vex Call";
            case RAVAGER_CHARGE -> "Ravager Charge";
            case GUARDIAN_RAY -> "Guardian Ray";
            case TIDAL_BURST -> "Tidal Burst";
            case ELDER_CURSE -> "Elder Curse";
            case WRATH_OF_MONUMENT -> "Wrath of the Monument";
            case ECHO_SENSE -> "Echo Sense";
            case SONIC_BOOM -> "Sonic Boom";
            case SONIC_DEVASTATION -> "Sonic Devastation";
            case INFERNO -> "Inferno";
            case GOLDEN_RUSH -> "Golden Rush";
            case GHAST_FIREBALL -> "Ghast Fireball";
            case WITHERING_BARRAGE -> "Withering Barrage";
            case ZENITH_STORM -> "Zenith Storm";
        };
    }

    // Materials
    public static final Item ENDER_ESSENCE = material("ender_essence");
    public static final Item SHULKER_ESSENCE = material("shulker_essence");
    public static final Item RAVAGER_HORN = material("ravager_horn");
    public static final Item RAVAGER_HEART = material("ravager_heart");
    public static final Item MANSION_KEY = material("mansion_key");
    public static final Item GUARDIAN_SCALE = material("guardian_scale");
    public static final Item ELDER_GUARDIAN_CORE = material("elder_guardian_core");
    public static final Item WARDEN_HEART = material("warden_heart");
    public static final Item BLAZING_CORE = material("blazing_core");
    public static final Item WITHERED_FRAGMENT = material("withered_fragment");
    public static final Item PIGLIN_SIGIL = material("piglin_sigil");
    public static final Item BRUTES_EMBLEM = material("brutes_emblem");
    public static final Item GHAST_ESSENCE = material("ghast_essence");

    // Ender
    public static final Item ENDER_BLADE = sword(
            "ender_blade", "Ender Blade", ChatFormatting.LIGHT_PURPLE,
            ZenithAbility.ENDER_STEP, ZenithCategory.ENDER, false,
            "A blade infused with unstable End energy."
    );
    public static final Item SHULKER_BLADE = sword(
            "shulker_blade", "Shulker Blade", ChatFormatting.DARK_PURPLE,
            ZenithAbility.SHULKER_SHOT, ZenithCategory.ENDER, false,
            "Shulker energy bends gravity around its target."
    );
    public static final Item ENDER_DRAGON_BLADE = sword(
            "ender_dragon_blade", "Ender Dragon Blade", ChatFormatting.DARK_PURPLE,
            ZenithAbility.DRAGON_WARP, ZenithCategory.ENDER, true,
            "Boss Blade - the completed weapon of the End."
    );

    // Ravager
    public static final Item SWORD_OF_UNDYING = sword(
            "sword_of_undying", "Sword of Undying", ChatFormatting.GOLD,
            ZenithAbility.LAST_STAND, ZenithCategory.RAVAGER, false,
            "Totem magic strengthens its wielder at the brink."
    );
    public static final Item MANSION_BLADE = sword(
            "mansion_blade", "Mansion Blade", ChatFormatting.DARK_GREEN,
            ZenithAbility.VEX_CALL, ZenithCategory.RAVAGER, false,
            "Illager magic answers the blade's command."
    );
    public static final Item RAVAGER_BLADE = sword(
            "ravager_blade", "Ravager Blade", ChatFormatting.GREEN,
            ZenithAbility.RAVAGER_CHARGE, ZenithCategory.RAVAGER, true,
            "Boss Blade - built to hit like a charging Ravager."
    );

    // Guardian
    public static final Item PRISMARINE_BLADE = sword(
            "prismarine_blade", "Prismarine Blade", ChatFormatting.AQUA,
            ZenithAbility.GUARDIAN_RAY, ZenithCategory.GUARDIAN, false,
            "Channels a focused Guardian beam."
    );
    public static final Item SPONGE_BLADE = sword(
            "sponge_blade", "Sponge Blade", ChatFormatting.YELLOW,
            ZenithAbility.TIDAL_BURST, ZenithCategory.GUARDIAN, false,
            "Releases a violent burst of stored water."
    );
    public static final Item ELDER_TIDE_BLADE = sword(
            "elder_tide_blade", "Elder Tide Blade", ChatFormatting.DARK_AQUA,
            ZenithAbility.ELDER_CURSE, ZenithCategory.GUARDIAN, false,
            "Carries the oppressive curse of an Elder Guardian."
    );
    public static final Item ELDER_GUARDIAN_BLADE = sword(
            "elder_guardian_blade", "Elder Guardian Blade", ChatFormatting.AQUA,
            ZenithAbility.WRATH_OF_MONUMENT, ZenithCategory.GUARDIAN, true,
            "Boss Blade - the monument's full power."
    );

    // Warden
    public static final Item ECHO_BLADE = sword(
            "echo_blade", "Echo Blade", ChatFormatting.AQUA,
            ZenithAbility.ECHO_SENSE, ZenithCategory.WARDEN, false,
            "Reads nearby movement through Sculk resonance."
    );
    public static final Item WARDENS_WRATH = sword(
            "wardens_wrath", "Warden's Wrath", ChatFormatting.DARK_AQUA,
            ZenithAbility.SONIC_BOOM, ZenithCategory.WARDEN, false,
            "Releases a focused sonic strike."
    );
    public static final Item WARDEN_BLADE = sword(
            "warden_blade", "Warden Blade", ChatFormatting.DARK_AQUA,
            ZenithAbility.SONIC_DEVASTATION, ZenithCategory.WARDEN, true,
            "Boss Blade - a devastating wave of sonic force."
    );

    // Wither
    public static final Item BLADE_OF_FIRE = sword(
            "blade_of_fire", "Blade of Fire", ChatFormatting.RED,
            ZenithAbility.INFERNO, ZenithCategory.WITHER, false,
            "Ignites everything caught in its inferno."
    );
    public static final Item GOLDEN_DESIRE = sword(
            "golden_desire", "Golden Desire", ChatFormatting.GOLD,
            ZenithAbility.GOLDEN_RUSH, ZenithCategory.WITHER, false,
            "Piglin greed turned into raw speed and power."
    );
    public static final Item GHOST_BLADE = sword(
            "ghost_blade", "Ghost Blade", ChatFormatting.WHITE,
            ZenithAbility.GHAST_FIREBALL, ZenithCategory.WITHER, false,
            "Ghast energy strikes without damaging terrain."
    );
    public static final Item WITHER_BLADE = sword(
            "wither_blade", "Wither Blade", ChatFormatting.DARK_GRAY,
            ZenithAbility.WITHERING_BARRAGE, ZenithCategory.WITHER, true,
            "Boss Blade - unleashes a barrage of Withering force."
    );

    // Final
    public static final Item ZENITH_BLADE = sword(
            "zenith_blade", "✦ Zenith Blade ✦", ChatFormatting.LIGHT_PURPLE,
            ZenithAbility.ZENITH_STORM, ZenithCategory.ZENITH, true,
            "The five paths united into one ultimate weapon."
    );

    public static void initialize() {
        // Static registration above does the work.
    }
}
