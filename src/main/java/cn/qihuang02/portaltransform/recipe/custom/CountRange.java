package cn.qihuang02.portaltransform.recipe.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CountRange(int min, int max) {
    public static final Codec<CountRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(CountRange::min),
            Codec.INT.fieldOf("max").forGetter(CountRange::max)
    ).apply(instance, CountRange::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CountRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CountRange::min,
            ByteBufCodecs.VAR_INT, CountRange::max,
            CountRange::new
    );

    public boolean isValid() {
        return min > 0 && max >= min;
    }
}
