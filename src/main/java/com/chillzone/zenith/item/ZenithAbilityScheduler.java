package com.chillzone.zenith.item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ZenithAbilityScheduler {
    private ZenithAbilityScheduler() {}

    private record PendingWardenBeam(UUID playerId, long fireTick) {}

    private static final List<PendingWardenBeam> WARDEN_BEAMS = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(ZenithAbilityScheduler::tick);
    }

    public static void scheduleWardenBeam(ServerPlayer player, int delayTicks) {
        long now = player.serverLevel().getGameTime();
        WARDEN_BEAMS.add(new PendingWardenBeam(player.getUUID(), now + delayTicks));
    }

    private static void tick(MinecraftServer server) {
        if (WARDEN_BEAMS.isEmpty()) return;

        Iterator<PendingWardenBeam> iterator = WARDEN_BEAMS.iterator();

        while (iterator.hasNext()) {
            PendingWardenBeam pending = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId());

            if (player == null) {
                iterator.remove();
                continue;
            }

            ServerLevel level = player.serverLevel();

            if (level.getGameTime() < pending.fireTick()) {
                continue;
            }

            fireWardenBeam(level, player);
            iterator.remove();
        }
    }

    private static void fireWardenBeam(ServerLevel level, ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(18.0),
                entity -> entity != player && entity.isAlive()
        )) {
            Vec3 to = target.getEyePosition().subtract(start);
            double distance = to.length();

            if (distance <= 0.01 || distance > 15.0) continue;

            double dot = to.normalize().dot(look);
            if (dot < 0.94) continue;

            double score = distance - (dot * 3.0);
            if (score < bestScore) {
                bestScore = score;
                best = target;
            }
        }

        for (double distance = 1.0; distance <= 15.0; distance += 1.0) {
            Vec3 point = start.add(look.scale(distance));
            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    point.x, point.y, point.z,
                    1,
                    0, 0, 0,
                    0
            );
        }

        if (best != null) {
            best.hurtServer(
                    level,
                    player.damageSources().playerAttack(player),
                    18.0F
            );

            Vec3 push = best.position().subtract(player.position()).normalize().scale(1.4);
            best.push(push.x, 0.35, push.z);
        }
    }
}
