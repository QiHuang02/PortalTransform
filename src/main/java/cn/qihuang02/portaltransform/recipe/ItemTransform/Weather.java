package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
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

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(this.ordinal());
    }

    public static Weather fromNetwork(FriendlyByteBuf buf) {
        int index = buf.readVarInt();
        return ENUM_MAP.getOrDefault(index, ANY);
    }
}
