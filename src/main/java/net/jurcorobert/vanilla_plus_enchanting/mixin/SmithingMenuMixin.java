package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    @Shadow
    private Level level;

    @Unique
    private RecipeHolder<SmithingRecipe> selectedRecipe;

    @Unique
    private ItemStack cachedResult = ItemStack.EMPTY;

    @Unique
    private int lastBaseHash = 0;

    @Unique
    private int craftRandomSeed = 0;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        SmithingMenu menu = (SmithingMenu)(Object)this;

        ItemStack template = menu.getSlot(0).getItem();
        ItemStack base = menu.getSlot(1).getItem();
        ItemStack addition = menu.getSlot(2).getItem();

        if (template.isEmpty() || base.isEmpty() || addition.isEmpty()) {
            menu.getSlot(3).set(ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        // Only Netherite upgrade
        if (!(template.getItem() == Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE && addition.getItem() == Items.NETHERITE_INGOT)) {
            cachedResult = ItemStack.EMPTY;
            lastBaseHash = 0;
            return;
        }

        // If new crafting attempt, generate new random seed
        if (craftRandomSeed == 0) {
            craftRandomSeed = new Random().nextInt();
        }

        // Combine base hash and random seed
        int baseHash = ItemStack.hashItemAndComponents(base) ^ craftRandomSeed;
        if (baseHash == lastBaseHash) {
            menu.getSlot(3).set(cachedResult.copy());
            ci.cancel();
            return;
        }
        lastBaseHash = baseHash;

        // Recipe lookup on server only
        if (!(level instanceof ServerLevel serverLevel)) {
            menu.getSlot(3).set(ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        var optional = serverLevel.recipeAccess().getRecipeFor(
                RecipeType.SMITHING,
                new SmithingRecipeInput(template, base, addition),
                serverLevel
        );

        if (optional.isEmpty()) {
            menu.getSlot(3).set(ItemStack.EMPTY);
            cachedResult = ItemStack.EMPTY;
            ci.cancel();
            return;
        }

        RecipeHolder<SmithingRecipe> recipeHolder = optional.get();
        ItemStack result = recipeHolder.value().assemble(
                new SmithingRecipeInput(template, base, addition),
                level.registryAccess()
        );

        // Random Enchanting Power increase
        int increase = 20 + (new Random(baseHash)).nextInt(21);
        EnchantingPower.set(result, EnchantingPower.get(result) + increase);

        cachedResult = result.copy();
        menu.getSlot(3).set(cachedResult.copy());
        selectedRecipe = recipeHolder;

        ci.cancel();
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && !cachedResult.isEmpty()) {
            // Copy the enchanting power
            EnchantingPower.set(stack, EnchantingPower.get(cachedResult));
        }

        // Reset random seed only if the craft was a Netherite upgrade
        if (selectedRecipe != null) {
            ItemStack template = ((SmithingMenu)(Object)this).getSlot(0).getItem();
            ItemStack addition = ((SmithingMenu)(Object)this).getSlot(2).getItem();

            if (template.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) && addition.is(Items.NETHERITE_INGOT)) {
                craftRandomSeed = 0;
            }
        }

        cachedResult = ItemStack.EMPTY;
        selectedRecipe = null;
    }
}
