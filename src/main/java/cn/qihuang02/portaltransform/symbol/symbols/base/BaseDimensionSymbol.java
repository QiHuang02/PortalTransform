package cn.qihuang02.portaltransform.symbol.symbols.base;

import cn.qihuang02.portaltransform.symbol.symbols.IDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 基础维度象征。
 */
public abstract class BaseDimensionSymbol implements IDimensionSymbol {
    private final ResourceLocation id;
    private final int color;
    private final @Nullable ResourceLocation oppositeId;

    protected BaseDimensionSymbol(ResourceLocation id, int color, @Nullable ResourceLocation oppositeId) {
        this.id = Objects.requireNonNull(id, "基础象征 ID 不能为空");
        this.color = color;
        validateOpposite(this.id, oppositeId);
        this.oppositeId = oppositeId;
    }

    protected static void validateOpposite(@NotNull ResourceLocation id, @Nullable ResourceLocation oppositeId) {
        if (id.equals(oppositeId)) {
            throw new IllegalArgumentException("基础象征的对位象征不能指向自身: " + id);
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

    @Override
    public final int tier() {
        return SymbolTier.BASE.tier();
    }

    @Override
    public final @Nullable ResourceLocation oppositeId() {
        return oppositeId;
    }
}
