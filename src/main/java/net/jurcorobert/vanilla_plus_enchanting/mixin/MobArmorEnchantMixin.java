package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobArmorEnchantMixin {

    @Inject(method = "enchantSpawnedEquipment", at = @At("RETURN"))
    private void vanillaPlus$afterMobEnchant(
            ServerLevelAccessor level,
            EquipmentSlot slot,
            RandomSource random,
            float enchantChance,
            DifficultyInstance difficulty,
            CallbackInfo ci
    ) {
        Mob mob = (Mob)(Object)this;
        ItemStack stack = mob.getItemBySlot(slot);

        if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
            int power = EnchantingPowerManager.calculateEnchantedItemPower(stack);
            EnchantingPower.set(stack, power);
        }
    }
}

