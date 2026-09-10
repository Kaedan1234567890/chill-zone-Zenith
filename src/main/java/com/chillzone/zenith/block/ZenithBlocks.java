package com.chillzone.zenith.block;

import com.chillzone.zenith.ZenithMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ZenithBlocks {
    private ZenithBlocks() {}

    private static Block registerTable(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = new ZenithCraftingTableBlock(
                BlockBehaviour.Properties.of()
                        .strength(2.5F)
                        .sound(SoundType.WOOD)
                        .setId(blockKey)
        );

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        BlockItem blockItem = new BlockItem(
                block,
                new Item.Properties()
                        .useBlockDescriptionPrefix()
                        .setId(itemKey)
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static final Block ENDER_CRAFTING_TABLE = registerTable("ender_crafting_table");
    public static final Block RAVAGER_CRAFTING_TABLE = registerTable("ravager_crafting_table");
    public static final Block GUARDIAN_CRAFTING_TABLE = registerTable("guardian_crafting_table");
    public static final Block WARDEN_CRAFTING_TABLE = registerTable("warden_crafting_table");
    public static final Block WITHER_CRAFTING_TABLE = registerTable("wither_crafting_table");
    public static final Block ZENITH_CRAFTING_TABLE = registerTable("zenith_crafting_table");

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(ENDER_CRAFTING_TABLE.asItem());
            entries.accept(RAVAGER_CRAFTING_TABLE.asItem());
            entries.accept(GUARDIAN_CRAFTING_TABLE.asItem());
            entries.accept(WARDEN_CRAFTING_TABLE.asItem());
            entries.accept(WITHER_CRAFTING_TABLE.asItem());
            entries.accept(ZENITH_CRAFTING_TABLE.asItem());
        });
    }
}
