package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record Height(Optional<Integer> minY, Optional<Integer> maxY) {
    public static final String ERROR_MIN_GREATER_THAN_MAX = "Height minimum cannot be greater than maximum.";
    public static final String ERROR_EMPTY_HEIGHT = "Height condition must specify at least a minimum or maximum value.";

    public void toNetwork(FriendlyByteBuf buf) {
        encodeOptionalInt(buf, minY);
        encodeOptionalInt(buf, maxY);
    }

    public static Height fromNetwork(FriendlyByteBuf buf) {
        Optional<Integer> min = decodeOptionalInt(buf);
        Optional<Integer> max = decodeOptionalInt(buf);
        return new Height(min, max);
    }

    private static void encodeOptionalInt(FriendlyByteBuf buf, Optional<Integer> value) {
        buf.writeBoolean(value.isPresent());
        value.ifPresent(buf::writeVarInt);
    }

    private static Optional<Integer> decodeOptionalInt(FriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(buf.readVarInt()) : Optional.empty();
    }

    private static final MapCodec<Height> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min").forGetter(Height::minY),
            Codec.INT.optionalFieldOf("max").forGetter(Height::maxY)
    ).apply(instance, Height::new));

    public static final Codec<Height> CODEC = BASE_CODEC.codec().flatXmap(Height::validate, Height::validate);

    public Height(@NotNull Optional<Integer> minY, @NotNull Optional<Integer> maxY) {
        this.minY = Objects.requireNonNull(minY, "minY optional cannot be null");
        this.maxY = Objects.requireNonNull(maxY, "maxY optional cannot be null");
    }

    private static DataResult<Height> validate(@NotNull Height height) {
        Optional<Integer> minOpt = height.minY();
        Optional<Integer> maxOpt = height.maxY();

        if (minOpt.isEmpty() && maxOpt.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_HEIGHT);
        }

        if (minOpt.isPresent() && maxOpt.isPresent() && minOpt.get() > maxOpt.get()) {
            return DataResult.error(() -> ERROR_MIN_GREATER_THAN_MAX);
        }

        return DataResult.success(height);
    }

    public boolean matches(int y) {
        if (minY.isPresent() && y < minY.get()) {
            return false;
        }
        if (maxY.isPresent() && y > maxY.get()) {
            return false;
        }
        return true;
    }
}
