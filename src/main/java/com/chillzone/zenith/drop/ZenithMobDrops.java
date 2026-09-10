package com.chillzone.zenith.drop;

import com.chillzone.zenith.item.ZenithItems;
import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithGate;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Custom progression drops.
 *
 * Drops are gated by the matching /zenith activate/deactivate branch.
 * Vanilla loot is untouched; these stacks are spawned in addition to it.
 */
public final class ZenithMobDrops {
    private ZenithMobDrops() {}

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof ServerLevel level)) {
                return;
            }

            EntityType<?> type = entity.getType();

            // ENDER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.ENDER)) {
                if (type == EntityType.ENDERMAN) {
                    roll(entity, level, ZenithItems.ENDER_ESSENCE, 0.12);
                } else if (type == EntityType.SHULKER) {
                    roll(entity, level, ZenithItems.SHULKER_ESSENCE, 0.25);
                }
            }

            // RAVAGER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.RAVAGER)) {
                if (type == EntityType.RAVAGER) {
                    roll(entity, level, ZenithItems.RAVAGER_HORN, 0.35);
                    roll(entity, level, ZenithItems.RAVAGER_HEART, 0.20);
                } else if (type == EntityType.EVOKER) {
                    // Current prototype: Evokers can drop the Mansion Key.
                    // The original design calls for mansion-only/first-time logic,
                    // which can be tightened separately without blocking testing.
                    roll(entity, level, ZenithItems.MANSION_KEY, 1.00);
                }
            }

            // GUARDIAN / ELDER GUARDIAN
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.GUARDIAN)) {
                if (type == EntityType.GUARDIAN) {
                    roll(entity, level, ZenithItems.GUARDIAN_SCALE, 0.10);
                } else if (type == EntityType.ELDER_GUARDIAN) {
                    roll(entity, level, ZenithItems.ELDER_GUARDIAN_CORE, 1.00);
                }
            }

            // WARDEN
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.WARDEN)
                    && type == EntityType.WARDEN) {
                roll(entity, level, ZenithItems.WARDEN_HEART, 1.00);
            }

            // WITHER / NETHER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.WITHER)) {
                if (type == EntityType.BLAZE) {
                    roll(entity, level, ZenithItems.BLAZING_CORE, 0.15);
                } else if (type == EntityType.WITHER_SKELETON) {
                    roll(entity, level, ZenithItems.WITHERED_FRAGMENT, 0.12);
                } else if (type == EntityType.PIGLIN) {
                    roll(entity, level, ZenithItems.PIGLIN_SIGIL, 0.08);
                } else if (type == EntityType.PIGLIN_BRUTE) {
                    roll(entity, level, ZenithItems.BRUTES_EMBLEM, 1.00);
                } else if (type == EntityType.GHAST) {
                    roll(entity, level, ZenithItems.GHAST_ESSENCE, 0.15);
                }
            }
        });
    }

    private static void roll(
            LivingEntity entity,
            ServerLevel level,
            Item item,
            double chance
    ) {
        if (level.getRandom().nextDouble() < chance) {
            entity.spawnAtLocation(level, new ItemStack(item));
        }
    }
}
