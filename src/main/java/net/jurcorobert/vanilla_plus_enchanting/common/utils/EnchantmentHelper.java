package net.jurcorobert.vanilla_plus_enchanting.common.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentHelper {

    public static ItemStack getBookFromEnchantment(RegistryAccess registryAccess, String enchantmentId, int enchantmentLevel) {
        // Get the Enchantment
        Holder<Enchantment> enchantment = getEnchantmentHolder(registryAccess, enchantmentId);

        // Create an enchanted book
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

        // Apply the enchantment
        book.enchant(enchantment, enchantmentLevel);

        return book;
    }

    public static Holder<Enchantment> getEnchantmentHolder(RegistryAccess registryAccess, String enchantmentId) {
        Registry<Enchantment> registry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        Identifier id = Identifier.parse(enchantmentId);

        return registry.get(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown enchantment: " + enchantmentId)
                );
    }

    public static List<Object2IntMap.Entry<Holder<Enchantment>>> getItemEnchantments(ItemStack stack) {
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        return enchants != null ? new ArrayList<>(enchants.entrySet()) : List.of();
    }

    public static List<Holder<Enchantment>> getApplicableEnchantments(ItemStack stack) {
        List<Holder<Enchantment>> result = new ArrayList<>();

        return result;
    }
}
