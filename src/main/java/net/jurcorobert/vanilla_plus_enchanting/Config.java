package net.jurcorobert.vanilla_plus_enchanting;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ENCHANTED_BOOK_TRADE_MULTIPLIER = BUILDER
            .comment("Enchanted book trade multiplier.")
            .defineInRange("enchantedBookTradeMultiplier", 0.8, 0, 1);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
