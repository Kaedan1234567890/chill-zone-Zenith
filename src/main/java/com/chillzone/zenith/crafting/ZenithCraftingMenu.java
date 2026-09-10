package com.chillzone.zenith.crafting;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ZenithCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final ZenithCategory category;
    private final Block tableBlock;

    private ZenithRecipeBook.RecipeDef pendingUniqueRecipe;

    public ZenithCraftingMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            ZenithCategory category,
            Block tableBlock
    ) {
        super(containerId, inventory, access);
        this.access = access;
        this.player = inventory.player;
        this.category = category;
        this.tableBlock = tableBlock;

        // Recalculate once our branch fields are initialized.
        slotsChanged(this.craftSlots);
    }

    @Override
    public boolean stillValid(Player player) {
        // Vanilla CraftingMenu checks specifically for minecraft:crafting_table.
        // That was why the custom GUI immediately closed in Fix 6.
        return stillValid(this.access, player, this.tableBlock);
    }

    @Override
    public void slotsChanged(Container container) {
        // CraftingMenu's constructor can call this before our subclass fields exist.
        if (this.category == null || this.access == null) return;

        /*
         * If a unique boss result was previously visible and the player now owns
         * that result (inventory or cursor), the craft just completed.
         */
        if (this.pendingUniqueRecipe != null && playerHasResult(this.pendingUniqueRecipe.output())) {
            ZenithProgressionState.get(this.player.getServer())
                    .markBossCrafted(this.pendingUniqueRecipe.category());
            this.pendingUniqueRecipe = null;
        }

        this.access.execute((level, blockPos) -> {
            if (!(level instanceof ServerLevel serverLevel)) return;
            if (container != this.craftSlots) return;

            ZenithRecipeBook.RecipeDef match =
                    ZenithRecipeBook.findMatch(serverLevel, this.category, this.craftSlots);

            ItemStack result = ZenithRecipeBook.makeResult(match);

            if (match != null && match.uniqueBoss()) {
                this.pendingUniqueRecipe = match;
            } else {
                this.pendingUniqueRecipe = null;
            }

            this.resultSlots.setItem(0, result);
            this.setRemoteSlot(0, result);

            if (this.player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(
                        new ClientboundContainerSetSlotPacket(
                                this.containerId,
                                this.incrementStateId(),
                                0,
                                result
                        )
                );
            }
        });
    }

    private boolean playerHasResult(String itemId) {
        if (ZenithRecipeBook.stackIs(this.getCarried(), itemId)) return true;

        Inventory inventory = this.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ZenithRecipeBook.stackIs(inventory.getItem(i), itemId)) {
                return true;
            }
        }

        return false;
    }
}
