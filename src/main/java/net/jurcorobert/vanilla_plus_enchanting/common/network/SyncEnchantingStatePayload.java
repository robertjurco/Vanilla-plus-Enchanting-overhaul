package net.jurcorobert.vanilla_plus_enchanting.common.network;

import io.netty.buffer.ByteBuf;
import net.jurcorobert.vanilla_plus_enchanting.client.screen.EnchantingScreen;
import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenuState;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEnchantingStatePayload(EnchantingMenuState state) implements CustomPacketPayload {

    public static final Type<SyncEnchantingStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "sync_enchanting_state"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, SyncEnchantingStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    EnchantingMenuState.STREAM_CODEC,
                    SyncEnchantingStatePayload::state,
                    SyncEnchantingStatePayload::new
            );


    public static void encode(SyncEnchantingStatePayload payload, FriendlyByteBuf buf) {
        buf.writeInt(payload.state().getMode());
        buf.writeFloat(payload.state().getToolBreakChance());
        buf.writeFloat(payload.state().getFailChance());
        buf.writeFloat(payload.state().getReducePowerMultiplier());
        buf.writeFloat(payload.state().getExtraEnchantChance());
        buf.writeFloat(payload.state().getUpgradeLevelChance());
        buf.writeFloat(payload.state().getDisenchantEfficiency());
        buf.writeBoolean(payload.state().getCursesLocked());
        buf.writeBoolean(payload.state().getExistingEnchantsLocked());

        buf.writeBoolean(payload.state().hasTooMuchSugar());
        buf.writeBoolean(payload.state().hasTooMuchGlowstoneDust());
        buf.writeBoolean(payload.state().hasTooMuchDiamondDust());
        buf.writeBoolean(payload.state().hasTooMuchAmethystDust());

        buf.writeBoolean(payload.state().hasNonCurseEnchantments());

        buf.writeBoolean(payload.state().hasApplicableEnchants());
        buf.writeBoolean(payload.state().hasExtraEnchant());
        buf.writeBoolean(payload.state().hasUpgradableEnchant());

        buf.writeBoolean(payload.state().hasEnoughPowerToApply());
        buf.writeBoolean(payload.state().hasEnoughPowerForExtra());
        buf.writeBoolean(payload.state().hasEnoughPowerToUpgrade());

        buf.writeBoolean(payload.state().canUpgradeLevelDisenchant());
    }

    public static SyncEnchantingStatePayload decode(FriendlyByteBuf buf) {
        return new SyncEnchantingStatePayload(new EnchantingMenuState(
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

                buf.readBoolean(),   // hasNonCurseEnchantments

                buf.readBoolean(),   // hasApplicableEnchants
                buf.readBoolean(),   // hasExtraEnchant
                buf.readBoolean(),   // hasUpgradableEnchant

                buf.readBoolean(),   // hasEnoughPowerToApply
                buf.readBoolean(),   // hasEnoughPowerForExtra
                buf.readBoolean(),   // hasEnoughPowerToUpgrade
                buf.readBoolean()    // canUpgradeLevelDisenchant
        ));
    }

    public static void handle(SyncEnchantingStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null && context.player().level().isClientSide()) {
                var mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof EnchantingScreen screen) {

                    screen.setEnchantingState(payload.state());
                }
            }
        });
    }
}
