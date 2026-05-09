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
 */
public final class DimensionSymbolVector {
    public static final DimensionSymbolVector EMPTY = new DimensionSymbolVector(Map.of());

    private final Map<ResourceLocation, Integer> values;

    public DimensionSymbolVector(Map<ResourceLocation, Integer> values) {
        Objects.requireNonNull(values, "基础象征向量不能为空");
        this.values = Map.copyOf(new LinkedHashMap<>(values));
    }

    public static @NotNull DimensionSymbolVector of(ResourceLocation symbolId, int value) {
        return new DimensionSymbolVector(Map.of(symbolId, value));
    }

    public int getValue(ResourceLocation symbolId) {
        return values.getOrDefault(symbolId, 0);
    }

    @Contract(pure = true)
    public @NotNull Set<ResourceLocation> symbolIds() {
        return values.keySet();
    }

    public Map<ResourceLocation, Integer> asMap() {
        return values;
    }

    public @NotNull DimensionSymbolVector plus(ResourceLocation symbolId, int value) {
        Objects.requireNonNull(symbolId, "象征 ID 不能为空");
        if (value == 0) {
            return this;
        }

        Map<ResourceLocation, Integer> merged = new LinkedHashMap<>(values);
        merged.merge(symbolId, value, Integer::sum);
        return new DimensionSymbolVector(merged);
    }

    public @NotNull DimensionSymbolVector plus(@NotNull DimensionSymbolVector other) {
        Objects.requireNonNull(other, "待叠加向量不能为空");
        DimensionSymbolVector result = this;
        for (Map.Entry<ResourceLocation, Integer> entry : other.values.entrySet()) {
            result = result.plus(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public @NotNull DimensionSymbolVector minus(@NotNull DimensionSymbolVector other) {
        Objects.requireNonNull(other, "待相减向量不能为空");
        DimensionSymbolVector result = this;
        for (Map.Entry<ResourceLocation, Integer> entry : other.values.entrySet()) {
            result = result.plus(entry.getKey(), -entry.getValue());
        }
        return result;
    }

    public @NotNull DimensionSymbolVector clamp(int minValue, int maxValue) {
        Map<ResourceLocation, Integer> clamped = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : values.entrySet()) {
            int value = Math.max(minValue, Math.min(maxValue, entry.getValue()));
            clamped.put(entry.getKey(), value);
        }
        return new DimensionSymbolVector(clamped);
    }
}
