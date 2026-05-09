package cn.qihuang02.portaltransform.symbol.symbols;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * 维度象征的统一接口，仅承载稳定的公共元数据。
 */
public interface IDimensionSymbol {
    /**
     * @return 象征唯一标识
     */
    ResourceLocation id();

    /**
     * @return 象征展示颜色
     */
    int color();

    /**
     * @return 象征层级；基础象征固定为 1，复合象征必须大于等于 2
     */
    int tier();

    /**
     * @return 对位象征 ID；若不存在则返回 null
     */
    @Nullable
    ResourceLocation oppositeId();
}
