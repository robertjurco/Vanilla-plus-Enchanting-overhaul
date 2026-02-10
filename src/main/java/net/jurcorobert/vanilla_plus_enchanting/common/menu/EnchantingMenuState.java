package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EnchantingMenuState(
        int mode
) {
    // Static method returning default instance
    public static EnchantingMenuState defaults() {
        return new EnchantingMenuState( //
                0           // mode
        );
    }

    public static final StreamCodec<ByteBuf, EnchantingMenuState> STREAM_CODEC =
            StreamCodec.of(
                    (buf, state) -> {
                        buf.writeInt(state.mode());
                    },
                    buf -> new EnchantingMenuState(
                            buf.readInt()           // mode
                    )
            );
}