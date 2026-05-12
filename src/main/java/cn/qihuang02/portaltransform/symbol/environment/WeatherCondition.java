package cn.qihuang02.portaltransform.symbol.environment;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 天气条件枚举，用于环境修正规则的天气匹配。
 */
public enum WeatherCondition implements StringRepresentable {
    CLEAR("clear"),
    RAIN("rain"),
    THUNDER("thunder");

    public static final Codec<WeatherCondition> CODEC = StringRepresentable.fromEnum(WeatherCondition::values);

    private final String serializedName;

    WeatherCondition(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    /**
     * 判断当前天气条件是否与给定世界的天气状态匹配。
     *
     * @param level 服务端世界实例
     * @return 是否匹配
     */
    public boolean matches(@NotNull ServerLevel level) {
        return switch (this) {
            case CLEAR -> !level.isRaining();
            case RAIN -> level.isRaining() && !level.isThundering();
            case THUNDER -> level.isThundering();
        };
    }
}
