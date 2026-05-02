package cn.qihuang02.project_dimension.symbol.basic;

import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.IDimensionSymbol;

@DimensionSymbol(value = "life", displayName = "Life")
public final class LifeSymbol implements IDimensionSymbol {
    @Override
    public int resolve(DimensionSymbolVector vector) {
        return vector.life();
    }
}
