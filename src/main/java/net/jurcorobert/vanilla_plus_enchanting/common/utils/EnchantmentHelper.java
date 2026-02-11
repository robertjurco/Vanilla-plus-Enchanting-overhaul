package net.jurcorobert.vanilla_plus_enchanting.common.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        ItemEnchantments enchants;

        if (stack.is(Items.ENCHANTED_BOOK)) {
            enchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
        } else {
            enchants = stack.get(DataComponents.ENCHANTMENTS);
        }

        return enchants != null ? new ArrayList<>(enchants.entrySet()) : List.of();
    }

    public static ItemStack getBookFromItem(ItemStack item) {
        ItemEnchantments enchantments = item.get(DataComponents.ENCHANTMENTS);

        if (enchantments == null || enchantments.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

        // Write enchantments into the book
        book.set(DataComponents.STORED_ENCHANTMENTS, enchantments);

        return book;
    }

    public static List<Holder<Enchantment>> getPossibleEnchantments(ItemStack stack) {
        if (stack.isEmpty())
            return List.of();

        Registry<Enchantment> registry = ModConstants.SERVER.overworld().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        List<Holder<Enchantment>> result = new ArrayList<>();

        for (Map.Entry<ResourceKey<Enchantment>, Enchantment> entry : registry.entrySet()) {
            Enchantment enchantment = entry.getValue();
            Holder<Enchantment> holder = registry.wrapAsHolder(enchantment);

            // BOOKS: all enchantments allowed
            if (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK)) {
                result.add(holder);
                continue;
            }

            // NORMAL ITEMS: check supported items
            HolderSet<Item> supported = enchantment.definition().supportedItems();
            if (stack.is(supported)) {
                result.add(holder);
            }
        }

        return result;
    }
}
