package com.chillzone.zenith.command;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public final class ZenithCommands {
    private ZenithCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("zenith")
                    .requires(source ->
                        source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
                    )

                    .then(Commands.literal("status")
                        .executes(ctx -> showStatus(ctx.getSource())))

                    .then(Commands.literal("activate")
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> setCategory(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category"),
                                true
                            ))))

                    .then(Commands.literal("deactivate")
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> setCategory(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category"),
                                false
                            ))))

                    .then(Commands.literal("activateall")
                        .executes(ctx -> setAll(ctx.getSource(), true)))

                    .then(Commands.literal("deactivateall")
                        .executes(ctx -> setAll(ctx.getSource(), false)))
            );
        });
    }

    private static int setCategory(
            CommandSourceStack source,
            String rawCategory,
            boolean enabled
    ) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal(
                    "Unknown Zenith category: " + rawCategory
            ));
            return 0;
        }

        ZenithProgressionState state =
                ZenithProgressionState.get(source.getServer());

        state.setEnabled(category, enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] " + category.id() + " " + action
                ),
                true
        );

        return 1;
    }

    private static int setAll(
            CommandSourceStack source,
            boolean enabled
    ) {
        ZenithProgressionState.get(source.getServer()).setAll(enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal("[Zenith] ALL CATEGORIES " + action),
                true
        );

        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ZenithProgressionState state =
                ZenithProgressionState.get(source.getServer());

        source.sendSuccess(
                () -> Component.literal("----- Zenith Progression -----"),
                false
        );

        for (ZenithCategory category : ZenithCategory.values()) {
            boolean enabled = state.isEnabled(category);
            String status = enabled ? "ON" : "OFF";

            source.sendSuccess(
                    () -> Component.literal(
                            category.id() + ": " + status
                    ),
                    false
            );
        }

        return 1;
    }
}
