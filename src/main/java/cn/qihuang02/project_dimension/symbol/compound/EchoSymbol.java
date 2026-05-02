package cn.qihuang02.project_dimension.symbol.compound;

import cn.qihuang02.project_dimension.register.DimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.IDimensionSymbol;

@DimensionSymbol(value = "echo", displayName = "Echo", compound = true)
public final class EchoSymbol implements IDimensionSymbol {
    private static int minPositive(int first, int second) {
        return Math.min(Math.max(first, 0), Math.max(second, 0));
    }

    @Override
    public int resolve(DimensionSymbolVector vector) {
        return minPositive(vector.voidAffinity(), vector.soul());
    }
}
