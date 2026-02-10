package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.common.utils.SeedHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {

    @Shadow
    private Container repairSlots;

    @Shadow
    private Container resultSlots;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void vanillaPlus$modifyResultPreview(CallbackInfo ci) {
        ItemStack top = ((Container)repairSlots).getItem(0);
        ItemStack bottom = ((Container)repairSlots).getItem(1);

        // If nothing is in the top/bottom, skip
        if (top.isEmpty() && bottom.isEmpty()) return;

        // Take the vanilla result
        ItemStack result = ((Container)resultSlots).getItem(0);
        if (result.isEmpty()) return;

        // Get random
        Random random = new Random(SeedHelper.computeSeed(top, bottom));

        // Make a copy and modify it
        ItemStack modified = result.copy();
        int power = EnchantingPowerManager.getRandomPowerCrafted(modified, random);
        EnchantingPower.set(modified, power);

        // Put it back so preview shows the modified item
        ((Container)resultSlots).setItem(0, modified);

        // call broadcastChanges via super
        ((AbstractContainerMenu)(Object)this).broadcastChanges();
    }
}