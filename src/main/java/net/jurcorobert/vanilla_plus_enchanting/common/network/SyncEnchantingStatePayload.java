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
        buf.writeBoolean(payload.state().slot_0());
        buf.writeBoolean(payload.state().slot_1());
        buf.writeBoolean(payload.state().slot_2());
        buf.writeBoolean(payload.state().slot_3());
        buf.writeBoolean(payload.state().slot_4());
        buf.writeBoolean(payload.state().slot_5());
        buf.writeInt(payload.state().mode());
    }

    public static SyncEnchantingStatePayload decode(FriendlyByteBuf buf) {
        return new SyncEnchantingStatePayload(new EnchantingMenuState(
                buf.readBoolean(),      // slot 0
                buf.readBoolean(),      // slot 1
                buf.readBoolean(),      // slot 2
                buf.readBoolean(),      // slot 3
                buf.readBoolean(),      // slot 4
                buf.readBoolean(),      // slot 5
                buf.readInt()           // mode
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
