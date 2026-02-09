package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinAi.class)
public class PiglinBarterMixin {

    @Inject(method = "getBarterResponseItems", at = @At("RETURN"))
    private static void vanillaPlus$onBarter(
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        for (ItemStack stack : cir.getReturnValue()) {
            if (stack.is(Items.ENCHANTED_BOOK)){
                int power = EnchantingPowerManager.getBookEnchPowerTrade(stack);
                EnchantingPower.set(stack, power);
            }
            if (stack.isDamageableItem()) {
                int power = EnchantingPowerManager.calculateEnchantedItemPower(stack);
                EnchantingPower.set(stack, power);
            }
        }
    }
}