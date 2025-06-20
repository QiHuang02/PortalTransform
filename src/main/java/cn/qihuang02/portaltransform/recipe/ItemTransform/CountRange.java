package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

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

    public CountRange {
        if (min <= 0) {
            throw new IllegalArgumentException("Minimum count must be greater than 0, got: " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("Maximum count (" + max + ") cannot be less than minimum count (" + min + ")");
        }
    }

    public boolean isValid() {
        return min > 0 && max >= min;
    }

    public int getRandomCount(RandomSource random) {
        if (min == max) {
            return min;
        }
        // random.nextInt(min, max + 1) -> [min, max]
        return random.nextInt(min, max + 1);
    }
}
