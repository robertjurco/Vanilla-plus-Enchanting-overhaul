package net.jurcorobert.vanilla_plus_enchanting.datagen;

import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> VITALITY = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "vitality")
    );

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var items = context.lookup(Registries.ITEM);

        HolderSet<Item> armor = items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE);

        register(context, VITALITY, Enchantment.enchantment(
                Enchantment.definition(
                        armor,
                        5, // weight
                        3, // max level
                        Enchantment.constantCost(10),
                        Enchantment.dynamicCost(15, 5),
                        1,
                        EquipmentSlotGroup.CHEST
                )
        ).withEffect(
                EnchantmentEffectComponents.ATTRIBUTES,
                new EnchantmentAttributeEffect(
                        Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "vitality"),
                        Attributes.MAX_HEALTH,
                        LevelBasedValue.perLevel(2.0F),
                        AttributeModifier.Operation.ADD_VALUE
                )
        ));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        registry.register(key, builder.build(key.identifier()));
    }
}