package cn.qihuang02.project_dimension.symbol.basic;

import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.IDimensionSymbol;

@DimensionSymbol(value = "phase", displayName = "Phase")
public final class PhaseSymbol implements IDimensionSymbol {
    @Override
    public int resolve(DimensionSymbolVector vector) {
        return vector.phase();
    }
}
