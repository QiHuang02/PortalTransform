package cn.qihuang02.portaltransform.symbol.symbols;

import org.jetbrains.annotations.NotNull;

/**
 * 维度象征层级规则，统一维护层级编号与对应值域。
 */
public enum SymbolTier {
    /**
     * 基础象征层级。
     */
    BASE(1, -10.0F, 10.0F),
    /**
     * 二阶复合象征层级。
     */
    SECOND(2, -30.0F, 30.0F),
    /**
     * 三阶复合象征层级。
     */
    THIRD(3, -50.0F, 50.0F);

    private final int tier;
    private final float minValue;
    private final float maxValue;

    SymbolTier(int tier, float minValue, float maxValue) {
        this.tier = tier;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * 按层级编号查找规则定义。
     */
    public static @NotNull SymbolTier fromTier(int tier) {
        for (SymbolTier value : values()) {
            if (value.tier == tier) {
                return value;
            }
        }
        throw new IllegalArgumentException("暂不支持的维度象征层级: " + tier);
    }

    /**
     * @return 指定象征允许的最小值
     */
    public static float getMinValue(@NotNull IDimensionSymbol symbol) {
        return fromTier(symbol.tier()).minValue();
    }

    /**
     * @return 指定象征允许的最大值
     */
    public static float getMaxValue(@NotNull IDimensionSymbol symbol) {
        return fromTier(symbol.tier()).maxValue();
    }

    public static float getMinValueByTier(int tier) {
        return fromTier(tier).minValue();
    }

    public static float getMaxValueByTier(int tier) {
        return fromTier(tier).maxValue();
    }

    public int tier() {
        return tier;
    }

    public float minValue() {
        return minValue;
    }

    public float maxValue() {
        return maxValue;
    }
}
