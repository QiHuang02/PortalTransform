package cn.qihuang02.portaltransform.symbol.symbols.compound;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.IDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 复合维度象征，必须由多个父象征复合而成。
 */
public abstract class CompoundDimensionSymbol implements IDimensionSymbol {
    private final ResourceLocation id;
    private final int color;
    private final Map<ResourceLocation, Integer> parents;
    private final int tier;
    private final @Nullable ResourceLocation oppositeId;

    protected CompoundDimensionSymbol(
            ResourceLocation id,
            int color,
            Map<ResourceLocation, Integer> parents,
            int tier,
            @Nullable ResourceLocation oppositeId
    ) {
        this.id = Objects.requireNonNull(id, "复合象征 ID 不能为空");
        this.color = color;
        this.parents = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(parents, "复合象征父象征列表不能为空")));
        this.tier = tier;
        validateParents(this.id, this.parents);
        validateTier(this.tier);
        validateOpposite(this.id, oppositeId);
        this.oppositeId = oppositeId;
    }

    /**
     * 构造一个带权父象征条目。
     */
    protected static Map.@NotNull Entry<ResourceLocation, Integer> weighted(@NotNull ResourceLocation id, int weight) {
        Objects.requireNonNull(id, "父象征 ID 不能为空");
        if (weight <= 0) {
            throw new IllegalArgumentException("父象征权重必须大于 0: " + id + " -> " + weight);
        }
        return Map.entry(id, weight);
    }

    /**
     * 构造一个带权父象征条目（使用 mod 内路径）。
     */
    protected static Map.@NotNull Entry<ResourceLocation, Integer> weighted(@NotNull String path, int weight) {
        return weighted(PortalTransform.getRL(path), weight);
    }

    /**
     * 将多个带权父象征条目组装为不可变有序 Map。
     */
    @SafeVarargs
    protected static @NotNull Map<ResourceLocation, Integer> parents(Map.Entry<ResourceLocation, Integer> @NotNull ... entries) {
        LinkedHashMap<ResourceLocation, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    protected static void validateParents(ResourceLocation id, @NotNull Map<ResourceLocation, Integer> parents) {
        if (parents.size() < 2) {
            throw new IllegalArgumentException("复合象征至少需要两个父象征: " + id);
        }

        for (Map.Entry<ResourceLocation, Integer> entry : parents.entrySet()) {
            ResourceLocation parentId = entry.getKey();
            int weight = entry.getValue();
            Objects.requireNonNull(parentId, "复合象征父象征 ID 不能为空: " + id);
            if (weight <= 0) {
                throw new IllegalArgumentException("父象征权重必须大于 0: " + id + " -> " + parentId + " = " + weight);
            }
            if (id.equals(parentId)) {
                throw new IllegalArgumentException("复合象征不能将自身声明为父象征: " + id);
            }
        }
    }

    protected static void validateTier(int tier) {
        SymbolTier symbolTier = SymbolTier.fromTier(tier);
        if (symbolTier == SymbolTier.BASE) {
            throw new IllegalArgumentException("复合象征不能使用基础层级: " + tier);
        }
    }

    protected static void validateOpposite(@NotNull ResourceLocation id, @Nullable ResourceLocation oppositeId) {
        if (id.equals(oppositeId)) {
            throw new IllegalArgumentException("复合象征的对位象征不能指向自身: " + id);
        }
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final int color() {
        return color;
    }

    /**
     * 返回父象征映射：父象征 ID → 权重。
     */
    public final Map<ResourceLocation, Integer> parents() {
        return parents;
    }

    @Override
    public final int tier() {
        return tier;
    }

    @Override
    public final @Nullable ResourceLocation oppositeId() {
        return oppositeId;
    }
}
