package com.chillzone.zenith;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ZenithJoinMessage {
    private ZenithJoinMessage() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();

            player.sendSystemMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.DARK_PURPLE));

            player.sendSystemMessage(Component.literal("✦ ZENITH PROGRESSION ✦")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            player.sendSystemMessage(Component.literal(
                    "The Zenith progression mod is installed on this server.")
                    .withStyle(ChatFormatting.GRAY));

            player.sendSystemMessage(
                    Component.literal("Recipes: ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(
                                    "/zenith crafting recipe <category> <weapon>")
                                    .withStyle(ChatFormatting.AQUA))
            );

            player.sendSystemMessage(
                    Component.literal("Each branch has multiple weapons, but only ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("ONE Boss Blade")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(
                                    " can be legitimately crafted in the world.")
                                    .withStyle(ChatFormatting.GRAY))
            );

            player.sendSystemMessage(Component.literal(
                    "The five Boss Blades are needed to eventually craft the Zenith Blade.")
                    .withStyle(ChatFormatting.GRAY));

            player.sendSystemMessage(
                    Component.literal("Need help? Use ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("/discord")
                                    .withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(
                                    " to get the server Discord link.")
                                    .withStyle(ChatFormatting.GRAY))
            );

            player.sendSystemMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        });
    }
}
