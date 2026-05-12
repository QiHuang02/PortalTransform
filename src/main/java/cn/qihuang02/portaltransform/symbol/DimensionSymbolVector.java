package cn.qihuang02.portaltransform.symbol;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 展开后的基础象征向量，仅承载基础象征最终值。
 * <p>精度 0.1，浮点比较使用 epsilon 容差 {@value EPSILON}。</p>
 */
public final class DimensionSymbolVector {
    /** 浮点比较容差 */
    public static final float EPSILON = 0.01F;

    public static final DimensionSymbolVector EMPTY = new DimensionSymbolVector(Map.of());

    private final Map<ResourceLocation, Float> values;

    public DimensionSymbolVector(Map<ResourceLocation, Float> values) {
        Objects.requireNonNull(values, "基础象征向量不能为空");
        this.values = Map.copyOf(new LinkedHashMap<>(values));
    }

    public static @NotNull DimensionSymbolVector of(ResourceLocation symbolId, float value) {
        return new DimensionSymbolVector(Map.of(symbolId, value));
    }

    public float getValue(ResourceLocation symbolId) {
        return values.getOrDefault(symbolId, 0.0F);
    }

    @Contract(pure = true)
    public @NotNull Set<ResourceLocation> symbolIds() {
        return values.keySet();
    }

    public Map<ResourceLocation, Float> asMap() {
        return values;
    }

    public @NotNull DimensionSymbolVector plus(ResourceLocation symbolId, float value) {
        Objects.requireNonNull(symbolId, "象征 ID 不能为空");
        if (Math.abs(value) < EPSILON) {
            return this;
        }

        Map<ResourceLocation, Float> merged = new LinkedHashMap<>(values);
        merged.merge(symbolId, value, Float::sum);
        return new DimensionSymbolVector(merged);
    }

    public @NotNull DimensionSymbolVector plus(@NotNull DimensionSymbolVector other) {
        Objects.requireNonNull(other, "待叠加向量不能为空");
        DimensionSymbolVector result = this;
        for (Map.Entry<ResourceLocation, Float> entry : other.values.entrySet()) {
            result = result.plus(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public @NotNull DimensionSymbolVector minus(@NotNull DimensionSymbolVector other) {
        Objects.requireNonNull(other, "待相减向量不能为空");
        DimensionSymbolVector result = this;
        for (Map.Entry<ResourceLocation, Float> entry : other.values.entrySet()) {
            result = result.plus(entry.getKey(), -entry.getValue());
        }
        return result;
    }

    public @NotNull DimensionSymbolVector clamp(float minValue, float maxValue) {
        Map<ResourceLocation, Float> clamped = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Float> entry : values.entrySet()) {
            float value = Math.max(minValue, Math.min(maxValue, entry.getValue()));
            clamped.put(entry.getKey(), value);
        }
        return new DimensionSymbolVector(clamped);
    }
}
