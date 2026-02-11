package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import io.netty.buffer.ByteBuf;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Items;

public record EnchantingMenuState(
        int getMode,                          // 0 = enchant, 1 = disenchant
        float getToolBreakChance,             // chance tool breaks
        float getFailChance,                  // chance enchanting fails
        float getReducePowerMultiplier,       // power cost multiplier
        float getExtraEnchantChance,          // chance for extra enchant
        float getUpgradeLevelChance,          // chance to upgrade enchant level
        float getDisenchantEfficiency,        // efficiency when disenchanting
        boolean getCursesLocked,              // curses cannot be applied/removed
        boolean getExistingEnchantsLocked,    // existing enchants are protected

        boolean hasTooMuchSugar,              // ingredient limit exceeded
        boolean hasTooMuchGlowstoneDust,
        boolean hasTooMuchDiamondDust,
        boolean hasTooMuchAmethystDust,
        boolean hasApplicableEnchantments,
        boolean hasNonCurseEnchantments,

        boolean canEnchant,
        boolean canDisenchant
) {
    // Static method returning default instance
    public static EnchantingMenuState defaults() {
        return new EnchantingMenuState( //
                0,      // getMode
                0.0f,   // getToolBreakChance
                0.0f,   // getFailChance
                1.0f,   // getReducePowerMultiplier
                0.0f,   // getExtraEnchantChance
                0.0f,   // getUpgradeLevelChance
                1.0f,   // getDisenchantEfficiency
                false,  // getCursesLocked
                false,  // getExistingEnchantsLocked

                false,   // hasTooMuchSugar
                false,   // hasTooMuchGlowstoneDust
                false,   // hasTooMuchDiamondDust
                false,   // hasTooMuchAmethystDust
                false,   // hasApplicableEnchantments
                false,   // hasNonCurseEnchantments

                false,  // canEnchant
                false   // canDisenchant
        );
    }

    public static final StreamCodec<ByteBuf, EnchantingMenuState> STREAM_CODEC =
            StreamCodec.of(
                    (buf, state) -> {
                        buf.writeInt(state.getMode());
                        buf.writeFloat(state.getToolBreakChance());
                        buf.writeFloat(state.getFailChance());
                        buf.writeFloat(state.getReducePowerMultiplier());
                        buf.writeFloat(state.getExtraEnchantChance());
                        buf.writeFloat(state.getUpgradeLevelChance());
                        buf.writeFloat(state.getDisenchantEfficiency());
                        buf.writeBoolean(state.getCursesLocked());
                        buf.writeBoolean(state.getExistingEnchantsLocked());

                        buf.writeBoolean(state.hasTooMuchSugar());
                        buf.writeBoolean(state.hasTooMuchGlowstoneDust());
                        buf.writeBoolean(state.hasTooMuchDiamondDust());
                        buf.writeBoolean(state.hasTooMuchAmethystDust());
                        buf.writeBoolean(state.hasApplicableEnchantments());
                        buf.writeBoolean(state.hasNonCurseEnchantments());

                        buf.writeBoolean(state.canEnchant());
                        buf.writeBoolean(state.canDisenchant());
                    },
                    buf -> new EnchantingMenuState(
                            buf.readInt(),     // getMode
                            buf.readFloat(),   // getToolBreakChance
                            buf.readFloat(),   // getFailChance
                            buf.readFloat(),   // getReducePowerMultiplier
                            buf.readFloat(),   // getExtraEnchantChance
                            buf.readFloat(),   // getUpgradeLevelChance
                            buf.readFloat(),   // getDisenchantEfficiency
                            buf.readBoolean(), // getCursesLocked
                            buf.readBoolean(), // getExistingEnchantsLocked

                            buf.readBoolean(), // hasTooMuchSugar
                            buf.readBoolean(), // hasTooMuchGlowstoneDust
                            buf.readBoolean(), // hasTooMuchDiamondDust
                            buf.readBoolean(),  // hasTooMuchAmethystDust
                            buf.readBoolean(),  // hasApplicableEnchantments
                            buf.readBoolean(),  // hasNonCurseEnchantments

                            buf.readBoolean(), // canEnchant
                            buf.readBoolean()  // canDisenchant
                    )
            );
}