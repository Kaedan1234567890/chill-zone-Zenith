package com.chillzone.zenith.item;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbilitySwordItem extends Item {
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private final ZenithAbility ability;
    private final ZenithCategory category;
    private final boolean uniqueBoss;

    public AbilitySwordItem(
            Properties properties,
            ZenithAbility ability,
            ZenithCategory category,
            boolean uniqueBoss
    ) {
        super(properties);
        this.ability = ability;
        this.category = category;
        this.uniqueBoss = uniqueBoss;
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ZenithProgressionState state = ZenithProgressionState.get(serverLevel.getServer());

        if (!state.isEnabled(this.category)) {
            user.sendSystemMessage(
                    Component.literal("This Zenith branch is currently deactivated.")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.FAIL;
        }

        String cooldownKey = user.getUUID() + ":" + this.ability.name();
        long now = serverLevel.getGameTime();
        long readyAt = COOLDOWNS.getOrDefault(cooldownKey, 0L);

        if (now < readyAt) {
            long seconds = Math.max(1L, (readyAt - now + 19L) / 20L);
            user.sendSystemMessage(
                    Component.literal("Ability ready in " + seconds + "s")
                            .withStyle(ChatFormatting.GRAY)
            );
            return InteractionResult.FAIL;
        }

        activate(serverLevel, user);
        COOLDOWNS.put(cooldownKey, now + this.ability.cooldownTicks());

        user.sendSystemMessage(
                Component.literal(abilityDisplayName() + " activated!")
                        .withStyle(ChatFormatting.AQUA)
        );

        return InteractionResult.SUCCESS;
    }

    private void activate(ServerLevel level, Player user) {
        switch (this.ability) {
            case ENDER_STEP -> teleportForward(level, user, 8.0);
            case SHULKER_SHOT -> {
                LivingEntity target = targetInFront(level, user, 18.0);
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
                    damage(level, user, target, 5.0F);
                }
                portalTrail(level, user, 12.0);
            }
            case DRAGON_WARP -> {
                teleportForward(level, user, 12.0);
                blast(level, user, 4.5, 10.0F, 1.4);
                portalBurst(level, user.position(), 45);
            }

            case LAST_STAND -> {
                if (user.getHealth() <= 8.0F) {
                    user.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 1));
                    user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1));
                } else {
                    user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0));
                }
            }
            case VEX_CALL -> {
                // Prototype: spectral "assist" represented by temporary combat buffs.
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 0));
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 0));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        25, 1.2, 1.0, 1.2, 0.03);
            }
            case RAVAGER_CHARGE -> {
                Vec3 look = horizontalLook(user).scale(1.8);
                user.setDeltaMovement(look.x, 0.35, look.z);
                user.hurtMarked = true;
                blast(level, user, 3.0, 12.0F, 2.2);
            }

            case GUARDIAN_RAY -> {
                LivingEntity target = targetInFront(level, user, 22.0);
                if (target != null) damage(level, user, target, 9.0F);
                prismarineTrail(level, user, 20.0);
            }
            case TIDAL_BURST -> {
                blast(level, user, 5.0, 7.0F, 2.0);
                level.sendParticles(ParticleTypes.SPLASH,
                        user.getX(), user.getY() + 0.5, user.getZ(),
                        60, 2.5, 1.0, 2.5, 0.12);
            }
            case ELDER_CURSE -> {
                for (LivingEntity target : nearby(level, user, 10.0)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 180, 1));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1));
                }
            }
            case WRATH_OF_MONUMENT -> {
                LivingEntity target = targetInFront(level, user, 26.0);
                if (target != null) {
                    damage(level, user, target, 15.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1));
                }
                blast(level, user, 5.0, 6.0F, 1.7);
                prismarineTrail(level, user, 24.0);
            }

            case ECHO_SENSE -> {
                for (LivingEntity target : nearby(level, user, 18.0)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        35, 2.0, 1.0, 2.0, 0.02);
            }
            case SONIC_BOOM -> {
                LivingEntity target = targetInFront(level, user, 24.0);
                if (target != null) damage(level, user, target, 13.0F);
                sonicTrail(level, user, 22.0);
            }
            case SONIC_DEVASTATION -> {
                for (LivingEntity target : coneTargets(level, user, 16.0, 0.45)) {
                    damage(level, user, target, 17.0F);
                    Vec3 push = target.position().subtract(user.position()).normalize().scale(1.5);
                    target.push(push.x, 0.45, push.z);
                }
                sonicTrail(level, user, 16.0);
            }

            case INFERNO -> {
                for (LivingEntity target : nearby(level, user, 5.0)) {
                    damage(level, user, target, 7.0F);
                    target.igniteForSeconds(5.0F);
                }
                level.sendParticles(ParticleTypes.FLAME,
                        user.getX(), user.getY() + 0.5, user.getZ(),
                        55, 2.2, 1.0, 2.2, 0.06);
            }
            case GOLDEN_RUSH -> {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 1));
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 240, 1));
            }
            case GHAST_FIREBALL -> {
                // Prototype: ray-hit version; terrain is never damaged.
                LivingEntity target = targetInFront(level, user, 28.0);
                if (target != null) {
                    damage(level, user, target, 11.0F);
                    target.igniteForSeconds(3.0F);
                }
                level.sendParticles(ParticleTypes.FLAME,
                        user.getX(), user.getEyeY(), user.getZ(),
                        20, 0.3, 0.3, 0.3, 0.04);
            }
            case WITHERING_BARRAGE -> {
                List<LivingEntity> targets = coneTargets(level, user, 22.0, 0.72);
                int hit = 0;
                for (LivingEntity target : targets) {
                    damage(level, user, target, 7.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    if (++hit >= 3) break;
                }
                level.sendParticles(ParticleTypes.SOUL,
                        user.getX(), user.getEyeY(), user.getZ(),
                        35, 1.0, 0.8, 1.0, 0.04);
            }

            case ZENITH_STORM -> {
                // Prototype spectral-blade storm: five successive damage lanes.
                double[] offsets = {-1.2, -0.6, 0.0, 0.6, 1.2};
                for (double offset : offsets) {
                    zenithLane(level, user, offset);
                }
                level.sendParticles(ParticleTypes.END_ROD,
                        user.getX(), user.getEyeY(), user.getZ(),
                        70, 1.4, 1.0, 1.4, 0.1);
            }
        }
    }

    /*
     * Vanilla calls Item#onDestroyed when a dropped ItemEntity is actually destroyed
     * (for example by fire/explosion). Pickup, storage, Ender Chests and player logout
     * are NOT destruction, so they do not unlock the unique recipe.
     */
    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        if (!this.uniqueBoss) return;
        if (!(itemEntity.level() instanceof ServerLevel serverLevel)) return;

        ZenithProgressionState.get(serverLevel.getServer()).resetBossCrafted(this.category);
    }

    private void damage(ServerLevel level, Player attacker, LivingEntity target, float amount) {
        target.hurtServer(level, attacker.damageSources().playerAttack(attacker), amount);
    }

    private List<LivingEntity> nearby(ServerLevel level, Player user, double radius) {
        AABB box = user.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity != user && entity.isAlive()
        );
    }

    private List<LivingEntity> coneTargets(
            ServerLevel level,
            Player user,
            double range,
            double minDot
    ) {
        Vec3 origin = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        return level.getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(range),
                entity -> {
                    if (entity == user || !entity.isAlive()) return false;

                    Vec3 to = entity.getEyePosition().subtract(origin);
                    double distance = to.length();

                    if (distance <= 0.01 || distance > range) return false;
                    return to.normalize().dot(look) >= minDot;
                }
        );
    }

    private LivingEntity targetInFront(ServerLevel level, Player user, double range) {
        Vec3 origin = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(range),
                candidate -> candidate != user && candidate.isAlive()
        )) {
            Vec3 to = entity.getEyePosition().subtract(origin);
            double distance = to.length();

            if (distance <= 0.01 || distance > range) continue;

            double dot = to.normalize().dot(look);
            if (dot < 0.94) continue;

            double score = distance - (dot * 2.0);
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private void teleportForward(ServerLevel level, Player user, double distance) {
        Vec3 look = horizontalLook(user);
        Vec3 destination = user.position().add(look.scale(distance));

        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(
                    level,
                    destination.x,
                    destination.y,
                    destination.z,
                    Set.of(),
                    user.getYRot(),
                    user.getXRot(),
                    false
            );
        } else {
            user.setPos(destination);
        }

        portalBurst(level, destination, 25);
    }

    private Vec3 horizontalLook(Player user) {
        Vec3 look = user.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z);

        if (horizontal.lengthSqr() < 0.0001) {
            return new Vec3(0, 0, 1);
        }

        return horizontal.normalize();
    }

    private void blast(
            ServerLevel level,
            Player user,
            double radius,
            float damage,
            double knockback
    ) {
        for (LivingEntity target : nearby(level, user, radius)) {
            damage(level, user, target, damage);

            Vec3 push = target.position()
                    .subtract(user.position())
                    .normalize()
                    .scale(knockback);

            target.push(push.x, 0.35, push.z);
        }
    }

    private void portalBurst(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x,
                pos.y + 1.0,
                pos.z,
                count,
                1.0,
                1.0,
                1.0,
                0.12
        );
    }

    private void portalTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.PORTAL);
    }

    private void prismarineTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.END_ROD);
    }

    private void sonicTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.SONIC_BOOM);
    }

    private void trail(
            ServerLevel level,
            Player user,
            double range,
            net.minecraft.core.particles.ParticleOptions particle
    ) {
        Vec3 start = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        for (double d = 1.0; d <= range; d += 1.5) {
            Vec3 p = start.add(look.scale(d));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private void zenithLane(ServerLevel level, Player user, double sideways) {
        Vec3 look = user.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize();
        Vec3 origin = user.getEyePosition().add(side.scale(sideways));

        for (double d = 2.0; d <= 28.0; d += 2.0) {
            Vec3 p = origin.add(look.scale(d));

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    2,
                    0.1, 0.1, 0.1,
                    0.01
            );

            AABB hitBox = new AABB(
                    p.x - 0.8, p.y - 0.8, p.z - 0.8,
                    p.x + 0.8, p.y + 0.8, p.z + 0.8
            );

            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class,
                    hitBox,
                    entity -> entity != user && entity.isAlive()
            )) {
                damage(level, user, target, 10.0F);
            }
        }
    }

    private String abilityDisplayName() {
        return switch (this.ability) {
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
}
