package net.jurcorobert.vanilla_plus_enchanting.common.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

public class SeedHelper {
    @Unique
    public static long computeSeed(ItemStack stack1, ItemStack stack2) {
        long seed = 1L;
        seed = 31 * seed + stack1.getItem().hashCode();
        seed = 31 * seed + stack2.getItem().hashCode();
        seed = 31 * seed + stack1.getDamageValue();
        seed = 31 * seed + stack2.getDamageValue();
        seed = 31 * seed + EnchantingPower.get(stack1);
        seed = 31 * seed + EnchantingPower.get(stack2);

        for (ItemStack stack : List.of(stack1, stack2)) {
            ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
            if (enchants != null) {
                for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
                    seed = 31 * seed + e.getKey().hashCode();
                    seed = 31 * seed + e.getIntValue();
                }
            }
        }
        return seed;
    }

    @Unique
    private static long computeSeed(ItemStack stack1) {
        return computeSeed(stack1, stack1);
    }
}
