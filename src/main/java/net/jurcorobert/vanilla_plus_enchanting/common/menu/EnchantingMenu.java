package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import net.jurcorobert.vanilla_plus_enchanting.common.network.SyncEnchantingStatePayload;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModMenus;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

public class EnchantingMenu extends AbstractContainerMenu {

    private final Level level;
    private final BlockPos pos;
    private final Inventory inv;
    private final EnchantingMenuData tableData; // null on client
    private final ContainerData data = new SimpleContainerData(0);

    // ---- Constructors ---- //

    // Client-side constructor
    public EnchantingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    // Server-side constructor
    public EnchantingMenu(int id, Inventory inv, BlockPos pos) {
        super(ModMenus.ENCHANTING_MENU.get(), id);
        this.level = inv.player.level();
        this.pos = pos;
        this.inv = inv;

        this.tableData = !level.isClientSide() ? EnchantingMenuManager.get(level, pos) : null;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Setup enchanting table slots
        Container tableInv;
        if (level.isClientSide()) {
            tableInv = new DummySimpleContainer(6);
        } else {
            assert tableData != null;
            tableInv = tableData.container;
        }


        addSlot(new EnchantingMenuSlot(this, tableInv, 0, 123, 18));
        addSlot(new EnchantingMenuSlot(this, tableInv, 1, 65, 18));
        for (int i = 0; i < 3; i++) addSlot(new EnchantingMenuSlot(this, tableInv, i+2, 47 + i * 18, 53));
        addSlot(new EnchantingMenuSlot(this, tableInv, 5, 123, 53));

        addDataSlots(data);

        sendTableState();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (level.isClientSide()) return;
        if (tableData == null) return;

        // Drop all items from the enchanting table inventory
        for (int i = 0; i < tableData.container.getContainerSize(); i++) {
            ItemStack stack = tableData.container.getItem(i);
            if (stack.isEmpty()) continue;

            // Try to put back into player's inventory
            boolean remaining = player.getInventory().add(stack); // returns leftover that didn't fit
            if (!remaining) {
                // Inventory full → drop in world
                player.drop(stack, false);
            }

            // Clear the slot
            tableData.container.setItem(i, ItemStack.EMPTY);
        }
    }


    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return inv.player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnchantingMenuData getTableData() {
        return tableData;
    }


    // ---- Dummy handler for client-side menu ---- //

    private static class DummySimpleContainer extends SimpleContainer {
        public DummySimpleContainer(int size) {
            super(6); // same 6 slots as server
            for (int i = 0; i < 6; i++) setItem(i, ItemStack.EMPTY);
        }

        @Override
        public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.isDamageableItem();
                case 1 -> stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
                case 2,3,4 -> stack.is(ModTags.Items.CUSTOM_ENCHANTING_INGREDIENTS);
                case 5 -> stack.getItem() instanceof DyeItem;
                default -> false;
            };
        }
    }

    // ---- Server-side enchant action ---- //

    public void performEnchant(ServerPlayer player) {
        if (player.level().isClientSide() || !stillValid(player)) return;
        if (tableData == null) return;

        //int cost = tableData.expCost();
        //if (player.experienceLevel < cost) return;

        //player.giveExperienceLevels(-cost);
        tableData.craftItemServer();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, Blocks.ENCHANTING_TABLE);
    }

    // ---- Quick-move logic for shift-clicking ---- //

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 6;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack,
                    TE_INVENTORY_FIRST_SLOT_INDEX,
                    TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT,
                    false)) return ItemStack.EMPTY;
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack,
                    VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT,
                    false)) return ItemStack.EMPTY;
        }

        sourceSlot.set(sourceStack.isEmpty() ? ItemStack.EMPTY : sourceStack);
        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    // ---- Player inventory setup ---- //

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    // ---- Menu state ---- //

    public void sendTableState() {
        if (!level.isClientSide() && tableData != null) {

            // Send updated state to all players viewing this table
            EnchantingMenuState state = tableData.getState();
            System.out.println("sending state");

            level.getServer().getPlayerList().getPlayers().forEach(player -> {
                if (player.containerMenu instanceof EnchantingMenu) {
                    player.connection.send(new SyncEnchantingStatePayload(state));
                }
            });
        }
    }
}
