package cn.qihuang02.project_dimension.symbol;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CompoundSymbolResolver {

    public static Map<String, Integer> resolve(DimensionSymbolVector vector) {
        Map<String, Integer> compounds = new LinkedHashMap<>();
        for (AbstractDimensionSymbol symbol : DimensionSymbolRegistry.compounds()) {
            compounds.put(symbol.key(), symbol.resolve(vector));
        }
        return Map.copyOf(compounds);
    }
}
