package com.chillzone.zenith.item;

public enum ZenithAbility {
    ENDER_STEP(12),
    SHULKER_SHOT(15),
    DRAGON_WARP(25),

    LAST_STAND(60),
    VEX_CALL(30),
    RAVAGER_CHARGE(20),

    GUARDIAN_RAY(10),
    TIDAL_BURST(12),
    ELDER_CURSE(20),
    WRATH_OF_MONUMENT(25),

    ECHO_SENSE(20),
    SONIC_BOOM(15),
    SONIC_DEVASTATION(30),

    INFERNO(10),
    GOLDEN_RUSH(25),
    GHAST_FIREBALL(15),
    WITHERING_BARRAGE(25),

    ZENITH_STORM(45);

    private final int cooldownSeconds;

    ZenithAbility(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int cooldownTicks() {
        return cooldownSeconds * 20;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }
}
