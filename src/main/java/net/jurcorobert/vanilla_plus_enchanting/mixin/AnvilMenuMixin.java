package net.jurcorobert.vanilla_plus_enchanting.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.common.utils.EnchantmentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Unique
    private ItemStack cachedResult = ItemStack.EMPTY;
    @Unique
    private int cachedCost = 0;
    @Unique
    private int lastLeftHash = 0;
    @Unique
    private int lastRightHash = 0;

    @Shadow
    private String itemName;

    @Shadow
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        ItemCombinerMenu menu = (ItemCombinerMenu) (Object) this;

        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();

        if (left.isEmpty() || right.isEmpty()) return;

        // Block enchanted books completely
        if (left.is(Items.ENCHANTED_BOOK) || right.is(Items.ENCHANTED_BOOK)) {
            resetResult(menu);
            ci.cancel();
            return;
        }

        // Handle repair with materials first
        if (tryRepairWithMaterial(left, right, menu)) {
            ci.cancel();
            return;
        }

        // Handle same-item combination with enchantments
        if (left.getItem() == right.getItem()) {
            handleItemCombination(left, right, menu);
            ci.cancel();
        }
    }

    // ============================
    // ====== REPAIR LOGIC =========
    // ============================
    private boolean tryRepairWithMaterial(ItemStack left, ItemStack right, ItemCombinerMenu menu) {
        if (!(left.isDamageableItem() && left.isValidRepairItem(right))) return false;

        int singleRepair = left.getMaxDamage() / 4; // 25% per material
        int neededMaterials = (int) Math.ceil((double) left.getDamageValue() / singleRepair);
        int materialsUsed = Math.min(neededMaterials, right.getCount());
        int repairAmount = materialsUsed * singleRepair;

        if (materialsUsed <= 0) return false; // nothing to repair
        ((AnvilMenu)(Object)menu).repairItemCountCost = materialsUsed; // use vanilla field

        // Apply durability repair
        ItemStack result = left.copy();
        result.setDamageValue(Math.max(0, left.getDamageValue() - repairAmount));

        // Calculate XP cost
        int costValue = calculateEnchantPowerCost(left);

        cachedResult = result;
        cachedCost = costValue;

        // Set the result, but DO NOT shrink here
        menu.getSlot(2).set(cachedResult.copy());
        cost.set(cachedCost);

        applyRename(menu);

        return true;
    }

    private int calculateEnchantPowerCost(ItemStack stack) {
        int enchantPower = EnchantingPowerManager.getEPofEnchantsOnItem(stack);
        return Math.max(1, (int)(enchantPower * 0.3f));
    }

    // ============================
    // ====== COMBINE ITEMS =======
    // ============================
    private void handleItemCombination(ItemStack left, ItemStack right, ItemCombinerMenu menu) {
        if (!inputsChanged(left, right)) return;

        Random random = new Random(computeSeed(left, right));

        int power = rollHighestPower(left, random, 3);

        ItemStack result = left.copy();
        ItemEnchantments.Mutable resultEnchants = new ItemEnchantments.Mutable(result.get(DataComponents.ENCHANTMENTS));

        List<Object2IntMap.Entry<Holder<Enchantment>>> leftEnchants = EnchantmentHelper.getItemEnchantments(left);
        List<Object2IntMap.Entry<Holder<Enchantment>>> rightEnchants = EnchantmentHelper.getItemEnchantments(right);

        interleaveApplyEnchants(leftEnchants, rightEnchants, resultEnchants, random, power);

        result.set(DataComponents.ENCHANTMENTS, resultEnchants.toImmutable());

        combineDurability(left, right, result);

        EnchantingPower.set(result, power);

        // Calculate XP cost
        int costValue = calculateEnchantPowerCost(left);

        cachedResult = result;
        cachedCost = costValue;

        applyRename(menu);
    }

    private boolean inputsChanged(ItemStack left, ItemStack right) {
        int leftHash = ItemStack.hashItemAndComponents(left);
        int rightHash = ItemStack.hashItemAndComponents(right);

        if (leftHash != lastLeftHash || rightHash != lastRightHash) {
            lastLeftHash = leftHash;
            lastRightHash = rightHash;
            return true;
        }
        return false;
    }

    private int rollHighestPower(ItemStack stack, Random random, int rolls) {
        int highest = EnchantingPowerManager.getRandomPowerCrafted(stack, random);
        for (int i = 1; i < rolls; i++) {
            int power = EnchantingPowerManager.getRandomPowerCrafted(stack, random);
            if (power > highest) highest = power;
        }
        return highest;
    }

    private void interleaveApplyEnchants(List<Object2IntMap.Entry<Holder<Enchantment>>> left,
                                         List<Object2IntMap.Entry<Holder<Enchantment>>> right,
                                         ItemEnchantments.Mutable resultEnchants,
                                         Random random,
                                         int availablePower) {
        int max = Math.max(left.size(), right.size());
        for (int i = 0; i < max; i++) {
            if (i < left.size()) availablePower = tryApplyEnchant(left.get(i), resultEnchants, random, availablePower);
            if (i < right.size()) availablePower = tryApplyEnchant(right.get(i), resultEnchants, random, availablePower);
        }
    }

    private int tryApplyEnchant(Object2IntMap.Entry<Holder<Enchantment>> entry,
                                ItemEnchantments.Mutable resultEnchants,
                                Random random,
                                int availablePower) {
        if (random.nextDouble() >= 0.6) return availablePower;

        Holder<Enchantment> holder = entry.getKey();
        int level = entry.getIntValue();
        int currentLevel = resultEnchants.getLevel(holder);

        int refunded = 0;
        if (currentLevel > 0) {
            refunded = EnchantingPowerManager.getEnchantPower(holder, currentLevel);
            availablePower += refunded;
        }

        int costPower = EnchantingPowerManager.getEnchantPower(holder, level);
        if (costPower <= availablePower) {
            resultEnchants.set(holder, Math.max(currentLevel, level));
            availablePower -= costPower;
        } else {
            availablePower -= refunded;
        }

        return availablePower;
    }

    private void combineDurability(ItemStack left, ItemStack right, ItemStack result) {
        if (!result.isDamageableItem()) return;

        int repairLeft = left.getMaxDamage() - left.getDamageValue();
        int repairRight = right.getMaxDamage() - right.getDamageValue();
        int bonus = (int) (0.05f * (repairLeft + repairRight));

        int repaired = Math.min(left.getMaxDamage(), repairLeft + repairRight + bonus);
        result.setDamageValue(left.getMaxDamage() - repaired);
    }

    // ============================
    // ====== RENAME LOGIC =========
    // ============================
    private void applyRename(ItemCombinerMenu menu) {
        ItemStack finalResult = cachedResult.copy();

        // Only add rename cost if the new name is different from current
        String currentName = finalResult.has(DataComponents.CUSTOM_NAME)
                ? finalResult.get(DataComponents.CUSTOM_NAME).getString()
                : "";

        if (this.itemName != null && !this.itemName.isBlank() && !this.itemName.equals(currentName)) {
            cachedCost += 1; // rename cost only once per actual change
            finalResult.set(DataComponents.CUSTOM_NAME,
                    net.minecraft.network.chat.Component.literal(this.itemName));
        } else if (this.itemName == null || this.itemName.isBlank()) {
            finalResult.remove(DataComponents.CUSTOM_NAME);
        }

        menu.getSlot(2).set(finalResult);
        cost.set(cachedCost);
    }

    private void resetResult(ItemCombinerMenu menu) {
        menu.getSlot(2).set(ItemStack.EMPTY);
        cost.set(0);
    }

    @Unique
    private static long computeSeed(ItemStack left, ItemStack right) {
        long seed = 1L;
        seed = 31 * seed + left.getItem().hashCode();
        seed = 31 * seed + right.getItem().hashCode();
        seed = 31 * seed + left.getDamageValue();
        seed = 31 * seed + right.getDamageValue();
        seed = 31 * seed + EnchantingPower.get(left);
        seed = 31 * seed + EnchantingPower.get(right);

        for (ItemStack stack : List.of(left, right)) {
            ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
            if (enchants != null) {
                for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
                    seed = 31 * seed + e.getKey().hashCode();
                    seed = 31 * seed + e.getIntValue();
                }
            }
        }
        return seed;
    }
}
