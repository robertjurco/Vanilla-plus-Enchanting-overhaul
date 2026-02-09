package net.jurcorobert.vanilla_plus_enchanting.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class EnchantingPowerModifier extends LootModifier {

    public static final MapCodec<EnchantingPowerModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst) // handles the `conditions` field automatically
                    .apply(inst, EnchantingPowerModifier::new)
    );

    public EnchantingPowerModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (ItemStack stack : generatedLoot) {
            if (stack.is(Items.ENCHANTED_BOOK)){
                int power = EnchantingPowerManager.getBookEnchPowerTrade(stack);
                EnchantingPower.set(stack, power);
            }

            if (stack.isDamageableItem()) {
                int power = EnchantingPowerManager.calculateEnchantedItemPower(stack);
                EnchantingPower.set(stack, power);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}