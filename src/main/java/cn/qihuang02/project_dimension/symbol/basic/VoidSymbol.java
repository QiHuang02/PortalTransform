package cn.qihuang02.project_dimension.symbol.basic;

import cn.qihuang02.project_dimension.api.IDimensionSymbol;
import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;

@DimensionSymbol(value = "void", displayName = "Void", color = 0xAA55FF)
public final class VoidSymbol implements IDimensionSymbol {
    @Override
    public int resolve(DimensionSymbolVector vector) {
        return vector.voidAffinity();
    }
}
