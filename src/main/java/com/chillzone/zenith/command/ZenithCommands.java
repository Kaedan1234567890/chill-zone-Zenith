package com.chillzone.zenith.command;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

                    // Admin testing helper. Gives any Chill Zone Zenith item/block by short ID.
                    // Example: /zenith give ender_essence
                    // Example: /zenith give ender_blade
                    // Example: /zenith give ender_crafting_table
                    .then(Commands.literal("give")
                        .then(Commands.argument("item", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (String id : zenithItemIds()) {
                                    builder.suggest(id);
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> giveTestingItem(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "item")
                            ))))
            );
        });
    }

    private static List<String> zenithItemIds() {
        List<String> ids = new ArrayList<>();

        BuiltInRegistries.ITEM.keySet().forEach(id -> {
            if (ZenithMod.MOD_ID.equals(id.getNamespace())) {
                ids.add(id.getPath());
            }
        });

        ids.sort(String::compareTo);
        return ids;
    }

    private static int giveTestingItem(CommandSourceStack source, String shortId) {
        Identifier id = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, shortId);

        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            source.sendFailure(Component.literal(
                    "[Zenith] Unknown custom item: " + shortId
            ));
            return 0;
        }

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal(
                    "[Zenith] This testing command must be run by a player."
            ));
            return 0;
        }

        Item item = BuiltInRegistries.ITEM.getValue(id);
        ItemStack stack = new ItemStack(item);

        boolean inserted = player.getInventory().add(stack);

        if (!inserted) {
            player.drop(stack, false);
        }

        source.sendSuccess(
                () -> Component.literal("[Zenith] Gave 1x " + shortId),
                false
        );

        return 1;
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
