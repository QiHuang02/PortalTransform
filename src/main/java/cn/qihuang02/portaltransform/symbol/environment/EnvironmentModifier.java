package cn.qihuang02.portaltransform.symbol.environment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * 环境修正统一数据模型。
 * <p>通过 sealed interface + dispatch Codec 实现类型安全的多态序列化。</p>
 */
public sealed interface EnvironmentModifier permits
        EnvironmentModifier.WeatherModifier,
        EnvironmentModifier.TimeModifier,
        EnvironmentModifier.BiomeModifier,
        EnvironmentModifier.HeightModifier {

    /**
     * 顶层 dispatch Codec，通过 "type" 字段区分子类型
     */
    Codec<EnvironmentModifier> CODEC = Codec.STRING.dispatch(
            "type",
            EnvironmentModifier::type,
            EnvironmentModifier::codecByType
    );

    /**
     * 根据类型名称返回对应的 MapCodec。
     */
    @Contract(pure = true)
    static MapCodec<? extends EnvironmentModifier> codecByType(@NotNull String type) {
        return switch (type) {
            case "weather" -> WeatherModifier.MAP_CODEC;
            case "time" -> TimeModifier.MAP_CODEC;
            case "biome" -> BiomeModifier.MAP_CODEC;
            case "height" -> HeightModifier.MAP_CODEC;
            default -> throw new IllegalArgumentException("未知的环境修正类型: " + type);
        };
    }

    /**
     * 修正值映射：象征 ID → 修正量
     */
    Map<ResourceLocation, Float> modifiers();

    /**
     * 类型标识符，用于 dispatch
     */
    String type();

    // ─────────────────────────────────────────────────────────────────────────────
    // 天气修正
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * 天气条件修正：当世界天气匹配指定条件时生效。
     *
     * @param condition 天气条件
     * @param modifiers 象征修正值映射
     */
    record WeatherModifier(
            WeatherCondition condition,
            Map<ResourceLocation, Float> modifiers
    ) implements EnvironmentModifier {
        public static final MapCodec<WeatherModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        WeatherCondition.CODEC.fieldOf("condition").forGetter(WeatherModifier::condition),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT)
                                .fieldOf("modifiers").forGetter(WeatherModifier::modifiers)
                ).apply(instance, WeatherModifier::new));

        public WeatherModifier {
            Objects.requireNonNull(condition, "天气条件不能为空");
            Objects.requireNonNull(modifiers, "修正值映射不能为空");
            modifiers = Map.copyOf(modifiers);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String type() {
            return "weather";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 时间修正
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * 时间段修正：当世界时间处于 [minTick, maxTick] 范围内时生效。
     *
     * @param minTick   起始刻（含）
     * @param maxTick   结束刻（含）
     * @param modifiers 象征修正值映射
     */
    record TimeModifier(
            int minTick,
            int maxTick,
            Map<ResourceLocation, Float> modifiers
    ) implements EnvironmentModifier {
        public static final MapCodec<TimeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.INT.fieldOf("min_tick").forGetter(TimeModifier::minTick),
                        Codec.INT.fieldOf("max_tick").forGetter(TimeModifier::maxTick),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT)
                                .fieldOf("modifiers").forGetter(TimeModifier::modifiers)
                ).apply(instance, TimeModifier::new));

        public TimeModifier {
            if (minTick < 0 || maxTick < 0) {
                throw new IllegalArgumentException("时间刻不能为负数");
            }
            if (minTick > maxTick) {
                throw new IllegalArgumentException("minTick 不能大于 maxTick");
            }
            Objects.requireNonNull(modifiers, "修正值映射不能为空");
            modifiers = Map.copyOf(modifiers);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String type() {
            return "time";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 群系修正
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * 群系修正：当玩家所在群系匹配指定群系或群系标签时生效。
     * <p>biome 和 biomeTag 至少提供一个。</p>
     *
     * @param biome     精确群系键（可选）
     * @param biomeTag  群系标签（可选）
     * @param modifiers 象征修正值映射
     */
    record BiomeModifier(
            Optional<ResourceKey<Biome>> biome,
            Optional<TagKey<Biome>> biomeTag,
            Map<ResourceLocation, Float> modifiers
    ) implements EnvironmentModifier {
        public static final MapCodec<BiomeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome")
                                .forGetter(BiomeModifier::biome),
                        TagKey.codec(Registries.BIOME).optionalFieldOf("biome_tag")
                                .forGetter(BiomeModifier::biomeTag),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT)
                                .fieldOf("modifiers").forGetter(BiomeModifier::modifiers)
                ).apply(instance, BiomeModifier::new));

        public BiomeModifier {
            if (biome.isEmpty() && biomeTag.isEmpty()) {
                throw new IllegalArgumentException("biome 和 biome_tag 至少需要提供一个");
            }
            Objects.requireNonNull(modifiers, "修正值映射不能为空");
            modifiers = Map.copyOf(modifiers);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String type() {
            return "biome";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 高度修正
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * 高度修正：当玩家 Y 坐标处于 [minY, maxY] 范围内时生效。
     * <p>minY 和 maxY 均为可选，省略表示不限制该方向。</p>
     *
     * @param minY      最低 Y 坐标（可选）
     * @param maxY      最高 Y 坐标（可选）
     * @param modifiers 象征修正值映射
     */
    record HeightModifier(OptionalInt minY, OptionalInt maxY, Map<ResourceLocation, Float> modifiers)
            implements EnvironmentModifier {

        public static final MapCodec<HeightModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("min_y").forGetter(h -> h.minY.isPresent()
                                ? Optional.of(h.minY.getAsInt()) : Optional.empty()),
                        Codec.INT.optionalFieldOf("max_y").forGetter(h -> h.maxY.isPresent()
                                ? Optional.of(h.maxY.getAsInt()) : Optional.empty()),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT)
                                .fieldOf("modifiers").forGetter(HeightModifier::modifiers)
                ).apply(instance, HeightModifier::fromOptionals));

        public HeightModifier {
            Objects.requireNonNull(minY, "minY 不能为 null");
            Objects.requireNonNull(maxY, "maxY 不能为 null");
            if (minY.isPresent() && maxY.isPresent() && minY.getAsInt() > maxY.getAsInt()) {
                throw new IllegalArgumentException("minY 不能大于 maxY");
            }
            Objects.requireNonNull(modifiers, "修正值映射不能为空");
            modifiers = Map.copyOf(modifiers);
        }

        /**
         * 从 Optional 构造，用于 Codec 反序列化。
         */
        @Contract("_, _, _ -> new")
        public static @NotNull HeightModifier fromOptionals(
                @NotNull Optional<Integer> minY,
                @NotNull Optional<Integer> maxY,
                Map<ResourceLocation, Float> modifiers
        ) {
            return new HeightModifier(
                    minY.map(OptionalInt::of).orElse(OptionalInt.empty()),
                    maxY.map(OptionalInt::of).orElse(OptionalInt.empty()),
                    modifiers
            );
        }

        @Contract(pure = true)
        @Override
        public @NotNull String type() {
            return "height";
        }
    }
}
