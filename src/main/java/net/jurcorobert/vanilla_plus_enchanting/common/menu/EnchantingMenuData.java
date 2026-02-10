package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
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

    // ---- Dust / Modifier Utilities ---- //

    private void consumeOneDust() {
        for (int slot = 2; slot <= 4; slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) container.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private int countDust(Item item) {
        int count = 0;
        for (int slot = 2; slot <= 4; slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) count++;
        }
        return count;
    }

    // ----- Getters ---- //

    public int getMode() {
        return countDust(ModItems.DIAMOND_DUST.get()) > 0 ? 1 : 0;
    }

    public float getToolBreakChance() {
        return countDust(Items.GLOWSTONE_DUST) == 1 ? 0f : 0.05f;
    }

    public float getFailChance() {
        int redstone = countDust(Items.REDSTONE);
        return redstone > 0 ? 0.15f - 0.05f * redstone : 0.15f;
    }

    private float getReducePowerMultiplier() {
        return Math.min(1f, 1f - 0.1f * countDust(ModItems.ENCHANTING_POWDER.get()));
    }

    public float getExtraEnchantChance() {
        return 0.25f * countDust(Items.GUNPOWDER);
    }

    public float getUpgradeLevelChance() {
        return 0.2f * countDust(ModItems.NETHERITE_POWDER.get());
    }

    private float getDisenchantEfficiency() {
        return Math.min(1f, 1f - 0.15f * countDust(ModItems.ECHO_POWDER.get()));
    }

    public boolean getCursesLocked() {
        return countDust(Items.SUGAR) > 0;
    }

    public boolean getExistingEnchantsLocked() {
        return countDust(ModItems.AMETHYST_POWDER.get()) > 0;
    }


    // ----- Main Logic ---- //

    private boolean canEnchant(){

        return true;
    }

    private boolean canDisenchant(){

        return true;
    }

    private void enchantItem(){

    }

    private void disenchantItem(){

    }

    public void craftItemServer()  {
        int mode = getMode();
        if (mode == 1 && canDisenchant())
            disenchantItem();
        if (mode == 0 && canEnchant())
            enchantItem();
    }


    // ----- State ---- //
    public EnchantingMenuState getState() {
        return new EnchantingMenuState(
                getMode()
        );
    }

}
