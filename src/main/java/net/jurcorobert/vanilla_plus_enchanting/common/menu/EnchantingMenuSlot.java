package net.jurcorobert.vanilla_plus_enchanting.common.menu;


import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnchantingMenuSlot extends Slot {
    private final EnchantingMenu menu;

    public EnchantingMenuSlot(EnchantingMenu menu, Container container, int index, int x, int y) {
        super(container, index, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getSlotIndex(), stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        menu.sendTableState();
    }
}