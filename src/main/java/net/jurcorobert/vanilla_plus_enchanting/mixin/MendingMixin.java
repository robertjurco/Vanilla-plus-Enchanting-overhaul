package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MendingMixin {

    @Inject(method = "modifyDurabilityToRepairFromXp", at = @At("HEAD"), cancellable = true)
    private static void overrideMending(ServerLevel level, ItemStack stack, int durabilityToRepairFromXp, CallbackInfoReturnable<Integer> cir) {

        // Get the enchantments for this item
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        RegistryAccess registryAccess = level.registryAccess(); // or level.registryAccess()
        Holder<Enchantment> mendingHolder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING);

        // Check if Mending is present
        if (enchants.getLevel(mendingHolder) > 0) {

            // Convert vanilla durability → XP count
            int xpPoints = durabilityToRepairFromXp;
            if (xpPoints <= 0) {
                cir.setReturnValue(0);
                return;
            }

            // Probability: 1 durability per 5 XP
            final float chancePerXp = 0.2f;

            int repair = 0;
            RandomSource random = level.getRandom();

            for (int i = 0; i < xpPoints; i++) {
                if (random.nextFloat() < chancePerXp) {
                    repair++;
                }
            }

            if (repair > 0) {
                stack.setDamageValue(Math.max(stack.getDamageValue() - repair, 0));
            }

            System.out.println(
                    "[MENDING OVERRIDE] item=" + stack.getItem()
                            + " vanillaDurability=" + durabilityToRepairFromXp
                            + " xpPoints=" + xpPoints
                            + " repaired=" + repair
                            + " newDamage=" + stack.getDamageValue()
            );

            cir.setReturnValue(repair); // cancel vanilla logic
        }
    }
}