package net.jurcorobert.vanilla_plus_enchanting.common.network;

import io.netty.buffer.ByteBuf;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


public record EnchantRequestPayload(int containerId) implements CustomPacketPayload {

    public static final Type<EnchantRequestPayload> TYPE
            = new Type<>(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "enchant_button"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, EnchantRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EnchantRequestPayload::containerId,
            EnchantRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}