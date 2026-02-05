package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.components.CraftingTablePlayerComponent;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void onSlotsChangedInject(Container inventory, CallbackInfo ci) {
        CraftingMenu menu = (CraftingMenu)(Object)this;
        ItemStack output = menu.getResultSlot().getItem();
        if (output.isEmpty()) return;

        // compute deterministic output seed
        Identifier id = BuiltInRegistries.ITEM.getKey(output.getItem());
        String itemName = id.toString();
        int outputSeed = itemName.hashCode();

        // get player crafting seed
        Player player = ((CraftingMenuAccessor) menu).vpe$getPlayer();
        CraftingTablePlayerComponent comp = CraftingTablePlayerComponent.get(player);

        int playerCraftingSeed = comp.getPlayerCraftingSeed();
        int combinedSeed = outputSeed + playerCraftingSeed;

        // set preview EP
        int ep = EnchantingPowerManager.getRandomPowerCrafted(output, combinedSeed);
        EnchantingPower.set(output, ep);

        // safely reroll if needed
        comp.rerollIfNeeded();
    }
}