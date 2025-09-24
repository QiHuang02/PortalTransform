package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

public record CountRange(int min, int max) {
    public static final Codec<CountRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(CountRange::min),
            Codec.INT.fieldOf("max").forGetter(CountRange::max)
    ).apply(instance, CountRange::new));

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(min);
        buf.writeVarInt(max);
    }

    public static CountRange fromNetwork(FriendlyByteBuf buf) {
        return new CountRange(buf.readVarInt(), buf.readVarInt());
    }

    public static CountRange fromJson(JsonObject json) {
        int min = GsonHelper.getAsInt(json, "min");
        int max = GsonHelper.getAsInt(json, "max");
        return new CountRange(min, max);
    }

    public CountRange copy() {
        return new CountRange(min, max);
    }

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
