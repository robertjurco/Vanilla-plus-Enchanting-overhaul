package net.jurcorobert.vanilla_plus_enchanting.common.menu;


import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnchantingMenuSlot extends Slot {
    public EnchantingMenuSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getSlotIndex(), stack);
    }
}