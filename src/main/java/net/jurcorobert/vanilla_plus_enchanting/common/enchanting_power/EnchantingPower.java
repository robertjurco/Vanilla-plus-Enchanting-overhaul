package net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power;

import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class EnchantingPower {

    public static final String KEY = "EnchantingPower";

    private static int readValue(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(KEY)) {
                return tag.getInt(KEY).orElse(-1);
            }
        }
        return -1;
    }

    public static int get(ItemStack stack, boolean isCreative) {
        int ench_power = -1;

        if (isCreative) {
            ench_power = EnchantingPowerManager.getEnchPowerCreative(stack);
        }
        else {
            ench_power = readValue(stack);
        }

        if (ench_power < 0) {
            ModConstants.LOGGER.warn("EnchantingPower.get() returned -1 (fallback)!");
            return -1;
        }

        return ench_power;
    }

    public static int get(ItemStack stack) {
        return get(stack, false);
    }

    public static void set(ItemStack stack, int value) {
        CompoundTag tag = new CompoundTag();
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            tag = existing.copyTag();
        }

        tag.putInt(KEY, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
