package com.chillzone.zenith.command;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import com.chillzone.zenith.item.ZenithTestMode;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.Collection;
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

                    .then(Commands.literal("test")
                        .executes(ctx -> setTestModeSelf(ctx.getSource(), true))
                        .then(Commands.literal("all")
                            .executes(ctx -> setTestModeAll(ctx.getSource(), true)))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(ctx -> setTestModeTargets(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    true
                            ))))

                    .then(Commands.literal("untest")
                        .executes(ctx -> setTestModeSelf(ctx.getSource(), false))
                        .then(Commands.literal("all")
                            .executes(ctx -> setTestModeAll(ctx.getSource(), false)))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(ctx -> setTestModeTargets(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    false
                            ))))

                    .then(Commands.literal("activate")
                        .then(categoryArgument(true)))

                    .then(Commands.literal("deactivate")
                        .then(categoryArgument(false)))

                    .then(Commands.literal("activateall")
                        .executes(ctx -> setAll(ctx.getSource(), true)))

                    .then(Commands.literal("deactivateall")
                        .executes(ctx -> setAll(ctx.getSource(), false)))

                    /*
                     * Admin testing command.
                     * Examples:
                     * /zenith give @s ender_essence
                     * /zenith give Kaedan ender_blade
                     * /zenith give @a ender_crafting_table
                     */
                    .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("item", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String id : zenithItemIds()) {
                                        builder.suggest(id);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> giveTestingItem(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    StringArgumentType.getString(ctx, "item"),
                                    1
                                ))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                    .executes(ctx -> giveTestingItem(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        StringArgumentType.getString(ctx, "item"),
                                        IntegerArgumentType.getInteger(ctx, "count")
                                    ))))))
                    .then(Commands.literal("detect")
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> detectBossBlade(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category")
                            ))))

                    .then(Commands.literal("resetboss")
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> resetBoss(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category")
                            ))))
            );
        });
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
    categoryArgument(boolean enabled) {
        return Commands.argument("category", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    for (ZenithCategory category : ZenithCategory.values()) {
                        builder.suggest(category.id());
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> setCategory(
                        ctx.getSource(),
                        StringArgumentType.getString(ctx, "category"),
                        enabled
                ));
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

    private static int giveTestingItem(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            String shortId,
            int amount
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, shortId);

        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            source.sendFailure(Component.literal("[Zenith] Unknown custom item: " + shortId));
            return 0;
        }

        Item item = BuiltInRegistries.ITEM.getValue(id);
        int count = 0;

        for (ServerPlayer player : targets) {
            ItemStack stack = new ItemStack(item, amount);

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }

            count++;
        }

        int finalCount = count;
        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Gave " + amount + "x " + shortId + " to " + finalCount + " player(s)."
                ),
                true
        );

        return count;
    }


    private static int detectBossBlade(CommandSourceStack source, String rawCategory) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("[Zenith] Unknown category: " + rawCategory));
            return 0;
        }

        String bossPath = switch (category) {
            case ENDER -> "ender_dragon_blade";
            case RAVAGER -> "ravager_blade";
            case GUARDIAN -> "elder_guardian_blade";
            case WARDEN -> "warden_blade";
            case WITHER -> "wither_blade";
            case ZENITH -> "zenith_blade";
        };

        Identifier bossId = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, bossPath);
        int visibleCopies = 0;

        source.sendSuccess(
                () -> Component.literal("----- Zenith Detect: " + category.id() + " -----"),
                false
        );

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            int inv = countInContainer(player.getInventory(), bossId);
            int ender = countInContainer(player.getEnderChestInventory(), bossId);

            if (inv > 0) {
                visibleCopies += inv;
                int found = inv;
                source.sendSuccess(
                        () -> Component.literal(
                                "FOUND " + found + "x in "
                                        + player.getGameProfile().name()
                                        + "'s inventory"
                        ),
                        false
                );
            }

            if (ender > 0) {
                visibleCopies += ender;
                int found = ender;
                source.sendSuccess(
                        () -> Component.literal(
                                "FOUND " + found + "x in "
                                        + player.getGameProfile().name()
                                        + "'s Ender Chest"
                        ),
                        false
                );
            }
        }

        for (var level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (!(entity instanceof ItemEntity dropped)) {
                    continue;
                }

                if (stackIs(dropped.getItem(), bossId)) {
                    int found = dropped.getItem().getCount();
                    visibleCopies += found;

                    source.sendSuccess(
                            () -> Component.literal(
                                    "FOUND " + found + "x dropped at "
                                            + dropped.blockPosition().toShortString()
                            ),
                            false
                    );
                }
            }
        }

        ZenithProgressionState state =
                ZenithProgressionState.get(source.getServer());

        int finalVisibleCopies = visibleCopies;

        if (visibleCopies == 0) {
            source.sendSuccess(
                    () -> Component.literal(
                            "No visible copy found in ONLINE player inventories, "
                                    + "ONLINE Ender Chests, or loaded dropped items."
                    ),
                    false
            );
        } else {
            source.sendSuccess(
                    () -> Component.literal(
                            "Visible copies found: " + finalVisibleCopies
                    ),
                    false
            );
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Craft lock: "
                                + (state.isBossCrafted(category) ? "LOCKED" : "AVAILABLE")
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Note: this does NOT scan offline players, chests, barrels, "
                                + "shulker boxes, or unloaded chunks."
                ),
                false
        );

        return visibleCopies > 0 ? 1 : 0;
    }

    private static int countInContainer(Container container, Identifier itemId) {
        int count = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stackIs(stack, itemId)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static boolean stackIs(ItemStack stack, Identifier itemId) {
        if (stack == null || stack.isEmpty()) return false;

        Identifier actual = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId.equals(actual);
    }

    private static int setCategory(
            CommandSourceStack source,
            String rawCategory,
            boolean enabled
    ) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("Unknown Zenith category: " + rawCategory));
            return 0;
        }

        ZenithProgressionState.get(source.getServer()).setEnabled(category, enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal("[Zenith] " + category.id() + " " + action),
                true
        );

        return 1;
    }

    private static int setAll(CommandSourceStack source, boolean enabled) {
        ZenithProgressionState.get(source.getServer()).setAll(enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal("[Zenith] ALL CATEGORIES " + action),
                true
        );

        return 1;
    }

    private static int resetBoss(CommandSourceStack source, String rawCategory) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("Unknown Zenith category: " + rawCategory));
            return 0;
        }

        ZenithProgressionState.get(source.getServer()).resetBossCrafted(category);
        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Reset unique crafting flag for " + category.id()
                ),
                true
        );

        return 1;
    }



    private static int setTestModeSelf(
            CommandSourceStack source,
            boolean enabled
    ) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(
                    Component.literal(
                            "[Zenith] Console must use /zenith test <player>, "
                                    + "/zenith test all, /zenith untest <player>, "
                                    + "or /zenith untest all."
                    )
            );
            return 0;
        }

        return setTestModeTargets(
                source,
                java.util.List.of(player),
                enabled
        );
    }

    private static int setTestModeAll(
            CommandSourceStack source,
            boolean enabled
    ) {
        return setTestModeTargets(
                source,
                source.getServer().getPlayerList().getPlayers(),
                enabled
        );
    }

    private static int setTestModeTargets(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            boolean enabled
    ) {
        int changed = 0;

        for (ServerPlayer player : targets) {
            if (enabled) {
                ZenithTestMode.enable(player.getUUID());
            } else {
                ZenithTestMode.disable(player.getUUID());
            }

            player.sendSystemMessage(
                    Component.literal(
                            "[Zenith] Test mode "
                                    + (enabled
                                    ? "ENABLED - Zenith weapon cooldowns are ignored."
                                    : "DISABLED - normal Zenith cooldowns apply.")
                    )
            );

            changed++;
        }

        int finalChanged = changed;

        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Test mode "
                                + (enabled ? "enabled" : "disabled")
                                + " for "
                                + finalChanged
                                + " player(s)."
                ),
                true
        );

        return changed;
    }

    private static int showStatus(CommandSourceStack source) {
        ZenithProgressionState state = ZenithProgressionState.get(source.getServer());

        source.sendSuccess(
                () -> Component.literal("----- Zenith Progression -----"),
                false
        );

        for (ZenithCategory category : ZenithCategory.values()) {
            String onOff = state.isEnabled(category) ? "ON" : "OFF";
            String unique = state.isBossCrafted(category) ? "BOSS CRAFTED" : "boss available";

            source.sendSuccess(
                    () -> Component.literal(
                            category.id() + ": " + onOff + " | " + unique
                    ),
                    false
            );
        }

        return 1;
    }
}
