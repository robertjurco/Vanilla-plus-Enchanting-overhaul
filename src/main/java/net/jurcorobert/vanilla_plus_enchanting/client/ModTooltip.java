package net.jurcorobert.vanilla_plus_enchanting.client;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ModTooltip {

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Enchantable item (with durability)
        if (stack.isDamageableItem()) {
            int power = EnchantingPower.get(stack, event.getFlags().isCreative());
            if (power < 0) return;

            int maxDurability = stack.getMaxDamage();
            int damage = stack.getDamageValue();
            int currentDurability = maxDurability - damage;

            event.getToolTip().add(Component.empty());

            event.getToolTip().add(
                    Component.literal("Enchanting Power: " + power)
                            .withStyle(ChatFormatting.BLUE)
            );

            event.getToolTip().add(
                    Component.literal(
                            "Durability: " + currentDurability + " / " + maxDurability
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        // Enchanted book
        if (stack.is(Items.ENCHANTED_BOOK)) {
            int power = EnchantingPower.get(stack, event.getFlags().isCreative());
            if (power < 0) return;

            event.getToolTip().add(Component.empty());
            event.getToolTip().add(
                    Component.literal("Enchanting Power: " + power)
                            .withStyle(ChatFormatting.BLUE)
            );
        }

        // Ingredients
        if (stack.is(ModTags.Items.CUSTOM_ENCHANTING_INGREDIENTS)) {

            if (stack.is(ModItems.DIAMOND_DUST))
                event.getToolTip().add(Component.literal("Allows disenchanting items.").withStyle(ChatFormatting.GRAY));

            if (stack.is(ModItems.ECHO_POWDER))
                event.getToolTip().add(Component.literal("Increases disenchanting efficiency.").withStyle(ChatFormatting.GRAY));

            if (stack.is(ModItems.NETHERITE_POWDER))
                event.getToolTip().add(Component.literal("Adds a chance to increase a level of enchantment.").withStyle(ChatFormatting.GRAY));

            if (stack.is(ModItems.AMETHYST_POWDER))
                event.getToolTip().add(Component.literal("Prevents replacing any enchantment on an item.").withStyle(ChatFormatting.GRAY));

            if (stack.is(ModItems.ENCHANTING_POWDER))
                event.getToolTip().add(Component.literal("Decreases the enchanting power used.").withStyle(ChatFormatting.GRAY));

            if (stack.is(Items.GUNPOWDER))
                event.getToolTip().add(Component.literal("Adds a chance for extra enchantment.").withStyle(ChatFormatting.GRAY));

            if (stack.is(Items.SUGAR))
                event.getToolTip().add(Component.literal("Prevents curses from appearing while enchanting.").withStyle(ChatFormatting.GRAY));

            if (stack.is(Items.GLOWSTONE_DUST))
                event.getToolTip().add(Component.literal("Removes chance to break item while enchanting.").withStyle(ChatFormatting.GRAY));

            if (stack.is(Items.REDSTONE))
                event.getToolTip().add(Component.literal("Decreases chance to fail during enchanting.").withStyle(ChatFormatting.GRAY));
        }
    }
}
