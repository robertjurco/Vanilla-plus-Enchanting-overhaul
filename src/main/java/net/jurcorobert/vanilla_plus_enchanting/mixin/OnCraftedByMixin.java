package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.components.CraftingTablePlayerComponent;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class OnCraftedByMixin {

    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraftedByInject(ItemStack stack, Player player, CallbackInfo ci) {
        if (!stack.isEnchantable() && stack.getItem() != Items.BOOK && stack.getItem() != Items.ENCHANTED_BOOK) return;

        // compute deterministic output seed
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = id.toString();
        int outputSeed = itemName.hashCode();

        // get player crafting seed
        CraftingTablePlayerComponent comp = CraftingTablePlayerComponent.get(player);

        int playerCraftingSeed = comp.getPlayerCraftingSeed();
        int combinedSeed = outputSeed + playerCraftingSeed;

        // set reulting EP
        int ep = EnchantingPowerManager.getRandomPowerCrafted(stack, combinedSeed);
        EnchantingPower.set(stack, ep);

        // Request reroll menu seed if not already rerolled
        comp.markForReroll();
    }
}