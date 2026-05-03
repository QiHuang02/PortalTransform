package cn.qihuang02.project_dimension.symbol.basic;

import cn.qihuang02.project_dimension.api.IDimensionSymbol;
import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;

@DimensionSymbol(value = "flame", displayName = "Flame", color = 0xFFAA55)
public final class FlameSymbol implements IDimensionSymbol {
    @Override
    public int resolve(DimensionSymbolVector vector) {
        return vector.flame();
    }
}
