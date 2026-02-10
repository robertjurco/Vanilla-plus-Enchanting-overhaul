package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import net.jurcorobert.vanilla_plus_enchanting.constants.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class EnchantingMenuData {

    // ---- Slots ---- //
    public final Container container = new SimpleContainer(6) {

        @Override
        public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.isDamageableItem(); // tool
                case 1 -> stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK); // book
                case 2, 3, 4 -> stack.is(ModTags.Items.CUSTOM_ENCHANTING_INGREDIENTS); // modifiers
                case 5 -> stack.getItem() instanceof DyeItem; // new slot for dye
                default -> false;
            };
        }
    };

    private static final int OUTPUT_SLOT = 0;
    private static final int DYE_SLOT = 5;

    private static final int REQUIRED_BOOKSHELVES = 16;

    private final BlockPos pos;
    private final ServerLevel level;

    public EnchantingMenuData(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    public boolean isItemValidForSlotClient(int slot, ItemStack stack) {
        return container.canPlaceItem(slot, stack);
    }


}
