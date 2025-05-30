package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Weather implements StringRepresentable {
    ANY("any"),
    CLEAR("clear"),
    RAIN("rain"),
    THUNDER("thunder");

    public static final Codec<Weather> CODEC = StringRepresentable.fromEnum(Weather::values);
    private static final Map<Integer, Weather> ENUM_MAP = Arrays.stream(values()).collect(Collectors.toMap(Enum::ordinal, Function.identity()));
    public static final StreamCodec<ByteBuf, Weather> STREAM_CODEC = ByteBufCodecs.idMapper(ENUM_MAP::get, Weather::ordinal);
    private final String name;

    Weather(String name) {
        this.name = name;
    }

    public static @Nullable Weather fromName(String name) {
        for (Weather weather : values()) {
            if (weather.getSerializedName().equalsIgnoreCase(name)) {
                return weather;
            }
        }
        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
