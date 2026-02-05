package net.jurcorobert.vanilla_plus_enchanting.constants;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> CUSTOM_ENCHANTING_INGREDIENTS = createTag("custom_enchanting_ingredients");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, name));
        }
    }
}