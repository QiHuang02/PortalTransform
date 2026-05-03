package cn.qihuang02.project_dimension.symbol.basic;

import cn.qihuang02.project_dimension.api.IDimensionSymbol;
import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;

@DimensionSymbol(value = "soul", displayName = "Soul", color = 0x5555FF)
public final class SoulSymbol implements IDimensionSymbol {
    @Override
    public int resolve(DimensionSymbolVector vector) {
        return vector.soul();
    }
}
