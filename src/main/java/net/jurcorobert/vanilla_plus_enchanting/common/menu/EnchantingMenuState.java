package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EnchantingMenuState(
        boolean slot_0,
        boolean slot_1,
        boolean slot_2,
        boolean slot_3,
        boolean slot_4,
        boolean slot_5,
        int mode
) {
    // Static method returning default instance
    public static EnchantingMenuState defaults() {
        return new EnchantingMenuState( //
                true,       // slot 0
                true,       // slot 1
                true,       // slot 2
                true,       // slot 3
                true,       // slot 4
                true,       // slot 5
                0           // mode
        );
    }

    public static final StreamCodec<ByteBuf, EnchantingMenuState> STREAM_CODEC =
            StreamCodec.of(
                    (buf, state) -> {
                        buf.writeBoolean(state.slot_0());
                        buf.writeBoolean(state.slot_1());
                        buf.writeBoolean(state.slot_2());
                        buf.writeBoolean(state.slot_3());
                        buf.writeBoolean(state.slot_4());
                        buf.writeBoolean(state.slot_5());
                        buf.writeInt(state.mode());
                    },
                    buf -> new EnchantingMenuState(
                            buf.readBoolean(),      // slot 0
                            buf.readBoolean(),      // slot 1
                            buf.readBoolean(),      // slot 2
                            buf.readBoolean(),      // slot 3
                            buf.readBoolean(),      // slot 4
                            buf.readBoolean(),      // slot 5
                            buf.readInt()           // mode
                    )
            );
}